package net.minecraft.world.level.dimension;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeSourceType;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.biome.MultiNoiseBiomeSourceSettings;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.border.WorldBorder;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.chunk.ChunkGeneratorType;
import net.minecraft.world.level.levelgen.NetherGeneratorSettings;
import net.minecraft.world.phys.Vec3;

public class NetherDimension extends Dimension {
	public NetherDimension(Level level, DimensionType dimensionType) {
		super(level, dimensionType, 0.1F);
		this.ultraWarm = true;
		this.hasCeiling = true;
	}

	@Environment(EnvType.CLIENT)
	@Override
	public Vec3 getBrightnessDependentFogColor(Vec3 vec3, float f) {
		return vec3;
	}

	@Override
	public ChunkGenerator<?> createRandomLevelGenerator() {
		NetherGeneratorSettings netherGeneratorSettings = ChunkGeneratorType.CAVES.createSettings();
		netherGeneratorSettings.setDefaultBlock(Blocks.NETHERRACK.defaultBlockState());
		netherGeneratorSettings.setDefaultFluid(Blocks.LAVA.defaultBlockState());
		MultiNoiseBiomeSourceSettings multiNoiseBiomeSourceSettings = BiomeSourceType.MULTI_NOISE
			.createSettings(this.level.getSeed())
			.setBiomes(
				ImmutableMap.of(
					Biomes.NETHER_WASTES,
					ImmutableList.of(new Biome.ClimateParameters(0.0F, 0.0F, 0.0F, -0.5F, 1.0F)),
					Biomes.SOUL_SAND_VALLEY,
					ImmutableList.of(new Biome.ClimateParameters(0.0F, 0.0F, 0.0F, 0.5F, 1.0F)),
					Biomes.CRIMSON_FOREST,
					ImmutableList.of(new Biome.ClimateParameters(0.0F, -0.5F, 0.0F, 0.0F, 1.0F)),
					Biomes.WARPED_FOREST,
					ImmutableList.of(new Biome.ClimateParameters(0.0F, 0.5F, 0.0F, 0.0F, 1.0F))
				)
			);
		return ChunkGeneratorType.CAVES.create(this.level, BiomeSourceType.MULTI_NOISE.create(multiNoiseBiomeSourceSettings), netherGeneratorSettings);
	}

	@Override
	public boolean isNaturalDimension() {
		return false;
	}

	@Nullable
	@Override
	public BlockPos getSpawnPosInChunk(ChunkPos chunkPos, boolean bl) {
		return null;
	}

	@Nullable
	@Override
	public BlockPos getValidSpawnPosition(int i, int j, boolean bl) {
		return null;
	}

	@Override
	public float getTimeOfDay(long l, float f) {
		return 0.5F;
	}

	@Override
	public boolean mayRespawn() {
		return false;
	}

	@Environment(EnvType.CLIENT)
	@Override
	public boolean isFoggyAt(int i, int j) {
		return true;
	}

	@Override
	public float getBlockShade(Direction direction, boolean bl) {
		if (!bl) {
			return 0.9F;
		} else {
			switch (direction) {
				case DOWN:
					return 0.9F;
				case UP:
					return 0.9F;
				case NORTH:
				case SOUTH:
					return 0.8F;
				case WEST:
				case EAST:
					return 0.6F;
				default:
					return 1.0F;
			}
		}
	}

	@Override
	public WorldBorder createWorldBorder() {
		return new WorldBorder() {
			@Override
			public double getCenterX() {
				return super.getCenterX() / 8.0;
			}

			@Override
			public double getCenterZ() {
				return super.getCenterZ() / 8.0;
			}
		};
	}
}
