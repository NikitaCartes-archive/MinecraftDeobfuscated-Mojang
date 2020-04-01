package net.minecraft.world.level.dimension.special;

import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeSourceType;
import net.minecraft.world.level.biome.FixedBiomeSource;
import net.minecraft.world.level.biome.FixedBiomeSourceSettings;
import net.minecraft.world.level.biome.OverworldBiomeSource;
import net.minecraft.world.level.dimension.Dimension;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.dimension.NormalDimension;

public abstract class SpecialDimensionBase extends Dimension {
	protected final Level level;

	public SpecialDimensionBase(Level level, DimensionType dimensionType, float f) {
		super(level, dimensionType, f);
		this.level = level;
	}

	@Nullable
	@Override
	public BlockPos getSpawnPosInChunk(ChunkPos chunkPos, boolean bl) {
		return NormalDimension.getSpawnPosInChunkI(this.level, chunkPos, bl);
	}

	@Nullable
	@Override
	public BlockPos getValidSpawnPosition(int i, int j, boolean bl) {
		return NormalDimension.getValidSpawnPositionI(this.level, i, j, bl);
	}

	@Override
	public boolean isNaturalDimension() {
		return false;
	}

	@Override
	public boolean mayRespawn() {
		return false;
	}

	public static FixedBiomeSource fixedBiome(Biome biome) {
		FixedBiomeSourceSettings fixedBiomeSourceSettings = BiomeSourceType.FIXED.createSettings(0L).setBiome(biome);
		return new FixedBiomeSource(fixedBiomeSourceSettings);
	}

	public static OverworldBiomeSource normalBiomes(long l) {
		return BiomeSourceType.VANILLA_LAYERED.create(BiomeSourceType.VANILLA_LAYERED.createSettings(l));
	}
}
