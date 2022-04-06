package net.minecraft.world.level.block;

import java.util.function.Supplier;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.HugeFungusConfiguration;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class FungusBlock extends BushBlock implements BonemealableBlock {
	protected static final VoxelShape SHAPE = Block.box(4.0, 0.0, 4.0, 12.0, 9.0, 12.0);
	private static final double BONEMEAL_SUCCESS_PROBABILITY = 0.4;
	private final Supplier<Holder<ConfiguredFeature<HugeFungusConfiguration, ?>>> feature;

	protected FungusBlock(BlockBehaviour.Properties properties, Supplier<Holder<ConfiguredFeature<HugeFungusConfiguration, ?>>> supplier) {
		super(properties);
		this.feature = supplier;
	}

	@Override
	public VoxelShape getShape(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, CollisionContext collisionContext) {
		return SHAPE;
	}

	@Override
	protected boolean mayPlaceOn(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos) {
		return blockState.is(BlockTags.NYLIUM)
			|| blockState.is(Blocks.MYCELIUM)
			|| blockState.is(Blocks.SOUL_SOIL)
			|| super.mayPlaceOn(blockState, blockGetter, blockPos);
	}

	@Override
	public boolean isValidBonemealTarget(BlockGetter blockGetter, BlockPos blockPos, BlockState blockState, boolean bl) {
		Block block = ((HugeFungusConfiguration)((ConfiguredFeature)((Holder)this.feature.get()).value()).config()).validBaseState.getBlock();
		BlockState blockState2 = blockGetter.getBlockState(blockPos.below());
		return blockState2.is(block);
	}

	@Override
	public boolean isBonemealSuccess(Level level, RandomSource randomSource, BlockPos blockPos, BlockState blockState) {
		return (double)randomSource.nextFloat() < 0.4;
	}

	@Override
	public void performBonemeal(ServerLevel serverLevel, RandomSource randomSource, BlockPos blockPos, BlockState blockState) {
		((ConfiguredFeature)((Holder)this.feature.get()).value()).place(serverLevel, serverLevel.getChunkSource().getGenerator(), randomSource, blockPos);
	}
}
