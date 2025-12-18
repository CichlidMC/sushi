open module fish.cichlidmc.sushi {
	requires static transitive org.jetbrains.annotations;
	requires static transitive org.jspecify;
	requires transitive fish.cichlidmc.tinycodecs;
	requires transitive fish.cichlidmc.tinyjson;
	requires transitive fish.cichlidmc.fishflakes;
	requires transitive org.glavo.classfile;

	exports fish.cichlidmc.sushi.api;

	exports fish.cichlidmc.sushi.api.attach;
	exports fish.cichlidmc.sushi.api.codec;

	exports fish.cichlidmc.sushi.api.condition;
	exports fish.cichlidmc.sushi.api.condition.builtin;

	exports fish.cichlidmc.sushi.api.metadata;

	exports fish.cichlidmc.sushi.api.model;
	exports fish.cichlidmc.sushi.api.model.code;

	exports fish.cichlidmc.sushi.api.param;
	exports fish.cichlidmc.sushi.api.param.builtin;
	exports fish.cichlidmc.sushi.api.param.builtin.local;

	exports fish.cichlidmc.sushi.api.ref;

	exports fish.cichlidmc.sushi.api.registry;
	exports fish.cichlidmc.sushi.api.registry.content;

	exports fish.cichlidmc.sushi.api.requirement;
	exports fish.cichlidmc.sushi.api.requirement.builtin;
	exports fish.cichlidmc.sushi.api.requirement.interpreter;
	exports fish.cichlidmc.sushi.api.requirement.interpreter.exception;

	exports fish.cichlidmc.sushi.api.target;
	exports fish.cichlidmc.sushi.api.target.builtin;
	exports fish.cichlidmc.sushi.api.target.expression;
	exports fish.cichlidmc.sushi.api.target.expression.builtin;
	exports fish.cichlidmc.sushi.api.target.inject;
	exports fish.cichlidmc.sushi.api.target.inject.builtin;

	exports fish.cichlidmc.sushi.api.transformer;
	exports fish.cichlidmc.sushi.api.transformer.base;
	exports fish.cichlidmc.sushi.api.transformer.builtin;
	exports fish.cichlidmc.sushi.api.transformer.builtin.access;
	exports fish.cichlidmc.sushi.api.transformer.infra;
	exports fish.cichlidmc.sushi.api.transformer.phase;

	exports fish.cichlidmc.sushi.api.util;

	// references to classes in these packages are injected, they need to be accessible
	exports fish.cichlidmc.sushi.impl.operation.runtime;
	exports fish.cichlidmc.sushi.impl.ref.runtime;
}
