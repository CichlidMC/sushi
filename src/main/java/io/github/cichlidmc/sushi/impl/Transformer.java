package io.github.cichlidmc.sushi.impl;

import io.github.cichlidmc.sushi.api.transform.TransformException;
import io.github.cichlidmc.sushi.api.util.Id;
import org.objectweb.asm.tree.ClassNode;

import java.util.Comparator;

public final class Transformer {
	public static final Comparator<Transformer> PRIORITY_COMPARATOR = Comparator.comparingInt(transform -> transform.definition.priority);

	public final Id id;
	public final TransformerDefinition definition;

	public Transformer(Id id, TransformerDefinition definition) {
		this.id = id;
		this.definition = definition;
	}

	public boolean apply(ClassNode node) throws TransformException {
		try {
			return this.definition.apply(node);
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
