package io.github.cichlidmc.sushi.impl.transform.wrap_op;

import io.github.cichlidmc.sushi.api.BuiltInPhases;
import io.github.cichlidmc.sushi.api.transform.HookingTransform;
import io.github.cichlidmc.sushi.api.transform.TransformException;
import io.github.cichlidmc.sushi.api.transform.TransformType;
import io.github.cichlidmc.sushi.api.transform.expression.ExpressionTarget;
import io.github.cichlidmc.sushi.api.transform.expression.FoundExpressionTargets;
import io.github.cichlidmc.sushi.api.transform.wrap_op.Operation;
import io.github.cichlidmc.sushi.api.util.JavaType;
import io.github.cichlidmc.sushi.api.util.Utils;
import io.github.cichlidmc.sushi.api.util.method.MethodDescription;
import io.github.cichlidmc.sushi.api.util.method.MethodTarget;
import io.github.cichlidmc.tinycodecs.codec.map.CompositeCodec;
import io.github.cichlidmc.tinycodecs.map.MapCodec;
import org.objectweb.asm.Handle;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.InvokeDynamicInsnNode;
import org.objectweb.asm.tree.LdcInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.VarInsnNode;

import java.lang.invoke.CallSite;
import java.lang.invoke.LambdaMetafactory;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class WrapOpTransform extends HookingTransform {
	public static final String LMF_DESC = Utils.createMethodDesc(
			CallSite.class,
			MethodHandles.Lookup.class, String.class, MethodType.class, MethodType.class, MethodHandle.class, MethodType.class
	);
	/**
	 * @see LambdaMetafactory#metafactory(MethodHandles.Lookup, String, MethodType, MethodType, MethodHandle, MethodType)
	 */
	public static final Handle LMF_HANDLE = new Handle(
			Opcodes.H_INVOKESTATIC,
			Type.getInternalName(LambdaMetafactory.class),
			"metafactory", LMF_DESC, false
	);
	public static final String OPERATION_DESC = Utils.createMethodDesc(
			Object.class, Object[].class
	);

	public static final MapCodec<WrapOpTransform> CODEC = CompositeCodec.of(
			MethodTarget.CODEC.fieldOf("method"), transform -> transform.method,
			MethodDescription.WITH_CLASS_CODEC.fieldOf("wrapper"), transform -> transform.hook,
			ExpressionTarget.CODEC.fieldOf("target"), transform -> transform.target,
			WrapOpTransform::new
	);

	public static final TransformType TYPE = new TransformType(CODEC, BuiltInPhases.WRAP_OPERATION);

	private static final AtomicInteger idGenerator = new AtomicInteger();

	private final ExpressionTarget target;

	public WrapOpTransform(MethodTarget method, MethodDescription wrapper, ExpressionTarget target) {
		super(method, wrapper);
		this.target = target;
	}

	@Override
	protected boolean apply(ClassNode clazz, MethodNode method, Method hook) throws TransformException {
		// apply to initial targets, not already wrapped by another transform
		boolean transformed = this.applyInitial(clazz, method, hook);
		// apply to already wrapped targets
		transformed |= this.applyWrapped(clazz, method, hook);

		return transformed;
	}

	// TODO: 'this' argument is missing from hooks for non-static methods
	private boolean applyInitial(ClassNode clazz, MethodNode method, Method hook) throws TransformException {
		FoundExpressionTargets targets = this.target.find(method.instructions);
		if (targets == null)
			return false;

		for (AbstractInsnNode instruction : targets.instructions) {
			// generate lambda method
			MethodNode lambda = new MethodNode();
			lambda.access = Opcodes.ACC_PRIVATE + Opcodes.ACC_STATIC + Opcodes.ACC_SYNTHETIC;
			// TODO: improve name generation
			lambda.name = "wrap_operation$" + method.name + "$" + idGenerator.getAndIncrement();
			lambda.desc = JavaType.methodDesc(targets.output, JavaType.of(Object[].class));

			// validate array size
			lambda.instructions.add(WrapOpValidation.newInvoke(targets.inputs.length));

			// unpack array
			for (int i = 0; i < targets.inputs.length; i++) {
				// push array
				lambda.instructions.add(new VarInsnNode(Opcodes.ALOAD, 0));
				// push index
				lambda.instructions.add(new LdcInsnNode(i));
				// get from array
				lambda.instructions.add(new InsnNode(Opcodes.AALOAD));
			}

			// move original
			lambda.instructions.add(instruction.clone(new HashMap<>()));
			// and return
			lambda.instructions.add(new InsnNode(targets.output.returnCode()));

			clazz.methods.add(lambda);

			InsnList list = new InsnList();
			list.add(new InvokeDynamicInsnNode(
					// lambda type method name
					"call",
					// lambda factory. args are captured args (none), returns lambda type
					Utils.createMethodDesc(Operation.class),
					// metafactory handle
					LMF_HANDLE,
					// args start
					// lambda method desc
					OPERATION_DESC,
					// lambda impl handle
					new Handle(
							Opcodes.H_INVOKESTATIC,
							clazz.name,
							lambda.name,
							lambda.desc,
							(clazz.access & Opcodes.ACC_INTERFACE) != 0
					),
					// lambda method desc again?
					OPERATION_DESC
			));

			// push new invoke
			list.add(new MethodInsnNode(
					Opcodes.INVOKESTATIC,
					Type.getInternalName(hook.getDeclaringClass()),
					hook.getName(),
					Type.getMethodDescriptor(hook)
			));

			// push the new Operation and invoke
			method.instructions.insert(instruction, list);
			// remove the old one
			method.instructions.remove(instruction);
		}

		return true;
	}

	private boolean applyWrapped(ClassNode clazz, MethodNode method, Method hook) throws TransformException {
		return false;
	}

	@Override
	public String describe() {
		return "Wrap operation @ [" + this.target.describe() + "] in [" + this.method + "] calling [" + this.hook + ']';
	}

	@Override
	public TransformType type() {
		return TYPE;
	}
}
