package net.minecraft.world.level.levelgen.feature;

import com.mojang.serialization.Codec;
import java.util.function.Predicate;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.RegistryCodecs;
import net.minecraft.resources.RegistryFileCodec;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureManager;

public class ConfiguredStructureFeature<FC extends FeatureConfiguration, F extends StructureFeature<FC>> {
	public static final Codec<ConfiguredStructureFeature<?, ?>> DIRECT_CODEC = Registry.STRUCTURE_FEATURE
		.byNameCodec()
		.dispatch(configuredStructureFeature -> configuredStructureFeature.feature, StructureFeature::configuredStructureCodec);
	public static final Codec<Holder<ConfiguredStructureFeature<?, ?>>> CODEC = RegistryFileCodec.create(
		Registry.CONFIGURED_STRUCTURE_FEATURE_REGISTRY, DIRECT_CODEC
	);
	public static final Codec<HolderSet<ConfiguredStructureFeature<?, ?>>> LIST_CODEC = RegistryCodecs.homogeneousList(
		Registry.CONFIGURED_STRUCTURE_FEATURE_REGISTRY, DIRECT_CODEC
	);
	public final F feature;
	public final FC config;
	public final HolderSet<Biome> biomes;

	public ConfiguredStructureFeature(F structureFeature, FC featureConfiguration, HolderSet<Biome> holderSet) {
		this.feature = structureFeature;
		this.config = featureConfiguration;
		this.biomes = holderSet;
	}

	public StructureStart<?> generate(
		RegistryAccess registryAccess,
		ChunkGenerator chunkGenerator,
		BiomeSource biomeSource,
		StructureManager structureManager,
		long l,
		ChunkPos chunkPos,
		int i,
		LevelHeightAccessor levelHeightAccessor,
		Predicate<Holder<Biome>> predicate
	) {
		return this.feature.generate(registryAccess, chunkGenerator, biomeSource, structureManager, l, chunkPos, i, this.config, levelHeightAccessor, predicate);
	}

	public HolderSet<Biome> biomes() {
		return this.biomes;
	}
}
