package net.minecraft.world.level.dimension;

import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.border.WorldBorder;
import net.minecraft.world.level.storage.ServerLevelData;

public abstract class Dimension {
	public static final float[] MOON_BRIGHTNESS_PER_PHASE = new float[]{1.0F, 0.75F, 0.5F, 0.25F, 0.0F, 0.25F, 0.5F, 0.75F};
	protected final Level level;
	private final DimensionType type;
	protected final float[] brightnessRamp = new float[16];

	public Dimension(Level level, DimensionType dimensionType, float f) {
		this.level = level;
		this.type = dimensionType;

		for (int i = 0; i <= 15; i++) {
			float g = (float)i / 15.0F;
			float h = g / (4.0F - 3.0F * g);
			this.brightnessRamp[i] = Mth.lerp(f, h, 1.0F);
		}
	}

	public int getMoonPhase(long l) {
		return (int)(l / 24000L % 8L + 8L) % 8;
	}

	public float getBrightness(int i) {
		return this.brightnessRamp[i];
	}

	public abstract float getTimeOfDay(long l, float f);

	public WorldBorder createWorldBorder() {
		return new WorldBorder();
	}

	public abstract DimensionType getType();

	@Nullable
	public BlockPos getDimensionSpecificSpawn() {
		return null;
	}

	public void saveData(ServerLevelData serverLevelData) {
	}

	public void tick() {
	}

	@Nullable
	public abstract BlockPos getSpawnPosInChunk(long l, ChunkPos chunkPos, boolean bl);

	@Nullable
	public abstract BlockPos getValidSpawnPosition(long l, int i, int j, boolean bl);

	public abstract boolean isNaturalDimension();

	public abstract boolean mayRespawn();
}
