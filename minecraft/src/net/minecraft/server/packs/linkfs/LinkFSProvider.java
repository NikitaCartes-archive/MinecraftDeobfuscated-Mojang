package net.minecraft.server.packs.linkfs;

import java.io.IOException;
import java.net.URI;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.AccessDeniedException;
import java.nio.file.AccessMode;
import java.nio.file.CopyOption;
import java.nio.file.DirectoryIteratorException;
import java.nio.file.DirectoryStream;
import java.nio.file.FileStore;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.NoSuchFileException;
import java.nio.file.NotDirectoryException;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.ProviderMismatchException;
import java.nio.file.ReadOnlyFileSystemException;
import java.nio.file.StandardOpenOption;
import java.nio.file.DirectoryStream.Filter;
import java.nio.file.attribute.BasicFileAttributeView;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileAttribute;
import java.nio.file.attribute.FileAttributeView;
import java.nio.file.spi.FileSystemProvider;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import javax.annotation.Nullable;

class LinkFSProvider extends FileSystemProvider {
	public static final String SCHEME = "x-mc-link";

	public String getScheme() {
		return "x-mc-link";
	}

	public FileSystem newFileSystem(URI uRI, Map<String, ?> map) {
		throw new UnsupportedOperationException();
	}

	public FileSystem getFileSystem(URI uRI) {
		throw new UnsupportedOperationException();
	}

	public Path getPath(URI uRI) {
		throw new UnsupportedOperationException();
	}

	public SeekableByteChannel newByteChannel(Path path, Set<? extends OpenOption> set, FileAttribute<?>... fileAttributes) throws IOException {
		if (!set.contains(StandardOpenOption.CREATE_NEW)
			&& !set.contains(StandardOpenOption.CREATE)
			&& !set.contains(StandardOpenOption.APPEND)
			&& !set.contains(StandardOpenOption.WRITE)) {
			Path path2 = toLinkPath(path).toAbsolutePath().getTargetPath();
			if (path2 == null) {
				throw new NoSuchFileException(path.toString());
			} else {
				return Files.newByteChannel(path2, set, fileAttributes);
			}
		} else {
			throw new UnsupportedOperationException();
		}
	}

	public DirectoryStream<Path> newDirectoryStream(Path path, Filter<? super Path> filter) throws IOException {
		final PathContents.DirectoryContents directoryContents = toLinkPath(path).toAbsolutePath().getDirectoryContents();
		if (directoryContents == null) {
			throw new NotDirectoryException(path.toString());
		} else {
			return new DirectoryStream<Path>() {
				public Iterator<Path> iterator() {
					return directoryContents.children().values().stream().filter(linkFSPath -> {
						try {
							return filter.accept(linkFSPath);
						} catch (IOException var3) {
							throw new DirectoryIteratorException(var3);
						}
					}).map(linkFSPath -> linkFSPath).iterator();
				}

				public void close() {
				}
			};
		}
	}

	public void createDirectory(Path path, FileAttribute<?>... fileAttributes) {
		throw new ReadOnlyFileSystemException();
	}

	public void delete(Path path) {
		throw new ReadOnlyFileSystemException();
	}

	public void copy(Path path, Path path2, CopyOption... copyOptions) {
		throw new ReadOnlyFileSystemException();
	}

	public void move(Path path, Path path2, CopyOption... copyOptions) {
		throw new ReadOnlyFileSystemException();
	}

	public boolean isSameFile(Path path, Path path2) {
		return path instanceof LinkFSPath && path2 instanceof LinkFSPath && path.equals(path2);
	}

	public boolean isHidden(Path path) {
		return false;
	}

	public FileStore getFileStore(Path path) {
		return toLinkPath(path).getFileSystem().store();
	}

	public void checkAccess(Path path, AccessMode... accessModes) throws IOException {
		if (accessModes.length == 0 && !toLinkPath(path).exists()) {
			throw new NoSuchFileException(path.toString());
		} else {
			AccessMode[] var3 = accessModes;
			int var4 = accessModes.length;
			int var5 = 0;

			while (var5 < var4) {
				AccessMode accessMode = var3[var5];
				switch (accessMode) {
					case READ:
						if (!toLinkPath(path).exists()) {
							throw new NoSuchFileException(path.toString());
						}
					default:
						var5++;
						break;
					case EXECUTE:
					case WRITE:
						throw new AccessDeniedException(accessMode.toString());
				}
			}
		}
	}

	@Nullable
	public <V extends FileAttributeView> V getFileAttributeView(Path path, Class<V> class_, LinkOption... linkOptions) {
		LinkFSPath linkFSPath = toLinkPath(path);
		return (V)(class_ == BasicFileAttributeView.class ? linkFSPath.getBasicAttributeView() : null);
	}

	public <A extends BasicFileAttributes> A readAttributes(Path path, Class<A> class_, LinkOption... linkOptions) throws IOException {
		LinkFSPath linkFSPath = toLinkPath(path).toAbsolutePath();
		if (class_ == BasicFileAttributes.class) {
			return (A)linkFSPath.getBasicAttributes();
		} else {
			throw new UnsupportedOperationException("Attributes of type " + class_.getName() + " not supported");
		}
	}

	public Map<String, Object> readAttributes(Path path, String string, LinkOption... linkOptions) {
		throw new UnsupportedOperationException();
	}

	public void setAttribute(Path path, String string, Object object, LinkOption... linkOptions) {
		throw new ReadOnlyFileSystemException();
	}

	private static LinkFSPath toLinkPath(@Nullable Path path) {
		if (path == null) {
			throw new NullPointerException();
		} else if (path instanceof LinkFSPath) {
			return (LinkFSPath)path;
		} else {
			throw new ProviderMismatchException();
		}
	}
}
