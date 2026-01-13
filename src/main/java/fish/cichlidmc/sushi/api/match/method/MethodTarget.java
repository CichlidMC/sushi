package fish.cichlidmc.sushi.api.match.method;

import fish.cichlidmc.sushi.api.detail.Details;
import fish.cichlidmc.sushi.api.match.Target;
import fish.cichlidmc.sushi.api.model.TransformableClass;
import fish.cichlidmc.sushi.api.model.TransformableMethod;
import fish.cichlidmc.sushi.api.model.code.element.InstructionHolder;
import fish.cichlidmc.sushi.api.transformer.TransformException;
import fish.cichlidmc.tinycodecs.api.codec.Codec;

import java.lang.classfile.instruction.InvokeInstruction;
import java.util.Collection;
import java.util.NavigableSet;

/// A [Target] for [methods][MethodSelector].
public record MethodTarget(MethodSelector selector, int expected) implements Target {
	public static final Codec<MethodTarget> CODEC = Target.codec(
			MethodSelector.CODEC, MethodTarget::new, MethodTarget::selector
	);

	public MethodTarget {
		Target.checkExpected(expected);
	}

	public MethodTarget(MethodSelector selector) {
		this(selector, DEFAULT);
	}

	/// Find all matching methods in the given class.
	/// @throws TransformException if the number of found methods does not match this target
	public Collection<TransformableMethod> find(TransformableClass clazz) throws TransformException {
		Collection<TransformableMethod> found = this.selector.find(clazz);

		try {
			Target.checkFound(this, found);
		} catch (TransformException e) {
			Details details = e.details();
			for (TransformableMethod match : found) {
				details.add("Match", match);
			}
			throw e;
		}

		return found;
	}

	public NavigableSet<InstructionHolder.Real<InvokeInstruction>> find(NavigableSet<InstructionHolder<?>> instructions) throws TransformException {
		NavigableSet<InstructionHolder.Real<InvokeInstruction>> found = this.selector.find(instructions);
		Target.checkFound(this, found);
		return found;
	}
}
