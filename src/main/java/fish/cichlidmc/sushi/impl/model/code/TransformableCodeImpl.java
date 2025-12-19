package fish.cichlidmc.sushi.impl.model.code;

import fish.cichlidmc.sushi.api.attach.AttachmentMap;
import fish.cichlidmc.sushi.api.model.TransformableMethod;
import fish.cichlidmc.sushi.api.model.code.InstructionList;
import fish.cichlidmc.sushi.api.model.code.Selection;
import fish.cichlidmc.sushi.api.model.code.TransformableCode;
import fish.cichlidmc.sushi.impl.model.TransformableMethodImpl;
import fish.cichlidmc.sushi.impl.model.code.selection.SelectionBuilderImpl;
import fish.cichlidmc.sushi.impl.operation.Operations;
import fish.cichlidmc.sushi.impl.transformer.TransformContextImpl;

import java.lang.classfile.CodeElement;
import java.lang.classfile.CodeModel;
import java.lang.classfile.Instruction;
import java.lang.classfile.Opcode;
import java.lang.classfile.instruction.ArrayLoadInstruction;
import java.lang.classfile.instruction.ArrayStoreInstruction;
import java.lang.classfile.instruction.ConstantInstruction;
import java.lang.classfile.instruction.ConstantInstruction.IntrinsicConstantInstruction;
import java.lang.classfile.instruction.ConvertInstruction;
import java.lang.classfile.instruction.LoadInstruction;
import java.lang.classfile.instruction.MonitorInstruction;
import java.lang.classfile.instruction.NopInstruction;
import java.lang.classfile.instruction.OperatorInstruction;
import java.lang.classfile.instruction.ReturnInstruction;
import java.lang.classfile.instruction.StackInstruction;
import java.lang.classfile.instruction.StoreInstruction;
import java.lang.classfile.instruction.ThrowInstruction;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class TransformableCodeImpl implements TransformableCode {
	public final Operations operations;

	private final SafeCodeModelImpl model;
	private final TransformableMethodImpl owner;
	private final InstructionListImpl instructions;
	private final SelectionBuilderImpl selectionBuilder;
	private final AttachmentMap attachments;

	public TransformableCodeImpl(CodeModel model, TransformableMethodImpl owner) {
		// only call this once. this is the definitive list of elements.
		List<CodeElement> elements = getElements(model);
		this.model = new SafeCodeModelImpl(model, elements);
		this.owner = owner;
		this.instructions = InstructionListImpl.ofElements(elements);

		this.operations = new Operations(this.instructions);

		TransformContextImpl context = owner.owner().context;
		this.selectionBuilder = new SelectionBuilderImpl(context::transformerId, this.instructions, this.operations);

		this.attachments = AttachmentMap.create();
	}

	@Override
	public SafeCodeModelImpl model() {
		return this.model;
	}

	@Override
	public TransformableMethod owner() {
		return this.owner;
	}

	@Override
	public InstructionList instructions() {
		return this.instructions;
	}

	@Override
	public Selection.Builder select() {
		return this.selectionBuilder;
	}

	@Override
	public AttachmentMap attachments() {
		return this.attachments;
	}

	private static List<CodeElement> getElements(CodeModel code) {
		List<CodeElement> list = new ArrayList<>();
		code.forEach(element -> list.add(makeUnique(element)));
		return Collections.unmodifiableList(list);
	}

	/// A handful of instructions are singletons for optimization, but that makes it impossible to track which is which.
	/// See `CodeImpl.SINGLETON_INSTRUCTIONS`.
	private static CodeElement makeUnique(CodeElement element) {
		if (!(element instanceof Instruction instruction))
			return element;

		Opcode opcode = instruction.opcode();
		return switch (instruction) {
			case ArrayLoadInstruction _ -> ArrayLoadInstruction.of(opcode);
			case ArrayStoreInstruction _ -> ArrayStoreInstruction.of(opcode);
			case IntrinsicConstantInstruction _ -> ConstantInstruction.ofIntrinsic(opcode);
			case ConvertInstruction _ -> ConvertInstruction.of(opcode);
			case LoadInstruction load when load.slot() == getSlot(opcode) -> LoadInstruction.of(opcode, load.slot());
			case MonitorInstruction _ -> MonitorInstruction.of(opcode);
			case NopInstruction _ -> NopInstruction.of();
			case OperatorInstruction _ -> OperatorInstruction.of(opcode);
			case ReturnInstruction _ -> ReturnInstruction.of(opcode);
			case StackInstruction _ -> StackInstruction.of(opcode);
			case StoreInstruction store when store.slot() == getSlot(opcode) -> StoreInstruction.of(opcode, store.slot());
			case ThrowInstruction _ -> ThrowInstruction.of();
			default -> instruction;
		};
	}

	private static int getSlot(Opcode opcode) {
		return switch (opcode) {
			case ILOAD_0, LLOAD_0, FLOAD_0, DLOAD_0, ALOAD_0, ISTORE_0, LSTORE_0, FSTORE_0, DSTORE_0, ASTORE_0 -> 0;
			case ILOAD_1, LLOAD_1, FLOAD_1, DLOAD_1, ALOAD_1, ISTORE_1, LSTORE_1, FSTORE_1, DSTORE_1, ASTORE_1 -> 1;
			case ILOAD_2, LLOAD_2, FLOAD_2, DLOAD_2, ALOAD_2, ISTORE_2, LSTORE_2, FSTORE_2, DSTORE_2, ASTORE_2 -> 2;
			case ILOAD_3, LLOAD_3, FLOAD_3, DLOAD_3, ALOAD_3, ISTORE_3, LSTORE_3, FSTORE_3, DSTORE_3, ASTORE_3 -> 3;
			default -> -1;
		};
	}
}
