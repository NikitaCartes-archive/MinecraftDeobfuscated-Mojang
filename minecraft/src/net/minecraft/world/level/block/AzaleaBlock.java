package net.minecraft.world.level.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.grower.TreeGrower;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class AzaleaBlock extends BushBlock implements BonemealableBlock {
	public static final MapCodec<AzaleaBlock> CODEC = simpleCodec(AzaleaBlock::new);
	private static final VoxelShape SHAPE = Shapes.or(Block.box(0.0, 8.0, 0.0, 16.0, 16.0, 16.0), Block.box(6.0, 0.0, 6.0, 10.0, 8.0, 10.0));

	@Override
	public MapCodec<AzaleaBlock> codec() {
		return CODEC;
	}

	protected AzaleaBlock(BlockBehaviour.Properties properties) {
		super(properties);
	}

	@Override
	public VoxelShape getShape(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, CollisionContext collisionContext) {
		return SHAPE;
	}

	@Override
	protected boolean mayPlaceOn(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos) {
		return blockState.is(Blocks.CLAY) || super.mayPlaceOn(blockState, blockGetter, blockPos);
	}

	@Override
	public boolean isValidBonemealTarget(LevelReader levelReader, BlockPos blockPos, BlockState blockState) {
		return levelReader.getFluidState(blockPos.above()).isEmpty();
	}

	@Override
	public boolean isBonemealSuccess(Level level, RandomSource randomSource, BlockPos blockPos, BlockState blockState) {
		return (double)level.random.nextFloat() < 0.45;
	}

	@Override
	public void performBonemeal(ServerLevel serverLevel, RandomSource randomSource, BlockPos blockPos, BlockState blockState) {
		TreeGrower.AZALEA.growTree(serverLevel, serverLevel.getChunkSource().getGenerator(), blockPos, blockState, randomSource);
	}
}
