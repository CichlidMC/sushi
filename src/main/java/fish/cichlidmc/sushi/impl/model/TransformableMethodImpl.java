package fish.cichlidmc.sushi.impl.model;

import fish.cichlidmc.sushi.api.attach.AttachmentMap;
import fish.cichlidmc.sushi.api.model.TransformableMethod;
import fish.cichlidmc.sushi.api.model.code.TransformableCode;
import fish.cichlidmc.sushi.api.model.key.MethodKey;
import fish.cichlidmc.sushi.api.registry.Id;
import fish.cichlidmc.sushi.api.transformer.DirectTransform;
import fish.cichlidmc.sushi.api.util.ClassDescs;
import fish.cichlidmc.sushi.impl.model.code.TransformableCodeImpl;
import fish.cichlidmc.sushi.impl.transformer.DirectTransformContextImpl;
import fish.cichlidmc.sushi.impl.transformer.PreparedDirectTransform;
import fish.cichlidmc.sushi.impl.transformer.TransformContextImpl;
import fish.cichlidmc.sushi.impl.util.IdentifiedTransform;
import org.jspecify.annotations.Nullable;

import java.lang.classfile.ClassBuilder;
import java.lang.classfile.MethodModel;
import java.lang.classfile.MethodTransform;
import java.lang.constant.MethodTypeDesc;
import java.lang.reflect.AccessFlag;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

public final class TransformableMethodImpl implements TransformableMethod {
	private final MethodModel model;
	private final MethodKey key;
	private final TransformableClassImpl owner;
	private final AttachmentMap attachments;
	private final List<PreparedDirectTransform<DirectTransform.Method>> directTransforms;

	private Optional<Optional<TransformableCode>> code;

	public TransformableMethodImpl(MethodModel model, MethodKey key, TransformableClassImpl owner, @Nullable TransformableMethod previous) {
		if (!MethodKey.of(model).equals(key)) {
			throw new IllegalArgumentException("Incorrect key: " + key);
		}

		this.model = model;
		this.key = key;
		this.owner = owner;
		this.attachments = previous == null ? AttachmentMap.create() : previous.attachments();
		this.directTransforms = new ArrayList<>();
		this.code = Optional.empty();
	}

	@Override
	public MethodModel model() {
		return this.model;
	}

	@Override
	public MethodKey key() {
		return this.key;
	}

	@Override
	public TransformableClassImpl owner() {
		return this.owner;
	}

	@Override
	public Optional<TransformableCode> code() {
		if (this.code.isEmpty()) {
			this.code = Optional.of(this.model.code().map(code -> new TransformableCodeImpl(code, this)));
		}

		return this.code.flatMap(Function.identity());
	}

	@Override
	public AttachmentMap attachments() {
		return this.attachments;
	}

	@Override
	public void transformDirect(DirectTransform.Method transform) {
		this.owner.checkFrozen();
		TransformContextImpl context = TransformContextImpl.current();
		this.directTransforms.add(new PreparedDirectTransform<>(transform, context));
	}

	@Override
	public String toString() {
		String name = this.model.methodName().stringValue();
		MethodTypeDesc desc = this.model.methodTypeSymbol();
		Set<AccessFlag> flags = this.model.flags().flags();

		StringBuilder builder = new StringBuilder();
		for (AccessFlag flag : flags) {
			if (flag != AccessFlag.SUPER) {
				builder.append(flag.name().toLowerCase(Locale.ROOT)).append(' ');
			}
		}

		builder.append(ClassDescs.fullName(desc.returnType())).append(' ');
		builder.append(name);

		builder.append(
				desc.parameterList().stream()
						.map(ClassDescs::fullName)
						.collect(Collectors.joining(", ", "(", ")"))
		);

		return builder.toString();
	}

	public Optional<MethodTransform> toTransform(ClassBuilder classBuilder) {
		Optional<MethodTransform> direct = this.createDirectTransform(classBuilder);
		if (this.code.isEmpty() || this.code.get().isEmpty())
			return direct;

		TransformableCodeImpl code = (TransformableCodeImpl) this.code.get().get();
		Optional<MethodTransform> codeTransform = code.toTransform(classBuilder);

		return codeTransform.map(t -> direct.isEmpty() ? t : t.andThen(direct.get())).or(() -> direct);
	}

	private Optional<MethodTransform> createDirectTransform(ClassBuilder classBuilder) {
		MethodTransform transform = null;

		for (PreparedDirectTransform<DirectTransform.Method> prepared : this.directTransforms) {
			DirectTransform.Context.Method context = new DirectTransformContextImpl.MethodImpl(prepared.context(), classBuilder, this);
			Id owner = prepared.context().transformerId();
			MethodTransform direct = prepared.transform().create(context);
			MethodTransform identified = IdentifiedTransform.ofMethod(owner, direct);
			transform = transform == null ? identified : transform.andThen(identified);
		}

		return Optional.ofNullable(transform);
	}
}
