/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.chunk.storage;

import it.unimi.dsi.fastutil.longs.Long2ObjectLinkedOpenHashMap;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import net.minecraft.FileUtil;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.StreamTagVisitor;
import net.minecraft.util.ExceptionCollector;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.storage.RegionFile;
import org.jetbrains.annotations.Nullable;

public final class RegionFileStorage
implements AutoCloseable {
    public static final String ANVIL_EXTENSION = ".mca";
    private static final int MAX_CACHE_SIZE = 256;
    private final Long2ObjectLinkedOpenHashMap<RegionFile> regionCache = new Long2ObjectLinkedOpenHashMap();
    private final Path folder;
    private final boolean sync;

    RegionFileStorage(Path path, boolean bl) {
        this.folder = path;
        this.sync = bl;
    }

    private RegionFile getRegionFile(ChunkPos chunkPos) throws IOException {
        long l = ChunkPos.asLong(chunkPos.getRegionX(), chunkPos.getRegionZ());
        RegionFile regionFile = this.regionCache.getAndMoveToFirst(l);
        if (regionFile != null) {
            return regionFile;
        }
        if (this.regionCache.size() >= 256) {
            this.regionCache.removeLast().close();
        }
        FileUtil.createDirectoriesSafe(this.folder);
        Path path = this.folder.resolve("r." + chunkPos.getRegionX() + "." + chunkPos.getRegionZ() + ANVIL_EXTENSION);
        RegionFile regionFile2 = new RegionFile(path, this.folder, this.sync);
        this.regionCache.putAndMoveToFirst(l, regionFile2);
        return regionFile2;
    }

    @Nullable
    public CompoundTag read(ChunkPos chunkPos) throws IOException {
        RegionFile regionFile = this.getRegionFile(chunkPos);
        try (DataInputStream dataInputStream = regionFile.getChunkDataInputStream(chunkPos);){
            if (dataInputStream == null) {
                CompoundTag compoundTag = null;
                return compoundTag;
            }
            CompoundTag compoundTag = NbtIo.read(dataInputStream);
            return compoundTag;
        }
    }

    public void scanChunk(ChunkPos chunkPos, StreamTagVisitor streamTagVisitor) throws IOException {
        RegionFile regionFile = this.getRegionFile(chunkPos);
        try (DataInputStream dataInputStream = regionFile.getChunkDataInputStream(chunkPos);){
            if (dataInputStream != null) {
                NbtIo.parse(dataInputStream, streamTagVisitor);
            }
        }
    }

    protected void write(ChunkPos chunkPos, @Nullable CompoundTag compoundTag) throws IOException {
        RegionFile regionFile = this.getRegionFile(chunkPos);
        if (compoundTag == null) {
            regionFile.clear(chunkPos);
        } else {
            try (DataOutputStream dataOutputStream = regionFile.getChunkDataOutputStream(chunkPos);){
                NbtIo.write(compoundTag, dataOutputStream);
            }
        }
    }

    @Override
    public void close() throws IOException {
        ExceptionCollector<IOException> exceptionCollector = new ExceptionCollector<IOException>();
        for (RegionFile regionFile : this.regionCache.values()) {
            try {
                regionFile.close();
            } catch (IOException iOException) {
                exceptionCollector.add(iOException);
            }
        }
        exceptionCollector.throwIfPresent();
    }

    public void flush() throws IOException {
        for (RegionFile regionFile : this.regionCache.values()) {
            regionFile.flush();
        }
    }
}

