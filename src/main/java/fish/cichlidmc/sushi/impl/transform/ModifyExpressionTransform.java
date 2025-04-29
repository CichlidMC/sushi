package fish.cichlidmc.sushi.impl.transform;

import fish.cichlidmc.sushi.api.transform.HookingTransform;
import fish.cichlidmc.sushi.api.transform.TransformContext;
import fish.cichlidmc.sushi.api.transform.TransformException;
import fish.cichlidmc.sushi.api.transform.TransformType;
import fish.cichlidmc.sushi.api.transform.expression.ExpressionTarget;
import fish.cichlidmc.sushi.api.transform.expression.FoundExpressionTargets;
import fish.cichlidmc.sushi.api.util.JavaType;
import fish.cichlidmc.sushi.api.util.method.MethodDescription;
import fish.cichlidmc.sushi.api.util.method.MethodTarget;
import fish.cichlidmc.tinycodecs.codec.map.CompositeCodec;
import fish.cichlidmc.tinycodecs.map.MapCodec;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;

import java.lang.reflect.Method;

public final class ModifyExpressionTransform extends HookingTransform {
	public static final MapCodec<ModifyExpressionTransform> CODEC = CompositeCodec.of(
			MethodTarget.CODEC.fieldOf("method"), transform -> transform.method,
			MethodDescription.WITH_CLASS_CODEC.fieldOf("modifier"), transform -> transform.hook,
			ExpressionTarget.CODEC.fieldOf("target"), transform -> transform.target,
			ModifyExpressionTransform::new
	);

	public static final TransformType TYPE = new TransformType(CODEC);

	private final ExpressionTarget target;

	private ModifyExpressionTransform(MethodTarget method, MethodDescription modifier, ExpressionTarget target) {
		super(method, modifier);
		this.target = target;
	}

	@Override
	protected boolean apply(TransformContext context, MethodNode method, Method hook) throws TransformException {
		FoundExpressionTargets targets = this.target.find(method.instructions);
		if (targets == null)
			return false;

		JavaType modifierType = JavaType.of(Type.getReturnType(hook));
		if (!modifierType.equals(targets.output)) {
			throw new TransformException("Found target and modifier have incompatible types: " + modifierType + " / " + targets.output);
		}

		for (AbstractInsnNode node : targets.instructions) {
			method.instructions.insert(node, this.buildInjection(hook));
		}

		return true;
	}

	private InsnList buildInjection(Method hook) {
		InsnList list = new InsnList();

		// TODO: insert parameters
		String desc = Type.getMethodDescriptor(hook);
		String owner = Type.getInternalName(hook.getDeclaringClass());
		list.add(new MethodInsnNode(Opcodes.INVOKESTATIC, owner, hook.getName(), desc));

		return list;
	}

	@Override
	protected Method getAndValidateHook() throws TransformException {
		Method hook = super.getAndValidateHook();

		Class<?>[] params = hook.getParameterTypes();
		if (params.length != 1) {
			throw new TransformException("Modifier method must take one parameter, the original value");
		}

		Class<?> returnType = hook.getReturnType();

		if (returnType != params[0]) {
			throw new TransformException("Modifier method must take and return the same type: " + this.hook);
		}

		return hook;
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
