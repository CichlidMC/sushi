package io.github.cichlidmc.sushi.impl.transform;

import io.github.cichlidmc.sushi.api.transform.Transform;
import io.github.cichlidmc.tinycodecs.MapCodec;
import org.objectweb.asm.tree.ClassNode;

public class InjectTransform implements Transform {
	@Override
	public boolean apply(ClassNode node) {
		return false;
	}

	@Override
	public MapCodec<? extends Transform> codec() {
		return null;
	}
}
