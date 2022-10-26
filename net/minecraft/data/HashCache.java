/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.data;

import com.google.common.collect.ImmutableMap;
import com.google.common.hash.HashCode;
import com.google.common.hash.Hashing;
import com.mojang.logging.LogUtils;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileVisitOption;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.attribute.FileAttribute;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;
import net.minecraft.WorldVersion;
import net.minecraft.data.CachedOutput;
import org.apache.commons.lang3.mutable.MutableInt;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

public class HashCache {
    static final Logger LOGGER = LogUtils.getLogger();
    private static final String HEADER_MARKER = "// ";
    private final Path rootDir;
    private final Path cacheDir;
    private final String versionId;
    private final Map<String, ProviderCache> caches;
    private final Set<String> cachesToWrite = new HashSet<String>();
    private final Set<Path> cachePaths = new HashSet<Path>();
    private final int initialCount;
    private int writes;

    private Path getProviderCachePath(String string) {
        return this.cacheDir.resolve(Hashing.sha1().hashString(string, StandardCharsets.UTF_8).toString());
    }

    public HashCache(Path path, Collection<String> collection, WorldVersion worldVersion) throws IOException {
        this.versionId = worldVersion.getName();
        this.rootDir = path;
        this.cacheDir = path.resolve(".cache");
        Files.createDirectories(this.cacheDir, new FileAttribute[0]);
        HashMap<String, ProviderCache> map = new HashMap<String, ProviderCache>();
        int i = 0;
        for (String string : collection) {
            Path path2 = this.getProviderCachePath(string);
            this.cachePaths.add(path2);
            ProviderCache providerCache = HashCache.readCache(path, path2);
            map.put(string, providerCache);
            i += providerCache.count();
        }
        this.caches = map;
        this.initialCount = i;
    }

    private static ProviderCache readCache(Path path, Path path2) {
        if (Files.isReadable(path2)) {
            try {
                return ProviderCache.load(path, path2);
            } catch (Exception exception) {
                LOGGER.warn("Failed to parse cache {}, discarding", (Object)path2, (Object)exception);
            }
        }
        return new ProviderCache("unknown", ImmutableMap.of());
    }

    public boolean shouldRunInThisVersion(String string) {
        ProviderCache providerCache = this.caches.get(string);
        return providerCache == null || !providerCache.version.equals(this.versionId);
    }

    public CompletableFuture<UpdateResult> generateUpdate(String string, UpdateFunction updateFunction) {
        ProviderCache providerCache = this.caches.get(string);
        if (providerCache == null) {
            throw new IllegalStateException("Provider not registered: " + string);
        }
        CacheUpdater cacheUpdater = new CacheUpdater(string, this.versionId, providerCache);
        return updateFunction.update(cacheUpdater).thenApply(object -> cacheUpdater.close());
    }

    public void applyUpdate(UpdateResult updateResult) {
        this.caches.put(updateResult.providerId(), updateResult.cache());
        this.cachesToWrite.add(updateResult.providerId());
        this.writes += updateResult.writes();
    }

    public void purgeStaleAndWrite() throws IOException {
        HashSet<Path> set = new HashSet<Path>();
        this.caches.forEach((string, providerCache) -> {
            if (this.cachesToWrite.contains(string)) {
                Path path = this.getProviderCachePath((String)string);
                providerCache.save(this.rootDir, path, DateTimeFormatter.ISO_LOCAL_DATE_TIME.format(LocalDateTime.now()) + "\t" + string);
            }
            set.addAll(providerCache.data().keySet());
        });
        set.add(this.rootDir.resolve("version.json"));
        MutableInt mutableInt = new MutableInt();
        MutableInt mutableInt2 = new MutableInt();
        try (Stream<Path> stream = Files.walk(this.rootDir, new FileVisitOption[0]);){
            stream.forEach(path -> {
                if (Files.isDirectory(path, new LinkOption[0])) {
                    return;
                }
                if (this.cachePaths.contains(path)) {
                    return;
                }
                mutableInt.increment();
                if (set.contains(path)) {
                    return;
                }
                try {
                    Files.delete(path);
                } catch (IOException iOException) {
                    LOGGER.warn("Failed to delete file {}", path, (Object)iOException);
                }
                mutableInt2.increment();
            });
        }
        LOGGER.info("Caching: total files: {}, old count: {}, new count: {}, removed stale: {}, written: {}", mutableInt, this.initialCount, set.size(), mutableInt2, this.writes);
    }

