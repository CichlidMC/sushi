package io.github.cichlidmc.sushi.test.framework.vineflower;

import org.jetbrains.java.decompiler.main.extern.IResultSaver;

import java.util.jar.Manifest;

// based on ConsoleFileSaver
public final class ResultSaver implements IResultSaver {
	private String content;

	@Override
	public void saveClassFile(String path, String qualifiedName, String entryName, String content, int[] mapping) {
		this.setContentOrThrow(content);
	}

	@Override
	public void saveClassEntry(String path, String archiveName, String qualifiedName, String entryName, String content) {
		this.setContentOrThrow(content);
	}

	public String getContentOrThrow() {
		if (this.content == null) {
			throw new IllegalStateException("Content not present");
		}

		return this.content;
	}

	private void setContentOrThrow(String content) {
		if (this.content != null) {
			throw new IllegalStateException("Content is already saved");
		}

		this.content = content;
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
