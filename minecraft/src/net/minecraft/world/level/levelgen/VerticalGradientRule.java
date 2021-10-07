package net.minecraft.world.level.levelgen;

import javax.annotation.Nullable;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.material.WorldGenMaterialRule;
import org.apache.commons.lang3.Validate;

public class VerticalGradientRule implements WorldGenMaterialRule {
	private final PositionalRandomFactory randomFactory;
	@Nullable
	private final BlockState lowerState;
	@Nullable
	private final BlockState upperState;
	private final int alwaysLowerAtAndBelow;
	private final int alwaysUpperAtAndAbove;

	public VerticalGradientRule(PositionalRandomFactory positionalRandomFactory, @Nullable BlockState blockState, @Nullable BlockState blockState2, int i, int j) {
		this.randomFactory = positionalRandomFactory;
		this.lowerState = blockState;
		this.upperState = blockState2;
		this.alwaysLowerAtAndBelow = i;
		this.alwaysUpperAtAndAbove = j;
		Validate.isTrue(i < j, "Below bounds (" + i + ") need to be smaller than above bounds (" + j + ")");
	}

	@Nullable
	@Override
	public BlockState apply(NoiseChunk noiseChunk, int i, int j, int k) {
		if (j <= this.alwaysLowerAtAndBelow) {
			return this.lowerState;
		} else if (j >= this.alwaysUpperAtAndAbove) {
			return this.upperState;
		} else {
			double d = Mth.map((double)j, (double)this.alwaysLowerAtAndBelow, (double)this.alwaysUpperAtAndAbove, 1.0, 0.0);
			RandomSource randomSource = this.randomFactory.at(i, j, k);
			return (double)randomSource.nextFloat() < d ? this.lowerState : this.upperState;
		}
	}
}
