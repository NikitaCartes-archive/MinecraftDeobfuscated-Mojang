package net.minecraft.world.level.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.features.NetherFeatures;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.lighting.LightEngine;

public class NyliumBlock extends Block implements BonemealableBlock {
	public static final MapCodec<NyliumBlock> CODEC = simpleCodec(NyliumBlock::new);

	@Override
	public MapCodec<NyliumBlock> codec() {
		return CODEC;
	}

	protected NyliumBlock(BlockBehaviour.Properties properties) {
		super(properties);
	}

	private static boolean canBeNylium(BlockState blockState, LevelReader levelReader, BlockPos blockPos) {
		BlockPos blockPos2 = blockPos.above();
		BlockState blockState2 = levelReader.getBlockState(blockPos2);
		int i = LightEngine.getLightBlockInto(blockState, blockState2, Direction.UP, blockState2.getLightBlock());
		return i < 15;
	}

	@Override
	protected void randomTick(BlockState blockState, ServerLevel serverLevel, BlockPos blockPos, RandomSource randomSource) {
		if (!canBeNylium(blockState, serverLevel, blockPos)) {
			serverLevel.setBlockAndUpdate(blockPos, Blocks.NETHERRACK.defaultBlockState());
		}
	}

	@Override
	public boolean isValidBonemealTarget(LevelReader levelReader, BlockPos blockPos, BlockState blockState) {
		return levelReader.getBlockState(blockPos.above()).isAir();
	}

	@Override
	public boolean isBonemealSuccess(Level level, RandomSource randomSource, BlockPos blockPos, BlockState blockState) {
		return true;
	}

	@Override
	public void performBonemeal(ServerLevel serverLevel, RandomSource randomSource, BlockPos blockPos, BlockState blockState) {
		BlockState blockState2 = serverLevel.getBlockState(blockPos);
		BlockPos blockPos2 = blockPos.above();
		ChunkGenerator chunkGenerator = serverLevel.getChunkSource().getGenerator();
		Registry<ConfiguredFeature<?, ?>> registry = serverLevel.registryAccess().lookupOrThrow(Registries.CONFIGURED_FEATURE);
		if (blockState2.is(Blocks.CRIMSON_NYLIUM)) {
			this.place(registry, NetherFeatures.CRIMSON_FOREST_VEGETATION_BONEMEAL, serverLevel, chunkGenerator, randomSource, blockPos2);
		} else if (blockState2.is(Blocks.WARPED_NYLIUM)) {
			this.place(registry, NetherFeatures.WARPED_FOREST_VEGETATION_BONEMEAL, serverLevel, chunkGenerator, randomSource, blockPos2);
			this.place(registry, NetherFeatures.NETHER_SPROUTS_BONEMEAL, serverLevel, chunkGenerator, randomSource, blockPos2);
			if (randomSource.nextInt(8) == 0) {
				this.place(registry, NetherFeatures.TWISTING_VINES_BONEMEAL, serverLevel, chunkGenerator, randomSource, blockPos2);
			}
		}
	}

	private void place(
		Registry<ConfiguredFeature<?, ?>> registry,
		ResourceKey<ConfiguredFeature<?, ?>> resourceKey,
		ServerLevel serverLevel,
		ChunkGenerator chunkGenerator,
		RandomSource randomSource,
		BlockPos blockPos
	) {
		registry.get(resourceKey).ifPresent(reference -> ((ConfiguredFeature)reference.value()).place(serverLevel, chunkGenerator, randomSource, blockPos));
	}

	@Override
	public BonemealableBlock.Type getType() {
		return BonemealableBlock.Type.NEIGHBOR_SPREADER;
	}
}
