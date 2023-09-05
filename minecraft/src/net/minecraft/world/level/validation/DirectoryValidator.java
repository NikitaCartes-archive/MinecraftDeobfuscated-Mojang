package net.minecraft.world.level.validation;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;

public class DirectoryValidator {
	private final PathMatcher symlinkTargetAllowList;

	public DirectoryValidator(PathMatcher pathMatcher) {
		this.symlinkTargetAllowList = pathMatcher;
	}

	public void validateSymlink(Path path, List<ForbiddenSymlinkInfo> list) throws IOException {
		Path path2 = Files.readSymbolicLink(path);
		if (!this.symlinkTargetAllowList.matches(path2)) {
			list.add(new ForbiddenSymlinkInfo(path, path2));
		}
	}

	public List<ForbiddenSymlinkInfo> validateSymlink(Path path) throws IOException {
		List<ForbiddenSymlinkInfo> list = new ArrayList();
		this.validateSymlink(path, list);
		return list;
	}

	public List<ForbiddenSymlinkInfo> validateDirectory(Path path, boolean bl) throws IOException {
		List<ForbiddenSymlinkInfo> list = new ArrayList();

		BasicFileAttributes basicFileAttributes;
		try {
			basicFileAttributes = Files.readAttributes(path, BasicFileAttributes.class, LinkOption.NOFOLLOW_LINKS);
		} catch (NoSuchFileException var6) {
			return list;
		}

		if (basicFileAttributes.isRegularFile()) {
			throw new IOException("Path " + path + " is not a directory");
		} else {
			if (basicFileAttributes.isSymbolicLink()) {
				if (!bl) {
					this.validateSymlink(path, list);
					return list;
				}

				path = Files.readSymbolicLink(path);
			}

			this.validateKnownDirectory(path, list);
			return list;
		}
	}

	public void validateKnownDirectory(Path path, List<ForbiddenSymlinkInfo> list) throws IOException {
		Files.walkFileTree(path, new SimpleFileVisitor<Path>() {
			private void validateSymlink(Path path, BasicFileAttributes basicFileAttributes) throws IOException {
				if (basicFileAttributes.isSymbolicLink()) {
					DirectoryValidator.this.validateSymlink(path, list);
				}
			}

			public FileVisitResult preVisitDirectory(Path path, BasicFileAttributes basicFileAttributes) throws IOException {
				this.validateSymlink(path, basicFileAttributes);
				return super.preVisitDirectory(path, basicFileAttributes);
			}

			public FileVisitResult visitFile(Path path, BasicFileAttributes basicFileAttributes) throws IOException {
				this.validateSymlink(path, basicFileAttributes);
				return super.visitFile(path, basicFileAttributes);
			}
		});
	}
}
