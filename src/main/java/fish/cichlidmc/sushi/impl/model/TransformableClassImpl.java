package fish.cichlidmc.sushi.impl.model;

import fish.cichlidmc.sushi.api.attach.AttachmentMap;
import fish.cichlidmc.sushi.api.model.TransformableClass;
import fish.cichlidmc.sushi.api.model.TransformableField;
import fish.cichlidmc.sushi.api.model.TransformableMethod;
import fish.cichlidmc.sushi.api.model.key.FieldKey;
import fish.cichlidmc.sushi.api.model.key.MethodKey;
import fish.cichlidmc.sushi.api.registry.Id;
import fish.cichlidmc.sushi.impl.Transformation;
import fish.cichlidmc.sushi.impl.transformer.TransformContextImpl;
import fish.cichlidmc.sushi.impl.util.IdentifiedTransform;
import org.jspecify.annotations.Nullable;

import java.lang.classfile.ClassModel;
import java.lang.classfile.ClassTransform;
import java.lang.classfile.FieldModel;
import java.lang.classfile.MethodModel;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.SequencedMap;

public final class TransformableClassImpl implements TransformableClass {
	public final Transformation transformation;
	private final ClassModel model;
	private final SequencedMap<MethodKey, TransformableMethod> methods;
	private final SequencedMap<FieldKey, TransformableField> fields;
	private final AttachmentMap attachments;

	@Nullable
	private ClassTransform directTransform;

	public TransformableClassImpl(Transformation transformation, ClassModel model, @Nullable TransformableClass previous) {
		this.transformation = transformation;
		this.model = model;
		this.attachments = previous == null ? AttachmentMap.create() : previous.attachments();

		// maintain ordering for these
		SequencedMap<MethodKey, TransformableMethod> methods = new LinkedHashMap<>();
		SequencedMap<FieldKey, TransformableField> fields = new LinkedHashMap<>();

		for (MethodModel method : model.methods()) {
			MethodKey key = MethodKey.of(method);
			TransformableMethod previousMethod = previous == null ? null : previous.methods().get(key);
			TransformableMethod transformable = new TransformableMethodImpl(method, key, this, previousMethod);
			if (methods.put(key, transformable) != null) {
				throw new IllegalStateException("Duplicate methods for key " + key);
			}
		}

		for (FieldModel field : model.fields()) {
			FieldKey key = FieldKey.of(field);
			TransformableField previousField = previous == null ? null : previous.fields().get(key);
			TransformableFieldImpl transformable = new TransformableFieldImpl(field, key, this, previousField);
			if (fields.put(key, transformable) != null) {
				throw new IllegalStateException("Duplicate fields for key " + key);
			}
		}

		this.methods = Collections.unmodifiableSequencedMap(methods);
		this.fields = Collections.unmodifiableSequencedMap(fields);
	}

	@Override
	public ClassModel model() {
		return this.model;
	}

	@Override
	public SequencedMap<MethodKey, TransformableMethod> methods() {
		return this.methods;
	}

	@Override
	public SequencedMap<FieldKey, TransformableField> fields() {
		return this.fields;
	}

	@Override
	public AttachmentMap attachments() {
		return this.attachments;
	}

	@Override
	public void transform(ClassTransform transform) {
		Id owner = TransformContextImpl.current().transformerId();
		transform = new IdentifiedTransform.Class(owner, transform);

		if (this.directTransform == null) {
			this.directTransform = transform;
		} else {
			this.directTransform = this.directTransform.andThen(transform);
		}
	}

	public ClassTransform append(ClassTransform base) {
		return this.directTransform == null ? base : base.andThen(this.directTransform);
	}
}
