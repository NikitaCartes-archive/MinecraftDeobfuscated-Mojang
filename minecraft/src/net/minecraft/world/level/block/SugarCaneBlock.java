package net.minecraft.world.level.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class SugarCaneBlock extends Block {
	public static final MapCodec<SugarCaneBlock> CODEC = simpleCodec(SugarCaneBlock::new);
	public static final IntegerProperty AGE = BlockStateProperties.AGE_15;
	protected static final float AABB_OFFSET = 6.0F;
	protected static final VoxelShape SHAPE = Block.box(2.0, 0.0, 2.0, 14.0, 16.0, 14.0);

	@Override
	public MapCodec<SugarCaneBlock> codec() {
		return CODEC;
	}

	protected SugarCaneBlock(BlockBehaviour.Properties properties) {
		super(properties);
		this.registerDefaultState(this.stateDefinition.any().setValue(AGE, Integer.valueOf(0)));
	}

	@Override
	protected VoxelShape getShape(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, CollisionContext collisionContext) {
		return SHAPE;
	}

	@Override
	protected void tick(BlockState blockState, ServerLevel serverLevel, BlockPos blockPos, RandomSource randomSource) {
		if (!blockState.canSurvive(serverLevel, blockPos)) {
			serverLevel.destroyBlock(blockPos, true);
		}
	}

	@Override
	protected void randomTick(BlockState blockState, ServerLevel serverLevel, BlockPos blockPos, RandomSource randomSource) {
		if (serverLevel.isEmptyBlock(blockPos.above())) {
			int i = 1;

			while (serverLevel.getBlockState(blockPos.below(i)).is(this)) {
				i++;
			}

			if (i < 3) {
				int j = (Integer)blockState.getValue(AGE);
				if (j == 15) {
					serverLevel.setBlockAndUpdate(blockPos.above(), this.defaultBlockState());
					serverLevel.setBlock(blockPos, blockState.setValue(AGE, Integer.valueOf(0)), 4);
				} else {
					serverLevel.setBlock(blockPos, blockState.setValue(AGE, Integer.valueOf(j + 1)), 4);
				}
			}
		}
	}

	@Override
	protected BlockState updateShape(
		BlockState blockState, Direction direction, BlockState blockState2, LevelAccessor levelAccessor, BlockPos blockPos, BlockPos blockPos2
	) {
		if (!blockState.canSurvive(levelAccessor, blockPos)) {
			levelAccessor.scheduleTick(blockPos, this, 1);
		}

		return super.updateShape(blockState, direction, blockState2, levelAccessor, blockPos, blockPos2);
	}

	@Override
	protected boolean canSurvive(BlockState blockState, LevelReader levelReader, BlockPos blockPos) {
		BlockState blockState2 = levelReader.getBlockState(blockPos.below());
		if (blockState2.is(this)) {
			return true;
		} else {
			if (blockState2.is(BlockTags.DIRT) || blockState2.is(BlockTags.SAND)) {
				BlockPos blockPos2 = blockPos.below();

				for (Direction direction : Direction.Plane.HORIZONTAL) {
					BlockState blockState3 = levelReader.getBlockState(blockPos2.relative(direction));
					FluidState fluidState = levelReader.getFluidState(blockPos2.relative(direction));
					if (fluidState.is(FluidTags.WATER) || blockState3.is(Blocks.FROSTED_ICE)) {
						return true;
					}
				}
			}

			return false;
		}
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		builder.add(AGE);
	}
}
