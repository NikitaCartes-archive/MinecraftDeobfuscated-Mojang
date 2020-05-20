package net.minecraft.world.level.levelgen.feature.blockplacers;

import com.mojang.serialization.Codec;
import java.util.Random;
import java.util.function.Supplier;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockState;

public class SimpleBlockPlacer extends BlockPlacer {
	public static final Codec<SimpleBlockPlacer> CODEC = Codec.unit((Supplier<SimpleBlockPlacer>)(() -> SimpleBlockPlacer.INSTANCE));
	public static final SimpleBlockPlacer INSTANCE = new SimpleBlockPlacer();

	@Override
	protected BlockPlacerType<?> type() {
		return BlockPlacerType.SIMPLE_BLOCK_PLACER;
	}

	@Override
	public void place(LevelAccessor levelAccessor, BlockPos blockPos, BlockState blockState, Random random) {
		levelAccessor.setBlock(blockPos, blockState, 2);
	}
}
