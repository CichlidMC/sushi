package io.github.cichlidmc.sushi.impl;

import io.github.cichlidmc.sushi.api.transform.TransformException;
import io.github.cichlidmc.sushi.api.util.Id;
import org.objectweb.asm.tree.ClassNode;

public final class Transformer {
	public final Id id;
	public final TransformerInstance instance;

	public Transformer(Id id, TransformerInstance instance) {
		this.id = id;
		this.instance = instance;
	}

	public boolean apply(ClassNode node) throws TransformException {
		try {
			return this.instance.apply(node);
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
