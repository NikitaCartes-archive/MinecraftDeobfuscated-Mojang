package net.minecraft.data;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;
import com.google.common.hash.HashCode;
import com.google.common.hash.Hashing;
import com.mojang.logging.LogUtils;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.Map.Entry;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;
import javax.annotation.Nullable;
import net.minecraft.WorldVersion;
import org.apache.commons.lang3.mutable.MutableInt;
import org.slf4j.Logger;

public class HashCache {
	static final Logger LOGGER = LogUtils.getLogger();
	private static final String HEADER_MARKER = "// ";
	private final Path rootDir;
	private final Path cacheDir;
	private final String versionId;
	private final Map<String, HashCache.ProviderCache> caches;
	private final Set<String> cachesToWrite = new HashSet();
	final Set<Path> cachePaths = new HashSet();
	private final int initialCount;
	private int writes;

	private Path getProviderCachePath(String string) {
		return this.cacheDir.resolve(Hashing.sha1().hashString(string, StandardCharsets.UTF_8).toString());
	}

	public HashCache(Path path, Collection<String> collection, WorldVersion worldVersion) throws IOException {
		this.versionId = worldVersion.getName();
		this.rootDir = path;
		this.cacheDir = path.resolve(".cache");
		Files.createDirectories(this.cacheDir);
		Map<String, HashCache.ProviderCache> map = new HashMap();
		int i = 0;

		for (String string : collection) {
			Path path2 = this.getProviderCachePath(string);
			this.cachePaths.add(path2);
			HashCache.ProviderCache providerCache = readCache(path, path2);
			map.put(string, providerCache);
			i += providerCache.count();
		}

		this.caches = map;
		this.initialCount = i;
	}

	private static HashCache.ProviderCache readCache(Path path, Path path2) {
		if (Files.isReadable(path2)) {
			try {
				return HashCache.ProviderCache.load(path, path2);
			} catch (Exception var3) {
				LOGGER.warn("Failed to parse cache {}, discarding", path2, var3);
			}
		}

		return new HashCache.ProviderCache("unknown", ImmutableMap.of());
	}

	public boolean shouldRunInThisVersion(String string) {
		HashCache.ProviderCache providerCache = (HashCache.ProviderCache)this.caches.get(string);
		return providerCache == null || !providerCache.version.equals(this.versionId);
	}

	public CompletableFuture<HashCache.UpdateResult> generateUpdate(String string, HashCache.UpdateFunction updateFunction) {
		HashCache.ProviderCache providerCache = (HashCache.ProviderCache)this.caches.get(string);
		if (providerCache == null) {
			throw new IllegalStateException("Provider not registered: " + string);
		} else {
			HashCache.CacheUpdater cacheUpdater = new HashCache.CacheUpdater(string, this.versionId, providerCache);
			return updateFunction.update(cacheUpdater).thenApply(object -> cacheUpdater.close());
		}
	}

	public void applyUpdate(HashCache.UpdateResult updateResult) {
		this.caches.put(updateResult.providerId(), updateResult.cache());
		this.cachesToWrite.add(updateResult.providerId());
		this.writes = this.writes + updateResult.writes();
	}

	public void purgeStaleAndWrite() throws IOException {
		final Set<Path> set = new HashSet();
		this.caches.forEach((string, providerCache) -> {
			if (this.cachesToWrite.contains(string)) {
				Path path = this.getProviderCachePath(string);
				providerCache.save(this.rootDir, path, DateTimeFormatter.ISO_LOCAL_DATE_TIME.format(LocalDateTime.now()) + "\t" + string);
			}

			set.addAll(providerCache.data().keySet());
		});
		set.add(this.rootDir.resolve("version.json"));
		final MutableInt mutableInt = new MutableInt();
		final MutableInt mutableInt2 = new MutableInt();
		Files.walkFileTree(this.rootDir, new SimpleFileVisitor<Path>() {
			public FileVisitResult visitFile(Path path, BasicFileAttributes basicFileAttributes) {
				if (HashCache.this.cachePaths.contains(path)) {
					return FileVisitResult.CONTINUE;
				} else {
					mutableInt.increment();
					if (set.contains(path)) {
						return FileVisitResult.CONTINUE;
					} else {
						try {
							Files.delete(path);
						} catch (IOException var4) {
							HashCache.LOGGER.warn("Failed to delete file {}", path, var4);
						}

						mutableInt2.increment();
						return FileVisitResult.CONTINUE;
					}
				}
			}
		});
		LOGGER.info(
			"Caching: total files: {}, old count: {}, new count: {}, removed stale: {}, written: {}",
			mutableInt,
			this.initialCount,
			set.size(),
			mutableInt2,
			this.writes
		);
	}

