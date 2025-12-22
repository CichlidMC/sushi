package fish.cichlidmc.sushi.impl.detail;

import fish.cichlidmc.sushi.api.detail.Detail;
import fish.cichlidmc.sushi.api.detail.Details;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public final class DetailsImpl implements Details {
	private final List<Detail> list;

	public DetailsImpl() {
		this.list = new ArrayList<>();
	}

	@Override
	public void add(String name, Object value) {
		this.list.add(new Detail(name, toStringSafe(value)));
	}

	@Override
	public Iterator<Detail> iterator() {
		return this.list.iterator();
	}

	public String buildMessage(String baseMessage) {
		StringBuilder builder = new StringBuilder(baseMessage)
				.append("\nDetails:");

		if (this.list.isEmpty()) {
			return builder.append(" <none>").toString();
		}

		// iterate end-to-start, since details are added bottom-to-top
		for (Detail detail : this.list.reversed()) {
			builder.append("\n\t- ").append(detail.name()).append(": ");
			String indent = '\t' + " ".repeat(detail.name().length() + 4);

			String[] lines = detail.value().split("\n");
			for (int i = 0; i < lines.length; i++) {
				if (i > 0) {
					builder.append(indent);
				}

				builder.append(lines[i]);
			}
		}

		return builder.toString();
	}

	private static String toStringSafe(Object value) {
		try {
			return value.toString();
		} catch (Throwable t) {
			return "<failed to get detail: " + t.getMessage() + '>';
		}
	}
}
