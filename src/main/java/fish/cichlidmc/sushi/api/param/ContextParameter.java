package fish.cichlidmc.sushi.api.param;

import fish.cichlidmc.sushi.api.model.code.Point;
import fish.cichlidmc.sushi.api.model.code.TransformableCode;
import fish.cichlidmc.sushi.api.transform.TransformContext;
import fish.cichlidmc.sushi.api.transform.TransformException;
import fish.cichlidmc.sushi.api.util.SimpleRegistry;
import fish.cichlidmc.sushi.impl.SushiInternals;
import fish.cichlidmc.tinycodecs.Codec;
import fish.cichlidmc.tinycodecs.map.MapCodec;
import org.glavo.classfile.CodeBuilder;

import java.lang.constant.ClassDesc;
import java.util.function.Function;

/**
 * Context parameters are additional parameters that may be appended to hook methods.
 */
public interface ContextParameter {
	SimpleRegistry<MapCodec<? extends ContextParameter>> REGISTRY = SimpleRegistry.create(SushiInternals::bootstrapContextParameters);
	Codec<ContextParameter> CODEC = REGISTRY.byIdCodec().dispatch(ContextParameter::codec, Function.identity());

	/**
	 * Prepare to inject this parameter for a hook that will be inserted at {@code point}.
	 * @return a prepared injection, which will be invoked when ready
	 */
	Prepared prepare(TransformContext context, TransformableCode code, Point point) throws TransformException;

	ClassDesc type();

	MapCodec<? extends ContextParameter> codec();

	interface Prepared {
		default void pre(CodeBuilder builder) {
		}

		default void post(CodeBuilder builder) {
		}
	}
}
