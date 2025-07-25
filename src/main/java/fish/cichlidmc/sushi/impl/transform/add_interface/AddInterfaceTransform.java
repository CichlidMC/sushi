package fish.cichlidmc.sushi.impl.transform.add_interface;

import fish.cichlidmc.sushi.api.transform.Transform;
import fish.cichlidmc.sushi.api.transform.TransformContext;
import fish.cichlidmc.sushi.api.transform.TransformException;
import fish.cichlidmc.sushi.api.transform.TransformType;
import fish.cichlidmc.sushi.api.util.ClassDescs;
import fish.cichlidmc.sushi.api.util.ElementModifier;
import fish.cichlidmc.sushi.api.validation.ClassInfo;
import fish.cichlidmc.tinycodecs.map.MapCodec;
import org.glavo.classfile.Interfaces;
import org.glavo.classfile.constantpool.ClassEntry;

import java.lang.constant.ClassDesc;
import java.lang.reflect.AccessFlag;
import java.util.ArrayList;
import java.util.List;

public record AddInterfaceTransform(ClassDesc interfaceDesc) implements Transform {
	public static final MapCodec<AddInterfaceTransform> CODEC = ClassDescs.CLASS_CODEC.xmap(
			AddInterfaceTransform::new, transform -> transform.interfaceDesc
	).fieldOf("interface");

	public static final TransformType TYPE = new TransformType(CODEC);

	@Override
	public void apply(TransformContext context) throws TransformException {
		context.validation().ifPresent(validation -> {
			ClassInfo interfaceInfo = validation.findClass(this.interfaceDesc).orElseThrow(
					() -> new TransformException("Interface does not exist")
			);

			if (!interfaceInfo.flags().contains(AccessFlag.INTERFACE)) {
				throw new TransformException("Interface being added isn't actually an interface");
			}
		});

		context.clazz().transform(ElementModifier.forClass(Interfaces.class, Interfaces::of, (builder, interfaces) -> {
			if (this.shouldAddInterface(interfaces)) {
				List<ClassEntry> newInterfaces = new ArrayList<>(interfaces.interfaces());
				newInterfaces.add(builder.constantPool().classEntry(this.interfaceDesc));
				return Interfaces.of(newInterfaces);
			}

			return interfaces;
		}));
	}

	private boolean shouldAddInterface(Interfaces interfaces) {
		return interfaces.interfaces().stream().noneMatch(entry -> this.interfaceDesc.equals(entry.asSymbol()));
	}

	@Override
	public String describe() {
		return "Add interface: " + ClassDescs.fullName(this.interfaceDesc);
	}

	@Override
	public TransformType type() {
		return TYPE;
	}
}
