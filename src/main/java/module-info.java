open module fish.cichlidmc.sushi {
	requires transitive org.glavo.classfile;
	requires static transitive org.jetbrains.annotations;
	requires transitive fish.cichlidmc.tinycodecs;
	requires transitive fish.cichlidmc.tinyjson;
	requires java.naming;

	exports fish.cichlidmc.sushi.api;

	exports fish.cichlidmc.sushi.api.attach;

	exports fish.cichlidmc.sushi.api.codec;
	exports fish.cichlidmc.sushi.api.metadata;

	exports fish.cichlidmc.sushi.api.model;
	exports fish.cichlidmc.sushi.api.model.code;

	exports fish.cichlidmc.sushi.api.param;
	exports fish.cichlidmc.sushi.api.param.builtin.local;

	exports fish.cichlidmc.sushi.api.ref;
	exports fish.cichlidmc.sushi.api.registry;

	exports fish.cichlidmc.sushi.api.target;
	exports fish.cichlidmc.sushi.api.target.builtin;
	exports fish.cichlidmc.sushi.api.target.expression;
	exports fish.cichlidmc.sushi.api.target.expression.builtin;
	exports fish.cichlidmc.sushi.api.target.inject;
	exports fish.cichlidmc.sushi.api.target.inject.builtin;

	exports fish.cichlidmc.sushi.api.transform;
	exports fish.cichlidmc.sushi.api.transform.base;
	exports fish.cichlidmc.sushi.api.transform.builtin;
	exports fish.cichlidmc.sushi.api.transform.builtin.access;
	exports fish.cichlidmc.sushi.api.transform.infra;

	exports fish.cichlidmc.sushi.api.util;

	exports fish.cichlidmc.sushi.api.validation;

	// references to classes in these packages are injected, they need to be accessible
	exports fish.cichlidmc.sushi.impl.operation.runtime;
	exports fish.cichlidmc.sushi.impl.ref.runtime;
}
