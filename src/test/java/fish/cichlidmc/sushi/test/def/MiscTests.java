package fish.cichlidmc.sushi.test.def;

import fish.cichlidmc.sushi.api.Sushi;
import org.junit.jupiter.api.Test;

public final class MiscTests {
	@Test
	public void checkModuleVersion() {
		Sushi.class.getModule().getDescriptor().version().orElseThrow();
	}
}
