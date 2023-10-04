package net.minecraft.world.level.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class TallGrassBlock extends BushBlock implements BonemealableBlock {
	public static final MapCodec<TallGrassBlock> CODEC = simpleCodec(TallGrassBlock::new);
	protected static final float AABB_OFFSET = 6.0F;
	protected static final VoxelShape SHAPE = Block.box(2.0, 0.0, 2.0, 14.0, 13.0, 14.0);

	@Override
	public MapCodec<TallGrassBlock> codec() {
		return CODEC;
	}

	protected TallGrassBlock(BlockBehaviour.Properties properties) {
		super(properties);
	}

	@Override
	public VoxelShape getShape(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, CollisionContext collisionContext) {
		return SHAPE;
	}

	@Override
	public boolean isValidBonemealTarget(LevelReader levelReader, BlockPos blockPos, BlockState blockState) {
		return true;
	}

	@Override
	public boolean isBonemealSuccess(Level level, RandomSource randomSource, BlockPos blockPos, BlockState blockState) {
		return true;
	}

	@Override
	public void performBonemeal(ServerLevel serverLevel, RandomSource randomSource, BlockPos blockPos, BlockState blockState) {
		DoublePlantBlock doublePlantBlock = (DoublePlantBlock)(blockState.is(Blocks.FERN) ? Blocks.LARGE_FERN : Blocks.TALL_GRASS);
		if (doublePlantBlock.defaultBlockState().canSurvive(serverLevel, blockPos) && serverLevel.isEmptyBlock(blockPos.above())) {
			DoublePlantBlock.placeAt(serverLevel, doublePlantBlock.defaultBlockState(), blockPos, 2);
		}
	}
}
