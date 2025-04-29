package fish.cichlidmc.sushi.impl.transform.add_interface;

import fish.cichlidmc.sushi.api.transform.Transform;
import fish.cichlidmc.sushi.api.transform.TransformContext;
import fish.cichlidmc.sushi.api.transform.TransformException;
import fish.cichlidmc.sushi.api.transform.TransformType;
import fish.cichlidmc.sushi.api.util.JavaType;
import fish.cichlidmc.tinycodecs.map.MapCodec;

public class AddInterfaceTransform implements Transform {
	public static final MapCodec<AddInterfaceTransform> CODEC = JavaType.CLASS_CODEC
			.xmap(AddInterfaceTransform::new, transform -> transform.interfaceType)
			.fieldOf("interface");

	public static final TransformType TYPE = new TransformType(CODEC);

	private final JavaType interfaceType;

	private AddInterfaceTransform(JavaType interfaceType) {
		this.interfaceType = interfaceType;
	}

	@Override
	public boolean apply(TransformContext context) throws TransformException {
		try {
			Class.forName(this.interfaceType.name, false, this.getClass().getClassLoader());
		} catch (ClassNotFoundException ignored) {
			throw new TransformException("Interface not found: " + this.interfaceType.name);
		}

		String internalName = this.interfaceType.internalName;
		if (context.node().interfaces.contains(internalName))
			throw new TransformException("Interface '" + this.interfaceType + "' is already applied");

		return context.node().interfaces.add(internalName);
	}

	@Override
	public String describe() {
		return "Add interface: " + this.interfaceType.name;
	}

	@Override
	public TransformType type() {
		return TYPE;
	}
}
