/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.data;

import com.google.common.hash.Hashing;
import com.mojang.logging.LogUtils;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileVisitOption;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.attribute.FileAttribute;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Stream;
import net.minecraft.WorldVersion;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import org.apache.commons.lang3.mutable.MutableInt;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

public class HashCache {
    static final Logger LOGGER = LogUtils.getLogger();
    private static final String HEADER_MARKER = "// ";
    private final Path rootDir;
    private final Path cacheDir;
    private final String versionId;
    private final Map<DataProvider, ProviderCache> existingCaches;
    private final Map<DataProvider, CacheUpdater> cachesToWrite = new HashMap<DataProvider, CacheUpdater>();
    private final Set<Path> cachePaths = new HashSet<Path>();
    private final int initialCount;

    private Path getProviderCachePath(DataProvider dataProvider) {
        return this.cacheDir.resolve(Hashing.sha1().hashString(dataProvider.getName(), StandardCharsets.UTF_8).toString());
    }

    public HashCache(Path path, List<DataProvider> list, WorldVersion worldVersion) throws IOException {
        this.versionId = worldVersion.getName();
        this.rootDir = path;
        this.cacheDir = path.resolve(".cache");
        Files.createDirectories(this.cacheDir, new FileAttribute[0]);
        HashMap<DataProvider, ProviderCache> map = new HashMap<DataProvider, ProviderCache>();
        int i = 0;
        for (DataProvider dataProvider : list) {
            Path path2 = this.getProviderCachePath(dataProvider);
            this.cachePaths.add(path2);
            ProviderCache providerCache = HashCache.readCache(path, path2);
            map.put(dataProvider, providerCache);
            i += providerCache.count();
        }
        this.existingCaches = map;
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
        return new ProviderCache("unknown");
    }

    public boolean shouldRunInThisVersion(DataProvider dataProvider) {
        ProviderCache providerCache = this.existingCaches.get(dataProvider);
        return providerCache == null || !providerCache.version.equals(this.versionId);
    }

    public CachedOutput getUpdater(DataProvider dataProvider2) {
        return this.cachesToWrite.computeIfAbsent(dataProvider2, dataProvider -> {
            ProviderCache providerCache = this.existingCaches.get(dataProvider);
            if (providerCache == null) {
                throw new IllegalStateException("Provider not registered: " + dataProvider.getName());
            }
            CacheUpdater cacheUpdater = new CacheUpdater(this.versionId, providerCache);
            this.existingCaches.put((DataProvider)dataProvider, cacheUpdater.newCache);
            return cacheUpdater;
        });
    }

    public void purgeStaleAndWrite() throws IOException {
        MutableInt mutableInt = new MutableInt();
        this.cachesToWrite.forEach((dataProvider, cacheUpdater) -> {
            Path path = this.getProviderCachePath((DataProvider)dataProvider);
            cacheUpdater.newCache.save(this.rootDir, path, DateTimeFormatter.ISO_LOCAL_DATE_TIME.format(LocalDateTime.now()) + "\t" + dataProvider.getName());
            mutableInt.add(cacheUpdater.writes);
        });
        HashSet<Path> set = new HashSet<Path>();
        this.existingCaches.values().forEach(providerCache -> set.addAll(providerCache.data().keySet()));
        set.add(this.rootDir.resolve("version.json"));
        MutableInt mutableInt2 = new MutableInt();
        MutableInt mutableInt3 = new MutableInt();
        try (Stream<Path> stream = Files.walk(this.rootDir, new FileVisitOption[0]);){
            stream.forEach(path -> {
                if (Files.isDirectory(path, new LinkOption[0])) {
                    return;
                }
                if (this.cachePaths.contains(path)) {
                    return;
                }
                mutableInt2.increment();
                if (set.contains(path)) {
                    return;
                }
                try {
                    Files.delete(path);
                } catch (IOException iOException) {
                    LOGGER.warn("Failed to delete file {}", path, (Object)iOException);
                }
                mutableInt3.increment();
            });
        }
        LOGGER.info("Caching: total files: {}, old count: {}, new count: {}, removed stale: {}, written: {}", mutableInt2, this.initialCount, set.size(), mutableInt3, mutableInt);
    }

    record ProviderCache(String version, Map<Path, String> data) {
        ProviderCache(String string) {
            this(string, new HashMap<Path, String>());
        }

        @Nullable
        public String get(Path path) {
            return this.data.get(path);
        }

        public void put(Path path, String string) {
            this.data.put(path, string);
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
                HashMap map = new HashMap();
                bufferedReader.lines().forEach(string -> {
                    int i = string.indexOf(32);
                    map.put(path.resolve(string.substring(i + 1)), string.substring(0, i));
                });
                ProviderCache providerCache = new ProviderCache(string22, Map.copyOf(map));
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
                for (Map.Entry<Path, String> entry : this.data.entrySet()) {
                    bufferedWriter.write(entry.getValue());
                    bufferedWriter.write(32);
                    bufferedWriter.write(path.relativize(entry.getKey()).toString());
                    bufferedWriter.newLine();
                }
            } catch (IOException iOException) {
                LOGGER.warn("Unable write cachefile {}: {}", (Object)path2, (Object)iOException);
            }
        }
    }

    static class CacheUpdater
    implements CachedOutput {
        private final ProviderCache oldCache;
        final ProviderCache newCache;
        int writes;

        CacheUpdater(String string, ProviderCache providerCache) {
            this.oldCache = providerCache;
            this.newCache = new ProviderCache(string);
        }

        private boolean shouldWrite(Path path, String string) {
            return !Objects.equals(this.oldCache.get(path), string) || !Files.exists(path, new LinkOption[0]);
        }

        @Override
        public void writeIfNeeded(Path path, String string) throws IOException {
            String string2 = Hashing.sha1().hashUnencodedChars(string).toString();
            if (this.shouldWrite(path, string2)) {
                ++this.writes;
                Files.createDirectories(path.getParent(), new FileAttribute[0]);
                try (BufferedWriter bufferedWriter = Files.newBufferedWriter(path, StandardCharsets.UTF_8, new OpenOption[0]);){
                    bufferedWriter.write(string);
                }
            }
            this.newCache.put(path, string2);
        }

        @Override
        public void writeIfNeeded(Path path, byte[] bs, String string) throws IOException {
            if (this.shouldWrite(path, string)) {
                ++this.writes;
                Files.createDirectories(path.getParent(), new FileAttribute[0]);
                try (OutputStream outputStream = Files.newOutputStream(path, new OpenOption[0]);){
                    outputStream.write(bs);
                }
            }
            this.newCache.put(path, string);
        }
    }
}

