open module fish.cichlidmc.sushi {
	requires transitive org.glavo.classfile;
	requires static transitive org.jetbrains.annotations;
	requires transitive fish.cichlidmc.tinycodecs;
	requires transitive fish.cichlidmc.tinyjson;

	exports fish.cichlidmc.sushi.api;

	exports fish.cichlidmc.sushi.api.model;
	exports fish.cichlidmc.sushi.api.model.code;

	exports fish.cichlidmc.sushi.api.target;

	exports fish.cichlidmc.sushi.api.transform;
	exports fish.cichlidmc.sushi.api.transform.expression;
	exports fish.cichlidmc.sushi.api.transform.inject;
	exports fish.cichlidmc.sushi.api.transform.wrap_op;

	exports fish.cichlidmc.sushi.api.util;
	exports fish.cichlidmc.sushi.api.util.method;

	exports fish.cichlidmc.sushi.api.validation;

	exports fish.cichlidmc.sushi.impl.runtime;
}
