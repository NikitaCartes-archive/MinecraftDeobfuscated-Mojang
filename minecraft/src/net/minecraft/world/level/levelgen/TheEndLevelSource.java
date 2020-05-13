package net.minecraft.world.level.levelgen;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.chunk.ChunkGenerator;

public class TheEndLevelSource extends NoiseBasedChunkGenerator<NoiseGeneratorSettings> {
	private final NoiseGeneratorSettings settings;

	public TheEndLevelSource(BiomeSource biomeSource, long l, NoiseGeneratorSettings noiseGeneratorSettings) {
		super(biomeSource, l, noiseGeneratorSettings, 8, 4, 128, true);
		this.settings = noiseGeneratorSettings;
	}

	@Environment(EnvType.CLIENT)
	@Override
	public ChunkGenerator withSeed(long l) {
		return new TheEndLevelSource(this.biomeSource.withSeed(l), l, this.settings);
	}

	@Override
	protected void fillNoiseColumn(double[] ds, int i, int j) {
		double d = 1368.824;
		double e = 684.412;
		double f = 17.110300000000002;
		double g = 4.277575000000001;
		int k = 64;
		int l = -3000;
		this.fillNoiseColumn(ds, i, j, 1368.824, 684.412, 17.110300000000002, 4.277575000000001, 64, -3000);
	}

	@Override
	protected double[] getDepthAndScale(int i, int j) {
		return new double[]{(double)this.biomeSource.getHeightValue(i, j), 0.0};
	}

	@Override
	protected double getYOffset(double d, double e, int i) {
		return 8.0 - d;
	}

	@Override
	protected double getTopSlideStart() {
		return (double)((int)super.getTopSlideStart() / 2);
	}

	@Override
	protected double getBottomSlideStart() {
		return 8.0;
	}

	@Override
	public int getSpawnHeight() {
		return 50;
	}

	@Override
	public int getSeaLevel() {
		return 0;
	}
}
