package fish.cichlidmc.sushi.impl.transform.sliced;

import fish.cichlidmc.sushi.api.model.TransformableClass;
import fish.cichlidmc.sushi.api.transform.TransformContext;
import fish.cichlidmc.sushi.api.transform.UniqueMethodGenerator;
import fish.cichlidmc.sushi.api.util.Id;
import fish.cichlidmc.sushi.api.validation.Validation;

import java.util.Optional;

public record SlicedTransformContext(TransformContext wrapped, TransformableClass clazz) implements TransformContext {
	@Override
	public UniqueMethodGenerator createMethodGenerator(String prefix) {
		return this.wrapped.createMethodGenerator(prefix);
	}

	@Override
	public Optional<Validation> validation() {
		return this.wrapped.validation();
	}

	@Override
	public Id transformerId() {
		return this.wrapped.transformerId();
	}
}
