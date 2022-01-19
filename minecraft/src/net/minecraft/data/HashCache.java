package net.minecraft.data;

import com.google.common.base.Charsets;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.mojang.logging.LogUtils;
import java.io.IOException;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;

public class HashCache {
	private static final Logger LOGGER = LogUtils.getLogger();
	private final Path path;
	private final Path cachePath;
	private int hits;
	private final Map<Path, String> oldCache = Maps.<Path, String>newHashMap();
	private final Map<Path, String> newCache = Maps.<Path, String>newHashMap();
	private final Set<Path> keep = Sets.<Path>newHashSet();

	public HashCache(Path path, String string) throws IOException {
		this.path = path;
		Path path2 = path.resolve(".cache");
		Files.createDirectories(path2);
		this.cachePath = path2.resolve(string);
		this.walkOutputFiles().forEach(pathx -> this.oldCache.put(pathx, ""));
		if (Files.isReadable(this.cachePath)) {
			IOUtils.readLines(Files.newInputStream(this.cachePath), Charsets.UTF_8).forEach(stringx -> {
				int i = stringx.indexOf(32);
				this.oldCache.put(path.resolve(stringx.substring(i + 1)), stringx.substring(0, i));
			});
		}
	}

	public void purgeStaleAndWrite() throws IOException {
		this.removeStale();

		Writer writer;
		try {
			writer = Files.newBufferedWriter(this.cachePath);
		} catch (IOException var3) {
			LOGGER.warn("Unable write cachefile {}: {}", this.cachePath, var3.toString());
			return;
		}

		IOUtils.writeLines(
			(Collection<?>)this.newCache
				.entrySet()
				.stream()
				.map(entry -> (String)entry.getValue() + " " + this.path.relativize((Path)entry.getKey()))
				.collect(Collectors.toList()),
			System.lineSeparator(),
			writer
		);
		writer.close();
		LOGGER.debug("Caching: cache hits: {}, created: {} removed: {}", this.hits, this.newCache.size() - this.hits, this.oldCache.size());
	}

	@Nullable
	public String getHash(Path path) {
		return (String)this.oldCache.get(path);
	}

	public void putNew(Path path, String string) {
		this.newCache.put(path, string);
		if (Objects.equals(this.oldCache.remove(path), string)) {
			this.hits++;
		}
	}

	public boolean had(Path path) {
		return this.oldCache.containsKey(path);
	}

	public void keep(Path path) {
		this.keep.add(path);
	}

	private void removeStale() throws IOException {
		this.walkOutputFiles().forEach(path -> {
			if (this.had(path) && !this.keep.contains(path)) {
				try {
					Files.delete(path);
				} catch (IOException var3) {
					LOGGER.debug("Unable to delete: {} ({})", path, var3.toString());
				}
			}
		});
	}

	private Stream<Path> walkOutputFiles() throws IOException {
		return Files.walk(this.path).filter(path -> !Objects.equals(this.cachePath, path) && !Files.isDirectory(path, new LinkOption[0]));
	}
}
