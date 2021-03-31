/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.chunk.storage;

import com.google.common.collect.ImmutableList;
import com.mojang.datafixers.DataFixer;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import net.minecraft.SharedConstants;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.IntArrayTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.storage.IOWorker;
import net.minecraft.world.level.entity.ChunkEntities;
import net.minecraft.world.level.entity.EntityPersistentStorage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class EntityStorage
implements EntityPersistentStorage<Entity> {
    private static final Logger LOGGER = LogManager.getLogger();
    private static final String ENTITIES_TAG = "Entities";
    private static final String POSITION_TAG = "Position";
    private final ServerLevel level;
    private final IOWorker worker;
    private final LongSet emptyChunks = new LongOpenHashSet();
    private final Executor mainThreadExecutor;
    protected final DataFixer fixerUpper;

    public EntityStorage(ServerLevel serverLevel, File file, DataFixer dataFixer, boolean bl, Executor executor) {
        this.level = serverLevel;
        this.fixerUpper = dataFixer;
        this.mainThreadExecutor = executor;
        this.worker = new IOWorker(file, bl, "entities");
    }

    @Override
    public CompletableFuture<ChunkEntities<Entity>> loadEntities(ChunkPos chunkPos) {
        if (this.emptyChunks.contains(chunkPos.toLong())) {
            return CompletableFuture.completedFuture(EntityStorage.emptyChunk(chunkPos));
        }
        return this.worker.loadAsync(chunkPos).thenApplyAsync(compoundTag -> {
            if (compoundTag == null) {
                this.emptyChunks.add(chunkPos.toLong());
                return EntityStorage.emptyChunk(chunkPos);
            }
            try {
                ChunkPos chunkPos2 = EntityStorage.readChunkPos(compoundTag);
                if (!Objects.equals(chunkPos, chunkPos2)) {
                    LOGGER.error("Chunk file at {} is in the wrong location. (Expected {}, got {})", (Object)chunkPos, (Object)chunkPos, (Object)chunkPos2);
                }
            } catch (Exception exception) {
                LOGGER.warn("Failed to parse chunk {} position info", (Object)chunkPos, (Object)exception);
            }
            CompoundTag compoundTag2 = this.upgradeChunkTag((CompoundTag)compoundTag);
            ListTag listTag = compoundTag2.getList(ENTITIES_TAG, 10);
            List list = EntityType.loadEntitiesRecursive(listTag, this.level).collect(ImmutableList.toImmutableList());
            return new ChunkEntities(chunkPos, list);
        }, this.mainThreadExecutor);
    }

    private static ChunkPos readChunkPos(CompoundTag compoundTag) {
        int[] is = compoundTag.getIntArray(POSITION_TAG);
        return new ChunkPos(is[0], is[1]);
    }

    private static void writeChunkPos(CompoundTag compoundTag, ChunkPos chunkPos) {
        compoundTag.put(POSITION_TAG, new IntArrayTag(new int[]{chunkPos.x, chunkPos.z}));
    }

    private static ChunkEntities<Entity> emptyChunk(ChunkPos chunkPos) {
        return new ChunkEntities<Entity>(chunkPos, ImmutableList.of());
    }

    @Override
    public void storeEntities(ChunkEntities<Entity> chunkEntities) {
        ChunkPos chunkPos = chunkEntities.getPos();
        if (chunkEntities.isEmpty()) {
            if (this.emptyChunks.add(chunkPos.toLong())) {
                this.worker.store(chunkPos, null);
            }
            return;
        }
        ListTag listTag = new ListTag();
        chunkEntities.getEntities().forEach(entity -> {
            CompoundTag compoundTag = new CompoundTag();
            if (entity.save(compoundTag)) {
                listTag.add(compoundTag);
            }
        });
        CompoundTag compoundTag = new CompoundTag();
        compoundTag.putInt("DataVersion", SharedConstants.getCurrentVersion().getWorldVersion());
        compoundTag.put(ENTITIES_TAG, listTag);
        EntityStorage.writeChunkPos(compoundTag, chunkPos);
        this.worker.store(chunkPos, compoundTag).exceptionally(throwable -> {
            LOGGER.error("Failed to store chunk {}", (Object)chunkPos, throwable);
            return null;
        });
        this.emptyChunks.remove(chunkPos.toLong());
    }

    @Override
    public void flush() {
        this.worker.synchronize().join();
    }

    private CompoundTag upgradeChunkTag(CompoundTag compoundTag) {
        int i = EntityStorage.getVersion(compoundTag);
        return NbtUtils.update(this.fixerUpper, DataFixTypes.ENTITY_CHUNK, compoundTag, i);
    }

    public static int getVersion(CompoundTag compoundTag) {
        return compoundTag.contains("DataVersion", 99) ? compoundTag.getInt("DataVersion") : -1;
    }

    @Override
    public void close() throws IOException {
        this.worker.close();
    }
}

