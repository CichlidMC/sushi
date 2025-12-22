package fish.cichlidmc.sushi.api.target;

import fish.cichlidmc.sushi.api.model.TransformableClass;
import fish.cichlidmc.sushi.api.model.TransformableField;
import fish.cichlidmc.sushi.api.transformer.TransformException;
import fish.cichlidmc.sushi.api.util.ClassDescs;
import fish.cichlidmc.tinycodecs.api.codec.Codec;
import fish.cichlidmc.tinycodecs.api.codec.CompositeCodec;

import java.lang.classfile.FieldModel;
import java.lang.constant.ClassDesc;
import java.util.List;
import java.util.Optional;

/// Fuzzily selects fields to be targeted by transforms.
///
/// The name of the targeted field is always required. When necessary
/// for disambiguation, the type of the desired field may also be specified.
public record FieldTarget(String name, Optional<ClassDesc> type) {
	private static final Codec<FieldTarget> nameOnlyCodec = Codec.STRING.xmap(FieldTarget::new, FieldTarget::name);
	private static final Codec<FieldTarget> fullCodec = CompositeCodec.of(
			Codec.STRING.fieldOf("name"), FieldTarget::name,
			ClassDescs.ANY_CODEC.optional().fieldOf("type"), FieldTarget::type,
			FieldTarget::new
	).codec();

	public static final Codec<FieldTarget> CODEC = fullCodec.withAlternative(nameOnlyCodec);

	public FieldTarget(String name) {
		this(name, Optional.empty());
	}

	public FieldTarget(String name, ClassDesc type) {
		this(name, Optional.of(type));
	}

	public List<TransformableField> find(TransformableClass clazz) {
		return clazz.fields().stream().filter(this::matches).toList();
	}

	public Optional<TransformableField> findSingle(TransformableClass clazz) {
		List<TransformableField> found = this.find(clazz);
		if (found.isEmpty()) {
			return Optional.empty();
		} else if (found.size() == 1) {
			return Optional.of(found.getFirst());
		} else {
			throw new TransformException("FieldTarget matched multiple fields", details -> {
				details.add("Expected Field Name", this.name);
				details.add("Expected Field Type", this.type.map(ClassDescs::fullName).orElse("<unspecified>"));
				for (TransformableField field : found) {
					details.add("Match", field);
				}
			});
		}
	}

	public boolean matches(TransformableField field) {
		return this.matches(field.model());
	}

	public boolean matches(FieldModel field) {
		String name = field.fieldName().stringValue();
		if (!this.name.equals(name))
			return false;

		return this.type.map(type -> type.equals(field.fieldTypeSymbol())).orElse(true);
	}
}
