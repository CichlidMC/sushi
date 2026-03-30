package fish.cichlidmc.sushi.test.def;

import fish.cichlidmc.sushi.api.match.classes.ClassPredicate;
import fish.cichlidmc.sushi.api.match.classes.builtin.SingleClassPredicate;
import fish.cichlidmc.sushi.api.match.method.MethodSelector;
import fish.cichlidmc.sushi.api.model.TransformableMethod;
import fish.cichlidmc.sushi.api.model.code.Point;
import fish.cichlidmc.sushi.api.model.code.TransformableCode;
import fish.cichlidmc.sushi.api.registry.Id;
import fish.cichlidmc.sushi.api.transformer.ClassOutput;
import fish.cichlidmc.sushi.api.transformer.TransformContext;
import fish.cichlidmc.sushi.api.transformer.TransformException;
import fish.cichlidmc.sushi.api.transformer.Transformer;
import fish.cichlidmc.sushi.api.transformer.base.SimpleTransformer;
import fish.cichlidmc.sushi.api.util.Instructions;
import fish.cichlidmc.sushi.api.util.MethodGeneration;
import fish.cichlidmc.sushi.test.framework.TestFactory;
import fish.cichlidmc.sushi.test.infra.TestTarget;
import fish.cichlidmc.tinycodecs.api.codec.Codec;
import fish.cichlidmc.tinycodecs.api.codec.CompositeCodec;
import fish.cichlidmc.tinycodecs.api.codec.dual.DualCodec;
import fish.cichlidmc.tinycodecs.api.codec.map.MapCodec;
import org.junit.jupiter.api.BeforeAll;

import java.lang.classfile.ClassTransform;
import java.lang.classfile.TypeKind;
import java.lang.classfile.attribute.InnerClassInfo;
import java.lang.classfile.attribute.InnerClassesAttribute;
import java.lang.classfile.attribute.NestHostAttribute;
import java.lang.classfile.attribute.NestMembersAttribute;
import java.lang.constant.ClassDesc;
import java.lang.constant.DirectMethodHandleDesc;
import java.lang.constant.MethodHandleDesc;
import java.lang.constant.MethodTypeDesc;
import java.lang.reflect.AccessFlag;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public final class ClassOutputTests {
	private static final TestFactory factory = TestFactory.ROOT.fork()
			.withClassTemplate("""
					class TestTarget {
					%s
					}
					"""
			);

	@BeforeAll
	public static void registerTransformer() {
		Transformer.REGISTRY.register(new Id("tests", "shunt"), ShuntTransformer.CODEC.mapCodec());
	}

	// @Test // FIXME
	public void run() {
		factory.compile("""
				static int add(int a, int b) {
					return a + b;
				}
				"""
		).transform(
				new ShuntTransformer(
						new SingleClassPredicate(TestTarget.DESC),
						"Shunt",
						new MethodSelector("add")
				)
		).decompile("""
				static int add(int a, int b) {
					return a + b;
				}
				"""
		).execute();
	}

	public record ShuntTransformer(ClassPredicate classPredicate, String shuntClassName, MethodSelector selector) implements SimpleTransformer {
		public static final DualCodec<ShuntTransformer> CODEC = CompositeCodec.of(
				ClassPredicate.CODEC.fieldOf("class"), ShuntTransformer::classPredicate,
				Codec.STRING.fieldOf("shunt_class_name"), ShuntTransformer::shuntClassName,
				MethodSelector.CODEC.fieldOf("method"), ShuntTransformer::selector,
				ShuntTransformer::new
		);

		@Override
		public void apply(TransformContext context) throws TransformException {
			List<TransformableMethod> methods = this.selector.find(context.target());
			if (methods.size() != 1) {
				throw new TransformException("Expected to match exactly 1 method");
			}

			TransformableMethod method = methods.getFirst();
			if (!method.model().flags().has(AccessFlag.STATIC)) {
				throw new IllegalStateException("Method must be static");
			}

			ClassDesc shuntClassDesc = context.target().desc().nested(this.shuntClassName);
			MethodTypeDesc desc = method.key().desc();
			DirectMethodHandleDesc shuntMethodDesc = MethodHandleDesc.ofMethod(
					DirectMethodHandleDesc.Kind.STATIC,
					shuntClassDesc, "shunt", desc
			);

			TransformableCode code = method.code().orElseThrow();
			Point head = Point.before(code.instructions().first());
			Point tail = Point.after(code.instructions().last());

			code.select().from(head).to(tail).replace(builder -> {
				// push args
				ClassDesc[] params = desc.parameterArray();
				for (int i = 0; i < params.length; i++) {
					ClassDesc param = params[i];
					builder.loadLocal(TypeKind.from(param), i);
				}

				// invoke
				Instructions.invokeMethod(builder, shuntMethodDesc);

				// return
				builder.return_(TypeKind.from(desc.returnType()));
			});

			ClassOutput output = context.classOutput();
			int flags = MethodGeneration.toMask(Set.of(AccessFlag.PUBLIC, AccessFlag.STATIC));

			int shuntClassFlags = AccessFlag.PUBLIC.mask() | AccessFlag.STATIC.mask();
			output.define(shuntClassDesc, classBuilder -> {
				classBuilder.withFlags(shuntClassFlags);
				classBuilder.withMethodBody("shunt", desc, flags, codeBuilder -> {
					code.model().forEach(codeBuilder);
				});

				classBuilder.with(NestHostAttribute.of(context.target().desc()));
			});

			context.target().transformDirect(_ -> ClassTransform.endHandler(builder -> {
				builder.with(InnerClassesAttribute.of(InnerClassInfo.of(
						shuntClassDesc, Optional.of(context.target().desc()),
						Optional.of(shuntClassDesc.displayName()), shuntClassFlags
				)));

				//builder.with(NestHostAttribute.of(context.target().desc()));

				builder.with(NestMembersAttribute.ofSymbols(shuntClassDesc));
			}));
		}

		@Override
		public MapCodec<? extends Transformer> codec() {
			return CODEC.mapCodec();
		}
	}
}
