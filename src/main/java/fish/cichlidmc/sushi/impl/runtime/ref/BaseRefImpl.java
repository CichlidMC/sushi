package fish.cichlidmc.sushi.impl.runtime.ref;

import java.lang.constant.ClassDesc;
import java.lang.constant.ConstantDescs;
import java.lang.constant.MethodTypeDesc;

public abstract class BaseRefImpl {
	public static final MethodTypeDesc DISCARD_DESC = MethodTypeDesc.of(ConstantDescs.CD_void);

	private boolean discarded;

	public void discard() {
		this.discarded = true;
	}

	protected void checkDiscarded() {
		if (this.discarded) {
			throw new IllegalStateException("Ref has already been discarded");
		}
	}

	public static ClassDesc refFor(ClassDesc type, boolean impl) {
		if (!type.isPrimitive()) {
			return impl ? ObjectRefImpl.IMPL_DESC : ObjectRefImpl.API_DESC;
		}

		return switch (type.descriptorString()) {
			case "I" -> impl ? IntRefImpl.IMPL_DESC : IntRefImpl.API_DESC;
			case "J" -> impl ? LongRefImpl.IMPL_DESC : LongRefImpl.API_DESC;
			case "F" -> impl ? FloatRefImpl.IMPL_DESC : FloatRefImpl.API_DESC;
			case "D" -> impl ? DoubleRefImpl.IMPL_DESC : DoubleRefImpl.API_DESC;
			case "S" -> impl ? ShortRefImpl.IMPL_DESC : ShortRefImpl.API_DESC;
			case "B" -> impl ? ByteRefImpl.IMPL_DESC : ByteRefImpl.API_DESC;
			case "C" -> impl ? CharRefImpl.IMPL_DESC : CharRefImpl.API_DESC;
			case "Z" -> impl ? BoolRefImpl.IMPL_DESC : BoolRefImpl.API_DESC;
			case "V" -> throw new IllegalArgumentException("no refs for void");
			default -> throw new IllegalArgumentException("Unknown primitive: " + type);
		};
	}
}
