package fish.cichlidmc.sushi.test.framework.vineflower;

import org.jetbrains.java.decompiler.main.extern.IContextSource;
import org.jetbrains.java.decompiler.main.extern.IResultSaver;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

// based on SingleFileContextSource
public final class ByteArrayContextSource implements IContextSource {
	private final String className;
	private final byte[] bytes;

	public ByteArrayContextSource(String className, byte[] bytes) {
		this.className = className;
		this.bytes = bytes;
	}

	@Override
	public String getName() {
		return this.className;
	}

	@Override
	public Entries getEntries() {
		return new Entries(List.of(Entry.atBase(this.className)), List.of(), List.of());
	}

	@Override
	public InputStream getInputStream(String resource) {
		if (this.bytes == null) {
			throw new IllegalStateException("Input bytes are not set");
		}

		return new ByteArrayInputStream(this.bytes);
	}

	@Override
	public IOutputSink createOutputSink(IResultSaver saver) {
		return new IOutputSink() {
			@Override
			public void acceptClass(String qualifiedName, String fileName, String content, int[] mapping) {
				String entryName = fileName.substring(fileName.lastIndexOf('/') + 1);
				saver.saveClassFile("", qualifiedName, entryName, content, mapping);
			}

			@Override
			public void begin() {
			}

			@Override
			public void acceptDirectory(String directory) {
			}

			@Override
			public void acceptOther(String path) {
			}

			@Override
			public void close() throws IOException {
			}
		};
	}
}
