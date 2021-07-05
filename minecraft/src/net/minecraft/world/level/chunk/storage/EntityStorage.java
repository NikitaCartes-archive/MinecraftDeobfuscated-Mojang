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
import net.minecraft.util.thread.ProcessorMailbox;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.entity.ChunkEntities;
import net.minecraft.world.level.entity.EntityPersistentStorage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class EntityStorage implements EntityPersistentStorage<Entity> {
	private static final Logger LOGGER = LogManager.getLogger();
	private static final String ENTITIES_TAG = "Entities";
	private static final String POSITION_TAG = "Position";
	private final ServerLevel level;
	private final IOWorker worker;
	private final LongSet emptyChunks = new LongOpenHashSet();
	private final ProcessorMailbox<Runnable> entityDeserializerQueue;
	protected final DataFixer fixerUpper;

	public EntityStorage(ServerLevel serverLevel, File file, DataFixer dataFixer, boolean bl, Executor executor) {
		this.level = serverLevel;
		this.fixerUpper = dataFixer;
		this.entityDeserializerQueue = ProcessorMailbox.create(executor, "entity-deserializer");
		this.worker = new IOWorker(file, bl, "entities");
	}

	@Override
	public CompletableFuture<ChunkEntities<Entity>> loadEntities(ChunkPos chunkPos) {
		return this.emptyChunks.contains(chunkPos.toLong())
			? CompletableFuture.completedFuture(emptyChunk(chunkPos))
			: this.worker.loadAsync(chunkPos).thenApplyAsync(compoundTag -> {
				if (compoundTag == null) {
					this.emptyChunks.add(chunkPos.toLong());
					return emptyChunk(chunkPos);
				} else {
					try {
						ChunkPos chunkPos2 = readChunkPos(compoundTag);
						if (!Objects.equals(chunkPos, chunkPos2)) {
							LOGGER.error("Chunk file at {} is in the wrong location. (Expected {}, got {})", chunkPos, chunkPos, chunkPos2);
						}
					} catch (Exception var6) {
						LOGGER.warn("Failed to parse chunk {} position info", chunkPos, var6);
					}

					CompoundTag compoundTag2 = this.upgradeChunkTag(compoundTag);
					ListTag listTag = compoundTag2.getList("Entities", 10);
					List<Entity> list = (List<Entity>)EntityType.loadEntitiesRecursive(listTag, this.level).collect(ImmutableList.toImmutableList());
					return new ChunkEntities(chunkPos, list);
				}
			}, this.entityDeserializerQueue::tell);
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
				this.worker.store(chunkPos, null);
			}
		} else {
			ListTag listTag = new ListTag();
			chunkEntities.getEntities().forEach(entity -> {
				CompoundTag compoundTagx = new CompoundTag();
				if (entity.save(compoundTagx)) {
					listTag.add(compoundTagx);
				}
			});
			CompoundTag compoundTag = new CompoundTag();
			compoundTag.putInt("DataVersion", SharedConstants.getCurrentVersion().getWorldVersion());
			compoundTag.put("Entities", listTag);
			writeChunkPos(compoundTag, chunkPos);
			this.worker.store(chunkPos, compoundTag).exceptionally(throwable -> {
				LOGGER.error("Failed to store chunk {}", chunkPos, throwable);
				return null;
			});
			this.emptyChunks.remove(chunkPos.toLong());
		}
	}

	@Override
	public void flush(boolean bl) {
		this.worker.synchronize(bl).join();
		this.entityDeserializerQueue.runAll();
	}

	private CompoundTag upgradeChunkTag(CompoundTag compoundTag) {
		int i = getVersion(compoundTag);
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
