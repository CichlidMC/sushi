package fish.cichlidmc.sushi.api.match.expression.builtin;

import fish.cichlidmc.sushi.api.match.expression.ExpressionSelector;
import fish.cichlidmc.sushi.api.model.code.StackDelta;
import fish.cichlidmc.sushi.api.model.code.TransformableCode;
import fish.cichlidmc.sushi.api.model.code.element.InstructionHolder;
import fish.cichlidmc.sushi.api.transformer.TransformException;
import fish.cichlidmc.sushi.api.util.ClassDescs;
import fish.cichlidmc.tinycodecs.api.codec.map.MapCodec;
import org.jspecify.annotations.Nullable;

import java.lang.classfile.Opcode;
import java.lang.classfile.instruction.NewMultiArrayInstruction;
import java.lang.classfile.instruction.NewObjectInstruction;
import java.lang.classfile.instruction.NewPrimitiveArrayInstruction;
import java.lang.classfile.instruction.NewReferenceArrayInstruction;
import java.lang.constant.ClassDesc;
import java.lang.constant.ConstantDescs;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

/// An [ExpressionSelector] matching object creation with one of the following opcodes:
/// - [Opcode#NEW] (normal object)
/// - [Opcode#NEWARRAY] (primitive array)
/// - [Opcode#ANEWARRAY] (reference array)
/// - [Opcode#MULTIANEWARRAY] (multidimensional array)
///
/// This selector is **unsafe**. It specifically targets object *creation*, which is only step 1 of
/// object *construction*. A `NEW` is always followed by a constructor invocation. Before that,
/// the object is in a larval state, where it is not safe to use since it has not been initialized.
/// @see ConstructionExpressionSelector
public final class NewExpressionSelector implements ExpressionSelector {
	public static final MapCodec<NewExpressionSelector> CODEC = ClassDescs.CLASS_OR_ARRAY_CODEC.fieldOf("class").xmap(
			NewExpressionSelector::new, selector -> selector.type
	);

	// note: void[] is forbidden by ClassDesc
	public final ClassDesc type;

	private final InternalSelector internal;

	/// Create a new selector matching creation of `type`.
	/// @param type the type being instantiated. Anything but a primitive.
	/// @throws IllegalArgumentException if `type` is a primitive
	public NewExpressionSelector(ClassDesc type) {
		this.internal = InternalSelector.of(type);
		this.type = type;
	}

	@Override
	public Collection<Found> find(TransformableCode code) throws TransformException {
		List<Found> list = new ArrayList<>();

		for (InstructionHolder<?> instruction : code.instructions()) {
			StackDelta delta = this.internal.find(instruction);
			if (delta != null) {
				list.add(new Found(code.select().only(instruction), delta));
			}
		}

		return list;
	}

	@Override
	public MapCodec<? extends ExpressionSelector> codec() {
		return CODEC;
	}

	private sealed interface InternalSelector {
		@Nullable
		StackDelta find(InstructionHolder<?> instruction);

		static InternalSelector of(ClassDesc type) throws IllegalArgumentException {
			if (type.isPrimitive()) {
				throw new IllegalArgumentException("Cannot target primitive creation");
			} else return switch (ClassDescs.arrayDimensions(type)) {
				case 0 -> new New(type);
				case 1 -> new Array(type.componentType());
				default -> new MultidimensionalArray(ClassDescs.arrayRoot(type));
			};
		}

		record New(ClassDesc type) implements InternalSelector {
			@Nullable
			@Override
			public StackDelta find(InstructionHolder<?> instruction) {
				if (!(instruction.get() instanceof NewObjectInstruction newObj))
					return null;

				if (!newObj.className().matches(this.type))
					return null;

				return StackDelta.of(List.of(), newObj.className().asSymbol());
			}
		}

		record Array(ClassDesc componentType) implements InternalSelector {
			// array size -> array instance
			private static final List<ClassDesc> popped = List.of(ConstantDescs.CD_int);

			@Nullable
			@Override
			public StackDelta find(InstructionHolder<?> instruction) {
				ClassDesc componentType = switch (instruction.get()) {
					case NewPrimitiveArrayInstruction primitive -> primitive.typeKind().upperBound();
					case NewReferenceArrayInstruction ref -> ref.componentType().asSymbol();
					default -> null;
				};

				if (!Objects.equals(componentType, this.componentType))
					return null;

				return StackDelta.of(popped, componentType.arrayType());
			}
		}

		record MultidimensionalArray(ClassDesc componentType) implements InternalSelector {
			@Nullable
			@Override
			public StackDelta find(InstructionHolder<?> instruction) {
				if (!(instruction.get() instanceof NewMultiArrayInstruction multiArray))
					return null;

				ClassDesc expectedArrayType = this.componentType.arrayType(multiArray.dimensions());
				if (!multiArray.arrayType().matches(expectedArrayType))
					return null;

				List<ClassDesc> popped = Stream.generate(() -> ConstantDescs.CD_int)
						.limit(multiArray.dimensions())
						.toList();

				return StackDelta.of(popped, expectedArrayType);
			}
		}
	}
}
