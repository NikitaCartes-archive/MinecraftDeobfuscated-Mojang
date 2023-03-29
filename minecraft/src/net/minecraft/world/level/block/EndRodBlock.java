package net.minecraft.world.level.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;

public class EndRodBlock extends RodBlock {
	protected EndRodBlock(BlockBehaviour.Properties properties) {
		super(properties);
		this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.UP));
	}

	@Override
	public BlockState getStateForPlacement(BlockPlaceContext blockPlaceContext) {
		Direction direction = blockPlaceContext.getClickedFace();
		BlockState blockState = blockPlaceContext.getLevel().getBlockState(blockPlaceContext.getClickedPos().relative(direction.getOpposite()));
		return blockState.is(this) && blockState.getValue(FACING) == direction
			? this.defaultBlockState().setValue(FACING, direction.getOpposite())
			: this.defaultBlockState().setValue(FACING, direction);
	}

	@Override
	public void animateTick(BlockState blockState, Level level, BlockPos blockPos, RandomSource randomSource) {
		Direction direction = blockState.getValue(FACING);
		double d = (double)blockPos.getX() + 0.55 - (double)(randomSource.nextFloat() * 0.1F);
		double e = (double)blockPos.getY() + 0.55 - (double)(randomSource.nextFloat() * 0.1F);
		double f = (double)blockPos.getZ() + 0.55 - (double)(randomSource.nextFloat() * 0.1F);
		double g = (double)(0.4F - (randomSource.nextFloat() + randomSource.nextFloat()) * 0.4F);
		if (randomSource.nextInt(5) == 0) {
			level.addParticle(
				ParticleTypes.END_ROD,
				d + (double)direction.getStepX() * g,
				e + (double)direction.getStepY() * g,
				f + (double)direction.getStepZ() * g,
				randomSource.nextGaussian() * 0.005,
				randomSource.nextGaussian() * 0.005,
				randomSource.nextGaussian() * 0.005
			);
		}
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		builder.add(FACING);
	}
}
