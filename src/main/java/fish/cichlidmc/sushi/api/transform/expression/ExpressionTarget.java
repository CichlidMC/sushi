package fish.cichlidmc.sushi.api.transform.expression;

import fish.cichlidmc.sushi.api.model.code.Selection;
import fish.cichlidmc.sushi.api.model.code.TransformableCode;
import fish.cichlidmc.sushi.api.transform.Transform;
import fish.cichlidmc.sushi.api.transform.TransformException;
import fish.cichlidmc.sushi.api.util.SimpleRegistry;
import fish.cichlidmc.sushi.impl.SushiInternals;
import fish.cichlidmc.tinycodecs.Codec;
import fish.cichlidmc.tinycodecs.map.MapCodec;
import org.jetbrains.annotations.Nullable;

import java.lang.constant.ClassDesc;
import java.util.List;

/**
 * Defines an expression in a method body that can be targeted for modification.
 */
public interface ExpressionTarget {
	SimpleRegistry<MapCodec<? extends ExpressionTarget>> REGISTRY = SimpleRegistry.create(SushiInternals::bootstrapExpressionTargets);
	Codec<ExpressionTarget> CODEC = Codec.codecDispatch(REGISTRY.byIdCodec(), ExpressionTarget::codec);

	/**
	 * Find all expressions matching this target.
	 * @throws TransformException if something goes wrong while finding targets
	 */
	@Nullable
	Found find(TransformableCode code) throws TransformException;

	/**
	 * @return a human-readable, single-line description of this target.
	 * <p>
	 * Examples: {@code all invokes of com.example.MyClass.myMethod}, {@code read #3 of com.example.MyClass.myField}
	 * @see Transform#describe()
	 */
	String describe();

	MapCodec<? extends ExpressionTarget> codec();

	/**
	 * One or more selections that have been found that match the targeted expression.
	 * @param inputs an array of the types that are on the stack at the start of the selection and will be consumed during it, ex. method args
	 * @param output the type on the top of the stack once execution of the code within the selection completes, ex. method return
	 */
	record Found(List<Selection> selections, ClassDesc[] inputs, ClassDesc output) {
		public Found {
			if (selections.isEmpty()) {
				throw new IllegalArgumentException("Must contain at least one selection");
			}
		}

		public Found(Selection selection, ClassDesc[] inputs, ClassDesc output) {
			this(List.of(selection), inputs, output);
		}
	}
}
