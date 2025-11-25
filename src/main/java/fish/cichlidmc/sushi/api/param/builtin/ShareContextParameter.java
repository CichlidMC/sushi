package fish.cichlidmc.sushi.api.param.builtin;

import fish.cichlidmc.sushi.api.attach.AttachmentKey;
import fish.cichlidmc.sushi.api.model.code.Point;
import fish.cichlidmc.sushi.api.model.code.TransformableCode;
import fish.cichlidmc.sushi.api.param.ContextParameter;
import fish.cichlidmc.sushi.api.registry.Id;
import fish.cichlidmc.sushi.api.target.inject.builtin.ReturnInjectionPoint;
import fish.cichlidmc.sushi.api.transformer.TransformContext;
import fish.cichlidmc.sushi.api.transformer.TransformException;
import fish.cichlidmc.sushi.api.util.ClassDescs;
import fish.cichlidmc.sushi.impl.ref.Refs;
import fish.cichlidmc.tinycodecs.api.codec.CompositeCodec;
import fish.cichlidmc.tinycodecs.api.codec.dual.DualCodec;
import fish.cichlidmc.tinycodecs.api.codec.map.MapCodec;
import org.glavo.classfile.CodeBuilder;
import org.glavo.classfile.TypeKind;

import java.lang.constant.ClassDesc;
import java.util.HashMap;
import java.util.Map;

public final class ShareContextParameter implements ContextParameter {
	public static final DualCodec<ShareContextParameter> CODEC = CompositeCodec.of(
			Id.CODEC.fieldOf("key"), param -> param.key,
			ClassDescs.ANY_CODEC.fieldOf("value_type"), param -> param.valueType,
			ShareContextParameter::new
	);

	private static final AttachmentKey<Map<Id, Integer>> indexKey = AttachmentKey.create();
	private static final int placeholder = -1;

	private final Id key;
	private final ClassDesc valueType;
	private final Refs.Type refType;

	public ShareContextParameter(Id key, ClassDesc valueType) {
		this.key = key;
		this.valueType = valueType;
		this.refType = Refs.holderOf(valueType);
	}

	@Override
	public Prepared prepare(TransformContext context, TransformableCode code, Point point) throws TransformException {
		Map<Id, Integer> index = code.attachments().getOrCreate(indexKey, HashMap::new);

		if (!index.containsKey(this.key)) {
			// first transformer using this key, we get to do setup

			// true value will be set when head insertion is invoked
			index.put(this.key, placeholder);

			code.select().head().insertBefore(builder -> {
				// allocate local slot
				int slot = builder.allocateLocal(TypeKind.ReferenceType);
				// create ref object
				this.refType.constructParameterless(builder);
				// store in slot
				builder.storeInstruction(TypeKind.ReferenceType, slot);
				// update index
				index.put(this.key, slot);
			});

			// discard at returns
			for (Point returnPoint : ReturnInjectionPoint.ALL.find(code)) {
				code.select().at(returnPoint).insertBefore(builder -> {
					this.load(builder, index);
					this.refType.invokeDiscard(builder);
				});
			}
		}

		// we just need to push the ref
		return Prepared.ofPre(builder -> this.load(builder, index));
	}

	@Override
	public ClassDesc type() {
		return this.refType.api;
	}

	@Override
	public MapCodec<? extends ContextParameter> codec() {
		return CODEC.mapCodec();
	}

	private void load(CodeBuilder builder, Map<Id, Integer> index) {
		int slot = this.getSlot(index);
		builder.loadInstruction(TypeKind.ReferenceType, slot);
		builder.checkcast(this.refType.impl);
	}

	private int getSlot(Map<Id, Integer> index) {
		Integer slot = index.get(this.key);
		if (slot == null) {
			throw new IllegalStateException("No slot for " + this.key + ", not even the placeholder");
		} else if (slot == placeholder) {
			throw new IllegalStateException("Slot is not set for " + this.key);
		} else {
			return slot;
		}
	}
}
