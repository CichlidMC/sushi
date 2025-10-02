package fish.cichlidmc.sushi.impl.transform.sliced.model;

import fish.cichlidmc.sushi.api.attach.AttachmentMap;
import fish.cichlidmc.sushi.api.model.TransformableMethod;
import fish.cichlidmc.sushi.api.model.code.InstructionList;
import fish.cichlidmc.sushi.api.model.code.Selection;
import fish.cichlidmc.sushi.api.model.code.TransformableCode;

public final class SlicedTransformableCode implements TransformableCode {
	private final TransformableCode wrapped;
	private final InstructionList subList;

	public SlicedTransformableCode(TransformableCode wrapped, InstructionList subList) {
		this.wrapped = wrapped;
		this.subList = subList;
	}

	@Override
	public SafeCodeModel model() {
		return this.wrapped.model();
	}

	@Override
	public TransformableMethod owner() {
		return this.wrapped.owner();
	}

	@Override
	public InstructionList instructions() {
		return this.subList;
	}

	@Override
	public Selection.Builder select() {
		return this.wrapped.select();
	}

	@Override
	public AttachmentMap attachments() {
		return this.wrapped.attachments();
	}
}
