package fish.cichlidmc.sushi.impl.transformer;

import fish.cichlidmc.sushi.api.transformer.base.HookingTransformer;

import java.lang.constant.ClassDesc;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiConsumer;

public record CoercionsImpl(Map<ClassDesc, ClassDesc> map) implements HookingTransformer.Hook.Coercions {
	public CoercionsImpl(Map<ClassDesc, ClassDesc> map) {
		this.map = Map.copyOf(map);
	}

	@Override
	public Optional<ClassDesc> get(ClassDesc type) {
		return Optional.ofNullable(this.map.get(type));
	}

	@Override
	public ClassDesc coerce(ClassDesc type) {
		return this.get(type).orElse(type);
	}

	@Override
	public boolean isEmpty() {
		return this.map.isEmpty();
	}

	@Override
	public void forEach(BiConsumer<ClassDesc, ClassDesc> consumer) {
		this.map.forEach(consumer);
	}

	public static CoercionsImpl ofTrusted(Map<ClassDesc, ClassDesc> map) {
		return new CoercionsImpl(map);
	}

	public static CoercionsImpl of(Map<ClassDesc, ClassDesc> map) {
		return new CoercionsImpl(Map.copyOf(map));
	}
}
