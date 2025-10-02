package fish.cichlidmc.sushi.impl.registry;

import fish.cichlidmc.sushi.api.param.ContextParameter;
import fish.cichlidmc.sushi.api.param.builtin.ShareContextParameter;
import fish.cichlidmc.sushi.api.param.builtin.local.SlottedLocalContextParameter;
import fish.cichlidmc.sushi.api.registry.Id;
import fish.cichlidmc.sushi.api.registry.SimpleRegistry;
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
import fish.cichlidmc.sushi.api.transform.Transform;
import fish.cichlidmc.sushi.api.transform.builtin.AddInterfaceTransform;
import fish.cichlidmc.sushi.api.transform.builtin.InjectTransform;
import fish.cichlidmc.sushi.api.transform.builtin.ModifyExpressionTransform;
import fish.cichlidmc.sushi.api.transform.builtin.SlicedTransform;
import fish.cichlidmc.sushi.api.transform.builtin.WrapOpTransform;
import fish.cichlidmc.sushi.api.transform.builtin.access.PublicizeClassTransform;
import fish.cichlidmc.sushi.api.transform.builtin.access.PublicizeFieldTransform;
import fish.cichlidmc.tinycodecs.map.MapCodec;

import java.util.function.Supplier;

public final class SushiBootstraps {
	public static <T> T make(Supplier<T> supplier) {
		return supplier.get();
	}

	public static void bootstrapTargets(SimpleRegistry.Builder<MapCodec<? extends ClassTarget>> registry) {
		registry.register(id("single_class"), SingleClassTarget.MAP_CODEC);
		registry.register(id("union"), UnionClassTarget.MAP_CODEC);
		registry.register(id("everything"), EverythingClassTarget.CODEC);
	}

	public static void bootstrapTransforms(SimpleRegistry.Builder<MapCodec<? extends Transform>> registry) {
		registry.register(id("inject"), InjectTransform.CODEC);
		registry.register(id("modify_expression"), ModifyExpressionTransform.CODEC);
		registry.register(id("wrap_operation"), WrapOpTransform.CODEC);
		registry.register(id("add_interface"), AddInterfaceTransform.CODEC);
		registry.register(id("sliced"), SlicedTransform.CODEC);
		registry.register(id("publicize/class"), PublicizeClassTransform.CODEC);
		registry.register(id("publicize/field"), PublicizeFieldTransform.CODEC);
	}

	public static void boostrapInjectionPoints(SimpleRegistry.Builder<MapCodec<? extends InjectionPoint>> registry) {
		registry.register(id("head"), HeadInjectionPoint.MAP_CODEC);
		registry.register(id("tail"), TailInjectionPoint.MAP_CODEC);
		registry.register(id("return"), ReturnInjectionPoint.CODEC);
		registry.register(id("expression"), ExpressionInjectionPoint.CODEC);
	}

	public static void bootstrapExpressionTargets(SimpleRegistry.Builder<MapCodec<? extends ExpressionTarget>> registry) {
		registry.register(id("invoke"), InvokeExpressionTarget.CODEC);
	}

	public static void bootstrapContextParameters(SimpleRegistry.Builder<MapCodec<? extends ContextParameter>> registry) {
		registry.register(id("local/slot"), SlottedLocalContextParameter.CODEC);
		registry.register(id("share"), ShareContextParameter.CODEC);
	}

	private static Id id(String path) {
		return new Id(Id.BUILT_IN_NAMESPACE, path);
	}
}
