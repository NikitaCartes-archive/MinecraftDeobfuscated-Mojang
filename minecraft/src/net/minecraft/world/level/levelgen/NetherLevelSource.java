package net.minecraft.world.level.levelgen;

import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.feature.Feature;

public class NetherLevelSource extends NoiseBasedChunkGenerator<NetherGeneratorSettings> {
	private final double[] yOffsets = this.makeYOffsets();

	public NetherLevelSource(LevelAccessor levelAccessor, BiomeSource biomeSource, NetherGeneratorSettings netherGeneratorSettings) {
		super(levelAccessor, biomeSource, 4, 8, 128, netherGeneratorSettings, false);
	}

	@Override
	protected void fillNoiseColumn(double[] ds, int i, int j) {
		double d = 684.412;
		double e = 2053.236;
		double f = 8.555150000000001;
		double g = 34.2206;
		int k = -10;
		int l = 3;
		this.fillNoiseColumn(ds, i, j, 684.412, 2053.236, 8.555150000000001, 34.2206, 3, -10);
	}

	@Override
	protected double[] getDepthAndScale(int i, int j) {
		return new double[]{0.0, 0.0};
	}

	@Override
	protected double getYOffset(double d, double e, int i) {
		return this.yOffsets[i];
	}

	private double[] makeYOffsets() {
		double[] ds = new double[this.getNoiseSizeY()];

		for (int i = 0; i < this.getNoiseSizeY(); i++) {
			ds[i] = Math.cos((double)i * Math.PI * 6.0 / (double)this.getNoiseSizeY()) * 2.0;
			double d = (double)i;
			if (i > this.getNoiseSizeY() / 2) {
				d = (double)(this.getNoiseSizeY() - 1 - i);
			}

			if (d < 4.0) {
				d = 4.0 - d;
				ds[i] -= d * d * d * 10.0;
			}
		}

		return ds;
	}

	@Override
	public List<Biome.SpawnerData> getMobsAt(MobCategory mobCategory, BlockPos blockPos) {
		if (mobCategory == MobCategory.MONSTER) {
			if (Feature.NETHER_BRIDGE.isInsideFeature(this.level, blockPos)) {
				return Feature.NETHER_BRIDGE.getSpecialEnemies();
			}

			if (Feature.NETHER_BRIDGE.isInsideBoundingFeature(this.level, blockPos) && this.level.getBlockState(blockPos.below()).getBlock() == Blocks.NETHER_BRICKS) {
				return Feature.NETHER_BRIDGE.getSpecialEnemies();
			}
		}

		return super.getMobsAt(mobCategory, blockPos);
	}

	@Override
	public int getSpawnHeight() {
		return this.level.getSeaLevel() + 1;
	}

	@Override
	public int getGenDepth() {
		return 128;
	}

	@Override
	public int getSeaLevel() {
		return 32;
	}
}
