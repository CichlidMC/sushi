package fish.cichlidmc.sushi.impl.detail;

import fish.cichlidmc.sushi.api.detail.Detail;

import java.util.function.Supplier;

public record DetailProviderImpl(Supplier<Object> supplier) implements Detail.Provider {
	@Override
	public String toString() {
		return this.supplier.get().toString();
	}
}
