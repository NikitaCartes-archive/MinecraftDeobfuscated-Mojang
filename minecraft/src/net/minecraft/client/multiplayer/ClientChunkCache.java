package net.minecraft.client.multiplayer;

import java.util.concurrent.atomic.AtomicReferenceArray;
import java.util.function.BooleanSupplier;
import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.chunk.ChunkBiomeContainer;
import net.minecraft.world.level.chunk.ChunkSource;
import net.minecraft.world.level.chunk.ChunkStatus;
import net.minecraft.world.level.chunk.EmptyLevelChunk;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.lighting.LevelLightEngine;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Environment(EnvType.CLIENT)
public class ClientChunkCache extends ChunkSource {
	private static final Logger LOGGER = LogManager.getLogger();
	private final LevelChunk emptyChunk;
	private final LevelLightEngine lightEngine;
	private volatile ClientChunkCache.Storage storage;
	private final ClientLevel level;

	public ClientChunkCache(ClientLevel clientLevel, int i) {
		this.level = clientLevel;
		this.emptyChunk = new EmptyLevelChunk(clientLevel, new ChunkPos(0, 0));
		this.lightEngine = new LevelLightEngine(this, true, clientLevel.getDimension().isHasSkyLight());
		this.storage = new ClientChunkCache.Storage(calculateStorageRange(i));
	}

	@Override
	public LevelLightEngine getLightEngine() {
		return this.lightEngine;
	}

	private static boolean isValidChunk(@Nullable LevelChunk levelChunk, int i, int j) {
		if (levelChunk == null) {
			return false;
		} else {
			ChunkPos chunkPos = levelChunk.getPos();
			return chunkPos.x == i && chunkPos.z == j;
		}
	}

	public void drop(int i, int j) {
		if (this.storage.inRange(i, j)) {
			int k = this.storage.getIndex(i, j);
			LevelChunk levelChunk = this.storage.getChunk(k);
			if (isValidChunk(levelChunk, i, j)) {
				this.storage.replace(k, levelChunk, null);
			}
		}
	}

	@Nullable
	public LevelChunk getChunk(int i, int j, ChunkStatus chunkStatus, boolean bl) {
		if (this.storage.inRange(i, j)) {
			LevelChunk levelChunk = this.storage.getChunk(this.storage.getIndex(i, j));
			if (isValidChunk(levelChunk, i, j)) {
				return levelChunk;
			}
		}

		return bl ? this.emptyChunk : null;
	}

	@Override
	public BlockGetter getLevel() {
		return this.level;
	}

	@Nullable
	public LevelChunk replaceWithPacketData(
		int i, int j, @Nullable ChunkBiomeContainer chunkBiomeContainer, FriendlyByteBuf friendlyByteBuf, CompoundTag compoundTag, int k, boolean bl
	) {
		if (!this.storage.inRange(i, j)) {
			LOGGER.warn("Ignoring chunk since it's not in the view range: {}, {}", i, j);
			return null;
		} else {
			int l = this.storage.getIndex(i, j);
			LevelChunk levelChunk = (LevelChunk)this.storage.chunks.get(l);
			if (!bl && isValidChunk(levelChunk, i, j)) {
				levelChunk.replaceWithPacketData(chunkBiomeContainer, friendlyByteBuf, compoundTag, k);
			} else {
				if (chunkBiomeContainer == null) {
					LOGGER.warn("Ignoring chunk since we don't have complete data: {}, {}", i, j);
					return null;
				}

				levelChunk = new LevelChunk(this.level, new ChunkPos(i, j), chunkBiomeContainer);
				levelChunk.replaceWithPacketData(chunkBiomeContainer, friendlyByteBuf, compoundTag, k);
				this.storage.replace(l, levelChunk);
			}

			LevelChunkSection[] levelChunkSections = levelChunk.getSections();
			LevelLightEngine levelLightEngine = this.getLightEngine();
			levelLightEngine.enableLightSources(new ChunkPos(i, j), true);

			for (int m = 0; m < levelChunkSections.length; m++) {
				LevelChunkSection levelChunkSection = levelChunkSections[m];
				levelLightEngine.updateSectionStatus(SectionPos.of(i, m, j), LevelChunkSection.isEmpty(levelChunkSection));
			}

			this.level.onChunkLoaded(i, j);
			return levelChunk;
		}
	}

