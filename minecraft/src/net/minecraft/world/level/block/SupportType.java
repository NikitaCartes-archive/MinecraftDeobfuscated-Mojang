package net.minecraft.world.level.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public enum SupportType {
	FULL {
		@Override
		public boolean isSupporting(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, Direction direction) {
			return Block.isFaceFull(blockState.getBlockSupportShape(blockGetter, blockPos), direction);
		}
	},
	CENTER {
		private final int CENTER_SUPPORT_WIDTH = 1;
		private final VoxelShape CENTER_SUPPORT_SHAPE = Block.box(7.0, 7.0, 7.0, 9.0, 9.0, 9.0);

		@Override
		public boolean isSupporting(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, Direction direction) {
			return Shapes.joinIsNotEmpty(blockState.getBlockSupportShape(blockGetter, blockPos).getFaceShape(direction), this.CENTER_SUPPORT_SHAPE, BooleanOp.AND);
		}
	},
	RIGID {
		private final int RIGID_SUPPORT_WIDTH = 2;
		private final VoxelShape RIGID_SUPPORT_SHAPE = Shapes.join(
			Shapes.block(),
			Shapes.or(Block.box(2.0, 0.0, 2.0, 14.0, 16.0, 14.0), Block.box(0.0, 2.0, 2.0, 16.0, 14.0, 14.0), Block.box(2.0, 2.0, 0.0, 14.0, 14.0, 16.0)),
			BooleanOp.ONLY_FIRST
		);

		@Override
		public boolean isSupporting(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, Direction direction) {
			return !Shapes.joinIsNotEmpty(
				blockState.getBlockSupportShape(blockGetter, blockPos).getFaceShape(direction), this.RIGID_SUPPORT_SHAPE, BooleanOp.ONLY_SECOND
			);
		}
	};

	public abstract boolean isSupporting(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, Direction direction);
}
