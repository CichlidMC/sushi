package fish.cichlidmc.sushi.impl.phase;

import fish.cichlidmc.sushi.api.Transformer;
import fish.cichlidmc.sushi.api.validation.Validation;
import fish.cichlidmc.sushi.impl.transform.TransformContextImpl;
import org.glavo.classfile.ClassFile;
import org.glavo.classfile.ClassModel;
import org.glavo.classfile.ClassTransform;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

public record TransformPhase(int phase, List<Transformer> transformers) {
	public static final Comparator<TransformPhase> BY_VALUE = Comparator.comparingInt(TransformPhase::phase);

	public TransformPhase(int phase, List<Transformer> transformers) {
		if (transformers.isEmpty()) {
			throw new IllegalArgumentException("Phase must contain at least 1 transformer");
		}

		this.phase = phase;
		this.transformers = Collections.unmodifiableList(transformers);
	}

	public byte[] transform(ClassFile context, ClassModel model, Optional<Validation> validation, boolean addMetadata, @Nullable ClassTransform andThen) {
		TransformContextImpl transformContext = new TransformContextImpl(model, validation, addMetadata);
		ClassTransform transform = new PhaseTransform(this.transformers, transformContext);
		ClassTransform finalTransform = andThen == null ? transform : transform.andThen(andThen);
		return context.transform(model, finalTransform);
	}
}
