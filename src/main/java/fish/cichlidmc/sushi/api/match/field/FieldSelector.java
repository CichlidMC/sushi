package fish.cichlidmc.sushi.api.match.field;

import fish.cichlidmc.sushi.api.model.TransformableClass;
import fish.cichlidmc.sushi.api.model.TransformableField;
import fish.cichlidmc.sushi.api.util.ClassDescs;
import fish.cichlidmc.tinycodecs.api.codec.Codec;
import fish.cichlidmc.tinycodecs.api.codec.CompositeCodec;

import java.lang.classfile.FieldModel;
import java.lang.classfile.instruction.FieldInstruction;
import java.lang.constant.ClassDesc;
import java.util.List;
import java.util.Optional;

/// Fuzzily selects fields to be targeted by transforms.
///
/// The name of the targeted field is always required. This is generally sufficient, since
/// you normally can't have multiple fields share a name in Java. However, it's allowed at
/// the bytecode level, so it's also possible to provide an expected type to disambiguate.
public record FieldSelector(String name, Optional<ClassDesc> type) {
	private static final Codec<FieldSelector> nameOnlyCodec = Codec.STRING.xmap(FieldSelector::new, FieldSelector::name);
	private static final Codec<FieldSelector> fullCodec = CompositeCodec.of(
			Codec.STRING.fieldOf("name"), FieldSelector::name,
			ClassDescs.ANY_CODEC.optional().fieldOf("type"), FieldSelector::type,
			FieldSelector::new
	).codec();

	public static final Codec<FieldSelector> CODEC = fullCodec.withAlternative(nameOnlyCodec);

	public FieldSelector(String name) {
		this(name, Optional.empty());
	}

	public FieldSelector(String name, ClassDesc type) {
		this(name, Optional.of(type));
	}

	/// @return a possibly empty list of [TransformableField]s matching this target
	public List<TransformableField> find(TransformableClass clazz) {
		return clazz.fields().values().stream().filter(this::matches).toList();
	}

	public boolean matches(TransformableField field) {
		return this.matches(field.model());
	}

	public boolean matches(FieldModel field) {
		return field.fieldName().equalsString(this.name) && (
				this.type.isEmpty() || this.type.get().equals(field.fieldTypeSymbol())
		);
	}

	public boolean matches(FieldInstruction instruction) {
		return instruction.name().equalsString(this.name) && (
				this.type.isEmpty() || this.type.get().equals(instruction.typeSymbol())
		);
	}

	@Override
	public String toString() {
		return this.name + ' ' + this.type.map(ClassDescs::fullName).orElse("(any type)");
	}
}
