package fish.cichlidmc.sushi.impl.transformer;

import fish.cichlidmc.sushi.api.model.TransformableField;
import fish.cichlidmc.sushi.api.model.TransformableMethod;
import fish.cichlidmc.sushi.api.model.code.TransformableCode;
import fish.cichlidmc.sushi.api.transformer.DirectTransform;
import fish.cichlidmc.sushi.api.transformer.TransformContext;

import java.lang.classfile.ClassBuilder;
import java.lang.classfile.MethodBuilder;

public sealed class DirectTransformContextImpl implements DirectTransform.Context {
	private final TransformContext transformContext;

	public DirectTransformContextImpl(TransformContext transformContext) {
		this.transformContext = transformContext;
	}

	@Override
	public TransformContext transformContext() {
		return this.transformContext;
	}

	public static sealed class MemberImpl extends DirectTransformContextImpl implements DirectTransform.Context.Member {
		private final ClassBuilder classBuilder;

		private MemberImpl(TransformContext transformContext, ClassBuilder classBuilder) {
			super(transformContext);
			this.classBuilder = classBuilder;
		}

		@Override
		public ClassBuilder classBuilder() {
			return this.classBuilder;
		}
	}

	public static final class FieldImpl extends MemberImpl implements DirectTransform.Context.Field {
		private final TransformableField field;

		public FieldImpl(TransformContext transformContext, ClassBuilder classBuilder, TransformableField field) {
			super(transformContext, classBuilder);
			this.field = field;
		}

		@Override
		public TransformableField field() {
			return this.field;
		}
	}

	public static sealed class MethodImpl extends MemberImpl implements DirectTransform.Context.Method {
		private final TransformableMethod method;

		public MethodImpl(TransformContext transformContext, ClassBuilder classBuilder, TransformableMethod method) {
			super(transformContext, classBuilder);
			this.method = method;
		}

		@Override
		public TransformableMethod method() {
			return this.method;
		}
	}

	public static final class CodeImpl extends MethodImpl implements DirectTransform.Context.Code {
		private final MethodBuilder methodBuilder;
		private final TransformableCode code;

		public CodeImpl(TransformContext transformContext, ClassBuilder classBuilder,
						 TransformableMethod method, MethodBuilder methodBuilder, TransformableCode code) {
			super(transformContext, classBuilder, method);
			this.methodBuilder = methodBuilder;
			this.code = code;
		}

		@Override
		public MethodBuilder methodBuilder() {
			return this.methodBuilder;
		}

		@Override
		public TransformableCode code() {
			return this.code;
		}
	}
}
