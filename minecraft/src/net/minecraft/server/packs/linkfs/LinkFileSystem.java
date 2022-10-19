package net.minecraft.server.packs.linkfs;

import com.google.common.base.Splitter;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import java.nio.file.FileStore;
import java.nio.file.FileSystem;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.WatchService;
import java.nio.file.attribute.UserPrincipalLookupService;
import java.nio.file.spi.FileSystemProvider;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.annotation.Nullable;

public class LinkFileSystem extends FileSystem {
	private static final Set<String> VIEWS = Set.of("basic");
	public static final String PATH_SEPARATOR = "/";
	private static final Splitter PATH_SPLITTER = Splitter.on('/');
	private final FileStore store;
	private final FileSystemProvider provider = new LinkFSProvider();
	private final LinkFSPath root;

	LinkFileSystem(String string, LinkFileSystem.DirectoryEntry directoryEntry) {
		this.store = new LinkFSFileStore(string);
		this.root = buildPath(directoryEntry, this, "", null);
	}

	private static LinkFSPath buildPath(
		LinkFileSystem.DirectoryEntry directoryEntry, LinkFileSystem linkFileSystem, String string, @Nullable LinkFSPath linkFSPath
	) {
		Object2ObjectOpenHashMap<String, LinkFSPath> object2ObjectOpenHashMap = new Object2ObjectOpenHashMap<>();
		LinkFSPath linkFSPath2 = new LinkFSPath(linkFileSystem, string, linkFSPath, new PathContents.DirectoryContents(object2ObjectOpenHashMap));
		directoryEntry.files
			.forEach((stringx, path) -> object2ObjectOpenHashMap.put(stringx, new LinkFSPath(linkFileSystem, stringx, linkFSPath2, new PathContents.FileContents(path))));
		directoryEntry.children
			.forEach((stringx, directoryEntryx) -> object2ObjectOpenHashMap.put(stringx, buildPath(directoryEntryx, linkFileSystem, stringx, linkFSPath2)));
		object2ObjectOpenHashMap.trim();
		return linkFSPath2;
	}

	public FileSystemProvider provider() {
		return this.provider;
	}

	public void close() {
	}

	public boolean isOpen() {
		return true;
	}

	public boolean isReadOnly() {
		return true;
	}

	public String getSeparator() {
		return "/";
	}

	public Iterable<Path> getRootDirectories() {
		return List.of(this.root);
	}

	public Iterable<FileStore> getFileStores() {
		return List.of(this.store);
	}

	public Set<String> supportedFileAttributeViews() {
		return VIEWS;
	}

	public Path getPath(String string, String... strings) {
		Stream<String> stream = Stream.of(string);
		if (strings.length > 0) {
			stream = Stream.concat(stream, Stream.of(strings));
		}

		String string2 = (String)stream.collect(Collectors.joining("/"));
		if (string2.equals("/")) {
			return this.root;
		} else if (string2.startsWith("/")) {
			LinkFSPath linkFSPath = this.root;

			for (String string3 : PATH_SPLITTER.split(string2.substring(1))) {
				if (string3.isEmpty()) {
					throw new IllegalArgumentException("Empty paths not allowed");
				}

				linkFSPath = linkFSPath.resolveName(string3);
			}

			return linkFSPath;
		} else {
			LinkFSPath linkFSPath = null;

			for (String string3 : PATH_SPLITTER.split(string2)) {
				if (string3.isEmpty()) {
					throw new IllegalArgumentException("Empty paths not allowed");
				}

				linkFSPath = new LinkFSPath(this, string3, linkFSPath, PathContents.RELATIVE);
			}

			if (linkFSPath == null) {
				throw new IllegalArgumentException("Empty paths not allowed");
			} else {
				return linkFSPath;
			}
		}
	}

	public PathMatcher getPathMatcher(String string) {
		throw new UnsupportedOperationException();
	}

	public UserPrincipalLookupService getUserPrincipalLookupService() {
		throw new UnsupportedOperationException();
	}

	public WatchService newWatchService() {
		throw new UnsupportedOperationException();
	}

	public FileStore store() {
		return this.store;
	}

	public LinkFSPath rootPath() {
		return this.root;
	}

	public static LinkFileSystem.Builder builder() {
		return new LinkFileSystem.Builder();
	}

	public static class Builder {
		private final LinkFileSystem.DirectoryEntry root = new LinkFileSystem.DirectoryEntry();

		public LinkFileSystem.Builder put(List<String> list, String string, Path path) {
			LinkFileSystem.DirectoryEntry directoryEntry = this.root;

			for (String string2 : list) {
				directoryEntry = (LinkFileSystem.DirectoryEntry)directoryEntry.children.computeIfAbsent(string2, stringx -> new LinkFileSystem.DirectoryEntry());
			}

			directoryEntry.files.put(string, path);
			return this;
		}

		public LinkFileSystem.Builder put(List<String> list, Path path) {
			if (list.isEmpty()) {
				throw new IllegalArgumentException("Path can't be empty");
			} else {
				int i = list.size() - 1;
				return this.put(list.subList(0, i), (String)list.get(i), path);
			}
		}

		public FileSystem build(String string) {
			return new LinkFileSystem(string, this.root);
		}
	}

	static record DirectoryEntry(Map<String, LinkFileSystem.DirectoryEntry> children, Map<String, Path> files) {

		public DirectoryEntry() {
			this(new HashMap(), new HashMap());
		}
	}
}
