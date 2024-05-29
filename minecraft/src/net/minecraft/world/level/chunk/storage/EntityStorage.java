package net.minecraft.world.level.chunk.storage;

import com.google.common.collect.ImmutableList;
import com.mojang.logging.LogUtils;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.IntArrayTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.thread.ProcessorMailbox;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.entity.ChunkEntities;
import net.minecraft.world.level.entity.EntityPersistentStorage;
import org.slf4j.Logger;

public class EntityStorage implements EntityPersistentStorage<Entity> {
	private static final Logger LOGGER = LogUtils.getLogger();
	private static final String ENTITIES_TAG = "Entities";
	private static final String POSITION_TAG = "Position";
	private final ServerLevel level;
	private final SimpleRegionStorage simpleRegionStorage;
	private final LongSet emptyChunks = new LongOpenHashSet();
	private final ProcessorMailbox<Runnable> entityDeserializerQueue;

	public EntityStorage(SimpleRegionStorage simpleRegionStorage, ServerLevel serverLevel, Executor executor) {
		this.simpleRegionStorage = simpleRegionStorage;
		this.level = serverLevel;
		this.entityDeserializerQueue = ProcessorMailbox.create(executor, "entity-deserializer");
	}

	@Override
	public CompletableFuture<ChunkEntities<Entity>> loadEntities(ChunkPos chunkPos) {
		if (this.emptyChunks.contains(chunkPos.toLong())) {
			return CompletableFuture.completedFuture(emptyChunk(chunkPos));
		} else {
			CompletableFuture<Optional<CompoundTag>> completableFuture = this.simpleRegionStorage.read(chunkPos);
			this.reportLoadFailureIfPresent(completableFuture, chunkPos);
			return completableFuture.thenApplyAsync(optional -> {
				if (optional.isEmpty()) {
					this.emptyChunks.add(chunkPos.toLong());
					return emptyChunk(chunkPos);
				} else {
					try {
						ChunkPos chunkPos2 = readChunkPos((CompoundTag)optional.get());
						if (!Objects.equals(chunkPos, chunkPos2)) {
							LOGGER.error("Chunk file at {} is in the wrong location. (Expected {}, got {})", chunkPos, chunkPos, chunkPos2);
							this.level.getServer().reportMisplacedChunk(chunkPos2, chunkPos, this.simpleRegionStorage.storageInfo());
						}
					} catch (Exception var6) {
						LOGGER.warn("Failed to parse chunk {} position info", chunkPos, var6);
						this.level.getServer().reportChunkLoadFailure(var6, this.simpleRegionStorage.storageInfo(), chunkPos);
					}

					CompoundTag compoundTag = this.simpleRegionStorage.upgradeChunkTag((CompoundTag)optional.get(), -1);
					ListTag listTag = compoundTag.getList("Entities", 10);
					List<Entity> list = (List<Entity>)EntityType.loadEntitiesRecursive(listTag, this.level).collect(ImmutableList.toImmutableList());
					return new ChunkEntities(chunkPos, list);
				}
			}, this.entityDeserializerQueue::tell);
		}
	}

	private static ChunkPos readChunkPos(CompoundTag compoundTag) {
		int[] is = compoundTag.getIntArray("Position");
		return new ChunkPos(is[0], is[1]);
	}

	private static void writeChunkPos(CompoundTag compoundTag, ChunkPos chunkPos) {
		compoundTag.put("Position", new IntArrayTag(new int[]{chunkPos.x, chunkPos.z}));
	}

	private static ChunkEntities<Entity> emptyChunk(ChunkPos chunkPos) {
		return new ChunkEntities<>(chunkPos, ImmutableList.of());
	}

	@Override
	public void storeEntities(ChunkEntities<Entity> chunkEntities) {
		ChunkPos chunkPos = chunkEntities.getPos();
		if (chunkEntities.isEmpty()) {
			if (this.emptyChunks.add(chunkPos.toLong())) {
				this.reportSaveFailureIfPresent(this.simpleRegionStorage.write(chunkPos, null), chunkPos);
			}
		} else {
			ListTag listTag = new ListTag();
			chunkEntities.getEntities().forEach(entity -> {
				CompoundTag compoundTagx = new CompoundTag();
				if (entity.save(compoundTagx)) {
					listTag.add(compoundTagx);
				}
			});
			CompoundTag compoundTag = NbtUtils.addCurrentDataVersion(new CompoundTag());
			compoundTag.put("Entities", listTag);
			writeChunkPos(compoundTag, chunkPos);
			this.reportSaveFailureIfPresent(this.simpleRegionStorage.write(chunkPos, compoundTag), chunkPos);
			this.emptyChunks.remove(chunkPos.toLong());
		}
	}

	private void reportSaveFailureIfPresent(CompletableFuture<?> completableFuture, ChunkPos chunkPos) {
		completableFuture.exceptionally(throwable -> {
			LOGGER.error("Failed to store entity chunk {}", chunkPos, throwable);
			this.level.getServer().reportChunkSaveFailure(throwable, this.simpleRegionStorage.storageInfo(), chunkPos);
			return null;
		});
	}

	private void reportLoadFailureIfPresent(CompletableFuture<?> completableFuture, ChunkPos chunkPos) {
		completableFuture.exceptionally(throwable -> {
			LOGGER.error("Failed to load entity chunk {}", chunkPos, throwable);
			this.level.getServer().reportChunkLoadFailure(throwable, this.simpleRegionStorage.storageInfo(), chunkPos);
			return null;
		});
	}

	@Override
	public void flush(boolean bl) {
		this.simpleRegionStorage.synchronize(bl).join();
		this.entityDeserializerQueue.runAll();
	}

	@Override
	public void close() throws IOException {
		this.simpleRegionStorage.close();
	}
}
