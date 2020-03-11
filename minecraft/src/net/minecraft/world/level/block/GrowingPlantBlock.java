package net.minecraft.world.level.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public abstract class GrowingPlantBlock extends Block {
	protected final Direction growthDirection;
	protected final boolean scheduleFluidTicks;
	protected final VoxelShape shape;

	protected GrowingPlantBlock(Block.Properties properties, Direction direction, VoxelShape voxelShape, boolean bl) {
		super(properties);
		this.growthDirection = direction;
		this.shape = voxelShape;
		this.scheduleFluidTicks = bl;
	}

	@Override
	public boolean canSurvive(BlockState blockState, LevelReader levelReader, BlockPos blockPos) {
		BlockPos blockPos2 = blockPos.relative(this.growthDirection.getOpposite());
		BlockState blockState2 = levelReader.getBlockState(blockPos2);
		Block block = blockState2.getBlock();
		return !this.canAttachToBlock(block)
			? false
			: block == this.getHeadBlock() || block == this.getBodyBlock() || blockState2.isFaceSturdy(levelReader, blockPos2, this.growthDirection);
	}

	protected boolean canAttachToBlock(Block block) {
		return true;
	}

	@Override
	public VoxelShape getShape(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, CollisionContext collisionContext) {
		return this.shape;
	}

	protected abstract GrowingPlantHeadBlock getHeadBlock();

	protected abstract Block getBodyBlock();
}
