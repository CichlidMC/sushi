package fish.cichlidmc.sushi.api.param.builtin.local;

import fish.cichlidmc.sushi.api.model.code.Point;
import fish.cichlidmc.sushi.api.model.code.TransformableCode;
import fish.cichlidmc.sushi.api.param.ContextParameter;
import fish.cichlidmc.sushi.api.ref.ObjectRef;
import fish.cichlidmc.sushi.api.transform.TransformContext;
import fish.cichlidmc.sushi.api.transform.TransformException;
import fish.cichlidmc.sushi.api.util.ClassDescs;
import fish.cichlidmc.sushi.impl.runtime.ref.BaseRefImpl;
import fish.cichlidmc.tinycodecs.Codec;
import fish.cichlidmc.tinycodecs.codec.map.CompositeCodec;
import fish.cichlidmc.tinycodecs.map.MapCodec;
import org.glavo.classfile.CodeBuilder;
import org.glavo.classfile.TypeKind;

import java.lang.constant.ClassDesc;
import java.lang.constant.ConstantDescs;
import java.lang.constant.MethodTypeDesc;

/**
 * Context parameter that loads a local from a slot.
 * May be mutable, in which case it will be wrapped in a {@link ObjectRef reference}.
 */
public record SlottedLocalContextParameter(int slot, ClassDesc expectedType, boolean mutable) implements ContextParameter {
	public static final MapCodec<SlottedLocalContextParameter> CODEC = CompositeCodec.of(
			Codec.INT.fieldOf("slot"), SlottedLocalContextParameter::slot,
			ClassDescs.ANY_CODEC.fieldOf("local_type"), SlottedLocalContextParameter::expectedType,
			Codec.BOOL.optional(false).fieldOf("mutable"), SlottedLocalContextParameter::mutable,
			SlottedLocalContextParameter::new
	);

	@Override
	public Prepared prepare(TransformContext context, TransformableCode code, Point point) throws TransformException {
		if (this.slot >= code.model().maxLocals()) {
			throw new TransformException("Slot is out of LVT bounds: " + this.slot + " / " + code.model().maxLocals());
		}

		return this.mutable ? new Mutable(this.expectedType, this.slot) : new Immutable(this.expectedType, this.slot);
	}

	@Override
	public ClassDesc type() {
		return !this.mutable ? this.expectedType : BaseRefImpl.refFor(this.expectedType, false);
	}

	@Override
	public MapCodec<? extends ContextParameter> codec() {
		return CODEC;
	}

	private static void load(CodeBuilder builder, ClassDesc expectedType, int slot) {
		builder.loadInstruction(TypeKind.from(expectedType), slot);
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

		// derived
		private final ClassDesc refType;
		private final ClassDesc constructorType;

		// newly allocated slot for the Ref
		private int refSlot = -1;

		private Mutable(ClassDesc expectedType, int slot) {
			this.expectedType = expectedType;
			this.slot = slot;

			this.refType = BaseRefImpl.refFor(this.expectedType, true);
			this.constructorType = this.expectedType.isPrimitive() ? this.expectedType : ConstantDescs.CD_Object;
		}

		@Override
		public void pre(CodeBuilder builder) {
			builder.new_(this.refType);
			builder.dup(); // invokespecial consumes one

			load(builder, this.expectedType, this.slot);

			MethodTypeDesc refConstructorDesc = MethodTypeDesc.of(ConstantDescs.CD_void, this.constructorType);
			builder.invokespecial(this.refType, "<init>", refConstructorDesc);

			this.refSlot = builder.allocateLocal(TypeKind.ReferenceType);
			builder.dup(); // store will consume one, we want one leftover
			builder.astore(this.refSlot);
		}

		@Override
		public void post(CodeBuilder builder) {
			if (this.refSlot < 0) {
				throw new TransformException("Ref slot is not allocated: " + this.refSlot);
			}

			builder.aload(this.refSlot);
			builder.dup();

			MethodTypeDesc refGetDesc = MethodTypeDesc.of(this.constructorType);
			builder.invokevirtual(this.refType, "get", refGetDesc);

			TypeKind kind = TypeKind.from(this.expectedType);
			builder.storeInstruction(kind, this.slot);
			builder.invokevirtual(this.refType, "discard", BaseRefImpl.DISCARD_DESC);
		}
	}
}
