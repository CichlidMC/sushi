package fish.cichlidmc.sushi.test.framework.vineflower;

import org.jetbrains.java.decompiler.main.extern.IResultSaver;

import java.util.HashMap;
import java.util.Map;
import java.util.jar.Manifest;

// based on ConsoleFileSaver
public final class ResultSaver implements IResultSaver {
	public final Map<String, String> results = new HashMap<>();

	@Override
	public void saveClassFile(String path, String qualifiedName, String entryName, String content, int[] mapping) {
		this.results.put(qualifiedName, content);
	}

	@Override
	public void saveClassEntry(String path, String archiveName, String qualifiedName, String entryName, String content) {
		this.results.put(qualifiedName, content);
	}

	// ignore all of these

	@Override
	public void saveFolder(String path) {
	}

	@Override
	public void copyFile(String source, String path, String entryName) {
	}

	@Override
	public void createArchive(String path, String archiveName, Manifest manifest) {
	}

	@Override
	public void saveDirEntry(String path, String archiveName, String entryName) {
	}

	@Override
	public void copyEntry(String source, String path, String archiveName, String entry) {
	}

	@Override
	public void closeArchive(String path, String archiveName) {
	}
}
