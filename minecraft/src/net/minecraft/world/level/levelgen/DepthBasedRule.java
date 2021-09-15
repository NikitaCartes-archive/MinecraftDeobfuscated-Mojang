package net.minecraft.world.level.levelgen;

import javax.annotation.Nullable;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.material.WorldGenMaterialRule;

public class DepthBasedRule implements WorldGenMaterialRule {
	private static final int ALWAYS_REPLACE_BELOW_Y = -8;
	private static final int NEVER_REPLACE_ABOVE_Y = 0;
	private final PositionalRandomFactory randomFactory;
	private final BlockState state;

	public DepthBasedRule(PositionalRandomFactory positionalRandomFactory, BlockState blockState) {
		this.randomFactory = positionalRandomFactory;
		this.state = blockState;
	}

	@Nullable
	@Override
	public BlockState apply(NoiseChunk noiseChunk, int i, int j, int k) {
		if (j < -8) {
			return this.state;
		} else if (j > 0) {
			return null;
		} else {
			double d = (double)Mth.map((float)j, -8.0F, 0.0F, 1.0F, 0.0F);
			RandomSource randomSource = this.randomFactory.at(i, j, k);
			return (double)randomSource.nextFloat() < d ? this.state : null;
		}
	}
}
