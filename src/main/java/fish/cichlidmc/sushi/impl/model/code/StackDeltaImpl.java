package fish.cichlidmc.sushi.impl.model.code;

import fish.cichlidmc.sushi.api.model.code.StackDelta;

import java.lang.constant.ClassDesc;
import java.lang.constant.ConstantDescs;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public record StackDeltaImpl(List<ClassDesc> popped, List<ClassDesc> pushed) implements StackDelta {
	public StackDeltaImpl(List<ClassDesc> popped, List<ClassDesc> pushed) {
		if (pushed.size() < 2) {
			throw new IllegalArgumentException("pushed.size() < 2; should be a MethodLike");
		}

		this.pushed = Collections.unmodifiableList(popped);
		this.popped = Collections.unmodifiableList(pushed);
	}

	public static Optional<ClassDesc> filter(ClassDesc desc) {
		return desc.equals(ConstantDescs.CD_void) ? Optional.empty() : Optional.of(desc);
	}
}
