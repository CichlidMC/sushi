package fish.cichlidmc.sushi.impl;

import org.glavo.classfile.MethodBuilder;

import java.lang.constant.MethodTypeDesc;
import java.util.function.Consumer;

public record MethodEntry(String name, MethodTypeDesc desc, int flags, Consumer<MethodBuilder> consumer) {
}
