package net.minecraft.world.level.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;

public class GravelBlock extends FallingBlock {
	public GravelBlock(BlockBehaviour.Properties properties) {
		super(properties);
	}

	@Override
	public int getDustColor(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos) {
		return -8356741;
	}

	@Override
	public void onLand(Level level, BlockPos blockPos, BlockState blockState, BlockState blockState2, FallingBlockEntity fallingBlockEntity) {
		super.onLand(level, blockPos, blockState, blockState2, fallingBlockEntity);

		for (Direction direction : Direction.Plane.HORIZONTAL) {
			this.trySetFire(level, blockPos.relative(direction));
		}
	}

	private void trySetFire(Level level, BlockPos blockPos) {
		FireBlock fireBlock = (FireBlock)Blocks.FIRE;
		int i = fireBlock.getFlameOdds(level.getBlockState(blockPos));
		if (i > 0 && level.random.nextInt(i) > 5) {
			level.setBlock(blockPos, fireBlock.getStateForPlacement(level, blockPos), 3);
		}
	}
}
