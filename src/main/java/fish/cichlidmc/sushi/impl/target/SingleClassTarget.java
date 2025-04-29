package fish.cichlidmc.sushi.impl.target;

import fish.cichlidmc.sushi.api.target.ClassTarget;
import fish.cichlidmc.sushi.api.util.JavaType;
import fish.cichlidmc.tinycodecs.Codec;
import fish.cichlidmc.tinycodecs.map.MapCodec;
import org.objectweb.asm.tree.ClassNode;

import java.util.Collections;
import java.util.Optional;
import java.util.Set;

public final class SingleClassTarget implements ClassTarget {
	public static final Codec<SingleClassTarget> CODEC = JavaType.CLASS_CODEC.xmap(
			SingleClassTarget::new, target -> target.clazz
	);
	public static final MapCodec<SingleClassTarget> MAP_CODEC = CODEC.fieldOf("class");

	private final JavaType clazz;

	public SingleClassTarget(JavaType clazz) {
		this.clazz = clazz;
	}

	@Override
	public boolean shouldApply(ClassNode target) {
		return this.clazz.internalName.equals(target.name);
	}

	@Override
	public Optional<Set<String>> concreteTargets() {
		return Optional.of(Collections.singleton(this.clazz.internalName));
	}

	@Override
	public MapCodec<? extends ClassTarget> codec() {
		return MAP_CODEC;
	}
}
