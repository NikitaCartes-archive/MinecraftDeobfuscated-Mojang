package net.minecraft.world.level.levelgen;

import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.StructureFeatureManager;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.feature.Feature;

public class NetherLevelSource extends NoiseBasedChunkGenerator<NetherGeneratorSettings> {
	private final double[] yOffsets = this.makeYOffsets();
	private final NetherGeneratorSettings settings;

	public NetherLevelSource(BiomeSource biomeSource, long l, NetherGeneratorSettings netherGeneratorSettings) {
		super(biomeSource, l, netherGeneratorSettings, 4, 8, 128, false);
		this.settings = netherGeneratorSettings;
	}

	@Environment(EnvType.CLIENT)
	@Override
	public ChunkGenerator withSeed(long l) {
		return new NetherLevelSource(this.biomeSource.withSeed(l), l, this.settings);
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
	public List<Biome.SpawnerData> getMobsAt(Biome biome, StructureFeatureManager structureFeatureManager, MobCategory mobCategory, BlockPos blockPos) {
		return mobCategory == MobCategory.MONSTER && Feature.NETHER_BRIDGE.isInsideFeature(structureFeatureManager, blockPos)
			? Feature.NETHER_BRIDGE.getSpecialEnemies()
			: super.getMobsAt(biome, structureFeatureManager, mobCategory, blockPos);
	}

	@Override
	public int getGenDepth() {
		return 128;
	}

	@Override
	public int getSeaLevel() {
		return 32;
	}

	@Override
	public int getBaseHeight(int i, int j, Heightmap.Types types) {
		return this.getGenDepth() / 2;
	}
}
