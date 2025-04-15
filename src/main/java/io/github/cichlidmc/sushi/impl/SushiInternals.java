package io.github.cichlidmc.sushi.impl;

import io.github.cichlidmc.sushi.api.SushiMetadata;
import io.github.cichlidmc.sushi.api.target.ClassTarget;
import io.github.cichlidmc.sushi.api.target.UnionClassTarget;
import io.github.cichlidmc.sushi.api.transform.TransformType;
import io.github.cichlidmc.sushi.api.transform.expression.ExpressionTarget;
import io.github.cichlidmc.sushi.api.transform.inject.InjectionPoint;
import io.github.cichlidmc.sushi.api.util.Id;
import io.github.cichlidmc.sushi.api.util.SimpleRegistry;
import io.github.cichlidmc.sushi.impl.target.EverythingClassTarget;
import io.github.cichlidmc.sushi.impl.target.SingleClassTarget;
import io.github.cichlidmc.sushi.impl.transform.InjectTransform;
import io.github.cichlidmc.sushi.impl.transform.ModifyExpressionTransform;
import io.github.cichlidmc.sushi.impl.transform.expression.InvokeExpressionTarget;
import io.github.cichlidmc.sushi.impl.transform.point.HeadInjectionPoint;
import io.github.cichlidmc.sushi.impl.transform.point.ReturnInjectionPoint;
import io.github.cichlidmc.sushi.impl.transform.point.TailInjectionPoint;
import io.github.cichlidmc.tinycodecs.map.MapCodec;
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
	}

	public static void boostrapInjectionPoints(SimpleRegistry.Builder<MapCodec<? extends InjectionPoint>> registry) {
		registry.register(id("head"), HeadInjectionPoint.MAP_CODEC);
		registry.register(id("tail"), TailInjectionPoint.MAP_CODEC);
		registry.register(id("return"), ReturnInjectionPoint.CODEC);
	}

	public static void bootstrapExpressionTargets(SimpleRegistry.Builder<MapCodec<? extends ExpressionTarget>> registry) {
		registry.register(id("invoke"), InvokeExpressionTarget.CODEC);
	}

	private static Id id(String path) {
		return new Id(Id.BUILT_IN_NAMESPACE, path);
	}
}
