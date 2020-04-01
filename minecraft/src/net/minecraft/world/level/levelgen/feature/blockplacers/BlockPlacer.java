package net.minecraft.world.level.levelgen.feature.blockplacers;

import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Serializable;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockState;

public abstract class BlockPlacer implements Serializable {
	protected final BlockPlacerType<?> type;

	protected BlockPlacer(BlockPlacerType<?> blockPlacerType) {
		this.type = blockPlacerType;
	}

	public abstract void place(LevelAccessor levelAccessor, BlockPos blockPos, BlockState blockState, Random random);

	public static BlockPlacer random(Random random) {
		return (BlockPlacer)(random.nextBoolean() ? new SimpleBlockPlacer() : new ColumnPlacer(random.nextInt(10), random.nextInt(5)));
	}
}
