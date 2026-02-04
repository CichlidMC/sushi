import fish.cichlidmc.sushi.test.framework.JunitExtension;
import org.junit.jupiter.api.extension.Extension;

// the test sourceSet must be a module for Gradle to treat the main sourceSet as a module.
// we want that so we can verify that the metadata is set correctly.
open module fish.cichlidmc.sushi.test {
	requires fish.cichlidmc.sushi;
	requires org.junit.jupiter.api;

	requires java.compiler;
	requires org.vineflower.vineflower;
	requires jdk.unsupported;

	exports fish.cichlidmc.sushi.test.infra;

	provides Extension with JunitExtension;
}
