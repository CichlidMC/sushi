package fish.cichlidmc.sushi.impl.model;

import fish.cichlidmc.sushi.api.attach.AttachmentMap;
import fish.cichlidmc.sushi.api.model.TransformableClass;
import fish.cichlidmc.sushi.api.model.TransformableField;
import fish.cichlidmc.sushi.api.model.key.FieldKey;
import fish.cichlidmc.sushi.api.registry.Id;
import fish.cichlidmc.sushi.api.transformer.DirectTransform;
import fish.cichlidmc.sushi.api.util.ClassDescs;
import fish.cichlidmc.sushi.impl.transformer.DirectTransformContextImpl;
import fish.cichlidmc.sushi.impl.transformer.PreparedDirectTransform;
import fish.cichlidmc.sushi.impl.transformer.TransformContextImpl;
import fish.cichlidmc.sushi.impl.util.IdentifiedTransform;
import org.jspecify.annotations.Nullable;

import java.lang.classfile.ClassBuilder;
import java.lang.classfile.FieldModel;
import java.lang.classfile.FieldTransform;
import java.lang.reflect.AccessFlag;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

public final class TransformableFieldImpl implements TransformableField {
	private final FieldModel model;
	private final FieldKey key;
	private final TransformableClassImpl owner;
	private final AttachmentMap attachments;
	private final List<PreparedDirectTransform<DirectTransform.Field>> directTransforms;

	public TransformableFieldImpl(FieldModel model, FieldKey key, TransformableClassImpl owner, @Nullable TransformableField previous) {
		if (!FieldKey.of(model).equals(key)) {
			throw new IllegalArgumentException("Incorrect key: " + key);
		}

		this.model = model;
		this.key = key;
		this.owner = owner;
		this.attachments = previous == null ? AttachmentMap.create() : previous.attachments();
		this.directTransforms = new ArrayList<>();
	}

	@Override
	public FieldModel model() {
		return this.model;
	}

	@Override
	public FieldKey key() {
		return this.key;
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
	public void transformDirect(DirectTransform.Field transform) {
		this.owner.checkFrozen();
		TransformContextImpl context = TransformContextImpl.current();
		this.directTransforms.add(new PreparedDirectTransform<>(transform, context));
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

	public Optional<FieldTransform> toTransform(ClassBuilder classBuilder) {
		if (this.directTransforms.isEmpty())
			return Optional.empty();

		FieldTransform transform = null;

		for (PreparedDirectTransform<DirectTransform.Field> prepared : this.directTransforms) {
			DirectTransform.Context.Field context = new DirectTransformContextImpl.FieldImpl(prepared.context(), classBuilder, this);
			FieldTransform direct = prepared.transform().create(context);
			Id owner = prepared.context().transformerId();
			FieldTransform identified = IdentifiedTransform.ofField(owner, direct);
			transform = transform == null ? identified : transform.andThen(identified);
		}

		return Optional.of(transform);
	}
}
