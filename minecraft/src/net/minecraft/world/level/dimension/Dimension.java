package net.minecraft.world.level.dimension;

import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelType;
import net.minecraft.world.level.border.WorldBorder;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.storage.ServerLevelData;
import net.minecraft.world.phys.Vec3;

public abstract class Dimension {
	public static final float[] MOON_BRIGHTNESS_PER_PHASE = new float[]{1.0F, 0.75F, 0.5F, 0.25F, 0.0F, 0.25F, 0.5F, 0.75F};
	protected final Level level;
	private final DimensionType type;
	protected boolean ultraWarm;
	protected boolean hasCeiling;
	protected final float[] brightnessRamp = new float[16];
	private final float[] sunriseCol = new float[4];

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

	@Nullable
	@Environment(EnvType.CLIENT)
	public float[] getSunriseColor(float f, float g) {
		float h = 0.4F;
		float i = Mth.cos(f * (float) (Math.PI * 2)) - 0.0F;
		float j = -0.0F;
		if (i >= -0.4F && i <= 0.4F) {
			float k = (i - -0.0F) / 0.4F * 0.5F + 0.5F;
			float l = 1.0F - (1.0F - Mth.sin(k * (float) Math.PI)) * 0.99F;
			l *= l;
			this.sunriseCol[0] = k * 0.3F + 0.7F;
			this.sunriseCol[1] = k * k * 0.7F + 0.2F;
			this.sunriseCol[2] = k * k * 0.0F + 0.2F;
			this.sunriseCol[3] = l;
			return this.sunriseCol;
		} else {
			return null;
		}
	}

	@Environment(EnvType.CLIENT)
	public float getCloudHeight() {
		return 128.0F;
	}

	@Environment(EnvType.CLIENT)
	public boolean hasGround() {
		return true;
	}

	@Nullable
	public BlockPos getDimensionSpecificSpawn() {
		return null;
	}

	@Environment(EnvType.CLIENT)
	public double getClearColorScale() {
		return this.level.getLevelData().getGeneratorType() == LevelType.FLAT ? 1.0 : 0.03125;
	}

	public boolean isUltraWarm() {
		return this.ultraWarm;
	}

	public boolean isHasSkyLight() {
		return this.type.hasSkyLight();
	}

	public boolean isHasCeiling() {
		return this.hasCeiling;
	}

	public float getBrightness(int i) {
		return this.brightnessRamp[i];
	}

	public WorldBorder createWorldBorder() {
		return new WorldBorder();
	}

	public void saveData(ServerLevelData serverLevelData) {
	}

	public void tick() {
	}

	public abstract ChunkGenerator<?> createRandomLevelGenerator();

	@Nullable
	public abstract BlockPos getSpawnPosInChunk(ChunkPos chunkPos, boolean bl);

	@Nullable
	public abstract BlockPos getValidSpawnPosition(int i, int j, boolean bl);

	public abstract float getTimeOfDay(long l, float f);

	public abstract boolean isNaturalDimension();

	@Environment(EnvType.CLIENT)
	public abstract Vec3 getBrightnessDependentFogColor(Vec3 vec3, float f);

	public abstract boolean mayRespawn();

	@Environment(EnvType.CLIENT)
	public abstract boolean isFoggyAt(int i, int j);

	public abstract DimensionType getType();
}
