package fish.cichlidmc.sushi.test.def;

import fish.cichlidmc.sushi.api.Sushi;
import fish.cichlidmc.sushi.api.registry.Id;
import fish.cichlidmc.sushi.api.requirement.Requirement;
import fish.cichlidmc.sushi.api.requirement.Requirements;
import fish.cichlidmc.sushi.api.requirement.builtin.ClassRequirement;
import fish.cichlidmc.sushi.api.requirement.builtin.FieldRequirement;
import fish.cichlidmc.sushi.api.requirement.builtin.FlagsRequirement;
import fish.cichlidmc.sushi.api.requirement.builtin.FullyDefinedRequirement;
import fish.cichlidmc.sushi.api.requirement.builtin.MethodRequirement;
import fish.cichlidmc.sushi.api.requirement.interpreter.RequirementInterpreters;
import fish.cichlidmc.sushi.api.requirement.interpreter.exception.MalformedRequirementsException;
import fish.cichlidmc.sushi.api.requirement.interpreter.exception.MissingInterpreterException;
import fish.cichlidmc.sushi.api.requirement.interpreter.exception.UnmetRequirementException;
import fish.cichlidmc.sushi.api.util.ClassDescs;
import org.glavo.classfile.AccessFlag;
import org.junit.jupiter.api.Test;

