package fish.cichlidmc.sushi.impl.model;

import fish.cichlidmc.sushi.api.attach.AttachmentMap;
import fish.cichlidmc.sushi.api.model.TransformableMethod;
import fish.cichlidmc.sushi.api.model.code.TransformableCode;
import fish.cichlidmc.sushi.api.registry.Id;
import fish.cichlidmc.sushi.api.util.ClassDescs;
import fish.cichlidmc.sushi.impl.model.code.TransformableCodeImpl;
import fish.cichlidmc.sushi.impl.util.IdentifiedTransform;
import fish.cichlidmc.sushi.impl.util.MethodGenerator;
import org.glavo.classfile.AccessFlag;
import org.glavo.classfile.CodeTransform;
import org.glavo.classfile.MethodModel;
import org.glavo.classfile.MethodTransform;
import org.jspecify.annotations.Nullable;

import java.lang.constant.MethodTypeDesc;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public final class TransformableMethodImpl implements TransformableMethod {
	private final MethodModel model;
	private final TransformableClassImpl owner;
	private final AttachmentMap attachments;

	@Nullable
	private Optional<TransformableCode> code;
	private MethodTransform directTransform;

	public TransformableMethodImpl(MethodModel model, TransformableClassImpl owner) {
		this.model = model;
		this.owner = owner;
		this.attachments = AttachmentMap.create();
	}

	@Override
	public MethodModel model() {
		return this.model;
	}

	@Override
	public TransformableClassImpl owner() {
		return this.owner;
	}

	@Override
	public Optional<TransformableCode> code() {
		if (this.code == null) {
			this.code = this.model.code().map(code -> new TransformableCodeImpl(code, this));
		}

		return this.code;
	}

	@Override
	public AttachmentMap attachments() {
		return this.attachments;
	}

	@Override
	public void transform(MethodTransform transform) {
		Id owner = this.owner.context.transformerId();
		transform = new IdentifiedTransform.Method(owner, transform);

		if (this.directTransform == null) {
			this.directTransform = transform;
		} else {
			this.directTransform = this.directTransform.andThen(transform);
		}
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

	public Optional<MethodTransform> toTransform(MethodGenerator methodGenerator) {
		if (this.code == null || this.code.isEmpty())
			return this.fallback();

		TransformableCodeImpl code = (TransformableCodeImpl) this.code.get();
		Optional<CodeTransform> applicator = code.operations.applicator(code, methodGenerator);
		if (applicator.isEmpty()) {
			return this.fallback();
		}

		MethodTransform transform = MethodTransform.transformingCode(applicator.get());
		return Optional.of(this.directTransform == null ? transform : transform.andThen(this.directTransform));
	}

	private Optional<MethodTransform> fallback() {
		return Optional.ofNullable(this.directTransform);
	}
}
