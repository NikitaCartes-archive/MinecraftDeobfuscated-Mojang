package net.minecraft.world.level.levelgen.material;

import javax.annotation.Nullable;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.DensityFunction;
import net.minecraft.world.level.levelgen.NoiseChunk;

public record MaterialRuleList(NoiseChunk.BlockStateFiller[] materialRuleList) implements NoiseChunk.BlockStateFiller {
	@Nullable
	@Override
	public BlockState calculate(DensityFunction.FunctionContext functionContext) {
		for (NoiseChunk.BlockStateFiller blockStateFiller : this.materialRuleList) {
			BlockState blockState = blockStateFiller.calculate(functionContext);
			if (blockState != null) {
				return blockState;
			}
		}

		return null;
	}
}
