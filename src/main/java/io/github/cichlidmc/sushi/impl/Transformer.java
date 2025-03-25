package io.github.cichlidmc.sushi.impl;

import io.github.cichlidmc.sushi.api.transform.TransformException;
import io.github.cichlidmc.sushi.api.util.Id;
import org.jetbrains.annotations.NotNull;
import org.objectweb.asm.tree.ClassNode;

public final class Transformer implements Comparable<Transformer> {
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

	@Override
	public int compareTo(@NotNull Transformer that) {
		int priority = Integer.compare(this.definition.priority, that.definition.priority);
		if (priority != 0)
			return priority;

		return this.id.compareTo(that.id);
	}

	@Override
	public int hashCode() {
		return this.id.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		return obj.getClass() == Transformer.class && this.id.equals(((Transformer) obj).id);
	}
}
