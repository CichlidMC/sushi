package fish.cichlidmc.sushi.impl.model;

import fish.cichlidmc.sushi.api.attach.AttachmentMap;
import fish.cichlidmc.sushi.api.model.TransformableClass;
import fish.cichlidmc.sushi.api.model.TransformableField;
import fish.cichlidmc.sushi.api.model.TransformableMethod;
import fish.cichlidmc.sushi.api.model.key.FieldKey;
import fish.cichlidmc.sushi.api.model.key.MethodKey;
import fish.cichlidmc.sushi.api.registry.Id;
import fish.cichlidmc.sushi.api.transformer.DirectTransform;
import fish.cichlidmc.sushi.impl.Transformation;
import fish.cichlidmc.sushi.impl.transformer.DirectTransformContextImpl;
import fish.cichlidmc.sushi.impl.transformer.PreparedDirectTransform;
import fish.cichlidmc.sushi.impl.transformer.TransformContextImpl;
import fish.cichlidmc.sushi.impl.util.IdentifiedTransform;
import org.jspecify.annotations.Nullable;

import java.lang.classfile.ClassModel;
import java.lang.classfile.ClassTransform;
import java.lang.classfile.FieldModel;
import java.lang.classfile.MethodModel;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.SequencedMap;
import java.util.Set;

public final class TransformableClassImpl implements TransformableClass {
	public final Transformation transformation;
	private final ClassModel model;
	private final SequencedMap<MethodKey, TransformableMethod> methods;
	private final SequencedMap<FieldKey, TransformableField> fields;
	private final AttachmentMap attachments;
	private final List<PreparedDirectTransform<DirectTransform.Class>> directTransforms;
	private final Set<String> methodNames;

	private boolean frozen;

	public TransformableClassImpl(Transformation transformation, ClassModel model, @Nullable TransformableClass previous) {
		this.transformation = transformation;
		this.model = model;
		this.attachments = previous == null ? AttachmentMap.create() : previous.attachments();
		this.directTransforms = new ArrayList<>();
		this.methodNames = new HashSet<>();

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

			this.methodNames.add(key.name());
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
	public String createUniqueMethodName(String prefix, Id owner) {
		String idealName = "sushi$" + prefix + '$' + owner.namespace + '$' + sanitizePath(owner.path);
		String name = idealName;

		for (int i = 0; this.methodNames.contains(name); i++) {
			name = idealName + '_' + i;
		}

		this.methodNames.add(name);
		return name;
	}

	@Override
	public void transformDirect(DirectTransform.Class transform) {
		this.checkFrozen();
		TransformContextImpl context = TransformContextImpl.current();
		this.directTransforms.add(new PreparedDirectTransform<>(transform, context));
	}

	public ClassTransform append(ClassTransform base) {
		if (this.directTransforms.isEmpty())
			return base;

		for (PreparedDirectTransform<DirectTransform.Class> prepared : this.directTransforms) {
			DirectTransform.Context context = new DirectTransformContextImpl(prepared.context());
			Id owner = prepared.context().transformerId();
			ClassTransform transform = prepared.transform().create(context);
			base = base.andThen(IdentifiedTransform.ofClass(owner, transform));
		}

		return base;
	}

	public void freeze() {
		this.checkFrozen();
		this.frozen = true;
	}

	public void checkFrozen() {
		if (this.frozen) {
			throw new IllegalStateException("Transformations have already been frozen");
		}
	}

	private static String sanitizePath(String path) {
		return path.replace('.', '_').replace('/', '_');
	}
}
