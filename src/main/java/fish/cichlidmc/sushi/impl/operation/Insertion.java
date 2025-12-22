package fish.cichlidmc.sushi.impl.operation;

import fish.cichlidmc.sushi.api.model.code.CodeBlock;
import fish.cichlidmc.sushi.api.model.code.Point;
import fish.cichlidmc.sushi.api.model.code.Selection.Timing;
import fish.cichlidmc.sushi.api.registry.Id;

public record Insertion(Point point, CodeBlock code, Id owner, Timing timing) {
}
