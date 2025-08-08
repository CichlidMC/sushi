package fish.cichlidmc.sushi.impl.operation.apply;

import fish.cichlidmc.sushi.impl.apply.MethodGenerator;
import fish.cichlidmc.sushi.impl.operation.Extraction;
import org.glavo.classfile.CodeBuilder;
import org.glavo.classfile.CodeElement;
import org.glavo.classfile.instruction.LoadInstruction;
import org.glavo.classfile.instruction.StoreInstruction;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Manages an extraction as instructions are iterated.
 */
public final class Extractor {
	public final Extraction extraction;

	private final List<CodeElement> elements;

	public Extractor(Extraction extraction) {
		this.extraction = extraction;
		this.elements = new ArrayList<>();
	}

	public void intercept(CodeElement element) {
		this.elements.add(element);
	}

	private Map<Integer, LocalInfo> collectInfo() {
		Map<Integer, LocalInfo> map = new HashMap<>();

		for (CodeElement element : this.elements) {
			if (element instanceof LoadInstruction load) {
				map.computeIfAbsent(load.slot(), LocalInfo::new).read = true;
			} else if (element instanceof StoreInstruction store) {
				map.computeIfAbsent(store.slot(), LocalInfo::new).written = true;
			}
		}

		return map;
	}

	public void finish(CodeBuilder builder, MethodGenerator methodGenerator) {
		// code that has been extracted could reference locals.
		// need to find each index, check if it was read, written, both, or neither
		// generate the proper LocalRef infrastructure
	}

	private static final class LocalInfo {
		private boolean read;
		private boolean written;

		private LocalInfo(int ignoredSlot) {
		}
	}
}
