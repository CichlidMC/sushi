package fish.cichlidmc.sushi.api.model.code.element.pattern;

import fish.cichlidmc.sushi.api.model.code.element.PatternInstruction;
import fish.cichlidmc.sushi.api.util.Instructions;

import java.lang.classfile.instruction.InvokeInstruction;
import java.lang.classfile.instruction.NewObjectInstruction;
import java.lang.constant.ClassDesc;
import java.lang.constant.ConstantDescs;
import java.lang.constant.MethodTypeDesc;
import java.util.List;

public record NewObjectPatternInstruction(ClassDesc type, List<ClassDesc> constructorParams) implements PatternInstruction {
	public MethodTypeDesc constructorDesc() {
		return MethodTypeDesc.of(ConstantDescs.CD_void, this.constructorParams);
	}

	public static NewObjectPatternInstruction of(NewObjectInstruction newInstruction, InvokeInstruction initInstruction) {
		if (!Instructions.isConstructor(initInstruction)) {
			throw new IllegalArgumentException("Not an <init>: " + initInstruction);
		}

		ClassDesc type = newInstruction.className().asSymbol();
		List<ClassDesc> params = initInstruction.typeSymbol().parameterList();
		return new NewObjectPatternInstruction(type, params);
	}
}
