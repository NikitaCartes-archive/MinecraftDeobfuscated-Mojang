package net.minecraft.server.packs.linkfs;

import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import javax.annotation.Nullable;

abstract class DummyFileAttributes implements BasicFileAttributes {
	private static final FileTime EPOCH = FileTime.fromMillis(0L);

	public FileTime lastModifiedTime() {
		return EPOCH;
	}

	public FileTime lastAccessTime() {
		return EPOCH;
	}

	public FileTime creationTime() {
		return EPOCH;
	}

	public boolean isSymbolicLink() {
		return false;
	}

	public boolean isOther() {
		return false;
	}

	public long size() {
		return 0L;
	}

	@Nullable
	public Object fileKey() {
		return null;
	}
}
