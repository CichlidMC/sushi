package fish.cichlidmc.sushi.impl.model.code;

import fish.cichlidmc.sushi.api.attach.AttachmentMap;
import fish.cichlidmc.sushi.api.model.TransformableMethod;
import fish.cichlidmc.sushi.api.model.code.InstructionHolder;
import fish.cichlidmc.sushi.api.model.code.TransformableCode;
import fish.cichlidmc.sushi.impl.model.TransformableMethodImpl;
import fish.cichlidmc.sushi.impl.model.code.selection.SelectionBuilderImpl;
import fish.cichlidmc.sushi.impl.operation.Operations;
import fish.cichlidmc.sushi.impl.transformer.TransformContextImpl;

import java.lang.classfile.CodeElement;
import java.lang.classfile.CodeModel;
import java.lang.classfile.Instruction;
import java.lang.classfile.PseudoInstruction;
import java.util.Collections;
import java.util.NavigableSet;
import java.util.TreeSet;

public final class TransformableCodeImpl implements TransformableCode {
	public final Operations operations;

	private final CodeModel model;
	private final TransformableMethodImpl owner;
	private final NavigableSet<InstructionHolder<?>> instructions;
	private final SelectionBuilderImpl selectionBuilder;
	private final AttachmentMap attachments;

	public TransformableCodeImpl(CodeModel model, TransformableMethodImpl owner) {
		this.model = model;
		this.owner = owner;
		this.instructions = getInstructions(model);
		this.operations = new Operations();

		TransformContextImpl context = owner.owner().context;
		this.selectionBuilder = new SelectionBuilderImpl(context::transformerId, this.instructions, this.operations);

		this.attachments = AttachmentMap.create();
	}

	@Override
	public CodeModel model() {
		return this.model;
	}

	@Override
	public TransformableMethod owner() {
		return this.owner;
	}

	@Override
	public NavigableSet<InstructionHolder<?>> instructions() {
		return this.instructions;
	}

	@Override
	public SelectionBuilderImpl select() {
		return this.selectionBuilder;
	}

	@Override
	public AttachmentMap attachments() {
		return this.attachments;
	}

	private static NavigableSet<InstructionHolder<?>> getInstructions(CodeModel code) {
		NavigableSet<InstructionHolder<?>> set = new TreeSet<>();

		int index = 0;
		for (CodeElement element : code) {
			if (element instanceof Instruction instruction) {
				set.add(new InstructionHolderImpl.RealImpl<>(index, instruction));
				index++;
			} else if (element instanceof PseudoInstruction instruction) {
				set.add(new InstructionHolderImpl.PseudoImpl<>(index, instruction));
				index++;
			}
		}

		return Collections.unmodifiableNavigableSet(set);
	}
}
