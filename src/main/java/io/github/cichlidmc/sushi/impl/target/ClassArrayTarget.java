package io.github.cichlidmc.sushi.impl.target;

import io.github.cichlidmc.sushi.api.target.ClassTarget;
import io.github.cichlidmc.sushi.api.util.SushiCodecs;
import io.github.cichlidmc.tinycodecs.Codec;
import io.github.cichlidmc.tinycodecs.MapCodec;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.ClassNode;

import java.util.List;

public final class ClassArrayTarget implements ClassTarget {
	public static final Codec<ClassArrayTarget> CODEC = SushiCodecs.TYPE.listOrSingle().xmap(
			ClassArrayTarget::new, target -> target.targets
	);
	public static final MapCodec<ClassArrayTarget> MAP_CODEC = CODEC.fieldOf("classes");

	private final List<Type> targets;

	public ClassArrayTarget(List<Type> targets) {
		this.targets = targets;
	}

	@Override
	public boolean shouldApply(ClassNode target) {
		for (Type type : this.targets) {
			if (type.getInternalName().equals(target.name)) {
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
