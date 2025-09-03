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

import java.lang.constant.MethodTypeDesc;
import java.util.List;

/**
 * Defines an expression in a method body that can be targeted for modification.
 * <p>
 * It is required that all expression targets result in a net decrease in stack size after execution.
 * For example, consider an expression targeting a method invocation of {@code double x = this.getX(int, boolean)}.
 * Before invocation, the top of the stack holds {@code this}, an {@code int}, and a {@code boolean}. After invocation,
 * the stack only holds a {@code double}.
 * @see Found#desc()
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

	MapCodec<? extends ExpressionTarget> codec();

	/**
	 * One or more selections that have been found that match the targeted expression.
	 * @param desc a descriptor defining the "inputs" and "outputs" of the found expressions, i.e. the list of types
	 *             on the top of the stack pre-expression which are consumed by it, and the type on the top post-expression.
	 *             For a static method invocation, this would just be its descriptor.
	 */
	record Found(List<Selection> selections, MethodTypeDesc desc) {
		public Found {
			if (selections.isEmpty()) {
				throw new IllegalArgumentException("Must contain at least one selection");
			}
		}

		public Found(Selection selection, MethodTypeDesc desc) {
			this(List.of(selection), desc);
		}
	}
}
