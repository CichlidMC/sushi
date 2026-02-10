package fish.cichlidmc.sushi.impl.transformer;

import fish.cichlidmc.sushi.api.model.ClassFileAccess;
import fish.cichlidmc.sushi.api.registry.Id;
import fish.cichlidmc.sushi.api.requirement.Requirement;
import fish.cichlidmc.sushi.api.transformer.TransformContext;
import fish.cichlidmc.sushi.impl.model.TransformableClassImpl;

public final class TransformContextImpl implements TransformContext {
	public static final ScopedValue<TransformContextImpl> CURRENT = ScopedValue.newInstance();

	private final TransformableClassImpl target;
	private final PreparedTransform transform;

	public TransformContextImpl(TransformableClassImpl target, PreparedTransform transform) {
		this.target = target;
		this.transform = transform;
	}

	@Override
	public TransformableClassImpl target() {
		return this.target;
	}

	@Override
	public void require(Requirement requirement) {
		this.target.transformation.requirements.add(this.transform, requirement);
	}

	@Override
	public boolean addMetadata() {
		return this.target.transformation.metadata;
	}

	@Override
	public ClassFileAccess classFile() {
		return this.target.transformation.classFile;
	}

	@Override
	public Id transformerId() {
		return this.transform.owner.id();
	}

	public static TransformContextImpl current() {
		return CURRENT.orElseThrow(() -> new IllegalStateException("No TransformContext available"));
	}
}
