package net.minecraft.world.level.chunk;

import java.io.IOException;
import java.util.function.BooleanSupplier;
import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.lighting.LevelLightEngine;

public abstract class ChunkSource implements LightChunkGetter, AutoCloseable {
	@Nullable
	public LevelChunk getChunk(int i, int j, boolean bl) {
		return (LevelChunk)this.getChunk(i, j, ChunkStatus.FULL, bl);
	}

	@Nullable
	public LevelChunk getChunkNow(int i, int j) {
		return this.getChunk(i, j, false);
	}

	@Nullable
	@Override
	public BlockGetter getChunkForLighting(int i, int j) {
		return this.getChunk(i, j, ChunkStatus.EMPTY, false);
	}

	public boolean hasChunk(int i, int j) {
		return this.getChunk(i, j, ChunkStatus.FULL, false) != null;
	}

	@Nullable
	public abstract ChunkAccess getChunk(int i, int j, ChunkStatus chunkStatus, boolean bl);

	@Environment(EnvType.CLIENT)
	public abstract void tick(BooleanSupplier booleanSupplier);

	public abstract String gatherStats();

	public abstract ChunkGenerator<?> getGenerator();

	public void close() throws IOException {
	}

	public abstract LevelLightEngine getLightEngine();

	public void setSpawnSettings(boolean bl, boolean bl2) {
	}

	public void updateChunkForced(ChunkPos chunkPos, boolean bl) {
	}

	public boolean isEntityTickingChunk(Entity entity) {
		return true;
	}

	public boolean isEntityTickingChunk(ChunkPos chunkPos) {
		return true;
	}

	public boolean isTickingChunk(BlockPos blockPos) {
		return true;
	}
}
