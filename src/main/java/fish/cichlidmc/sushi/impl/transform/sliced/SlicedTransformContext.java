package fish.cichlidmc.sushi.impl.transform.sliced;

import fish.cichlidmc.sushi.api.model.TransformableClass;
import fish.cichlidmc.sushi.api.registry.Id;
import fish.cichlidmc.sushi.api.requirement.Requirement;
import fish.cichlidmc.sushi.api.transform.TransformContext;

public record SlicedTransformContext(TransformContext wrapped, TransformableClass clazz) implements TransformContext {
	@Override
	public void require(Requirement requirement) {
		this.wrapped.require(requirement);
	}

	@Override
	public boolean addMetadata() {
		return this.wrapped.addMetadata();
	}

	@Override
	public Id transformerId() {
		return this.wrapped.transformerId();
	}
}
