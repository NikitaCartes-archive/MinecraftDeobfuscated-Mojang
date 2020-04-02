/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.chunk.storage;

import it.unimi.dsi.fastutil.longs.Long2ObjectLinkedOpenHashMap;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.util.ExceptionCollector;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.storage.RegionFile;
import org.jetbrains.annotations.Nullable;

public final class RegionFileStorage
implements AutoCloseable {
    private final Long2ObjectLinkedOpenHashMap<RegionFile> regionCache = new Long2ObjectLinkedOpenHashMap();
    private final File folder;
    private final boolean sync;

    RegionFileStorage(File file, boolean bl) {
        this.folder = file;
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
        if (!this.folder.exists()) {
            this.folder.mkdirs();
        }
        File file = new File(this.folder, "r." + chunkPos.getRegionX() + "." + chunkPos.getRegionZ() + ".mca");
        RegionFile regionFile2 = new RegionFile(file, this.folder, this.sync);
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

    protected void write(ChunkPos chunkPos, CompoundTag compoundTag) throws IOException {
        RegionFile regionFile = this.getRegionFile(chunkPos);
        try (DataOutputStream dataOutputStream = regionFile.getChunkDataOutputStream(chunkPos);){
            NbtIo.write(compoundTag, dataOutputStream);
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

