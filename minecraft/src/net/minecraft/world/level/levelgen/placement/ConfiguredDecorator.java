package net.minecraft.world.level.levelgen.placement;

import com.mojang.serialization.Codec;
import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.world.level.StructureFeatureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.DecoratorConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;

public class ConfiguredDecorator<DC extends DecoratorConfiguration> {
	public static final Codec<ConfiguredDecorator<?>> CODEC = Registry.DECORATOR
		.dispatch("name", configuredDecorator -> configuredDecorator.decorator, FeatureDecorator::configuredCodec);
	public final FeatureDecorator<DC> decorator;
	public final DC config;

	public ConfiguredDecorator(FeatureDecorator<DC> featureDecorator, DC decoratorConfiguration) {
		this.decorator = featureDecorator;
		this.config = decoratorConfiguration;
	}

	public <FC extends FeatureConfiguration, F extends Feature<FC>> boolean place(
		WorldGenLevel worldGenLevel,
		StructureFeatureManager structureFeatureManager,
		ChunkGenerator chunkGenerator,
		Random random,
		BlockPos blockPos,
		ConfiguredFeature<FC, F> configuredFeature
	) {
		return this.decorator.placeFeature(worldGenLevel, structureFeatureManager, chunkGenerator, random, blockPos, this.config, configuredFeature);
	}
}
