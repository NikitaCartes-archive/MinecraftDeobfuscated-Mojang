package net.minecraft.world.level.block;

import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.grower.AbstractTreeGrower;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class SaplingBlock extends BushBlock implements BonemealableBlock {
	public static final IntegerProperty STAGE = BlockStateProperties.STAGE;
	protected static final VoxelShape SHAPE = Block.box(2.0, 0.0, 2.0, 14.0, 12.0, 14.0);
	private final AbstractTreeGrower treeGrower;

	protected SaplingBlock(AbstractTreeGrower abstractTreeGrower, Block.Properties properties) {
		super(properties);
		this.treeGrower = abstractTreeGrower;
		this.registerDefaultState(this.stateDefinition.any().setValue(STAGE, Integer.valueOf(0)));
	}

	@Override
	public VoxelShape getShape(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, CollisionContext collisionContext) {
		return SHAPE;
	}

	@Override
	public void tick(BlockState blockState, Level level, BlockPos blockPos, Random random) {
		super.tick(blockState, level, blockPos, random);
		if (level.getMaxLocalRawBrightness(blockPos.above()) >= 9 && random.nextInt(7) == 0) {
			this.advanceTree(level, blockPos, blockState, random);
		}
	}

	public void advanceTree(LevelAccessor levelAccessor, BlockPos blockPos, BlockState blockState, Random random) {
		if ((Integer)blockState.getValue(STAGE) == 0) {
			levelAccessor.setBlock(blockPos, blockState.cycle(STAGE), 4);
		} else {
			this.treeGrower.growTree(levelAccessor, blockPos, blockState, random);
		}
	}

	@Override
	public boolean isValidBonemealTarget(BlockGetter blockGetter, BlockPos blockPos, BlockState blockState, boolean bl) {
		return true;
	}

	@Override
	public boolean isBonemealSuccess(Level level, Random random, BlockPos blockPos, BlockState blockState) {
		return (double)level.random.nextFloat() < 0.45;
	}

	@Override
	public void performBonemeal(Level level, Random random, BlockPos blockPos, BlockState blockState) {
		this.advanceTree(level, blockPos, blockState, random);
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		builder.add(STAGE);
	}
}
