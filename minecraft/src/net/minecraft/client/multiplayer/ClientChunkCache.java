package net.minecraft.client.multiplayer;

import com.mojang.logging.LogUtils;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicReferenceArray;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.core.Registry;
import net.minecraft.core.SectionPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.game.ClientboundLevelChunkPacketData;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.chunk.ChunkSource;
import net.minecraft.world.level.chunk.ChunkStatus;
import net.minecraft.world.level.chunk.EmptyLevelChunk;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.lighting.LevelLightEngine;
import org.slf4j.Logger;

@Environment(EnvType.CLIENT)
public class ClientChunkCache extends ChunkSource {
	static final Logger LOGGER = LogUtils.getLogger();
	private final LevelChunk emptyChunk;
	private final LevelLightEngine lightEngine;
	volatile ClientChunkCache.Storage storage;
	final ClientLevel level;

	public ClientChunkCache(ClientLevel clientLevel, int i) {
		this.level = clientLevel;
		this.emptyChunk = new EmptyLevelChunk(
			clientLevel, new ChunkPos(0, 0), clientLevel.registryAccess().registryOrThrow(Registry.BIOME_REGISTRY).getHolderOrThrow(Biomes.PLAINS)
		);
		this.lightEngine = new LevelLightEngine(this, true, clientLevel.dimensionType().hasSkyLight());
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
		int i, int j, FriendlyByteBuf friendlyByteBuf, CompoundTag compoundTag, Consumer<ClientboundLevelChunkPacketData.BlockEntityTagOutput> consumer
	) {
		if (!this.storage.inRange(i, j)) {
			LOGGER.warn("Ignoring chunk since it's not in the view range: {}, {}", i, j);
			return null;
		} else {
			int k = this.storage.getIndex(i, j);
			LevelChunk levelChunk = (LevelChunk)this.storage.chunks.get(k);
			ChunkPos chunkPos = new ChunkPos(i, j);
			if (!isValidChunk(levelChunk, i, j)) {
				levelChunk = new LevelChunk(this.level, chunkPos);
				levelChunk.replaceWithPacketData(friendlyByteBuf, compoundTag, consumer);
				this.storage.replace(k, levelChunk);
			} else {
				levelChunk.replaceWithPacketData(friendlyByteBuf, compoundTag, consumer);
			}

			this.level.onChunkLoaded(chunkPos);
			return levelChunk;
		}
	}

	@Override
	public void tick(BooleanSupplier booleanSupplier, boolean bl) {
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
		return this.storage.chunks.length() + ", " + this.getLoadedChunksCount();
	}

	@Override
	public int getLoadedChunksCount() {
		return this.storage.chunkCount;
	}

	@Override
	public void onLightUpdate(LightLayer lightLayer, SectionPos sectionPos) {
		Minecraft.getInstance().levelRenderer.setSectionDirty(sectionPos.x(), sectionPos.y(), sectionPos.z());
	}

	@Environment(EnvType.CLIENT)
	final class Storage {
		final AtomicReferenceArray<LevelChunk> chunks;
		final int chunkRadius;
		private final int viewRange;
		volatile int viewCenterX;
		volatile int viewCenterZ;
		int chunkCount;

		Storage(int i) {
			this.chunkRadius = i;
			this.viewRange = i * 2 + 1;
			this.chunks = new AtomicReferenceArray(this.viewRange * this.viewRange);
		}

		int getIndex(int i, int j) {
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

		boolean inRange(int i, int j) {
			return Math.abs(i - this.viewCenterX) <= this.chunkRadius && Math.abs(j - this.viewCenterZ) <= this.chunkRadius;
		}

		@Nullable
		protected LevelChunk getChunk(int i) {
			return (LevelChunk)this.chunks.get(i);
		}

		private void dumpChunks(String string) {
			try {
				FileOutputStream fileOutputStream = new FileOutputStream(string);

				try {
					int i = ClientChunkCache.this.storage.chunkRadius;

					for (int j = this.viewCenterZ - i; j <= this.viewCenterZ + i; j++) {
						for (int k = this.viewCenterX - i; k <= this.viewCenterX + i; k++) {
							LevelChunk levelChunk = (LevelChunk)ClientChunkCache.this.storage.chunks.get(ClientChunkCache.this.storage.getIndex(k, j));
							if (levelChunk != null) {
								ChunkPos chunkPos = levelChunk.getPos();
								fileOutputStream.write((chunkPos.x + "\t" + chunkPos.z + "\t" + levelChunk.isEmpty() + "\n").getBytes(StandardCharsets.UTF_8));
							}
						}
					}
				} catch (Throwable var9) {
					try {
						fileOutputStream.close();
					} catch (Throwable var8) {
						var9.addSuppressed(var8);
					}

					throw var9;
				}

				fileOutputStream.close();
			} catch (IOException var10) {
				ClientChunkCache.LOGGER.error("Failed to dump chunks to file {}", string, var10);
			}
		}
	}
}
