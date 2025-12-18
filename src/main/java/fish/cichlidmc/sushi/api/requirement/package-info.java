/// This package contains the requirement system, which is intended to
/// be used to provide validation for code adjacent to transformations.
///
/// For example, consider an injection that injects a call to `com.example.MyHooks.onDoThing()`.
/// For that injection to function, that method must obviously exist and have a matching signature.
/// However, Sushi cannot verify this as soon as the transformation is applied, since it may be applied ahead-of-time.
///
/// Instead, the requirements system collects this information so it can be put to use later,
/// potentially by a Gradle plugin, compiler plugin, IDE plugin, or similar.
package fish.cichlidmc.sushi.api.requirement;
