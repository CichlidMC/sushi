package fish.cichlidmc.sushi.api.model.code.element;

import fish.cichlidmc.sushi.api.model.code.element.pattern.NewObjectPatternInstruction;

/// An abstraction over a pattern in bytecode that is much easier to work with.
public sealed interface PatternInstruction permits NewObjectPatternInstruction {
}
