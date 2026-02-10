package fish.cichlidmc.sushi.api.match.classes;

import fish.cichlidmc.sushi.api.Sushi;
import fish.cichlidmc.sushi.api.match.classes.builtin.AnyClassPredicate;
import fish.cichlidmc.sushi.api.match.classes.builtin.SingleClassPredicate;
import fish.cichlidmc.sushi.api.model.ClassFileAccess;
import fish.cichlidmc.sushi.api.registry.SimpleRegistry;
import fish.cichlidmc.sushi.impl.match.ClassPredicateContextImpl;
import fish.cichlidmc.tinycodecs.api.codec.Codec;
import fish.cichlidmc.tinycodecs.api.codec.map.MapCodec;

import java.lang.classfile.ClassFile;
import java.lang.classfile.ClassModel;
import java.lang.constant.ClassDesc;
import java.util.Optional;
import java.util.Set;

/// An arbitrary predicate for [ClassModel]s, determining if a transform should apply or not.
public interface ClassPredicate {
	SimpleRegistry<MapCodec<? extends ClassPredicate>> REGISTRY = SimpleRegistry.create(Sushi.NAMESPACE);
	Codec<ClassPredicate> CODEC = Codec.codecDispatch(REGISTRY.byIdCodec(), ClassPredicate::codec)
			.withAlternative(SingleClassPredicate.CODEC) // single class is an alternative to allow inlining a class name directly
			.withAlternative(AnyClassPredicate.CODEC); // and union is an alternative to allow inlining an array of targets

	/// @return true if this predicate matches the given context
	boolean test(Context context);

	/// If all possible matches are known up-front, then Sushi can optimize transformation.
	/// Return a set of all possible targets, if known, to opt in.
	///
	/// If a value is returned, then [#test] will never be queried for a class outside of that set.
	/// It will still be queried for ones inside it, to allow for finer-grained control based on the [ClassModel].
	Optional<Set<ClassDesc>> concreteMatches();

	MapCodec<? extends ClassPredicate> codec();

	/// Context available when a ClassPredicate is queried.
	///
	/// Note that class parsing is lazy, so calling [#desc()] or [#model()]
	/// for the first time may involve parsing the class from a `byte[]`.
	sealed interface Context permits ClassPredicateContextImpl {
		/// @return the descriptor of the class being tested
		ClassDesc desc();

		/// @return the model of the class being tested
		ClassModel model();

		/// @return the [ClassFile] used for parsing and transforming.
		ClassFileAccess classFile();
	}
}
