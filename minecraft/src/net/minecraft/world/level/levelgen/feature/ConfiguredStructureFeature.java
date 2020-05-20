package net.minecraft.world.level.levelgen.feature;

import com.mojang.serialization.Codec;
import net.minecraft.core.Registry;
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
	public static final Codec<ConfiguredStructureFeature<?, ?>> CODEC = Registry.STRUCTURE_FEATURE
		.dispatch("name", configuredStructureFeature -> configuredStructureFeature.feature, StructureFeature::configuredStructureCodec);
	public final F feature;
	public final FC config;

	public ConfiguredStructureFeature(F structureFeature, FC featureConfiguration) {
		this.feature = structureFeature;
		this.config = featureConfiguration;
	}

	public StructureStart<?> generate(
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
			.generate(chunkGenerator, biomeSource, structureManager, l, chunkPos, biome, i, new WorldgenRandom(), structureFeatureConfiguration, this.config);
	}
}
