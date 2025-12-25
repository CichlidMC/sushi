package fish.cichlidmc.sushi.api.model.key;

import fish.cichlidmc.sushi.api.util.ClassDescs;

import java.lang.classfile.MethodModel;
import java.lang.constant.MethodTypeDesc;
import java.util.StringJoiner;

/// Uniquely identifies a method in a class.
public record MethodKey(String name, MethodTypeDesc desc) {
	@Override
	public String toString() {
		StringJoiner joiner = new StringJoiner(
				", ",
				this.name + '(',
				")" + ClassDescs.fullName(this.desc.returnType())
		);

		this.desc.parameterList().forEach(param -> joiner.add(ClassDescs.fullName(param)));
		return joiner.toString();
	}

	public static MethodKey of(MethodModel method) {
		return new MethodKey(method.methodName().stringValue(), method.methodTypeSymbol());
	}
}
