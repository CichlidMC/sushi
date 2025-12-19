package fish.cichlidmc.sushi.api.param.builtin.local;

import fish.cichlidmc.sushi.api.model.code.Point;
import fish.cichlidmc.sushi.api.model.code.TransformableCode;
import fish.cichlidmc.sushi.api.param.ContextParameter;
import fish.cichlidmc.sushi.api.ref.ObjectRef;
import fish.cichlidmc.sushi.api.transformer.TransformContext;
import fish.cichlidmc.sushi.api.transformer.TransformException;
import fish.cichlidmc.sushi.api.util.ClassDescs;
import fish.cichlidmc.sushi.api.util.Instructions;
import fish.cichlidmc.sushi.impl.ref.Refs;
import fish.cichlidmc.tinycodecs.api.codec.Codec;
import fish.cichlidmc.tinycodecs.api.codec.CompositeCodec;
import fish.cichlidmc.tinycodecs.api.codec.dual.DualCodec;
import fish.cichlidmc.tinycodecs.api.codec.map.MapCodec;

import java.lang.classfile.CodeBuilder;
import java.lang.classfile.TypeKind;
import java.lang.constant.ClassDesc;

/// Context parameter that loads a local from a slot.
/// May be mutable, in which case it will be wrapped in a [reference][ObjectRef].
public record SlottedLocalContextParameter(int slot, ClassDesc expectedType, boolean mutable) implements ContextParameter {
	public static final DualCodec<SlottedLocalContextParameter> CODEC = CompositeCodec.of(
			Codec.INT.fieldOf("slot"), SlottedLocalContextParameter::slot,
			ClassDescs.ANY_CODEC.fieldOf("local_type"), SlottedLocalContextParameter::expectedType,
			Codec.BOOL.optional(false).fieldOf("mutable"), SlottedLocalContextParameter::mutable,
			SlottedLocalContextParameter::new
	);

	@Override
	public Prepared prepare(TransformContext context, TransformableCode code, Point point) throws TransformException {
		return this.mutable ? new Mutable(this.expectedType, this.slot) : new Immutable(this.expectedType, this.slot);
	}

	@Override
	public ClassDesc type() {
		return !this.mutable ? this.expectedType : Refs.holderOf(this.expectedType).api;
	}

	@Override
	public MapCodec<? extends ContextParameter> codec() {
		return CODEC.mapCodec();
	}

	private static void load(CodeBuilder builder, ClassDesc expectedType, int slot) {
		builder.loadLocal(TypeKind.from(expectedType), slot);
		if (!expectedType.isPrimitive()) {
			builder.checkcast(expectedType);
		}
	}

	private record Immutable(ClassDesc expectedType, int slot) implements Prepared {
		@Override
		public void pre(CodeBuilder builder) {
			load(builder, this.expectedType, this.slot);
		}
	}

	private static final class Mutable implements Prepared {
		private final ClassDesc expectedType;
		private final int slot;
		private final Refs.Type refType;

		// newly allocated slot for the Ref
		private int refSlot = -1;

		private Mutable(ClassDesc expectedType, int slot) {
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
			builder.dup();

			this.refType.invokeGet(builder);
			Instructions.maybeCheckCast(builder, this.expectedType);

			TypeKind kind = TypeKind.from(this.expectedType);
			builder.storeLocal(kind, this.slot);
			this.refType.invokeDiscard(builder);
		}
	}
}
