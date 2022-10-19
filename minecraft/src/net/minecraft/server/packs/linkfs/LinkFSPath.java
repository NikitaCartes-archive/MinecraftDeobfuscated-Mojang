package net.minecraft.server.packs.linkfs;

import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.LinkOption;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.ProviderMismatchException;
import java.nio.file.ReadOnlyFileSystemException;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.nio.file.WatchEvent.Kind;
import java.nio.file.WatchEvent.Modifier;
import java.nio.file.attribute.BasicFileAttributeView;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import javax.annotation.Nullable;

class LinkFSPath implements Path {
	private static final BasicFileAttributes DIRECTORY_ATTRIBUTES = new DummyFileAttributes() {
		public boolean isRegularFile() {
			return false;
		}

		public boolean isDirectory() {
			return true;
		}
	};
	private static final BasicFileAttributes FILE_ATTRIBUTES = new DummyFileAttributes() {
		public boolean isRegularFile() {
			return true;
		}

		public boolean isDirectory() {
			return false;
		}
	};
	private static final Comparator<LinkFSPath> PATH_COMPARATOR = Comparator.comparing(LinkFSPath::pathToString);
	private final String name;
	private final LinkFileSystem fileSystem;
	@Nullable
	private final LinkFSPath parent;
	@Nullable
	private List<String> pathToRoot;
	@Nullable
	private String pathString;
	private final PathContents pathContents;

	public LinkFSPath(LinkFileSystem linkFileSystem, String string, @Nullable LinkFSPath linkFSPath, PathContents pathContents) {
		this.fileSystem = linkFileSystem;
		this.name = string;
		this.parent = linkFSPath;
		this.pathContents = pathContents;
	}

	private LinkFSPath createRelativePath(@Nullable LinkFSPath linkFSPath, String string) {
		return new LinkFSPath(this.fileSystem, string, linkFSPath, PathContents.RELATIVE);
	}

	public LinkFileSystem getFileSystem() {
		return this.fileSystem;
	}

	public boolean isAbsolute() {
		return this.pathContents != PathContents.RELATIVE;
	}

	public File toFile() {
		if (this.pathContents instanceof PathContents.FileContents fileContents) {
			return fileContents.contents().toFile();
		} else {
			throw new UnsupportedOperationException("Path " + this.pathToString() + " does not represent file");
		}
	}

	@Nullable
	public LinkFSPath getRoot() {
		return this.isAbsolute() ? this.fileSystem.rootPath() : null;
	}

	public LinkFSPath getFileName() {
		return this.createRelativePath(null, this.name);
	}

	@Nullable
	public LinkFSPath getParent() {
		return this.parent;
	}

	public int getNameCount() {
		return this.pathToRoot().size();
	}

	private List<String> pathToRoot() {
		if (this.name.isEmpty()) {
			return List.of();
		} else {
			if (this.pathToRoot == null) {
				Builder<String> builder = ImmutableList.builder();
				if (this.parent != null) {
					builder.addAll(this.parent.pathToRoot());
				}

				builder.add(this.name);
				this.pathToRoot = builder.build();
			}

			return this.pathToRoot;
		}
	}

	public LinkFSPath getName(int i) {
		List<String> list = this.pathToRoot();
		if (i >= 0 && i < list.size()) {
			return this.createRelativePath(null, (String)list.get(i));
		} else {
			throw new IllegalArgumentException("Invalid index: " + i);
		}
	}

	public LinkFSPath subpath(int i, int j) {
		List<String> list = this.pathToRoot();
		if (i >= 0 && j <= list.size() && i < j) {
			LinkFSPath linkFSPath = null;

			for (int k = i; k < j; k++) {
				linkFSPath = this.createRelativePath(linkFSPath, (String)list.get(k));
			}

			return linkFSPath;
		} else {
			throw new IllegalArgumentException();
		}
	}

	public boolean startsWith(Path path) {
		if (path.isAbsolute() != this.isAbsolute()) {
			return false;
		} else if (path instanceof LinkFSPath linkFSPath) {
			if (linkFSPath.fileSystem != this.fileSystem) {
				return false;
			} else {
				List<String> list = this.pathToRoot();
				List<String> list2 = linkFSPath.pathToRoot();
				int i = list2.size();
				if (i > list.size()) {
					return false;
				} else {
					for (int j = 0; j < i; j++) {
						if (!((String)list2.get(j)).equals(list.get(j))) {
							return false;
						}
					}

					return true;
				}
			}
		} else {
			return false;
		}
	}

	public boolean endsWith(Path path) {
		if (path.isAbsolute() && !this.isAbsolute()) {
			return false;
		} else if (path instanceof LinkFSPath linkFSPath) {
			if (linkFSPath.fileSystem != this.fileSystem) {
				return false;
			} else {
				List<String> list = this.pathToRoot();
				List<String> list2 = linkFSPath.pathToRoot();
				int i = list2.size();
				int j = list.size() - i;
				if (j < 0) {
					return false;
				} else {
					for (int k = i - 1; k >= 0; k--) {
						if (!((String)list2.get(k)).equals(list.get(j + k))) {
							return false;
						}
					}

					return true;
				}
			}
		} else {
			return false;
		}
	}

	public LinkFSPath normalize() {
		return this;
	}

	public LinkFSPath resolve(Path path) {
		LinkFSPath linkFSPath = this.toLinkPath(path);
		return path.isAbsolute() ? linkFSPath : this.resolve(linkFSPath.pathToRoot());
	}

