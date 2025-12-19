package fish.cichlidmc.sushi.impl.transformer.slice;

import fish.cichlidmc.sushi.api.attach.AttachmentMap;
import fish.cichlidmc.sushi.api.model.TransformableMethod;
import fish.cichlidmc.sushi.api.model.code.InstructionHolder;
import fish.cichlidmc.sushi.api.model.code.Selection;
import fish.cichlidmc.sushi.api.model.code.TransformableCode;

import java.lang.classfile.CodeModel;
import java.util.NavigableSet;

public final class SlicedTransformableCode implements TransformableCode {
	private final TransformableCode wrapped;
	private final NavigableSet<InstructionHolder<?>> slicedInstructions;
	private final SlicedSelectionBuilder slicedSelectionBuilder;

	public SlicedTransformableCode(TransformableCode wrapped, NavigableSet<InstructionHolder<?>> slicedInstructions) {
		this.wrapped = wrapped;
		this.slicedInstructions = slicedInstructions;
		this.slicedSelectionBuilder = new SlicedSelectionBuilder(wrapped.select(), slicedInstructions);
	}

	@Override
	public CodeModel model() {
		return this.wrapped.model();
	}

	@Override
	public TransformableMethod owner() {
		return this.wrapped.owner();
	}

	@Override
	public NavigableSet<InstructionHolder<?>> instructions() {
		return this.slicedInstructions;
	}

	@Override
	public Selection.Builder select() {
		return this.slicedSelectionBuilder;
	}

	@Override
	public AttachmentMap attachments() {
		return this.wrapped.attachments();
	}
}
