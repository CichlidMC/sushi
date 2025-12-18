package fish.cichlidmc.sushi.api.transformer.builtin;

import fish.cichlidmc.sushi.api.metadata.InterfaceAdded;
import fish.cichlidmc.sushi.api.registry.Id;
import fish.cichlidmc.sushi.api.requirement.builtin.ClassRequirement;
import fish.cichlidmc.sushi.api.requirement.builtin.FlagsRequirement;
import fish.cichlidmc.sushi.api.requirement.builtin.FullyDefinedRequirement;
import fish.cichlidmc.sushi.api.target.ClassTarget;
import fish.cichlidmc.sushi.api.transformer.TransformContext;
import fish.cichlidmc.sushi.api.transformer.TransformException;
import fish.cichlidmc.sushi.api.transformer.Transformer;
import fish.cichlidmc.sushi.api.transformer.base.SimpleTransformer;
import fish.cichlidmc.sushi.api.util.Annotations;
import fish.cichlidmc.sushi.api.util.ClassDescs;
import fish.cichlidmc.sushi.api.util.ElementModifier;
import fish.cichlidmc.tinycodecs.api.codec.CompositeCodec;
import fish.cichlidmc.tinycodecs.api.codec.dual.DualCodec;
import fish.cichlidmc.tinycodecs.api.codec.map.MapCodec;
import org.glavo.classfile.AccessFlag;
import org.glavo.classfile.AnnotationValue;
import org.glavo.classfile.ClassModel;
import org.glavo.classfile.Interfaces;
import org.glavo.classfile.constantpool.ClassEntry;

import java.lang.constant.ClassDesc;
import java.util.ArrayList;
import java.util.List;

/// Adds an interface to the target class. All methods in the interface must be defaulted.
/// If the target class already has the desired interface, the transform will fail.
/// @param interfaceDesc the interface to apply
public record AddInterfaceTransformer(ClassTarget classes, ClassDesc interfaceDesc) implements SimpleTransformer {
	public static final DualCodec<AddInterfaceTransformer> CODEC = CompositeCodec.of(
			ClassTarget.CODEC.fieldOf("target"), AddInterfaceTransformer::classes,
			ClassDescs.CLASS_CODEC.fieldOf("interface"), AddInterfaceTransformer::interfaceDesc,
			AddInterfaceTransformer::new
	);

	private static final ClassDesc metadataDesc = ClassDescs.of(InterfaceAdded.class);

	@Override
	public void apply(TransformContext context) throws TransformException {
		if (this.alreadyHasInterface(context.clazz().model())) {
			throw new TransformException("Interface being added is already on the target class");
		}

		context.require(new ClassRequirement(
				"Cannot add a non-existent interface",
				this.interfaceDesc,
				FlagsRequirement.builder("Added interfaces must be public")
						.require(AccessFlag.PUBLIC)
						.build(),
				new FullyDefinedRequirement(
						"Adding an interface to a class requires that all interface methods have an implementation"
				)
		));

		context.clazz().transform(ElementModifier.forClass(Interfaces.class, Interfaces::of, (builder, interfaces) -> {
			if (this.shouldAddInterface(interfaces)) {
				List<ClassEntry> newInterfaces = new ArrayList<>(interfaces.interfaces());
				newInterfaces.add(builder.constantPool().classEntry(this.interfaceDesc));
				return Interfaces.of(newInterfaces);
			}

			return interfaces;
		}));

		if (!context.addMetadata())
			return;

		// get this now, lambda is executed later
		Id id = context.transformerId();

		context.clazz().transform(Annotations.runtimeVisibleClassModifier(annotations -> {
			Annotations.Entry entry = annotations.findOrCreate(
					this::matches, () -> new Annotations.Entry(metadataDesc)
							.put("value", AnnotationValue.ofClass(this.interfaceDesc))
			);

			List<AnnotationValue> ids = new ArrayList<>(
					entry.get("by")
							.filter(value -> value instanceof AnnotationValue.OfArray)
							.map(value -> (AnnotationValue.OfArray) value)
							.map(AnnotationValue.OfArray::values)
							.orElse(List.of())
			);

			ids.add(AnnotationValue.ofString(id.toString()));
			entry.put("by", AnnotationValue.ofArray(ids));
		}));
	}

	private boolean shouldAddInterface(Interfaces interfaces) {
		return interfaces.interfaces().stream().noneMatch(entry -> this.interfaceDesc.equals(entry.asSymbol()));
	}

	private boolean alreadyHasInterface(ClassModel model) {
		return model.interfaces().stream().map(ClassEntry::asSymbol).anyMatch(this.interfaceDesc::equals);
	}

	private boolean matches(Annotations.Entry entry) {
		if (!entry.desc.equals(metadataDesc))
			return false;

		return entry.get("value")
				.filter(value -> value instanceof AnnotationValue.OfClass)
				.map(value -> (AnnotationValue.OfClass) value)
				.filter(value -> value.classSymbol().equals(this.interfaceDesc))
				.isPresent();
	}

	@Override
	public MapCodec<? extends Transformer> codec() {
		return CODEC.mapCodec();
	}
}
