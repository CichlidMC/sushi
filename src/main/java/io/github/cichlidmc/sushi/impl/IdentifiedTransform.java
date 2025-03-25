package io.github.cichlidmc.sushi.impl;

import io.github.cichlidmc.sushi.api.transform.TransformException;
import io.github.cichlidmc.sushi.api.util.Id;
import org.objectweb.asm.tree.ClassNode;

public final class IdentifiedTransform {
	public final Id id;
	public final TargetedTransform transform;

	public IdentifiedTransform(Id id, TargetedTransform transform) {
		this.id = id;
		this.transform = transform;
	}

	public boolean apply(ClassNode node) throws TransformException {
		try {
			return this.transform.apply(node);
		} catch (TransformException e) {
			throw new TransformException(
					String.format(
							"Error applying transformer %s to class %s: %s",
							this.id, node.name, e.getMessage()
					),
					e.getCause()
			);
		} catch (Throwable t) {
			throw new TransformException(String.format(
					"An unhandled exception occurred while applying transformer %s to class %s",
					this.id, node.name
			), t);
		}
	}
}
