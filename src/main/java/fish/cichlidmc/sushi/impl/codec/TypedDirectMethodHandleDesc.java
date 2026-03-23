package fish.cichlidmc.sushi.impl.codec;

import fish.cichlidmc.sushi.api.codec.SushiCodecs;
import fish.cichlidmc.sushi.api.util.ClassDescs;
import fish.cichlidmc.tinycodecs.api.codec.Codec;
import fish.cichlidmc.tinycodecs.api.codec.CompositeCodec;
import fish.cichlidmc.tinycodecs.api.codec.dual.DualCodec;
import fish.cichlidmc.tinycodecs.api.codec.map.MapCodec;

import java.lang.constant.ClassDesc;
import java.lang.constant.DirectMethodHandleDesc;
import java.lang.constant.MethodHandleDesc;
import java.lang.constant.MethodTypeDesc;
import java.util.List;
import java.util.Locale;

/// An alternate representation of a [DirectMethodHandleDesc] that makes serialization easier.
public abstract sealed class TypedDirectMethodHandleDesc {
	public static final Codec<TypedDirectMethodHandleDesc> CODEC = Type.CODEC.dispatch(TypedDirectMethodHandleDesc::type, type -> type.codec);

	public final ClassDesc owner;

	protected TypedDirectMethodHandleDesc(ClassDesc owner) {
		this.owner = owner;
	}

	protected abstract Type type();

	public abstract DirectMethodHandleDesc unwrap();

	public static TypedDirectMethodHandleDesc wrap(DirectMethodHandleDesc handle) {
		return switch (handle.kind()) {
			case STATIC -> method(handle, Method.Kind.STATIC, false);
			case INTERFACE_STATIC -> method(handle, Method.Kind.STATIC, true);
			case VIRTUAL -> method(handle, Method.Kind.VIRTUAL, false);
			case INTERFACE_VIRTUAL -> method(handle, Method.Kind.VIRTUAL, true);
			case SPECIAL -> method(handle, Method.Kind.SPECIAL, false);
			case INTERFACE_SPECIAL -> method(handle, Method.Kind.SPECIAL, true);

			case CONSTRUCTOR -> new Constructor(handle.owner(), handle.invocationType().parameterList());

			case GETTER -> field(handle, Field.Operation.GET, false);
			case SETTER -> field(handle, Field.Operation.SET, false);
			case STATIC_GETTER -> field(handle, Field.Operation.GET, true);
			case STATIC_SETTER -> field(handle, Field.Operation.SET, true);
		};
	}

	private static Method method(DirectMethodHandleDesc handle, Method.Kind kind, boolean ownerIsInterface) {
		// drop the implicit self reference added to non-static invocations
		MethodTypeDesc invocationType = handle.invocationType();
		MethodTypeDesc desc = kind == Method.Kind.STATIC ? invocationType : invocationType.dropParameterTypes(0, 1);

		return new Method(handle.owner(), kind, handle.methodName(), desc, ownerIsInterface);
	}

	private static Field field(DirectMethodHandleDesc handle, Field.Operation operation, boolean isStatic) {
		ClassDesc type = switch (operation) {
			case GET -> handle.invocationType().returnType();
			case SET -> handle.invocationType().parameterType(0);
		};

		return new Field(handle.owner(), handle.methodName(), type, operation, isStatic);
	}

	public static final class Method extends TypedDirectMethodHandleDesc {
		public static final DualCodec<Method> CODEC = CompositeCodec.of(
				ClassDescs.CLASS_CODEC.fieldOf("owner"), handle -> handle.owner,
				Kind.CODEC.fieldOf("kind"), handle -> handle.kind,
				Codec.STRING.fieldOf("name"), handle -> handle.name,
				SushiCodecs.METHOD_TYPE.codec().fieldOf("desc"), handle -> handle.desc,
				Codec.BOOL.optional(false).fieldOf("interface"), handle -> handle.ownerIsInterface,
				Method::new
		);

		public final Kind kind;
		public final String name;
		public final MethodTypeDesc desc;
		public final boolean ownerIsInterface;

		public Method(ClassDesc owner, Kind kind, String name, MethodTypeDesc desc, boolean ownerIsInterface) {
			super(owner);
			this.kind = kind;
			this.name = name;
			this.desc = desc;
			this.ownerIsInterface = ownerIsInterface;
		}

