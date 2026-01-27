package fish.cichlidmc.sushi.impl.transformer;

import fish.cichlidmc.sushi.api.transformer.DirectTransform;
import fish.cichlidmc.sushi.api.transformer.TransformContext;

public record PreparedDirectTransform<T extends DirectTransform>(T transform, TransformContext context) {
}
