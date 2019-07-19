package net.minecraft.world.level.levelgen;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.biome.BiomeSource;

public class TheEndLevelSource extends NoiseBasedChunkGenerator<TheEndGeneratorSettings> {
	private final BlockPos dimensionSpawnPosition;

	public TheEndLevelSource(LevelAccessor levelAccessor, BiomeSource biomeSource, TheEndGeneratorSettings theEndGeneratorSettings) {
		super(levelAccessor, biomeSource, 8, 4, 128, theEndGeneratorSettings, true);
		this.dimensionSpawnPosition = theEndGeneratorSettings.getSpawnPosition();
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
