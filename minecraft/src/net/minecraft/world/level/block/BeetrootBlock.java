package net.minecraft.world.level.block;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class BeetrootBlock extends CropBlock {
	public static final int MAX_AGE = 3;
	public static final IntegerProperty AGE = BlockStateProperties.AGE_3;
	private static final VoxelShape[] SHAPE_BY_AGE = new VoxelShape[]{
		Block.box(0.0, 0.0, 0.0, 16.0, 2.0, 16.0),
		Block.box(0.0, 0.0, 0.0, 16.0, 4.0, 16.0),
		Block.box(0.0, 0.0, 0.0, 16.0, 6.0, 16.0),
		Block.box(0.0, 0.0, 0.0, 16.0, 8.0, 16.0)
	};

	public BeetrootBlock(BlockBehaviour.Properties properties) {
		super(properties);
	}

	@Override
	protected IntegerProperty getAgeProperty() {
		return AGE;
	}

	@Override
	public int getMaxAge() {
		return 3;
	}

	@Override
	protected ItemLike getBaseSeedId() {
		return Items.BEETROOT_SEEDS;
	}

	@Override
	public void randomTick(BlockState blockState, ServerLevel serverLevel, BlockPos blockPos, RandomSource randomSource) {
		if (randomSource.nextInt(3) != 0) {
			super.randomTick(blockState, serverLevel, blockPos, randomSource);
		}
	}

	@Override
	protected int getBonemealAgeIncrease(Level level) {
		return super.getBonemealAgeIncrease(level) / 3;
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		builder.add(AGE);
	}

	@Override
	public VoxelShape getShape(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, CollisionContext collisionContext) {
		return SHAPE_BY_AGE[this.getAge(blockState)];
	}
}
