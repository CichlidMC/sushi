package fish.cichlidmc.sushi.api.requirement;

import fish.cichlidmc.sushi.api.Sushi;
import fish.cichlidmc.sushi.api.registry.SimpleRegistry;
import fish.cichlidmc.tinycodecs.api.codec.Codec;
import fish.cichlidmc.tinycodecs.api.codec.map.MapCodec;

import java.util.List;

/// A requirement indicates some state that Sushi expects to hold true for transformations to work as expected.
/// A requirement should be small in scope, defining exactly 1 thing. For complex sets of requirements, multiple
/// should be used, and possibly [chained][#chained()] together.
public interface Requirement {
	SimpleRegistry<MapCodec<? extends Requirement>> REGISTRY = SimpleRegistry.create(Sushi.NAMESPACE);
	Codec<Requirement> CODEC = Codec.codecDispatch(Requirement.REGISTRY.byIdCodec(), Requirement::codec);

	/// Codec for an optional list of chained requirements, used commonly by implementations.
	Codec<List<Requirement>> CHAINED_CODEC = CODEC.listOf().optional(List.of());
	
	/// @return a description of why this requirement was put in place
	String reason();

	/// Additional requirements that should only be checked if this requirement is met. May be empty.
	///
	/// All requirements in the list will be checked regardless of preceding requirements being met or not.
	///
	/// Most requirements are contextual, and will only be valid when chained after specific other requirements.
	/// It is the responsibility of whatever is checking requirements to ensure that chained requirements are
	/// sensical in the current context.
	/// @implNote it is expected that all implementations will support having arbitrary requirements chained after them
	List<Requirement> chained();

	MapCodec<? extends Requirement> codec();
}
