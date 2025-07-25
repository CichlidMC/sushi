package fish.cichlidmc.sushi.impl.transform.sliced;

import fish.cichlidmc.sushi.api.model.code.InstructionList;
import fish.cichlidmc.sushi.api.model.code.Point;
import fish.cichlidmc.sushi.api.model.code.TransformableCode;
import fish.cichlidmc.sushi.api.transform.Transform;
import fish.cichlidmc.sushi.api.transform.TransformContext;
import fish.cichlidmc.sushi.api.transform.TransformException;
import fish.cichlidmc.sushi.api.transform.TransformType;
import fish.cichlidmc.sushi.api.transform.inject.InjectionPoint;
import fish.cichlidmc.sushi.impl.transform.point.HeadInjectionPoint;
import fish.cichlidmc.sushi.impl.transform.point.TailInjectionPoint;
import fish.cichlidmc.sushi.impl.transform.sliced.model.SlicedTransformableClass;
import fish.cichlidmc.sushi.impl.transform.sliced.model.SlicedTransformableCode;
import fish.cichlidmc.tinycodecs.codec.map.CompositeCodec;
import fish.cichlidmc.tinycodecs.map.MapCodec;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public record SlicedTransform(InjectionPoint from, InjectionPoint to, List<Transform> wrapped) implements Transform {
	public static final MapCodec<SlicedTransform> CODEC = MapCodec.lazy(() -> CompositeCodec.of(
			InjectionPoint.CODEC.optional(HeadInjectionPoint.INSTANCE).fieldOf("from"), SlicedTransform::from,
			InjectionPoint.CODEC.optional(TailInjectionPoint.INSTANCE).fieldOf("to"), SlicedTransform::to,
			Transform.CODEC.listOrSingle().fieldOf("wrapped"), SlicedTransform::wrapped,
			SlicedTransform::new
	));

	public static final TransformType TYPE = new TransformType(CODEC);

	@Override
	public void apply(TransformContext context) throws TransformException {
		SlicedTransformableClass classWrapper = new SlicedTransformableClass(context.clazz(), this::slice);
		TransformContext contextWrapper = new SlicedTransformContext(context, classWrapper);

		for (Transform wrapped : this.wrapped) {
			wrapped.apply(contextWrapper);
		}
	}

	@Override
	public String describe() {
		String wrapped = this.wrapped.stream().map(Transform::describe).collect(Collectors.joining(" - "));
		return "sliced: [" + this.from.describe() + "] -> [" + this.to.describe() + "] - " + wrapped;
	}

	@Override
	public TransformType type() {
		return TYPE;
	}

	private SlicedTransformableCode slice(TransformableCode code) throws TransformException {
		InstructionList instructions = code.instructions();

		Collection<Point> from = this.from.find(code);
		if (from.size() != 1) {
			throw new TransformException("Slice start must match exactly 1 point, got " + from.size());
		}

		Collection<Point> to = this.to.find(code);
		if (to.size() != 1) {
			throw new TransformException("Slice end must match exactly 1 point, got " + to.size());
		}

		InstructionList subList = instructions.subList(from.iterator().next(), to.iterator().next());
		return new SlicedTransformableCode(code, subList);
	}
}
