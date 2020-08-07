package net.minecraft.world.level.block;

import java.util.Random;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public abstract class GrowingPlantBlock extends Block {
	protected final Direction growthDirection;
	protected final boolean scheduleFluidTicks;
	protected final VoxelShape shape;

	protected GrowingPlantBlock(BlockBehaviour.Properties properties, Direction direction, VoxelShape voxelShape, boolean bl) {
		super(properties);
		this.growthDirection = direction;
		this.shape = voxelShape;
		this.scheduleFluidTicks = bl;
	}

	@Nullable
	@Override
	public BlockState getStateForPlacement(BlockPlaceContext blockPlaceContext) {
		BlockState blockState = blockPlaceContext.getLevel().getBlockState(blockPlaceContext.getClickedPos().relative(this.growthDirection));
		return !blockState.is(this.getHeadBlock()) && !blockState.is(this.getBodyBlock())
			? this.getStateForPlacement(blockPlaceContext.getLevel())
			: this.getBodyBlock().defaultBlockState();
	}

	public BlockState getStateForPlacement(LevelAccessor levelAccessor) {
		return this.defaultBlockState();
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

	@Override
	public void tick(BlockState blockState, ServerLevel serverLevel, BlockPos blockPos, Random random) {
		if (!blockState.canSurvive(serverLevel, blockPos)) {
			serverLevel.destroyBlock(blockPos, true);
		}
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
