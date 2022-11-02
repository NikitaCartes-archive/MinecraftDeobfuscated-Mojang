package net.minecraft.world.level.block;

import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.HugeFungusConfiguration;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class FungusBlock extends BushBlock implements BonemealableBlock {
	protected static final VoxelShape SHAPE = Block.box(4.0, 0.0, 4.0, 12.0, 9.0, 12.0);
	private static final double BONEMEAL_SUCCESS_PROBABILITY = 0.4;
	private final ResourceKey<ConfiguredFeature<?, ?>> feature;

	protected FungusBlock(BlockBehaviour.Properties properties, ResourceKey<ConfiguredFeature<?, ?>> resourceKey) {
		super(properties);
		this.feature = resourceKey;
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

	private Optional<? extends Holder<ConfiguredFeature<?, ?>>> getFeature(LevelReader levelReader) {
		return levelReader.registryAccess().registryOrThrow(Registry.CONFIGURED_FEATURE_REGISTRY).getHolder(this.feature);
	}

	@Override
	public boolean isValidBonemealTarget(LevelReader levelReader, BlockPos blockPos, BlockState blockState, boolean bl) {
		Optional<? extends Holder<ConfiguredFeature<?, ?>>> optional = this.getFeature(levelReader);
		if (optional.isPresent() && ((ConfiguredFeature)((Holder)optional.get()).value()).config() instanceof HugeFungusConfiguration hugeFungusConfiguration) {
			Block block = hugeFungusConfiguration.validBaseState.getBlock();
			BlockState blockState2 = levelReader.getBlockState(blockPos.below());
			return blockState2.is(block);
		} else {
			return false;
		}
	}

	@Override
	public boolean isBonemealSuccess(Level level, RandomSource randomSource, BlockPos blockPos, BlockState blockState) {
		return (double)randomSource.nextFloat() < 0.4;
	}

	@Override
	public void performBonemeal(ServerLevel serverLevel, RandomSource randomSource, BlockPos blockPos, BlockState blockState) {
		this.getFeature(serverLevel)
			.ifPresent(holder -> ((ConfiguredFeature)holder.value()).place(serverLevel, serverLevel.getChunkSource().getGenerator(), randomSource, blockPos));
	}
}
