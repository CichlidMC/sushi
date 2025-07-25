package fish.cichlidmc.sushi.impl.transform.sliced.model;

import fish.cichlidmc.sushi.api.model.code.TransformableCode;

@FunctionalInterface
public interface CodeSlicer {
	SlicedTransformableCode slice(TransformableCode original);
}
