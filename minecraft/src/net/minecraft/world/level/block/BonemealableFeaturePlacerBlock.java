package net.minecraft.world.level.block;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;

public class BonemealableFeaturePlacerBlock extends Block implements BonemealableBlock {
	public static final MapCodec<BonemealableFeaturePlacerBlock> CODEC = RecordCodecBuilder.mapCodec(
		instance -> instance.group(
					ResourceKey.codec(Registries.CONFIGURED_FEATURE).fieldOf("feature").forGetter(bonemealableFeaturePlacerBlock -> bonemealableFeaturePlacerBlock.feature),
					propertiesCodec()
				)
				.apply(instance, BonemealableFeaturePlacerBlock::new)
	);
	private final ResourceKey<ConfiguredFeature<?, ?>> feature;

	@Override
	public MapCodec<BonemealableFeaturePlacerBlock> codec() {
		return CODEC;
	}

	public BonemealableFeaturePlacerBlock(ResourceKey<ConfiguredFeature<?, ?>> resourceKey, BlockBehaviour.Properties properties) {
		super(properties);
		this.feature = resourceKey;
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
		serverLevel.registryAccess()
			.lookup(Registries.CONFIGURED_FEATURE)
			.flatMap(registry -> registry.get(this.feature))
			.ifPresent(
				reference -> ((ConfiguredFeature)reference.value()).place(serverLevel, serverLevel.getChunkSource().getGenerator(), randomSource, blockPos.above())
			);
	}

	@Override
	public BonemealableBlock.Type getType() {
		return BonemealableBlock.Type.NEIGHBOR_SPREADER;
	}
}
