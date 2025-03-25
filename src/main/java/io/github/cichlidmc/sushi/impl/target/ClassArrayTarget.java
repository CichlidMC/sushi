package io.github.cichlidmc.sushi.impl.target;

import io.github.cichlidmc.sushi.api.target.ClassTarget;
import io.github.cichlidmc.sushi.impl.util.JavaType;
import io.github.cichlidmc.tinycodecs.Codec;
import io.github.cichlidmc.tinycodecs.MapCodec;
import org.objectweb.asm.tree.ClassNode;

import java.util.List;

public final class ClassArrayTarget implements ClassTarget {
	public static final Codec<ClassArrayTarget> CODEC = JavaType.CLASS_CODEC.listOrSingle().xmap(
			ClassArrayTarget::new, target -> target.targets
	);
	public static final MapCodec<ClassArrayTarget> MAP_CODEC = CODEC.fieldOf("classes");

	private final List<JavaType> targets;

	public ClassArrayTarget(List<JavaType> targets) {
		this.targets = targets;
	}

	@Override
	public boolean shouldApply(ClassNode target) {
		for (JavaType type : this.targets) {
			if (type.internalName.equals(target.name)) {
				return true;
			}
		}
		return false;
	}

	@Override
	public MapCodec<? extends ClassTarget> codec() {
		return MAP_CODEC;
	}
}
