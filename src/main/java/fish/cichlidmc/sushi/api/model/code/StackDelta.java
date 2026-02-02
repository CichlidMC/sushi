package fish.cichlidmc.sushi.api.model.code;

import fish.cichlidmc.sushi.impl.model.code.StackDeltaImpl;

import java.lang.constant.ClassDesc;
import java.lang.constant.ConstantDescs;
import java.lang.constant.MethodTypeDesc;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/// A change made to the top of the stack by an operation.
public sealed interface StackDelta permits StackDeltaImpl, StackDelta.MethodLike {
	/// The types consumed from the top of the stack.
	/// @return an immutable, possibly empty list
	List<ClassDesc> popped();

	/// The types pushed onto the stack afterward.
	/// @return an immutable, possibly empty list
	List<ClassDesc> pushed();

	/// Create a StackDelta based on a method invocation.
	///
	/// @param owner    the class containing the method
	/// @param isStatic when false, there's an implicit self-reference on the stack as well
	static MethodLike of(MethodTypeDesc desc, ClassDesc owner, boolean isStatic) {
		Optional<ClassDesc> returned = StackDeltaImpl.filter(desc.returnType());

		if (isStatic) {
			return new MethodLike(desc.parameterList(), returned);
		}

		List<ClassDesc> popped = new ArrayList<>();
		popped.add(owner);
		popped.addAll(desc.parameterList());
		return new MethodLike(popped, returned);
	}

	/// Create a new StackDelta.
	static StackDelta of(List<ClassDesc> popped, List<ClassDesc> pushed) {
		return switch (pushed.size()) {
			case 0 -> new MethodLike(popped, Optional.empty());
			case 1 -> of(popped, pushed.getFirst());
			default -> new StackDeltaImpl(popped, pushed);
		};
	}

	/// Create a new StackDelta that pops any number of values and pushes 0 or 1.
	/// This special type of StackDelta is called a [MethodLike].
	static MethodLike of(List<ClassDesc> popped, ClassDesc pushed) {
		return new MethodLike(popped, StackDeltaImpl.filter(pushed));
	}

	/// A StackDelta that pushes either 0 or 1 item onto the stack.
	/// This is similar to a method in the sense of having a single
	/// return value, where void corresponds to pushing nothing.
	/// @param singlePushed the single type pushed onto the stack, if present.
	///        will never be [void][ConstantDescs#CD_void].
	record MethodLike(List<ClassDesc> popped, Optional<ClassDesc> singlePushed) implements StackDelta {
		public MethodLike(List<ClassDesc> popped, Optional<ClassDesc> singlePushed) {
			this.popped = Collections.unmodifiableList(popped);
			this.singlePushed = singlePushed;
		}

		@Override
		public List<ClassDesc> pushed() {
			return this.singlePushed.map(List::of).orElse(List.of());
		}

		/// @return the single pushed value if present, otherwise [void][ConstantDescs#CD_void]
		public ClassDesc pushedOrVoid() {
			return this.singlePushed.orElse(ConstantDescs.CD_void);
		}

		/// @return the single pushed value if present, otherwise [Void][ConstantDescs#CD_Void]
		public ClassDesc pushedOrBoxedVoid() {
			return this.singlePushed.orElse(ConstantDescs.CD_Void);
		}
	}
}