	private LinkFSPath resolve(List<String> list) {
		LinkFSPath linkFSPath = this;

		for (String string : list) {
			linkFSPath = linkFSPath.resolveName(string);
		}

		return linkFSPath;
	}

	LinkFSPath resolveName(String string) {
		if (isRelativeOrMissing(this.pathContents)) {
			return new LinkFSPath(this.fileSystem, string, this, this.pathContents);
		} else if (this.pathContents instanceof PathContents.DirectoryContents directoryContents) {
			LinkFSPath linkFSPath = (LinkFSPath)directoryContents.children().get(string);
			return linkFSPath != null ? linkFSPath : new LinkFSPath(this.fileSystem, string, this, PathContents.MISSING);
		} else if (this.pathContents instanceof PathContents.FileContents) {
			return new LinkFSPath(this.fileSystem, string, this, PathContents.MISSING);
		} else {
			throw new AssertionError("All content types should be already handled");
		}
	}

	private static boolean isRelativeOrMissing(PathContents pathContents) {
		return pathContents == PathContents.MISSING || pathContents == PathContents.RELATIVE;
	}

	public LinkFSPath relativize(Path path) {
		LinkFSPath linkFSPath = this.toLinkPath(path);
		if (this.isAbsolute() != linkFSPath.isAbsolute()) {
			throw new IllegalArgumentException("absolute mismatch");
		} else {
			List<String> list = this.pathToRoot();
			List<String> list2 = linkFSPath.pathToRoot();
			if (list.size() >= list2.size()) {
				throw new IllegalArgumentException();
			} else {
				for (int i = 0; i < list.size(); i++) {
					if (!((String)list.get(i)).equals(list2.get(i))) {
						throw new IllegalArgumentException();
					}
				}

				return linkFSPath.subpath(list.size(), list2.size());
			}
		}
	}

	public URI toUri() {
		try {
			return new URI("x-mc-link", this.fileSystem.store().name(), this.pathToString(), null);
		} catch (URISyntaxException var2) {
			throw new AssertionError("Failed to create URI", var2);
		}
	}

	public LinkFSPath toAbsolutePath() {
		return this.isAbsolute() ? this : this.fileSystem.rootPath().resolve(this);
	}

	public LinkFSPath toRealPath(LinkOption... linkOptions) {
		return this.toAbsolutePath();
	}

	public WatchKey register(WatchService watchService, Kind<?>[] kinds, Modifier... modifiers) {
		throw new UnsupportedOperationException();
	}

	public int compareTo(Path path) {
		LinkFSPath linkFSPath = this.toLinkPath(path);
		return PATH_COMPARATOR.compare(this, linkFSPath);
	}

	public boolean equals(Object object) {
		if (object == this) {
			return true;
		} else if (object instanceof LinkFSPath linkFSPath) {
			if (this.fileSystem != linkFSPath.fileSystem) {
				return false;
			} else {
				boolean bl = this.hasRealContents();
				if (bl != linkFSPath.hasRealContents()) {
					return false;
				} else {
					return bl ? this.pathContents == linkFSPath.pathContents : Objects.equals(this.parent, linkFSPath.parent) && Objects.equals(this.name, linkFSPath.name);
				}
			}
		} else {
			return false;
		}
	}

	private boolean hasRealContents() {
		return !isRelativeOrMissing(this.pathContents);
	}

	public int hashCode() {
		return this.hasRealContents() ? this.pathContents.hashCode() : this.name.hashCode();
	}

	public String toString() {
		return this.pathToString();
	}

	private String pathToString() {
		if (this.pathString == null) {
			StringBuilder stringBuilder = new StringBuilder();
			if (this.isAbsolute()) {
				stringBuilder.append("/");
			}

			Joiner.on("/").appendTo(stringBuilder, this.pathToRoot());
			this.pathString = stringBuilder.toString();
		}

		return this.pathString;
	}

	private LinkFSPath toLinkPath(@Nullable Path path) {
		if (path == null) {
			throw new NullPointerException();
		} else {
			if (path instanceof LinkFSPath linkFSPath && linkFSPath.fileSystem == this.fileSystem) {
				return linkFSPath;
			}

			throw new ProviderMismatchException();
		}
	}

	public boolean exists() {
		return this.hasRealContents();
	}

	@Nullable
	public Path getTargetPath() {
		return this.pathContents instanceof PathContents.FileContents fileContents ? fileContents.contents() : null;
	}

	@Nullable
	public PathContents.DirectoryContents getDirectoryContents() {
		return this.pathContents instanceof PathContents.DirectoryContents directoryContents ? directoryContents : null;
	}

	public BasicFileAttributeView getBasicAttributeView() {
		return new BasicFileAttributeView() {
			public String name() {
				return "basic";
			}

			public BasicFileAttributes readAttributes() throws IOException {
				return LinkFSPath.this.getBasicAttributes();
			}

			public void setTimes(FileTime fileTime, FileTime fileTime2, FileTime fileTime3) {
				throw new ReadOnlyFileSystemException();
			}
		};
	}

	public BasicFileAttributes getBasicAttributes() throws IOException {
		if (this.pathContents instanceof PathContents.DirectoryContents) {
			return DIRECTORY_ATTRIBUTES;
		} else if (this.pathContents instanceof PathContents.FileContents) {
			return FILE_ATTRIBUTES;
		} else {
			throw new NoSuchFileException(this.pathToString());
		}
	}
}
