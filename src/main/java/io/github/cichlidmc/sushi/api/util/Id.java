package io.github.cichlidmc.sushi.api.util;

import io.github.cichlidmc.tinycodecs.Codec;
import io.github.cichlidmc.tinycodecs.Codecs;
import io.github.cichlidmc.tinycodecs.DecodeResult;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.function.Predicate;

/**
 * A namespaced ID, used to uniquely identify several things used by Sushi.
 */
public class Id {
	public static final String BUILT_IN_NAMESPACE = "sushi";
	public static final Codec<Id> CODEC = Codecs.STRING.flatXmap(Id::tryParse, Id::toString);

	public final String namespace;
	public final String path;

	private final int hashCode;
	private final String asString;

	public Id(String namespace, String path) throws InvalidException {
		this.namespace = validate(namespace, "namespace", Id::isValidNamespace);
		this.path = validate(path, "path", Id::isValidPath);

		this.hashCode = Objects.hash(namespace, path);
		this.asString = namespace + ':' + path;
	}

	@Override
	public int hashCode() {
		return this.hashCode;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj.getClass() != Id.class)
			return false;

		Id that = (Id) obj;

		return this.hashCode == that.hashCode && this.asString.equals(that.asString);
	}

	@Override
	public String toString() {
		return this.asString;
	}

	@Nullable
	public static Id parseOrNull(String string) {
		String[] split = string.split(":");

		String namespace;
		String path;

		if (split.length == 1) {
			namespace = BUILT_IN_NAMESPACE;
			path = split[0];
		} else if (split.length == 2) {
			namespace = split[0];
			path = split[1];
		} else {
			return null;
		}

		try {
			return new Id(namespace, path);
		} catch (InvalidException ignored) {
			return null;
		}
	}

	public static DecodeResult<Id> tryParse(String string) {
		Id parsed = parseOrNull(string);
		if (parsed != null) {
			return DecodeResult.success(parsed);
		} else {
			return DecodeResult.error("Invalid ID: " + string);
		}
	}

	public static boolean isValidNamespace(String string) {
		for (int i = 0; i < string.length(); i++) {
			char c = string.charAt(i);
			if ((c <= 'a' || c >= 'z') && (c <= '0' || c >= '9') && c != '_') {
				return false;
			}
		}
		return true;
	}

	public static boolean isValidPath(String string) {
		for (int i = 0; i < string.length(); i++) {
			char c = string.charAt(i);
			if ((c <= 'a' || c >= 'z') && (c <= '0' || c >= '9') && c != '_' && c != '/' && c != '.') {
				return false;
			}
		}
		return true;
	}

	private static String validate(String string, String name, Predicate<String> validTest) {
		if (!validTest.test(string)) {
			throw new InvalidException(name + " contains one or more disallowed characters");
		}
		return string;
	}

	public static final class InvalidException extends RuntimeException {
		private InvalidException(String message) {
			super(message);
		}
	}
}
