package net.minecraft.world.level.levelgen;

import net.minecraft.util.Mth;
import net.minecraft.world.level.block.state.BlockState;

public class DepthBasedReplacingBaseStoneSource implements BaseStoneSource {
	private static final int ALWAYS_REPLACE_BELOW_Y = -8;
	private static final int NEVER_REPLACE_ABOVE_Y = 0;
	private final long seed;
	private final BlockState normalBlock;
	private final BlockState replacementBlock;
	private final NoiseGeneratorSettings settings;

	public DepthBasedReplacingBaseStoneSource(long l, BlockState blockState, BlockState blockState2, NoiseGeneratorSettings noiseGeneratorSettings) {
		this.seed = l;
		this.normalBlock = blockState;
		this.replacementBlock = blockState2;
		this.settings = noiseGeneratorSettings;
	}

	@Override
	public BlockState getBaseBlock(int i, int j, int k) {
		if (!this.settings.isDeepslateEnabled()) {
			return this.normalBlock;
		} else if (j < -8) {
			return this.replacementBlock;
		} else if (j > 0) {
			return this.normalBlock;
		} else {
			double d = (double)Mth.map((float)j, -8.0F, 0.0F, 1.0F, 0.0F);
			WorldgenRandom worldgenRandom = new WorldgenRandom();
			worldgenRandom.setBaseStoneSeed(this.seed, i, j, k);
			return (double)worldgenRandom.nextFloat() < d ? this.replacementBlock : this.normalBlock;
		}
	}
}
