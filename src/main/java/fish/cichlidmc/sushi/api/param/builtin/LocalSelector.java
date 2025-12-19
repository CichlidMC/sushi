package fish.cichlidmc.sushi.api.param.builtin;

import fish.cichlidmc.sushi.api.codec.SushiCodecs;
import fish.cichlidmc.sushi.api.model.code.Point;
import fish.cichlidmc.sushi.api.model.code.TransformableCode;
import fish.cichlidmc.sushi.api.model.code.element.LocalVariables;
import fish.cichlidmc.sushi.api.transformer.TransformException;
import fish.cichlidmc.tinycodecs.api.codec.Codec;
import fish.cichlidmc.tinycodecs.api.codec.map.MapCodec;

import java.util.Locale;
import java.util.Map;

/// Determines the slot of a desired local variable. Used by [LocalContextParameter].
public sealed interface LocalSelector {
	Codec<LocalSelector> CODEC = Type.CODEC.dispatch(LocalSelector::type, type -> type.codec);

	int determineSlot(TransformableCode code, Point point) throws TransformException;

	Type type();

	record Slot(int slot) implements LocalSelector {
		public static final MapCodec<Slot> CODEC = SushiCodecs.NON_NEGATIVE_INT.fieldOf("slot").xmap(Slot::new, Slot::slot);

		@Override
		public int determineSlot(TransformableCode code, Point point) {
			return this.slot;
		}

		@Override
		public Type type() {
			return Type.SLOT;
		}
	}

	record ByName(String name) implements LocalSelector {
		public static final MapCodec<ByName> CODEC = Codec.STRING.fieldOf("name").xmap(ByName::new, ByName::name);

		@Override
		public int determineSlot(TransformableCode code, Point point) {
			LocalVariables locals = code.locals().orElseThrow(
					() -> new TransformException("LVT required for name lookup")
			);

			Map<Integer, LocalVariables.Entry> map = locals.findInScope(point);
			map.values().removeIf(entry -> !entry.value().get().name().equalsString(this.name));

			if (map.isEmpty()) {
				throw new TransformException("No local variable found with name " + this.name);
			} else if (map.size() != 1) {
				throw new TransformException("Multiple local variables with name " + this.name);
			} else {
				return map.keySet().iterator().next();
			}
		}

		@Override
		public Type type() {
			return Type.BY_NAME;
		}
	}

	enum Type {
		SLOT(Slot.CODEC),
		BY_NAME(ByName.CODEC);

		public static final Codec<Type> CODEC = Codec.byName(Type.class, type -> type.name().toLowerCase(Locale.ROOT));

		public final MapCodec<? extends LocalSelector> codec;

		Type(MapCodec<? extends LocalSelector> codec) {
			this.codec = codec;
		}
	}
}
