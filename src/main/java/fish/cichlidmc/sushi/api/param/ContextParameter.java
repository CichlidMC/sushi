package fish.cichlidmc.sushi.api.param;

import fish.cichlidmc.sushi.api.Sushi;
import fish.cichlidmc.sushi.api.model.code.CodeBlock;
import fish.cichlidmc.sushi.api.model.code.Point;
import fish.cichlidmc.sushi.api.model.code.TransformableCode;
import fish.cichlidmc.sushi.api.registry.SimpleRegistry;
import fish.cichlidmc.sushi.api.transformer.TransformContext;
import fish.cichlidmc.sushi.api.transformer.TransformException;
import fish.cichlidmc.tinycodecs.api.codec.Codec;
import fish.cichlidmc.tinycodecs.api.codec.map.MapCodec;
import org.glavo.classfile.CodeBuilder;

import java.lang.constant.ClassDesc;

/// Context parameters are additional parameters that may be appended to hook methods.
public interface ContextParameter {
	SimpleRegistry<MapCodec<? extends ContextParameter>> REGISTRY = SimpleRegistry.create(Sushi.NAMESPACE);
	Codec<ContextParameter> CODEC = Codec.codecDispatch(REGISTRY.byIdCodec(), ContextParameter::codec);

	/// Prepare to inject this parameter for a hook that will be inserted at `point`.
	/// @return a prepared injection, which will be invoked later
	Prepared prepare(TransformContext context, TransformableCode code, Point point) throws TransformException;

	ClassDesc type();

	MapCodec<? extends ContextParameter> codec();

	interface Prepared {
		default void pre(CodeBuilder builder) {
		}

		default void post(CodeBuilder builder) {
		}

		static Prepared ofPre(CodeBlock block) {
			return new Prepared() {
				@Override
				public void pre(CodeBuilder builder) {
					block.write(builder);
				}
			};
		}

		static Prepared ofPost(CodeBlock block) {
			return new Prepared() {
				@Override
				public void post(CodeBuilder builder) {
					block.write(builder);
				}
			};
		}
	}
}
