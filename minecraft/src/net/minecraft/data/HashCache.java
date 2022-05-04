package net.minecraft.data;

import com.google.common.hash.HashCode;
import com.google.common.hash.Hashing;
import com.mojang.logging.LogUtils;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.Map.Entry;
import java.util.stream.Stream;
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
	private final Map<DataProvider, HashCache.ProviderCache> existingCaches;
	private final Map<DataProvider, HashCache.CacheUpdater> cachesToWrite = new HashMap();
	private final Set<Path> cachePaths = new HashSet();
	private final int initialCount;

	private Path getProviderCachePath(DataProvider dataProvider) {
		return this.cacheDir.resolve(Hashing.sha1().hashString(dataProvider.getName(), StandardCharsets.UTF_8).toString());
	}

	public HashCache(Path path, List<DataProvider> list, WorldVersion worldVersion) throws IOException {
		this.versionId = worldVersion.getName();
		this.rootDir = path;
		this.cacheDir = path.resolve(".cache");
		Files.createDirectories(this.cacheDir);
		Map<DataProvider, HashCache.ProviderCache> map = new HashMap();
		int i = 0;

		for (DataProvider dataProvider : list) {
			Path path2 = this.getProviderCachePath(dataProvider);
			this.cachePaths.add(path2);
			HashCache.ProviderCache providerCache = readCache(path, path2);
			map.put(dataProvider, providerCache);
			i += providerCache.count();
		}

		this.existingCaches = map;
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

		return new HashCache.ProviderCache("unknown");
	}

	public boolean shouldRunInThisVersion(DataProvider dataProvider) {
		HashCache.ProviderCache providerCache = (HashCache.ProviderCache)this.existingCaches.get(dataProvider);
		return providerCache == null || !providerCache.version.equals(this.versionId);
	}

	public CachedOutput getUpdater(DataProvider dataProvider) {
		return (CachedOutput)this.cachesToWrite.computeIfAbsent(dataProvider, dataProviderx -> {
			HashCache.ProviderCache providerCache = (HashCache.ProviderCache)this.existingCaches.get(dataProviderx);
			if (providerCache == null) {
				throw new IllegalStateException("Provider not registered: " + dataProviderx.getName());
			} else {
				HashCache.CacheUpdater cacheUpdater = new HashCache.CacheUpdater(this.versionId, providerCache);
				this.existingCaches.put(dataProviderx, cacheUpdater.newCache);
				return cacheUpdater;
			}
		});
	}

	public void purgeStaleAndWrite() throws IOException {
		MutableInt mutableInt = new MutableInt();
		this.cachesToWrite.forEach((dataProvider, cacheUpdater) -> {
			Path path = this.getProviderCachePath(dataProvider);
			cacheUpdater.newCache.save(this.rootDir, path, DateTimeFormatter.ISO_LOCAL_DATE_TIME.format(LocalDateTime.now()) + "\t" + dataProvider.getName());
			mutableInt.add(cacheUpdater.writes);
		});
		Set<Path> set = new HashSet();
		this.existingCaches.values().forEach(providerCache -> set.addAll(providerCache.data().keySet()));
		set.add(this.rootDir.resolve("version.json"));
		MutableInt mutableInt2 = new MutableInt();
		MutableInt mutableInt3 = new MutableInt();
		Stream<Path> stream = Files.walk(this.rootDir);

		try {
			stream.forEach(path -> {
				if (!Files.isDirectory(path, new LinkOption[0])) {
					if (!this.cachePaths.contains(path)) {
						mutableInt2.increment();
						if (!set.contains(path)) {
							try {
								Files.delete(path);
							} catch (IOException var6) {
								LOGGER.warn("Failed to delete file {}", path, var6);
							}

							mutableInt3.increment();
						}
					}
				}
			});
		} catch (Throwable var9) {
			if (stream != null) {
				try {
					stream.close();
				} catch (Throwable var8) {
					var9.addSuppressed(var8);
				}
			}

			throw var9;
		}

		if (stream != null) {
			stream.close();
		}

		LOGGER.info(
			"Caching: total files: {}, old count: {}, new count: {}, removed stale: {}, written: {}",
			mutableInt2,
			this.initialCount,
			set.size(),
			mutableInt3,
			mutableInt
		);
	}

	static class CacheUpdater implements CachedOutput {
		private final HashCache.ProviderCache oldCache;
		final HashCache.ProviderCache newCache;
		int writes;

		CacheUpdater(String string, HashCache.ProviderCache providerCache) {
			this.oldCache = providerCache;
			this.newCache = new HashCache.ProviderCache(string);
		}

		private boolean shouldWrite(Path path, HashCode hashCode) {
			return !Objects.equals(this.oldCache.get(path), hashCode) || !Files.exists(path, new LinkOption[0]);
		}

		@Override
		public void writeIfNeeded(Path path, byte[] bs, HashCode hashCode) throws IOException {
			if (this.shouldWrite(path, hashCode)) {
				this.writes++;
				Files.createDirectories(path.getParent());
				OutputStream outputStream = Files.newOutputStream(path);

				try {
					outputStream.write(bs);
				} catch (Throwable var8) {
					if (outputStream != null) {
						try {
							outputStream.close();
						} catch (Throwable var7) {
							var8.addSuppressed(var7);
						}
					}

					throw var8;
				}

				if (outputStream != null) {
					outputStream.close();
				}
			}

			this.newCache.put(path, hashCode);
		}
	}

	static record ProviderCache(String version, Map<Path, HashCode> data) {

		ProviderCache(String string) {
			this(string, new HashMap());
		}

		@Nullable
		public HashCode get(Path path) {
			return (HashCode)this.data.get(path);
		}

		public void put(Path path, HashCode hashCode) {
			this.data.put(path, hashCode);
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
				Map<Path, HashCode> map = new HashMap();
				bufferedReader.lines().forEach(stringx -> {
					int i = stringx.indexOf(32);
					map.put(path.resolve(stringx.substring(i + 1)), HashCode.fromString(stringx.substring(0, i)));
				});
				var7 = new HashCache.ProviderCache(string2, Map.copyOf(map));
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
}
