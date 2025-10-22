package fish.cichlidmc.sushi.test.framework;

import fish.cichlidmc.sushi.api.Sushi;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

public final class JunitExtension implements BeforeAllCallback {
	@Override
	public void beforeAll(ExtensionContext context) {
		Sushi.bootstrap();
	}
}
