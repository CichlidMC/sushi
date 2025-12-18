package fish.cichlidmc.sushi.test.framework;

import fish.cichlidmc.sushi.api.registry.Id;
import fish.cichlidmc.sushi.api.transformer.ConfiguredTransformer;
import fish.cichlidmc.sushi.api.transformer.Transformer;

import java.util.function.Function;

public interface Transformable<T extends Transformable<T>> {
	T transform(ConfiguredTransformer transformer);

	T transform(Transformer transformer);

	T transform(Function<Id, ConfiguredTransformer> factory);
}
