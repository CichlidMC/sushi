package fish.cichlidmc.sushi.impl.model;

import fish.cichlidmc.sushi.api.attach.AttachmentMap;
import fish.cichlidmc.sushi.api.model.TransformableClass;
import fish.cichlidmc.sushi.api.model.TransformableField;
import fish.cichlidmc.sushi.api.registry.Id;
import fish.cichlidmc.sushi.api.util.ClassDescs;
import fish.cichlidmc.sushi.impl.util.IdentifiedTransform;
import org.jspecify.annotations.Nullable;

import java.lang.classfile.FieldModel;
import java.lang.classfile.FieldTransform;
import java.lang.reflect.AccessFlag;
import java.util.Locale;
import java.util.Optional;

public final class TransformableFieldImpl implements TransformableField {
	private final FieldModel model;
	private final TransformableClassImpl owner;
	private final AttachmentMap attachments;

	@Nullable
	private FieldTransform directTransform;

	public TransformableFieldImpl(FieldModel model, TransformableClassImpl owner) {
		this.model = model;
		this.owner = owner;
		this.attachments = AttachmentMap.create();
	}

	@Override
	public FieldModel model() {
		return this.model;
	}

	@Override
	public TransformableClass owner() {
		return this.owner;
	}

	@Override
	public AttachmentMap attachments() {
		return this.attachments;
	}

	@Override
	public void transform(FieldTransform transform) {
		Id owner = this.owner.context.transformerId();
		transform = new IdentifiedTransform.Field(owner, transform);

		if (this.directTransform == null) {
			this.directTransform = transform;
		} else {
			this.directTransform = this.directTransform.andThen(transform);
		}
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();

		for (AccessFlag flag : this.model.flags().flags()) {
			if (flag != AccessFlag.SUPER) {
				builder.append(flag.name().toLowerCase(Locale.ROOT)).append(' ');
			}
		}

		builder.append(this.model.fieldName().stringValue()).append(' ');
		builder.append(ClassDescs.fullName(this.model.fieldTypeSymbol()));

		return builder.toString();
	}

	public Optional<FieldTransform> transform() {
		return Optional.ofNullable(this.directTransform);
	}
}