import java.lang.constant.ClassDesc;
import java.lang.constant.ConstantDescs;
import java.lang.constant.MethodTypeDesc;
import java.lang.invoke.MethodHandles;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public final class RequirementTests {
	private static final RequirementInterpreters interpreters = RequirementInterpreters.forRuntime(MethodHandles.lookup());
	private static final Id owner = new Id("tests", "test");
	private static final ClassDesc desc = ClassDescs.of(RequirementTests.class);
	private static final String message = "test message";

	@Test
	public void empty() {
		Requirements requirements = Requirements.EMPTY;
		List<Requirements.Problem> problems = requirements.check(interpreters);
		assertEquals(List.of(), problems);
	}

	@Test
	public void classExists() {
		Requirements requirements = requirements(new ClassRequirement(message, desc));

		List<Requirements.Problem> problems = requirements.check(interpreters);
		assertEquals(List.of(), problems);
	}

	@Test
	public void classMissing() {
		Requirements requirements = requirements(
				new ClassRequirement(message, ClassDesc.of("com.example.MissingClass"))
		);

		List<Requirements.Problem> problems = requirements.check(interpreters);
		assertEquals(1, problems.size());

		Requirements.Problem problem = problems.getFirst();
		assertEquals(owner, problem.owner());
		assertEquals(1, problem.requirements().size());
		assertTrue(problem.exception() instanceof UnmetRequirementException);

		Requirement requirement = problem.requirements().getFirst();
		assertTrue(requirement instanceof ClassRequirement);
		assertEquals(message, requirement.reason());
	}

	@Test
	public void methodExists() {
		Requirements requirements = requirements(
				new ClassRequirement(message, desc, List.of(
						new MethodRequirement(message, "methodExists", MethodTypeDesc.of(ConstantDescs.CD_void))
				))
		);

		List<Requirements.Problem> problems = requirements.check(interpreters);
		assertEquals(List.of(), problems);
	}

	@Test
	public void methodMissing() {
		Requirements requirements = requirements(
				new ClassRequirement(message, desc, List.of(
						new MethodRequirement(message, "thisMethodDoesNotExist", MethodTypeDesc.of(ConstantDescs.CD_void))
				))
		);

		List<Requirements.Problem> problems = requirements.check(interpreters);
		assertEquals(1, problems.size());

		Requirements.Problem problem = problems.getFirst();
		assertTrue(problem.exception() instanceof UnmetRequirementException);
		assertEquals(2, problem.requirements().size());
		assertTrue(problem.requirements().getFirst() instanceof ClassRequirement);
		assertTrue(problem.requirements().getLast() instanceof MethodRequirement);
	}

	@Test
	public void missingContext() {
		Requirements requirements = requirements(
				new MethodRequirement(message, "thisMethodDoesNotExist", MethodTypeDesc.of(ConstantDescs.CD_void))
		);

		List<Requirements.Problem> problems = requirements.check(interpreters);
		assertEquals(1, problems.size());

		Requirements.Problem problem = problems.getFirst();
		assertTrue(problem.exception() instanceof MalformedRequirementsException);
		assertEquals(1, problem.requirements().size());
		assertTrue(problem.requirements().getFirst() instanceof MethodRequirement);
	}

	@Test
	public void missingInterpreter() {
		Requirements requirements = requirements(new FullyDefinedRequirement("a"));

		List<Requirements.Problem> problems = requirements.check(RequirementInterpreters.create());
		assertEquals(1, problems.size());

		Requirements.Problem problem = problems.getFirst();
		assertTrue(problem.exception() instanceof MissingInterpreterException);
		assertEquals(1, problem.requirements().size());
		assertTrue(problem.requirements().getFirst() instanceof FullyDefinedRequirement);
	}

	@Test
	public void weirdHierarchy() {
		// class -> flags -> method -> method
		Requirements requirements = requirements(new ClassRequirement(
				message, desc,
				FlagsRequirement.builder(message)
						.require(AccessFlag.PUBLIC)
						.require(AccessFlag.FINAL)
						.forbid(AccessFlag.PRIVATE)
						.chain(new MethodRequirement(
								message, "weirdHierarchy", MethodTypeDesc.of(ConstantDescs.CD_void),
								new MethodRequirement(message, "weirdHierarchy", MethodTypeDesc.of(ConstantDescs.CD_void))
						))
						.build()
		));

		List<Requirements.Problem> problems = requirements.check(interpreters);
		assertEquals(List.of(), problems);
	}

	@Test
	public void complex() {
		Requirements requirements = requirements(
				new ClassRequirement(
						message, desc,
						new MethodRequirement(
								message, "complex", MethodTypeDesc.of(ConstantDescs.CD_void),
								FlagsRequirement.builder(message)
										.require(AccessFlag.PUBLIC)
										.forbid(AccessFlag.STATIC)
										.build()
						),
						new FieldRequirement(
								message, "interpreters", ClassDescs.of(RequirementInterpreters.class),
								FlagsRequirement.builder(message)
										.require(AccessFlag.PRIVATE)
										.require(AccessFlag.STATIC)
										.require(AccessFlag.FINAL)
										.forbid(AccessFlag.PUBLIC)
										.chain(new ClassRequirement(
												// this one fails
												message, ClassDesc.of("com.example.MissingClass")
										))
										.build()
						)
				),
				new ClassRequirement(
						message, ClassDescs.of(Sushi.class),
						new MethodRequirement(
								message, "bootstrap", MethodTypeDesc.of(ConstantDescs.CD_void),
								FlagsRequirement.builder(message)
										.require(AccessFlag.PUBLIC)
										.require(AccessFlag.STATIC)
										.forbid(AccessFlag.FINAL)
										.build()
						)
				)
		);

		List<Requirements.Problem> problems = requirements.check(interpreters);
		assertEquals(1, problems.size());

		Requirements.Problem problem = problems.getFirst();
		assertTrue(problem.exception() instanceof UnmetRequirementException);

		// class -> field -> flags -> class
		assertEquals(4, problem.requirements().size());
		assertTrue(problem.requirements().get(0) instanceof ClassRequirement);
		assertTrue(problem.requirements().get(1) instanceof FieldRequirement);
		assertTrue(problem.requirements().get(2) instanceof FlagsRequirement);
		assertTrue(problem.requirements().get(3) instanceof ClassRequirement);
	}

	private static Requirements requirements(Requirement... requirements) {
		return Requirements.of(new Requirements.Owned(owner, List.of(requirements)));
	}
}
