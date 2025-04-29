package fish.cichlidmc.sushi.impl;

import fish.cichlidmc.sushi.api.SushiMetadata;
import fish.cichlidmc.sushi.api.target.ClassTarget;
import fish.cichlidmc.sushi.api.target.UnionClassTarget;
import fish.cichlidmc.sushi.api.transform.TransformType;
import fish.cichlidmc.sushi.api.transform.expression.ExpressionTarget;
import fish.cichlidmc.sushi.api.transform.inject.InjectionPoint;
import fish.cichlidmc.sushi.api.util.Id;
import fish.cichlidmc.sushi.api.util.SimpleRegistry;
import fish.cichlidmc.sushi.impl.target.EverythingClassTarget;
import fish.cichlidmc.sushi.impl.target.SingleClassTarget;
import fish.cichlidmc.sushi.impl.transform.InjectTransform;
import fish.cichlidmc.sushi.impl.transform.ModifyExpressionTransform;
import fish.cichlidmc.sushi.impl.transform.add_interface.AddInterfaceTransform;
import fish.cichlidmc.sushi.impl.transform.expression.InvokeExpressionTarget;
import fish.cichlidmc.sushi.impl.transform.point.ExpressionInjectionPoint;
import fish.cichlidmc.sushi.impl.transform.point.HeadInjectionPoint;
import fish.cichlidmc.sushi.impl.transform.point.ReturnInjectionPoint;
import fish.cichlidmc.sushi.impl.transform.point.TailInjectionPoint;
import fish.cichlidmc.sushi.impl.transform.wrap_op.WrapOpTransform;
import fish.cichlidmc.tinycodecs.map.MapCodec;
import org.objectweb.asm.Type;

import java.util.function.Supplier;

public final class SushiInternals {
	public static final String METADATA_DESC = Type.getDescriptor(SushiMetadata.class);

	public static <T> T make(Supplier<T> supplier) {
		return supplier.get();
	}

	public static void bootstrapTargets(SimpleRegistry.Builder<MapCodec<? extends ClassTarget>> registry) {
		registry.register(id("single_class"), SingleClassTarget.MAP_CODEC);
		registry.register(id("union"), UnionClassTarget.MAP_CODEC);
		registry.register(id("everything"), EverythingClassTarget.CODEC);
	}

	public static void bootstrapTransforms(SimpleRegistry.Builder<TransformType> registry) {
		registry.register(id("inject"), InjectTransform.TYPE);
		registry.register(id("modify_expression"), ModifyExpressionTransform.TYPE);
		registry.register(id("wrap_operation"), WrapOpTransform.TYPE);
		registry.register(id("add_interface"), AddInterfaceTransform.TYPE);
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

	private static Id id(String path) {
		return new Id(Id.BUILT_IN_NAMESPACE, path);
	}
}
