package fish.cichlidmc.sushi.impl.transform;

import fish.cichlidmc.sushi.api.Transformer;
import fish.cichlidmc.sushi.api.transform.TransformContext;
import org.objectweb.asm.tree.ClassNode;

import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public final class TransformContextImpl implements TransformContext, Iterator<Transformer> {
	private final ClassNode node;
	private final List<Transformer> transformers;
	private final Iterator<Transformer> iterator;
	private final Set<String> generatedMethodNames;

	private int currentTransformer;

	public TransformContextImpl(ClassNode node, List<Transformer> transformers) {
		if (transformers.isEmpty()) {
			throw new IllegalArgumentException("No transformers?");
		}

		this.node = node;
		this.transformers = Collections.unmodifiableList(transformers);
		this.iterator = this.transformers.iterator();
		this.generatedMethodNames = new HashSet<>();
	}

	@Override
	public boolean hasNext() {
		return this.iterator.hasNext();
	}

	@Override
	public Transformer next() {
		return this.iterator.next();
	}

	public void finishCurrent() {
		this.currentTransformer++;
	}

	@Override
	public ClassNode node() {
		return this.node;
	}

	@Override
	public String generateUniqueMethodName(String prefix) {
		String base = prefix + "$$" + this.sanitizeCurrentId();

		String name = base;
		for (int i = 1; this.generatedMethodNames.contains(name); i++) {
			name = base + "$$" + i;
		}

		this.generatedMethodNames.add(name);
		return name;
	}

	@Override
	public List<Transformer> applied() {
		return this.transformers.subList(0, this.currentTransformer);
	}

	@Override
	public Transformer applying() {
		return this.transformers.get(this.currentTransformer);
	}

	@Override
	public List<Transformer> toApply() {
		return this.transformers.subList(this.currentTransformer + 1, this.transformers.size());
	}

	private String sanitizeCurrentId() {
		Transformer transformer = this.applying();
		String sanitizedPath = transformer.id.path
				.replace('/', '$')
				.replace('.', '$');

		return transformer.id.namespace + "$" + sanitizedPath;
	}
}
