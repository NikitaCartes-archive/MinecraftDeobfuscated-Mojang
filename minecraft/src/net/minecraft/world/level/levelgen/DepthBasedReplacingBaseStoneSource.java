package net.minecraft.world.level.levelgen;

import java.util.function.Supplier;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.state.BlockState;

public class DepthBasedReplacingBaseStoneSource implements BaseStoneSource {
	private static final int ALWAYS_REPLACE_BELOW_Y = -8;
	private static final int NEVER_REPLACE_ABOVE_Y = 0;
	private final WorldgenRandom random;
	private final long seed;
	private final BlockState normalBlock;
	private final BlockState replacementBlock;
	private final Supplier<NoiseGeneratorSettings> settings;

	public DepthBasedReplacingBaseStoneSource(long l, BlockState blockState, BlockState blockState2, Supplier<NoiseGeneratorSettings> supplier) {
		this.random = new WorldgenRandom(l);
		this.seed = l;
		this.normalBlock = blockState;
		this.replacementBlock = blockState2;
		this.settings = supplier;
	}

	@Override
	public BlockState getBaseStone(int i, int j, int k) {
		if (!((NoiseGeneratorSettings)this.settings.get()).isDeepslateEnabled()) {
			return this.normalBlock;
		} else if (j < -8) {
			return this.replacementBlock;
		} else if (j > 0) {
			return this.normalBlock;
		} else {
			double d = Mth.map((double)j, -8.0, 0.0, 1.0, 0.0);
			this.random.setBaseStoneSeed(this.seed, i, j, k);
			return (double)this.random.nextFloat() < d ? this.replacementBlock : this.normalBlock;
		}
	}
}
