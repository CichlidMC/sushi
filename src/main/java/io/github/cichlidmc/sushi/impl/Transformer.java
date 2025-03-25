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
			throw new TransformException("Error applying transformer " + this.id + " to class " + node.name, e);
		} catch (Throwable t) {
			throw new TransformException(
					"An unhandled exception occurred while applying transformer " + this.id + " to class " + node.name, t
			);
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
