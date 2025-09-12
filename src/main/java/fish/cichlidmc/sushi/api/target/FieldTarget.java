package fish.cichlidmc.sushi.api.target;

import fish.cichlidmc.sushi.api.model.TransformableClass;
import fish.cichlidmc.sushi.api.model.TransformableField;
import fish.cichlidmc.sushi.api.transform.TransformException;
import fish.cichlidmc.sushi.api.util.ClassDescs;
import fish.cichlidmc.tinycodecs.Codec;
import fish.cichlidmc.tinycodecs.codec.map.CompositeCodec;
import org.glavo.classfile.FieldModel;

import java.lang.constant.ClassDesc;
import java.util.List;
import java.util.Optional;

public record FieldTarget(String name, Optional<ClassDesc> type) {
	private static final Codec<FieldTarget> nameOnlyCodec = Codec.STRING.xmap(FieldTarget::new, FieldTarget::name);
	private static final Codec<FieldTarget> fullCodec = CompositeCodec.of(
			Codec.STRING.fieldOf("name"), FieldTarget::name,
			ClassDescs.ANY_CODEC.optional().fieldOf("type"), FieldTarget::type,
			FieldTarget::new
	).asCodec();

	public static final Codec<FieldTarget> CODEC = fullCodec.withAlternative(nameOnlyCodec);

	public FieldTarget(String name) {
		this(name, Optional.empty());
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
			throw new TransformException("FieldTarget matched multiple fields; set the type to disambiguate");
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