	@Override
	public void tick(BooleanSupplier booleanSupplier) {
	}

	public void updateViewCenter(int i, int j) {
		this.storage.viewCenterX = i;
		this.storage.viewCenterZ = j;
	}

	public void updateViewRadius(int i) {
		int j = this.storage.chunkRadius;
		int k = calculateStorageRange(i);
		if (j != k) {
			ClientChunkCache.Storage storage = new ClientChunkCache.Storage(k);
			storage.viewCenterX = this.storage.viewCenterX;
			storage.viewCenterZ = this.storage.viewCenterZ;

			for (int l = 0; l < this.storage.chunks.length(); l++) {
				LevelChunk levelChunk = (LevelChunk)this.storage.chunks.get(l);
				if (levelChunk != null) {
					ChunkPos chunkPos = levelChunk.getPos();
					if (storage.inRange(chunkPos.x, chunkPos.z)) {
						storage.replace(storage.getIndex(chunkPos.x, chunkPos.z), levelChunk);
					}
				}
			}

			this.storage = storage;
		}
	}

	private static int calculateStorageRange(int i) {
		return Math.max(2, i) + 3;
	}

	@Override
	public String gatherStats() {
		return "Client Chunk Cache: " + this.storage.chunks.length() + ", " + this.getLoadedChunksCount();
	}

	public int getLoadedChunksCount() {
		return this.storage.chunkCount;
	}

	@Override
	public void onLightUpdate(LightLayer lightLayer, SectionPos sectionPos) {
		Minecraft.getInstance().levelRenderer.setSectionDirty(sectionPos.x(), sectionPos.y(), sectionPos.z());
	}

	@Override
	public boolean isTickingChunk(BlockPos blockPos) {
		return this.hasChunk(blockPos.getX() >> 4, blockPos.getZ() >> 4);
	}

	@Override
	public boolean isEntityTickingChunk(ChunkPos chunkPos) {
		return this.hasChunk(chunkPos.x, chunkPos.z);
	}

	@Override
	public boolean isEntityTickingChunk(Entity entity) {
		return this.hasChunk(Mth.floor(entity.getX()) >> 4, Mth.floor(entity.getZ()) >> 4);
	}

	@Environment(EnvType.CLIENT)
	final class Storage {
		private final AtomicReferenceArray<LevelChunk> chunks;
		private final int chunkRadius;
		private final int viewRange;
		private volatile int viewCenterX;
		private volatile int viewCenterZ;
		private int chunkCount;

		private Storage(int i) {
			this.chunkRadius = i;
			this.viewRange = i * 2 + 1;
			this.chunks = new AtomicReferenceArray(this.viewRange * this.viewRange);
		}

		private int getIndex(int i, int j) {
			return Math.floorMod(j, this.viewRange) * this.viewRange + Math.floorMod(i, this.viewRange);
		}

		protected void replace(int i, @Nullable LevelChunk levelChunk) {
			LevelChunk levelChunk2 = (LevelChunk)this.chunks.getAndSet(i, levelChunk);
			if (levelChunk2 != null) {
				this.chunkCount--;
				ClientChunkCache.this.level.unload(levelChunk2);
			}

			if (levelChunk != null) {
				this.chunkCount++;
			}
		}

		protected LevelChunk replace(int i, LevelChunk levelChunk, @Nullable LevelChunk levelChunk2) {
			if (this.chunks.compareAndSet(i, levelChunk, levelChunk2) && levelChunk2 == null) {
				this.chunkCount--;
			}

			ClientChunkCache.this.level.unload(levelChunk);
			return levelChunk;
		}

		private boolean inRange(int i, int j) {
			return Math.abs(i - this.viewCenterX) <= this.chunkRadius && Math.abs(j - this.viewCenterZ) <= this.chunkRadius;
		}

		@Nullable
		protected LevelChunk getChunk(int i) {
			return (LevelChunk)this.chunks.get(i);
		}
	}
}
