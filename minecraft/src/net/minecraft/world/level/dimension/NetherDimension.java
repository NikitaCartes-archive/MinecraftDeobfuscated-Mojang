package net.minecraft.world.level.dimension;

import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.BiomeSourceType;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.border.WorldBorder;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.chunk.ChunkGeneratorType;
import net.minecraft.world.level.levelgen.NetherGeneratorSettings;
import net.minecraft.world.phys.Vec3;

public class NetherDimension extends Dimension {
	private static final Vec3 NETHER_FOG_COLOR = new Vec3(0.2F, 0.03F, 0.03F);

	public NetherDimension(Level level, DimensionType dimensionType) {
		super(level, dimensionType, 0.1F);
		this.ultraWarm = true;
		this.hasCeiling = true;
	}

	@Environment(EnvType.CLIENT)
	@Override
	public Vec3 getFogColor(float f, float g) {
		return NETHER_FOG_COLOR;
	}

	@Override
	public ChunkGenerator<?> createRandomLevelGenerator() {
		NetherGeneratorSettings netherGeneratorSettings = ChunkGeneratorType.CAVES.createSettings();
		netherGeneratorSettings.setDefaultBlock(Blocks.NETHERRACK.defaultBlockState());
		netherGeneratorSettings.setDefaultFluid(Blocks.LAVA.defaultBlockState());
		return ChunkGeneratorType.CAVES
			.create(
				this.level, BiomeSourceType.FIXED.create(BiomeSourceType.FIXED.createSettings(this.level.getLevelData()).setBiome(Biomes.NETHER)), netherGeneratorSettings
			);
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

	@Override
	public DimensionType getType() {
		return DimensionType.NETHER;
	}
}