		@Override
		protected Type type() {
			return Type.METHOD;
		}

		@Override
		public DirectMethodHandleDesc unwrap() {
			DirectMethodHandleDesc.Kind kind = switch (this.kind) {
				case STATIC -> this.ownerIsInterface ? DirectMethodHandleDesc.Kind.INTERFACE_STATIC : DirectMethodHandleDesc.Kind.STATIC;
				case VIRTUAL -> this.ownerIsInterface ? DirectMethodHandleDesc.Kind.INTERFACE_VIRTUAL : DirectMethodHandleDesc.Kind.VIRTUAL;
				case SPECIAL -> this.ownerIsInterface ? DirectMethodHandleDesc.Kind.INTERFACE_SPECIAL : DirectMethodHandleDesc.Kind.SPECIAL;
			};

			return MethodHandleDesc.ofMethod(kind, this.owner, this.name, this.desc);
		}

		public enum Kind {
			STATIC, VIRTUAL, SPECIAL;

			public static final Codec<Kind> CODEC = Codec.byName(Kind.class, kind -> kind.name().toLowerCase(Locale.ROOT));
		}
	}

	public static final class Constructor extends TypedDirectMethodHandleDesc {
		public static final DualCodec<Constructor> CODEC = CompositeCodec.of(
				ClassDescs.CLASS_CODEC.fieldOf("owner"), handle -> handle.owner,
				ClassDescs.ANY_CODEC.listOf().fieldOf("parameters"), handle -> handle.parameters,
				Constructor::new
		);

		public final List<ClassDesc> parameters;

		public Constructor(ClassDesc owner, List<ClassDesc> parameters) {
			super(owner);
			this.parameters = List.copyOf(parameters);
		}

		@Override
		protected Type type() {
			return Type.CONSTRUCTOR;
		}

		@Override
		public DirectMethodHandleDesc unwrap() {
			return MethodHandleDesc.ofConstructor(this.owner, this.parameters.toArray(ClassDesc[]::new));
		}
	}

	public static final class Field extends TypedDirectMethodHandleDesc {
		public static final DualCodec<Field> CODEC = CompositeCodec.of(
				ClassDescs.CLASS_CODEC.fieldOf("owner"), handle -> handle.owner,
				Codec.STRING.fieldOf("name"), handle -> handle.name,
				ClassDescs.ANY_CODEC.fieldOf("field_type"), handle -> handle.fieldType,
				Operation.CODEC.fieldOf("operation"), handle -> handle.operation,
				Codec.BOOL.fieldOf("static"), handle -> handle.isStatic,
				Field::new
		);

		public final String name;
		public final ClassDesc fieldType;
		public final Operation operation;
		public final boolean isStatic;

		public Field(ClassDesc owner, String name, ClassDesc fieldType, Operation operation, boolean isStatic) {
			super(owner);
			this.name = name;
			this.fieldType = fieldType;
			this.operation = operation;
			this.isStatic = isStatic;
		}

		@Override
		protected Type type() {
			return Type.FIELD;
		}

		@Override
		public DirectMethodHandleDesc unwrap() {
			DirectMethodHandleDesc.Kind kind = switch (this.operation) {
				case GET -> this.isStatic ? DirectMethodHandleDesc.Kind.STATIC_GETTER : DirectMethodHandleDesc.Kind.GETTER;
				case SET -> this.isStatic ? DirectMethodHandleDesc.Kind.STATIC_SETTER : DirectMethodHandleDesc.Kind.SETTER;
			};

			return MethodHandleDesc.ofField(kind, this.owner, this.name, this.fieldType);
		}

		public enum Operation {
			GET, SET;

			public static final Codec<Operation> CODEC = Codec.byName(Operation.class, operation -> operation.name().toLowerCase(Locale.ROOT));
		}
	}

	public enum Type {
		METHOD(Method.CODEC.mapCodec()),
		CONSTRUCTOR(Constructor.CODEC.mapCodec()),
		FIELD(Field.CODEC.mapCodec());

		public static final Codec<Type> CODEC = Codec.byName(Type.class, type -> type.name().toLowerCase(Locale.ROOT));

		public final MapCodec<? extends TypedDirectMethodHandleDesc> codec;

		Type(MapCodec<? extends TypedDirectMethodHandleDesc> codec) {
			this.codec = codec;
		}
	}
}
