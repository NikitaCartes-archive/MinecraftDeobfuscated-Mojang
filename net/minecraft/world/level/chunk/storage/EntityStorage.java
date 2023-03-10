/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.chunk.storage;

import com.google.common.collect.ImmutableList;
import com.mojang.datafixers.DataFixer;
import com.mojang.logging.LogUtils;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.IntArrayTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.util.thread.ProcessorMailbox;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.storage.IOWorker;
import net.minecraft.world.level.entity.ChunkEntities;
import net.minecraft.world.level.entity.EntityPersistentStorage;
import org.slf4j.Logger;

public class EntityStorage
implements EntityPersistentStorage<Entity> {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final String ENTITIES_TAG = "Entities";
    private static final String POSITION_TAG = "Position";
    private final ServerLevel level;
    private final IOWorker worker;
    private final LongSet emptyChunks = new LongOpenHashSet();
    private final ProcessorMailbox<Runnable> entityDeserializerQueue;
    protected final DataFixer fixerUpper;

    public EntityStorage(ServerLevel serverLevel, Path path, DataFixer dataFixer, boolean bl, Executor executor) {
        this.level = serverLevel;
        this.fixerUpper = dataFixer;
        this.entityDeserializerQueue = ProcessorMailbox.create(executor, "entity-deserializer");
        this.worker = new IOWorker(path, bl, "entities");
    }

    @Override
    public CompletableFuture<ChunkEntities<Entity>> loadEntities(ChunkPos chunkPos) {
        if (this.emptyChunks.contains(chunkPos.toLong())) {
            return CompletableFuture.completedFuture(EntityStorage.emptyChunk(chunkPos));
        }
        return this.worker.loadAsync(chunkPos).thenApplyAsync(optional -> {
            if (optional.isEmpty()) {
                this.emptyChunks.add(chunkPos.toLong());
                return EntityStorage.emptyChunk(chunkPos);
            }
            try {
                ChunkPos chunkPos2 = EntityStorage.readChunkPos((CompoundTag)optional.get());
                if (!Objects.equals(chunkPos, chunkPos2)) {
                    LOGGER.error("Chunk file at {} is in the wrong location. (Expected {}, got {})", chunkPos, chunkPos, chunkPos2);
                }
            } catch (Exception exception) {
                LOGGER.warn("Failed to parse chunk {} position info", (Object)chunkPos, (Object)exception);
            }
            CompoundTag compoundTag = this.upgradeChunkTag((CompoundTag)optional.get());
            ListTag listTag = compoundTag.getList(ENTITIES_TAG, 10);
            List list = EntityType.loadEntitiesRecursive(listTag, this.level).collect(ImmutableList.toImmutableList());
            return new ChunkEntities(chunkPos, list);
        }, this.entityDeserializerQueue::tell);
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
        CompoundTag compoundTag = NbtUtils.addCurrentDataVersion(new CompoundTag());
        compoundTag.put(ENTITIES_TAG, listTag);
        EntityStorage.writeChunkPos(compoundTag, chunkPos);
        this.worker.store(chunkPos, compoundTag).exceptionally(throwable -> {
            LOGGER.error("Failed to store chunk {}", (Object)chunkPos, throwable);
            return null;
        });
        this.emptyChunks.remove(chunkPos.toLong());
    }

    @Override
    public void flush(boolean bl) {
        this.worker.synchronize(bl).join();
        this.entityDeserializerQueue.runAll();
    }

    private CompoundTag upgradeChunkTag(CompoundTag compoundTag) {
        int i = NbtUtils.getDataVersion(compoundTag, -1);
        return DataFixTypes.ENTITY_CHUNK.updateToCurrentVersion(this.fixerUpper, compoundTag, i);
    }

    @Override
    public void close() throws IOException {
        this.worker.close();
    }
}

