package net.minecraft.world.level.levelgen.feature;

import com.mojang.serialization.Codec;
import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.feature.configurations.DecoratedFeatureConfiguration;
import net.minecraft.world.level.levelgen.placement.DecorationContext;
import org.apache.commons.lang3.mutable.MutableBoolean;

public class DecoratedFeature extends Feature<DecoratedFeatureConfiguration> {
	public DecoratedFeature(Codec<DecoratedFeatureConfiguration> codec) {
		super(codec);
	}

	@Override
	public boolean place(FeaturePlaceContext<DecoratedFeatureConfiguration> featurePlaceContext) {
		MutableBoolean mutableBoolean = new MutableBoolean();
		WorldGenLevel worldGenLevel = featurePlaceContext.level();
		DecoratedFeatureConfiguration decoratedFeatureConfiguration = featurePlaceContext.config();
		ChunkGenerator chunkGenerator = featurePlaceContext.chunkGenerator();
		Random random = featurePlaceContext.random();
		BlockPos blockPos = featurePlaceContext.origin();
		ConfiguredFeature<?, ?> configuredFeature = (ConfiguredFeature<?, ?>)decoratedFeatureConfiguration.feature.get();
		decoratedFeatureConfiguration.decorator.getPositions(new DecorationContext(worldGenLevel, chunkGenerator), random, blockPos).forEach(blockPosx -> {
			if (configuredFeature.place(worldGenLevel, chunkGenerator, random, blockPosx)) {
				mutableBoolean.setTrue();
			}
		});
		return mutableBoolean.isTrue();
	}

	public String toString() {
		return String.format("< %s [%s] >", this.getClass().getSimpleName(), Registry.FEATURE.getKey(this));
	}
}
