package fish.cichlidmc.sushi.impl.transformer;

import fish.cichlidmc.sushi.api.registry.Id;
import fish.cichlidmc.sushi.api.requirement.Requirement;
import fish.cichlidmc.sushi.api.transformer.TransformContext;
import fish.cichlidmc.sushi.impl.model.TransformableClassImpl;

public final class TransformContextImpl implements TransformContext {
	public static final ScopedValue<TransformContextImpl> CURRENT = ScopedValue.newInstance();

	private final TransformableClassImpl clazz;
	private final PreparedTransform transform;

	public TransformContextImpl(TransformableClassImpl clazz, PreparedTransform transform) {
		this.clazz = clazz;
		this.transform = transform;
	}

	@Override
	public TransformableClassImpl clazz() {
		return this.clazz;
	}

	@Override
	public void require(Requirement requirement) {
		this.clazz.transformation.requirements.add(this.transform, requirement);
	}

	@Override
	public boolean addMetadata() {
		return this.clazz.transformation.metadata;
	}

	@Override
	public Id transformerId() {
		return this.transform.owner.id();
	}

	public static TransformContextImpl current() {
		return CURRENT.orElseThrow(() -> new IllegalStateException("No TransformContext available"));
	}
}
