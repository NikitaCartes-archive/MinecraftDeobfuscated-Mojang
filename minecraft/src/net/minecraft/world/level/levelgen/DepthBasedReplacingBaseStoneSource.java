package net.minecraft.world.level.levelgen;

import net.minecraft.util.Mth;
import net.minecraft.world.level.block.state.BlockState;

public class DepthBasedReplacingBaseStoneSource implements BaseStoneSource {
	private final WorldgenRandom random;
	private final long seed;
	private final BlockState normalBlock;
	private final BlockState replacementBlock;

	public DepthBasedReplacingBaseStoneSource(long l, BlockState blockState, BlockState blockState2) {
		this.random = new WorldgenRandom(l);
		this.seed = l;
		this.normalBlock = blockState;
		this.replacementBlock = blockState2;
	}

	@Override
	public BlockState getBaseStone(int i, int j, int k, NoiseGeneratorSettings noiseGeneratorSettings) {
		if (!noiseGeneratorSettings.isGrimstoneEnabled()) {
			return this.normalBlock;
		} else {
			this.random.setBaseStoneSeed(this.seed, i, j, k);
			double d = Mth.clampedMap((double)j, -8.0, 0.0, 1.0, 0.0);
			return (double)this.random.nextFloat() < d ? this.replacementBlock : this.normalBlock;
		}
	}
}
