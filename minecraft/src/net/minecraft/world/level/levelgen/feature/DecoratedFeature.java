package net.minecraft.world.level.levelgen.feature;

import com.mojang.serialization.Codec;
import java.util.Optional;
import java.util.Random;
import net.minecraft.core.Registry;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.biome.Biome;
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
		ConfiguredFeature<?, ?> configuredFeature = (ConfiguredFeature<?, ?>)decoratedFeatureConfiguration.feature.get();
		decoratedFeatureConfiguration.decorator
			.getPositions(new DecorationContext(worldGenLevel, chunkGenerator), random, featurePlaceContext.origin())
			.forEach(blockPos -> {
				Optional<ConfiguredFeature<?, ?>> optional = featurePlaceContext.topFeature();
				if (optional.isPresent() && !(configuredFeature.feature() instanceof DecoratedFeature)) {
					Biome biome = worldGenLevel.getBiome(blockPos);
					if (!biome.getGenerationSettings().hasFeature((ConfiguredFeature<?, ?>)optional.get())) {
						return;
					}
				}

				if (configuredFeature.placeWithBiomeCheck(optional, worldGenLevel, chunkGenerator, random, blockPos)) {
					mutableBoolean.setTrue();
				}
			});
		return mutableBoolean.isTrue();
	}

	public String toString() {
		return String.format("< %s [%s] >", this.getClass().getSimpleName(), Registry.FEATURE.getKey(this));
	}
}
