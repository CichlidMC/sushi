package fish.cichlidmc.sushi.impl.transform.wrap_op;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.LdcInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.VarInsnNode;

public final class WrapOpValidation {
	public static final String OWNER = Type.getInternalName(WrapOpValidation.class);
	public static final String NAME = "checkCount";
	public static final String DESC = Type.getMethodDescriptor(
			Type.VOID_TYPE,
			Type.getType(Object[].class), Type.INT_TYPE
	);

	public WrapOpValidation() {}

	public static void checkCount(Object[] args, int expectedSize) {
		if (args.length != expectedSize) {
			throw new RuntimeException(
					"Received invalid argument array after wrap: expected " + expectedSize + " element(s), got " + args.length
			);
		}
	}

	static InsnList newInvoke(int expectedSize) {
		InsnList list = new InsnList();

		// load array
		list.add(new VarInsnNode(Opcodes.ALOAD, 0));
		// push size
		list.add(new LdcInsnNode(expectedSize));
		// invoke
		list.add(new MethodInsnNode(Opcodes.INVOKESTATIC, OWNER, NAME, DESC));

		return list;
	}
}
