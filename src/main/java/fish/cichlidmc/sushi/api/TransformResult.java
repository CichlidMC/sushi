package fish.cichlidmc.sushi.api;

import fish.cichlidmc.sushi.api.requirement.Requirements;

/// The result of transforming a class.
/// @param bytes the new bytecode of the class
/// @param requirements the set of external requirements that must be met for the transformation to be correct
public record TransformResult(byte[] bytes, Requirements requirements) {
}
