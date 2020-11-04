package net.minecraft.world.level.block;

import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;

public class ChangeOverTimeSlabBlock extends SlabBlock implements ChangeOverTimeBlock {
	private final Block changeTo;

	public ChangeOverTimeSlabBlock(BlockBehaviour.Properties properties, Block block) {
		super(properties);
		this.changeTo = block;
	}

	@Override
	public void onPlace(BlockState blockState, Level level, BlockPos blockPos, BlockState blockState2, boolean bl) {
		super.onPlace(blockState, level, blockPos, blockState2, bl);
		this.scheduleChange(level, this, blockPos);
	}

	@Override
	public void tick(BlockState blockState, ServerLevel serverLevel, BlockPos blockPos, Random random) {
		this.change(serverLevel, blockState, blockPos);
	}

	@Override
	public BlockState getChangeTo(BlockState blockState) {
		return this.changeTo.defaultBlockState().setValue(TYPE, blockState.getValue(TYPE)).setValue(WATERLOGGED, blockState.getValue(WATERLOGGED));
	}
}
