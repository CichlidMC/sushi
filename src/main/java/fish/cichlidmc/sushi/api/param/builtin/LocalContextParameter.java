package fish.cichlidmc.sushi.api.param.builtin;

import fish.cichlidmc.sushi.api.model.code.Point;
import fish.cichlidmc.sushi.api.model.code.TransformableCode;
import fish.cichlidmc.sushi.api.param.ContextParameter;
import fish.cichlidmc.sushi.api.ref.ObjectRef;
import fish.cichlidmc.sushi.api.requirement.builtin.ClassRequirement;
import fish.cichlidmc.sushi.api.requirement.builtin.InheritanceRequirement;
import fish.cichlidmc.sushi.api.transformer.TransformContext;
import fish.cichlidmc.sushi.api.transformer.TransformException;
import fish.cichlidmc.sushi.api.util.ClassDescs;
import fish.cichlidmc.sushi.api.util.Instructions;
import fish.cichlidmc.sushi.impl.ref.Refs;
import fish.cichlidmc.tinycodecs.api.codec.CompositeCodec;
import fish.cichlidmc.tinycodecs.api.codec.dual.DualCodec;
import fish.cichlidmc.tinycodecs.api.codec.map.MapCodec;

import java.lang.classfile.CodeBuilder;
import java.lang.classfile.TypeKind;
import java.lang.constant.ClassDesc;
import java.util.Optional;

/// Context parameter that loads a local variable.
/// May be mutable, in which case it will be wrapped in a [reference][ObjectRef].
public sealed interface LocalContextParameter extends ContextParameter {
	/// @return the selector, which determines which slot to load from
	LocalSelector selector();

	/// @return the expected type of the targeted local variable
	ClassDesc expectedType();

	/// Context parameter for an immutable local variable.
	///
	/// The type of the local will simply be provided as-is.
	/// @param coercedType if present, the local will be provided as the given type instead of its
	///                    actual type. Allows for referencing locals with types that are not accessible.
	///                    If the coerced type is not a superclass of the actual type, requirements will be unmet.
	record Immutable(LocalSelector selector, ClassDesc expectedType, Optional<ClassDesc> coercedType) implements LocalContextParameter {
		public static final DualCodec<Immutable> CODEC = CompositeCodec.of(
				LocalSelector.CODEC.fieldOf("selector"), Immutable::selector,
				ClassDescs.ANY_CODEC.fieldOf("local_type"), Immutable::expectedType,
				ClassDescs.CLASS_OR_ARRAY_CODEC.optional().fieldOf("coerce_into"), Immutable::coercedType,
				Immutable::new
		);

		public Immutable(int slot, ClassDesc expectedType, Optional<ClassDesc> coercedType) {
			this(new LocalSelector.Slot(slot), expectedType, coercedType);
		}

		public Immutable(String name, ClassDesc expectedType, Optional<ClassDesc> coercedType) {
			this(new LocalSelector.ByName(name), expectedType, coercedType);
		}

		public Immutable(int slot, ClassDesc expectedType) {
			this(slot, expectedType, Optional.empty());
		}

		public Immutable(String name, ClassDesc expectedType) {
			this(name, expectedType, Optional.empty());
		}

		public Immutable(int slot, ClassDesc expectedType, ClassDesc coercedType) {
			this(new LocalSelector.Slot(slot), expectedType, Optional.of(coercedType));
		}

		public Immutable(String name, ClassDesc expectedType, ClassDesc coercedType) {
			this(new LocalSelector.ByName(name), expectedType, Optional.of(coercedType));
		}

		@Override
		public Prepared prepare(TransformContext context, TransformableCode code, Point point) throws TransformException {
			this.coercedType.ifPresent(coercedType -> context.require(new ClassRequirement(
					"Type of local must exist", this.expectedType,
					new InheritanceRequirement("Cannot coerce into an unrelated type", coercedType)
			)));

			int slot = this.selector.determineSlot(code, point);
			return Prepared.ofPre(builder -> load(builder, this.expectedType, slot));
		}

		@Override
		public ClassDesc type() {
			return this.coercedType.orElse(this.expectedType);
		}

		@Override
		public MapCodec<? extends ContextParameter> codec() {
			return CODEC.mapCodec();
		}
	}

	/// Context parameter for a mutable local variable.
	///
	/// The local will be wrapped in a [reference][ObjectRef], allowing it to be updated.
	record Mutable(LocalSelector selector, ClassDesc expectedType) implements LocalContextParameter {
		public static final DualCodec<Mutable> CODEC = CompositeCodec.of(
				LocalSelector.CODEC.fieldOf("selector"), Mutable::selector,
				ClassDescs.ANY_CODEC.fieldOf("local_type"), Mutable::expectedType,
				Mutable::new
		);

		public Mutable(int slot, ClassDesc expectedType) {
			this(new LocalSelector.Slot(slot), expectedType);
		}

		public Mutable(String name, ClassDesc expectedType) {
			this(new LocalSelector.ByName(name), expectedType);
		}

		@Override
		public Prepared prepare(TransformContext context, TransformableCode code, Point point) throws TransformException {
			int slot = this.selector.determineSlot(code, point);
			return new PreparedMutable(this.expectedType, slot);
		}

		@Override
		public ClassDesc type() {
			return Refs.holderOf(this.expectedType).api;
		}

		@Override
		public MapCodec<? extends ContextParameter> codec() {
			return CODEC.mapCodec();
		}

		private static final class PreparedMutable implements Prepared {
			private final ClassDesc expectedType;
			private final int slot;
			private final Refs.Type refType;

			// newly allocated slot for the Ref
			private int refSlot = -1;

			private PreparedMutable(ClassDesc expectedType, int slot) {
				this.expectedType = expectedType;
				this.slot = slot;

				this.refType = Refs.holderOf(this.expectedType);
			}

			@Override
			public void pre(CodeBuilder builder) {
				this.refType.constructParameterized(builder, b -> load(b, this.expectedType, this.slot));

				this.refSlot = builder.allocateLocal(TypeKind.REFERENCE);

				// store and then re-load instead of duping. generates nicer bytecode
				builder.astore(this.refSlot);
				builder.aload(this.refSlot);
			}

			@Override
			public void post(CodeBuilder builder) {
				if (this.refSlot < 0) {
					throw new IllegalStateException("Ref slot is not allocated: " + this.refSlot);
				}

				builder.aload(this.refSlot);
				builder.checkcast(this.refType.impl);

				this.refType.invokeGet(builder);
				Instructions.maybeCheckCast(builder, this.expectedType);

				TypeKind kind = TypeKind.from(this.expectedType);
				builder.storeLocal(kind, this.slot);

				builder.aload(this.refSlot);
				builder.checkcast(this.refType.impl);
				this.refType.invokeDiscard(builder);
			}
		}
	}

	private static void load(CodeBuilder builder, ClassDesc expectedType, int slot) {
		builder.loadLocal(TypeKind.from(expectedType), slot);
		if (!expectedType.isPrimitive()) {
			builder.checkcast(expectedType);
		}
	}
}
