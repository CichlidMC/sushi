package fish.cichlidmc.sushi.api;

import fish.cichlidmc.sushi.api.condition.Condition;
import fish.cichlidmc.sushi.api.condition.builtin.AllCondition;
import fish.cichlidmc.sushi.api.condition.builtin.AnyCondition;
import fish.cichlidmc.sushi.api.condition.builtin.NotCondition;
import fish.cichlidmc.sushi.api.condition.builtin.TransformerPresentCondition;
import fish.cichlidmc.sushi.api.param.ContextParameter;
import fish.cichlidmc.sushi.api.param.builtin.ShareContextParameter;
import fish.cichlidmc.sushi.api.param.builtin.local.SlottedLocalContextParameter;
import fish.cichlidmc.sushi.api.registry.Id;
import fish.cichlidmc.sushi.api.registry.content.SushiClassTargets;
import fish.cichlidmc.sushi.api.registry.content.SushiConditions;
import fish.cichlidmc.sushi.api.registry.content.SushiContextParameters;
import fish.cichlidmc.sushi.api.registry.content.SushiExpressionTargets;
import fish.cichlidmc.sushi.api.registry.content.SushiInjectionPoints;
import fish.cichlidmc.sushi.api.registry.content.SushiRequirements;
import fish.cichlidmc.sushi.api.registry.content.SushiTransformers;
import fish.cichlidmc.sushi.api.requirement.Requirement;
import fish.cichlidmc.sushi.api.requirement.builtin.ClassRequirement;
import fish.cichlidmc.sushi.api.requirement.builtin.FieldRequirement;
import fish.cichlidmc.sushi.api.requirement.builtin.FlagsRequirement;
import fish.cichlidmc.sushi.api.requirement.builtin.FullyDefinedRequirement;
import fish.cichlidmc.sushi.api.requirement.builtin.MethodRequirement;
import fish.cichlidmc.sushi.api.target.ClassTarget;
import fish.cichlidmc.sushi.api.target.builtin.EverythingClassTarget;
import fish.cichlidmc.sushi.api.target.builtin.SingleClassTarget;
import fish.cichlidmc.sushi.api.target.builtin.UnionClassTarget;
import fish.cichlidmc.sushi.api.target.expression.ExpressionTarget;
import fish.cichlidmc.sushi.api.target.expression.builtin.InvokeExpressionTarget;
import fish.cichlidmc.sushi.api.target.inject.InjectionPoint;
import fish.cichlidmc.sushi.api.target.inject.builtin.ExpressionInjectionPoint;
import fish.cichlidmc.sushi.api.target.inject.builtin.HeadInjectionPoint;
import fish.cichlidmc.sushi.api.target.inject.builtin.ReturnInjectionPoint;
import fish.cichlidmc.sushi.api.target.inject.builtin.TailInjectionPoint;
import fish.cichlidmc.sushi.api.transformer.Transformer;
import fish.cichlidmc.sushi.api.transformer.builtin.AddInterfaceTransformer;
import fish.cichlidmc.sushi.api.transformer.builtin.InjectTransformer;
import fish.cichlidmc.sushi.api.transformer.builtin.ModifyExpressionTransformer;
import fish.cichlidmc.sushi.api.transformer.builtin.WrapOpTransformer;
import fish.cichlidmc.sushi.api.transformer.builtin.access.PublicizeClassTransformer;
import fish.cichlidmc.sushi.api.transformer.builtin.access.PublicizeFieldTransformer;
import org.jetbrains.annotations.ApiStatus;

import java.util.function.Supplier;

public final class Sushi {
	public static final String NAMESPACE = "sushi";

	private static boolean initialized = false;

	/**
	 * Fills in all of Sushi's registries with default values. You probably want to call this before doing anything.
	 */
	public static void bootstrap() {
		if (initialized)
			return;

		ClassTarget.REGISTRY.register(SushiClassTargets.SINGLE_CLASS, SingleClassTarget.MAP_CODEC);
		ClassTarget.REGISTRY.register(SushiClassTargets.UNION, UnionClassTarget.MAP_CODEC);
		ClassTarget.REGISTRY.register(SushiClassTargets.EVERYTHING, EverythingClassTarget.CODEC);

		Transformer.REGISTRY.register(SushiTransformers.INJECT, InjectTransformer.CODEC.mapCodec());
		Transformer.REGISTRY.register(SushiTransformers.MODIFY_EXPRESSION, ModifyExpressionTransformer.CODEC.mapCodec());
		Transformer.REGISTRY.register(SushiTransformers.WRAP_OPERATION, WrapOpTransformer.CODEC.mapCodec());
		Transformer.REGISTRY.register(SushiTransformers.ADD_INTERFACE, AddInterfaceTransformer.CODEC.mapCodec());
		Transformer.REGISTRY.register(SushiTransformers.PUBLICIZE_CLASS, PublicizeClassTransformer.CODEC);
		Transformer.REGISTRY.register(SushiTransformers.PUBLICIZE_FIELD, PublicizeFieldTransformer.CODEC.mapCodec());

		InjectionPoint.REGISTRY.register(SushiInjectionPoints.HEAD, HeadInjectionPoint.MAP_CODEC);
		InjectionPoint.REGISTRY.register(SushiInjectionPoints.TAIL, TailInjectionPoint.MAP_CODEC);
		InjectionPoint.REGISTRY.register(SushiInjectionPoints.RETURN, ReturnInjectionPoint.CODEC);
		InjectionPoint.REGISTRY.register(SushiInjectionPoints.EXPRESSION, ExpressionInjectionPoint.CODEC.mapCodec());

		ExpressionTarget.REGISTRY.register(SushiExpressionTargets.INVOKE, InvokeExpressionTarget.CODEC);

		ContextParameter.REGISTRY.register(SushiContextParameters.LOCAL_SLOT, SlottedLocalContextParameter.CODEC.mapCodec());
		ContextParameter.REGISTRY.register(SushiContextParameters.SHARE, ShareContextParameter.CODEC.mapCodec());

		Condition.REGISTRY.register(SushiConditions.ALL, AllCondition.CODEC);
		Condition.REGISTRY.register(SushiConditions.ANY, AnyCondition.CODEC);
		Condition.REGISTRY.register(SushiConditions.NOT, NotCondition.CODEC);
		Condition.REGISTRY.register(SushiConditions.TRANSFORMER_PRESENT, TransformerPresentCondition.CODEC);

		Requirement.REGISTRY.register(SushiRequirements.CLASS, ClassRequirement.CODEC.mapCodec());
		Requirement.REGISTRY.register(SushiRequirements.FIELD, FieldRequirement.CODEC.mapCodec());
		Requirement.REGISTRY.register(SushiRequirements.METHOD, MethodRequirement.CODEC.mapCodec());
		Requirement.REGISTRY.register(SushiRequirements.FLAGS, FlagsRequirement.CODEC.mapCodec());
		Requirement.REGISTRY.register(SushiRequirements.FULLY_DEFINED, FullyDefinedRequirement.CODEC.mapCodec());

		initialized = true;
	}

	/**
	 * Create a new {@link Id} using Sushi's namespace.
	 */
	public static Id id(String path) {
		return new Id(Sushi.NAMESPACE, path);
	}

	@ApiStatus.Internal
	public static <T> T make(Supplier<T> supplier) {
		return supplier.get();
	}
}
