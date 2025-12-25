package fish.cichlidmc.sushi.api.match;

import fish.cichlidmc.sushi.api.detail.Details;
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
/// The name of the targeted field is always required. This is generally sufficient, since
/// you normally can't have multiple fields share a name in Java. However, it's allowed at
/// the bytecode level, so it's also possible to provide an expected type to disambiguate.
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

	/// @return a possibly empty list of [TransformableField]s matching this target
	public List<TransformableField> find(TransformableClass clazz) {
		return clazz.fields().stream().filter(this::matches).toList();
	}

	/// @return a single field matching this target, if found
	/// @throws TransformException if this target matches more than one field
	public Optional<TransformableField> findSingle(TransformableClass clazz) throws TransformException {
		List<TransformableField> found = this.find(clazz);
		if (found.isEmpty()) {
			return Optional.empty();
		} else if (found.size() == 1) {
			return Optional.of(found.getFirst());
		} else {
			throw new TransformException("FieldTarget matched multiple fields", details -> {
				this.addDetails(details);
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

	private void addDetails(Details details) {
		details.add("Expected Field Name", this.name);
		details.add("Expected Field Type", this.type.map(ClassDescs::fullName).orElse("<unspecified>"));
	}
}
