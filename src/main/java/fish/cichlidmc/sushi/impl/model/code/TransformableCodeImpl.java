package fish.cichlidmc.sushi.impl.model.code;

import fish.cichlidmc.sushi.api.model.TransformableMethod;
import fish.cichlidmc.sushi.api.model.code.InstructionList;
import fish.cichlidmc.sushi.api.model.code.Selection;
import fish.cichlidmc.sushi.api.model.code.TransformableCode;
import fish.cichlidmc.sushi.impl.model.TransformableMethodImpl;
import fish.cichlidmc.sushi.impl.model.code.selection.SelectionBuilderImpl;
import fish.cichlidmc.sushi.impl.operation.Operations;
import fish.cichlidmc.sushi.impl.transform.TransformContextImpl;
import org.glavo.classfile.CodeElement;
import org.glavo.classfile.CodeModel;
import org.glavo.classfile.Instruction;
import org.glavo.classfile.Opcode;
import org.glavo.classfile.instruction.ArrayLoadInstruction;
import org.glavo.classfile.instruction.ArrayStoreInstruction;
import org.glavo.classfile.instruction.ConstantInstruction;
import org.glavo.classfile.instruction.ConvertInstruction;
import org.glavo.classfile.instruction.LoadInstruction;
import org.glavo.classfile.instruction.MonitorInstruction;
import org.glavo.classfile.instruction.NopInstruction;
import org.glavo.classfile.instruction.OperatorInstruction;
import org.glavo.classfile.instruction.ReturnInstruction;
import org.glavo.classfile.instruction.StackInstruction;
import org.glavo.classfile.instruction.StoreInstruction;
import org.glavo.classfile.instruction.ThrowInstruction;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class TransformableCodeImpl implements TransformableCode {
	public final Operations operations;

	private final SafeCodeModelImpl model;
	private final TransformableMethodImpl owner;
	private final InstructionListImpl instructions;
	private final SelectionBuilderImpl selectionBuilder;

	public TransformableCodeImpl(CodeModel model, TransformableMethodImpl owner) {
		// only call this once. this is the definitive list of elements.
		List<CodeElement> elements = getElements(model);
		this.model = new SafeCodeModelImpl(model, elements);
		this.owner = owner;
		this.instructions = InstructionListImpl.ofElements(elements);

		this.operations = new Operations(this.instructions);

		TransformContextImpl context = owner.owner().context;
		this.selectionBuilder = new SelectionBuilderImpl(context::transformerId, this.instructions, this.operations);
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

	private static List<CodeElement> getElements(CodeModel code) {
		List<CodeElement> list = new ArrayList<>();
		code.forEachElement(element -> list.add(makeUnique(element)));
		return Collections.unmodifiableList(list);
	}

	/**
	 * A handful of instructions are singletons for optimization, but that makes it impossible to track which is which.
	 * See {@code CodeImpl.SINGLETON_INSTRUCTIONS}.
	 */
	private static CodeElement makeUnique(CodeElement element) {
		if (!(element instanceof Instruction instruction))
			return element;

		Opcode opcode = instruction.opcode();
		return switch (instruction) {
			case ArrayLoadInstruction ignored -> ArrayLoadInstruction.of(opcode);
			case ArrayStoreInstruction ignored -> ArrayStoreInstruction.of(opcode);
			case ConstantInstruction constant when constant.opcode().constantValue() != null -> ConstantInstruction.ofIntrinsic(opcode);
			case ConvertInstruction ignored -> ConvertInstruction.of(opcode);
			case LoadInstruction load when load.slot() == opcode.slot() -> LoadInstruction.of(opcode, opcode.slot());
			case MonitorInstruction ignored -> MonitorInstruction.of(opcode);
			case NopInstruction ignored -> NopInstruction.of();
			case OperatorInstruction ignored -> OperatorInstruction.of(opcode);
			case ReturnInstruction ignored -> ReturnInstruction.of(opcode);
			case StackInstruction ignored -> StackInstruction.of(opcode);
			case StoreInstruction store when store.slot() == opcode.slot() -> StoreInstruction.of(opcode, opcode.slot());
			case ThrowInstruction ignored -> ThrowInstruction.of();
			default -> instruction;
		};
	}
}
