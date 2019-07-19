/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.data;

import com.google.common.base.Charsets;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.Writer;
import java.nio.file.FileVisitOption;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.attribute.FileAttribute;
import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;

public class HashCache {
    private static final Logger LOGGER = LogManager.getLogger();
    private final Path path;
    private final Path cachePath;
    private int hits;
    private final Map<Path, String> oldCache = Maps.newHashMap();
    private final Map<Path, String> newCache = Maps.newHashMap();
    private final Set<Path> keep = Sets.newHashSet();

    public HashCache(Path path2, String string2) throws IOException {
        this.path = path2;
        Path path22 = path2.resolve(".cache");
        Files.createDirectories(path22, new FileAttribute[0]);
        this.cachePath = path22.resolve(string2);
        this.walkOutputFiles().forEach(path -> this.oldCache.put((Path)path, ""));
        if (Files.isReadable(this.cachePath)) {
            IOUtils.readLines(Files.newInputStream(this.cachePath, new OpenOption[0]), Charsets.UTF_8).forEach(string -> {
                int i = string.indexOf(32);
                this.oldCache.put(path2.resolve(string.substring(i + 1)), string.substring(0, i));
            });
        }
    }

    public void purgeStaleAndWrite() throws IOException {
        BufferedWriter writer;
        this.removeStale();
        try {
            writer = Files.newBufferedWriter(this.cachePath, new OpenOption[0]);
        } catch (IOException iOException) {
            LOGGER.warn("Unable write cachefile {}: {}", (Object)this.cachePath, (Object)iOException.toString());
            return;
        }
        IOUtils.writeLines((Collection)this.newCache.entrySet().stream().map(entry -> (String)entry.getValue() + ' ' + this.path.relativize((Path)entry.getKey())).collect(Collectors.toList()), System.lineSeparator(), writer);
        ((Writer)writer).close();
        LOGGER.debug("Caching: cache hits: {}, created: {} removed: {}", (Object)this.hits, (Object)(this.newCache.size() - this.hits), (Object)this.oldCache.size());
    }

    @Nullable
    public String getHash(Path path) {
        return this.oldCache.get(path);
    }

    public void putNew(Path path, String string) {
        this.newCache.put(path, string);
        if (Objects.equals(this.oldCache.remove(path), string)) {
            ++this.hits;
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
            if (this.had((Path)path) && !this.keep.contains(path)) {
                try {
                    Files.delete(path);
                } catch (IOException iOException) {
                    LOGGER.debug("Unable to delete: {} ({})", path, (Object)iOException.toString());
                }
            }
        });
    }

    private Stream<Path> walkOutputFiles() throws IOException {
        return Files.walk(this.path, new FileVisitOption[0]).filter(path -> !Objects.equals(this.cachePath, path) && !Files.isDirectory(path, new LinkOption[0]));
    }
}

