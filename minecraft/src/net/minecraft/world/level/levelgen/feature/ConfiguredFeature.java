package net.minecraft.world.level.levelgen.feature;

import com.mojang.serialization.Codec;
import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.world.level.StructureFeatureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.feature.configurations.DecoratedFeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraft.world.level.levelgen.placement.ConfiguredDecorator;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ConfiguredFeature<FC extends FeatureConfiguration, F extends Feature<FC>> {
	public static final ConfiguredFeature<?, ?> NOPE = new ConfiguredFeature<>(Feature.NO_OP, NoneFeatureConfiguration.NONE);
	public static final Codec<ConfiguredFeature<?, ?>> CODEC = Registry.FEATURE
		.<ConfiguredFeature<?, ?>>dispatch("name", configuredFeature -> configuredFeature.feature, Feature::configuredCodec)
		.withDefault(NOPE);
	public static final Logger LOGGER = LogManager.getLogger();
	public final F feature;
	public final FC config;

	public ConfiguredFeature(F feature, FC featureConfiguration) {
		this.feature = feature;
		this.config = featureConfiguration;
	}

	public ConfiguredFeature<?, ?> decorated(ConfiguredDecorator<?> configuredDecorator) {
		Feature<DecoratedFeatureConfiguration> feature = this.feature instanceof AbstractFlowerFeature ? Feature.DECORATED_FLOWER : Feature.DECORATED;
		return feature.configured(new DecoratedFeatureConfiguration(this, configuredDecorator));
	}

	public WeightedConfiguredFeature<FC> weighted(float f) {
		return new WeightedConfiguredFeature<>(this, f);
	}

	public boolean place(
		WorldGenLevel worldGenLevel, StructureFeatureManager structureFeatureManager, ChunkGenerator chunkGenerator, Random random, BlockPos blockPos
	) {
		return this.feature.place(worldGenLevel, structureFeatureManager, chunkGenerator, random, blockPos, this.config);
	}
}
