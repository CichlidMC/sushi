package io.github.cichlidmc.sushi.impl.target;

import io.github.cichlidmc.sushi.api.target.ClassTarget;
import io.github.cichlidmc.sushi.impl.util.JavaType;
import io.github.cichlidmc.tinycodecs.Codec;
import io.github.cichlidmc.tinycodecs.map.MapCodec;
import org.objectweb.asm.tree.ClassNode;

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
	public MapCodec<? extends ClassTarget> codec() {
		return MAP_CODEC;
	}
}
