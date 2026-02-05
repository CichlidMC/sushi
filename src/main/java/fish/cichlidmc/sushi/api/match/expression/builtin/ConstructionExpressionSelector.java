package fish.cichlidmc.sushi.api.match.expression.builtin;

import fish.cichlidmc.sushi.api.match.expression.ExpressionSelector;
import fish.cichlidmc.sushi.api.model.code.Point;
import fish.cichlidmc.sushi.api.model.code.Selection;
import fish.cichlidmc.sushi.api.model.code.StackDelta;
import fish.cichlidmc.sushi.api.model.code.TransformableCode;
import fish.cichlidmc.sushi.api.model.code.element.InstructionHolder;
import fish.cichlidmc.sushi.api.transformer.TransformException;
import fish.cichlidmc.sushi.api.util.ClassDescs;
import fish.cichlidmc.tinycodecs.api.codec.map.MapCodec;

import java.lang.classfile.Opcode;
import java.lang.classfile.instruction.InvokeInstruction;
import java.lang.classfile.instruction.NewObjectInstruction;
import java.lang.constant.ClassDesc;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.NavigableSet;

/// An [ExpressionSelector] matching object construction.
///
/// This includes both the [NEW][Opcode#NEW] and the `<init>` invocation.
/// If you only want to target `NEW`, see [NewExpressionSelector].
///
/// This selector cannot target array creation; you should use [NewExpressionSelector]
/// for that as well. Beware of its caveats, though.
/// @see NewExpressionSelector
public record ConstructionExpressionSelector(ClassDesc type) implements ExpressionSelector {
	public static final MapCodec<ConstructionExpressionSelector> CODEC = ClassDescs.CLASS_CODEC.fieldOf("class").xmap(
			ConstructionExpressionSelector::new, ConstructionExpressionSelector::type
	);

	public ConstructionExpressionSelector {
		if (type.isPrimitive()) {
			throw new IllegalArgumentException("Cannot target construction of primitives");
		} else if (type.isArray()) {
			throw new IllegalArgumentException("Cannot target arrays; Use NewExpressionSelector");
		}
	}

	@Override
	public Collection<Found> find(TransformableCode code) throws TransformException {
		List<Found> found = new ArrayList<>();

		NavigableSet<InstructionHolder<?>> instructions = code.instructions();
		for (InstructionHolder<?> instruction : instructions) {
			if (!this.matchesNew(instruction))
				continue;

			// found a matching NEW, now find the <init>
			InstructionHolder.Real<InvokeInstruction> init = this.findInit(instruction.after());

			// nothing should be popped, since all parameters get pushed between the NEW and <init>
			StackDelta delta = StackDelta.of(List.of(), this.type);
			Selection selection = code.select().from(Point.before(instruction)).to(Point.after(init));
			found.add(new Found(selection, delta));
		}

		return found;
	}

	private InstructionHolder.Real<InvokeInstruction> findInit(Iterable<InstructionHolder<?>> instructions) {
		// we need to keep track of depth to make sure we find the right constructor call.
		// consider a recursive construction, ex. 'new RecursiveType(new RecursiveType())'
		// this will put an extra matching <init> before the one we actually need.
		int depth = 0;

		for (InstructionHolder<?> instruction : instructions) {
			if (this.matchesNew(instruction)) {
				// found another NEW, increase depth
				depth++;
				continue;
			}

			if (this.matchesConstructor(instruction)) {
				// found a matching <init>
				if (depth == 0) {
					// this is the right one
					return instruction.checkHoldingReal(InvokeInstruction.class);
				}

				depth--;
			}
		}

		// if we get here, something has gone wrong.
		throw new TransformException("Weird bytecode; found NEW, but no matching <init>");
	}

	private boolean matchesNew(InstructionHolder<?> instruction) {
		if (!(instruction.get() instanceof NewObjectInstruction newObj))
			return false;

		return newObj.className().matches(this.type);
	}

	private boolean matchesConstructor(InstructionHolder<?> instruction) {
		if (!(instruction.get() instanceof InvokeInstruction invoke))
			return false;

		return invoke.name().equalsString("<init>") && invoke.owner().matches(this.type);
	}

	@Override
	public MapCodec<? extends ExpressionSelector> codec() {
		return CODEC;
	}
}
