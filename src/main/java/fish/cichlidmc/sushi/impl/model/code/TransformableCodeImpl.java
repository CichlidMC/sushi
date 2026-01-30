package fish.cichlidmc.sushi.impl.model.code;

import fish.cichlidmc.sushi.api.attach.AttachmentMap;
import fish.cichlidmc.sushi.api.model.TransformableMethod;
import fish.cichlidmc.sushi.api.model.code.TransformableCode;
import fish.cichlidmc.sushi.api.model.code.element.InstructionHolder;
import fish.cichlidmc.sushi.api.model.code.element.LabelLookup;
import fish.cichlidmc.sushi.api.model.code.element.LocalVariables;
import fish.cichlidmc.sushi.api.registry.Id;
import fish.cichlidmc.sushi.api.transformer.DirectTransform;
import fish.cichlidmc.sushi.impl.model.TransformableMethodImpl;
import fish.cichlidmc.sushi.impl.model.code.element.InstructionHolderImpl;
import fish.cichlidmc.sushi.impl.model.code.element.LabelLookupImpl;
import fish.cichlidmc.sushi.impl.model.code.element.LocalVariablesImpl;
import fish.cichlidmc.sushi.impl.model.code.selection.SelectionBuilderImpl;
import fish.cichlidmc.sushi.impl.operation.Operations;
import fish.cichlidmc.sushi.impl.transformer.DirectTransformContextImpl;
import fish.cichlidmc.sushi.impl.transformer.PreparedDirectTransform;
import fish.cichlidmc.sushi.impl.transformer.TransformContextImpl;
import fish.cichlidmc.sushi.impl.util.IdentifiedTransform;

import java.lang.classfile.Attributes;
import java.lang.classfile.ClassBuilder;
import java.lang.classfile.CodeElement;
import java.lang.classfile.CodeModel;
import java.lang.classfile.CodeTransform;
import java.lang.classfile.Instruction;
import java.lang.classfile.MethodBuilder;
import java.lang.classfile.MethodTransform;
import java.lang.classfile.PseudoInstruction;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.NavigableSet;
import java.util.Optional;
import java.util.TreeSet;

public final class TransformableCodeImpl implements TransformableCode {
	public final Operations operations;

	private final CodeModel model;
	private final TransformableMethodImpl owner;
	private final NavigableSet<InstructionHolder<?>> instructions;
	private final LabelLookup labels;
	private final Optional<LocalVariables> locals;
	private final SelectionBuilderImpl selectionBuilder;
	private final AttachmentMap attachments;
	private final List<PreparedDirectTransform<DirectTransform.Code>> directTransforms;

	public TransformableCodeImpl(CodeModel model, TransformableMethodImpl owner) {
		this.model = model;
		this.owner = owner;
		this.instructions = this.getInstructions(model);
		this.labels = LabelLookupImpl.create(this.instructions);
		this.locals = model.findAttribute(Attributes.localVariableTable()).map(
				_ -> LocalVariablesImpl.create(this.instructions, this.labels)
		);
		this.operations = new Operations();

		this.selectionBuilder = new SelectionBuilderImpl(this.instructions, this.operations);

		this.attachments = AttachmentMap.create();
		this.directTransforms = new ArrayList<>();
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
	public LabelLookup labels() {
		return this.labels;
	}

	@Override
	public Optional<LocalVariables> locals() {
		return this.locals;
	}

	@Override
	public SelectionBuilderImpl select() {
		this.owner.owner().checkFrozen();
		return this.selectionBuilder;
	}

	@Override
	public AttachmentMap attachments() {
		return this.attachments;
	}

	@Override
	public void transformDirect(DirectTransform.Code transform) {
		this.owner.owner().checkFrozen();
		TransformContextImpl context = TransformContextImpl.current();
		this.directTransforms.add(new PreparedDirectTransform<>(transform, context));
	}

	public Optional<MethodTransform> toTransform(ClassBuilder classBuilder) {
		Optional<MethodTransform> applicator = this.operations.applicator(this, classBuilder).map(MethodTransform::transformingCode);
		if (this.directTransforms.isEmpty())
			return applicator;

		MethodTransform direct = (methodBuilder, element) -> {
			if (element instanceof CodeModel code) {
				CodeTransform transform = this.createCodeTransform(classBuilder, methodBuilder);
				methodBuilder.transformCode(code, transform);
			} else {
				methodBuilder.with(element);
			}
		};

		return applicator.isEmpty() ? Optional.of(direct) : applicator.map(app -> app.andThen(direct));
	}

	private CodeTransform createCodeTransform(ClassBuilder classBuilder, MethodBuilder methodBuilder) {
		if (this.directTransforms.isEmpty()) {
			throw new IllegalStateException("No direct transforms to apply");
		}

		CodeTransform transform = null;

		for (PreparedDirectTransform<DirectTransform.Code> prepared : this.directTransforms) {
			DirectTransform.Context.Code context = new DirectTransformContextImpl.CodeImpl(
					prepared.context(), classBuilder, this.owner, methodBuilder, this
			);
			Id owner = prepared.context().transformerId();
			CodeTransform direct = prepared.transform().create(context);
			CodeTransform identified = IdentifiedTransform.ofCode(owner, direct);
			transform = transform == null ? identified : transform.andThen(identified);
		}

		return transform;
	}

	private NavigableSet<InstructionHolder<?>> getInstructions(CodeModel code) {
		NavigableSet<InstructionHolder<?>> set = new TreeSet<>();

		int index = 0;
		for (CodeElement element : code) {
			if (element instanceof Instruction instruction) {
				set.add(new InstructionHolderImpl.RealImpl<>(this, index, instruction));
				index++;
			} else if (element instanceof PseudoInstruction instruction) {
				set.add(new InstructionHolderImpl.PseudoImpl<>(this, index, instruction));
				index++;
			}
		}

		return Collections.unmodifiableNavigableSet(set);
	}
}
