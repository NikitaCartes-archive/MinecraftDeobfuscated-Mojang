package net.minecraft.world.level.levelgen.feature;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import java.util.function.Supplier;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.RegistryFileCodec;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.WorldgenRandom;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.StructureFeatureConfiguration;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureManager;

public class ConfiguredStructureFeature<FC extends FeatureConfiguration, F extends StructureFeature<FC>> {
	public static final MapCodec<ConfiguredStructureFeature<?, ?>> DIRECT_CODEC = Registry.STRUCTURE_FEATURE
		.dispatchMap("name", configuredStructureFeature -> configuredStructureFeature.feature, StructureFeature::configuredStructureCodec);
	public static final Codec<Supplier<ConfiguredStructureFeature<?, ?>>> CODEC = RegistryFileCodec.create(
		Registry.CONFIGURED_STRUCTURE_FEATURE_REGISTRY, DIRECT_CODEC
	);
	public final F feature;
	public final FC config;

	public ConfiguredStructureFeature(F structureFeature, FC featureConfiguration) {
		this.feature = structureFeature;
		this.config = featureConfiguration;
	}

	public StructureStart<?> generate(
		RegistryAccess registryAccess,
		ChunkGenerator chunkGenerator,
		BiomeSource biomeSource,
		StructureManager structureManager,
		long l,
		ChunkPos chunkPos,
		Biome biome,
		int i,
		StructureFeatureConfiguration structureFeatureConfiguration
	) {
		return this.feature
			.generate(
				registryAccess, chunkGenerator, biomeSource, structureManager, l, chunkPos, biome, i, new WorldgenRandom(), structureFeatureConfiguration, this.config
			);
	}
}
