package net.minecraft.world.level.levelgen.placement;

import com.google.common.collect.ImmutableMap;
import com.mojang.datafixers.Dynamic;
import com.mojang.datafixers.types.DynamicOps;
import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.StructureFeatureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.DecoratorConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;

public class ConfiguredDecorator<DC extends DecoratorConfiguration> {
	public final FeatureDecorator<DC> decorator;
	public final DC config;

	public ConfiguredDecorator(FeatureDecorator<DC> featureDecorator, Dynamic<?> dynamic) {
		this(featureDecorator, featureDecorator.createSettings(dynamic));
	}

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

	public <T> Dynamic<T> serialize(DynamicOps<T> dynamicOps) {
		return new Dynamic<>(
			dynamicOps,
			dynamicOps.createMap(
				ImmutableMap.of(
					dynamicOps.createString("name"),
					dynamicOps.createString(Registry.DECORATOR.getKey(this.decorator).toString()),
					dynamicOps.createString("config"),
					this.config.serialize(dynamicOps).getValue()
				)
			)
		);
	}

	public static <T> ConfiguredDecorator<?> deserialize(Dynamic<T> dynamic) {
		FeatureDecorator<? extends DecoratorConfiguration> featureDecorator = (FeatureDecorator<? extends DecoratorConfiguration>)Registry.DECORATOR
			.get(new ResourceLocation(dynamic.get("name").asString("")));
		return new ConfiguredDecorator<>(featureDecorator, dynamic.get("config").orElseEmptyMap());
	}
}