    record ProviderCache(String version, ImmutableMap<Path, HashCode> data) {
        @Nullable
        public HashCode get(Path path) {
            return this.data.get(path);
        }

        public int count() {
            return this.data.size();
        }

        public static ProviderCache load(Path path, Path path2) throws IOException {
            try (BufferedReader bufferedReader = Files.newBufferedReader(path2, StandardCharsets.UTF_8);){
                String string2 = bufferedReader.readLine();
                if (!string2.startsWith(HashCache.HEADER_MARKER)) {
                    throw new IllegalStateException("Missing cache file header");
                }
                String[] strings = string2.substring(HashCache.HEADER_MARKER.length()).split("\t", 2);
                String string22 = strings[0];
                ImmutableMap.Builder builder = ImmutableMap.builder();
                bufferedReader.lines().forEach(string -> {
                    int i = string.indexOf(32);
                    builder.put(path.resolve(string.substring(i + 1)), HashCode.fromString(string.substring(0, i)));
                });
                ProviderCache providerCache = new ProviderCache(string22, builder.build());
                return providerCache;
            }
        }

        public void save(Path path, Path path2, String string) {
            try (BufferedWriter bufferedWriter = Files.newBufferedWriter(path2, StandardCharsets.UTF_8, new OpenOption[0]);){
                bufferedWriter.write(HashCache.HEADER_MARKER);
                bufferedWriter.write(this.version);
                bufferedWriter.write(9);
                bufferedWriter.write(string);
                bufferedWriter.newLine();
                for (Map.Entry entry : this.data.entrySet()) {
                    bufferedWriter.write(((HashCode)entry.getValue()).toString());
                    bufferedWriter.write(32);
                    bufferedWriter.write(path.relativize((Path)entry.getKey()).toString());
                    bufferedWriter.newLine();
                }
            } catch (IOException iOException) {
                LOGGER.warn("Unable write cachefile {}: {}", (Object)path2, (Object)iOException);
            }
        }
    }

    class CacheUpdater
    implements CachedOutput {
        private final String provider;
        private final ProviderCache oldCache;
        private final ProviderCacheBuilder newCache;
        private final AtomicInteger writes = new AtomicInteger();
        private volatile boolean closed;

        CacheUpdater(String string, String string2, ProviderCache providerCache) {
            this.provider = string;
            this.oldCache = providerCache;
            this.newCache = new ProviderCacheBuilder(string2);
        }

        private boolean shouldWrite(Path path, HashCode hashCode) {
            return !Objects.equals(this.oldCache.get(path), hashCode) || !Files.exists(path, new LinkOption[0]);
        }

        @Override
        public void writeIfNeeded(Path path, byte[] bs, HashCode hashCode) throws IOException {
            if (this.closed) {
                throw new IllegalStateException("Cannot write to cache as it has already been closed");
            }
            if (this.shouldWrite(path, hashCode)) {
                this.writes.incrementAndGet();
                Files.createDirectories(path.getParent(), new FileAttribute[0]);
                Files.write(path, bs, new OpenOption[0]);
            }
            this.newCache.put(path, hashCode);
        }

        public UpdateResult close() {
            this.closed = true;
            return new UpdateResult(this.provider, this.newCache.build(), this.writes.get());
        }
    }

    @FunctionalInterface
    public static interface UpdateFunction {
        public CompletableFuture<?> update(CachedOutput var1);
    }

    public record UpdateResult(String providerId, ProviderCache cache, int writes) {
    }

    record ProviderCacheBuilder(String version, ConcurrentMap<Path, HashCode> data) {
        ProviderCacheBuilder(String string) {
            this(string, new ConcurrentHashMap<Path, HashCode>());
        }

        public void put(Path path, HashCode hashCode) {
            this.data.put(path, hashCode);
        }

        public ProviderCache build() {
            return new ProviderCache(this.version, ImmutableMap.copyOf(this.data));
        }
    }
}