	class CacheUpdater implements CachedOutput {
		private final String provider;
		private final HashCache.ProviderCache oldCache;
		private final HashCache.ProviderCacheBuilder newCache;
		private final AtomicInteger writes = new AtomicInteger();
		private volatile boolean closed;

		CacheUpdater(final String string, final String string2, final HashCache.ProviderCache providerCache) {
			this.provider = string;
			this.oldCache = providerCache;
			this.newCache = new HashCache.ProviderCacheBuilder(string2);
		}

		private boolean shouldWrite(Path path, HashCode hashCode) {
			return !Objects.equals(this.oldCache.get(path), hashCode) || !Files.exists(path, new LinkOption[0]);
		}

		@Override
		public void writeIfNeeded(Path path, byte[] bs, HashCode hashCode) throws IOException {
			if (this.closed) {
				throw new IllegalStateException("Cannot write to cache as it has already been closed");
			} else {
				if (this.shouldWrite(path, hashCode)) {
					this.writes.incrementAndGet();
					Files.createDirectories(path.getParent());
					Files.write(path, bs, new OpenOption[0]);
				}

				this.newCache.put(path, hashCode);
			}
		}

		public HashCache.UpdateResult close() {
			this.closed = true;
			return new HashCache.UpdateResult(this.provider, this.newCache.build(), this.writes.get());
		}
	}

	static record ProviderCache(String version, ImmutableMap<Path, HashCode> data) {

		@Nullable
		public HashCode get(Path path) {
			return this.data.get(path);
		}

		public int count() {
			return this.data.size();
		}

		public static HashCache.ProviderCache load(Path path, Path path2) throws IOException {
			BufferedReader bufferedReader = Files.newBufferedReader(path2, StandardCharsets.UTF_8);

			HashCache.ProviderCache var7;
			try {
				String string = bufferedReader.readLine();
				if (!string.startsWith("// ")) {
					throw new IllegalStateException("Missing cache file header");
				}

				String[] strings = string.substring("// ".length()).split("\t", 2);
				String string2 = strings[0];
				Builder<Path, HashCode> builder = ImmutableMap.builder();
				bufferedReader.lines().forEach(stringx -> {
					int i = stringx.indexOf(32);
					builder.put(path.resolve(stringx.substring(i + 1)), HashCode.fromString(stringx.substring(0, i)));
				});
				var7 = new HashCache.ProviderCache(string2, builder.build());
			} catch (Throwable var9) {
				if (bufferedReader != null) {
					try {
						bufferedReader.close();
					} catch (Throwable var8) {
						var9.addSuppressed(var8);
					}
				}

				throw var9;
			}

			if (bufferedReader != null) {
				bufferedReader.close();
			}

			return var7;
		}

		public void save(Path path, Path path2, String string) {
			try {
				BufferedWriter bufferedWriter = Files.newBufferedWriter(path2, StandardCharsets.UTF_8);

				try {
					bufferedWriter.write("// ");
					bufferedWriter.write(this.version);
					bufferedWriter.write(9);
					bufferedWriter.write(string);
					bufferedWriter.newLine();

					for (Entry<Path, HashCode> entry : this.data.entrySet()) {
						bufferedWriter.write(((HashCode)entry.getValue()).toString());
						bufferedWriter.write(32);
						bufferedWriter.write(path.relativize((Path)entry.getKey()).toString());
						bufferedWriter.newLine();
					}
				} catch (Throwable var8) {
					if (bufferedWriter != null) {
						try {
							bufferedWriter.close();
						} catch (Throwable var7) {
							var8.addSuppressed(var7);
						}
					}

					throw var8;
				}

				if (bufferedWriter != null) {
					bufferedWriter.close();
				}
			} catch (IOException var9) {
				HashCache.LOGGER.warn("Unable write cachefile {}: {}", path2, var9);
			}
		}
	}

	static record ProviderCacheBuilder(String version, ConcurrentMap<Path, HashCode> data) {
		ProviderCacheBuilder(String string) {
			this(string, new ConcurrentHashMap());
		}

		public void put(Path path, HashCode hashCode) {
			this.data.put(path, hashCode);
		}

		public HashCache.ProviderCache build() {
			return new HashCache.ProviderCache(this.version, ImmutableMap.copyOf(this.data));
		}
	}

	@FunctionalInterface
	public interface UpdateFunction {
		CompletableFuture<?> update(CachedOutput cachedOutput);
	}

	public static record UpdateResult(String providerId, HashCache.ProviderCache cache, int writes) {
	}
}
