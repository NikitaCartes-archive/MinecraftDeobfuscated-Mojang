package net.minecraft.world.level.levelgen.feature;

import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Random;
import java.util.function.Supplier;
import java.util.stream.Stream;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.data.BuiltinRegistries;
import net.minecraft.resources.RegistryFileCodec;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.blockpredicates.BlockPredicate;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;
import net.minecraft.world.level.levelgen.placement.BlockPredicateFilter;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.minecraft.world.level.levelgen.placement.PlacementModifier;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ConfiguredFeature<FC extends FeatureConfiguration, F extends Feature<FC>> {
	public static final Codec<ConfiguredFeature<?, ?>> DIRECT_CODEC = Registry.FEATURE
		.byNameCodec()
		.dispatch(configuredFeature -> configuredFeature.feature, Feature::configuredCodec);
	public static final Codec<Supplier<ConfiguredFeature<?, ?>>> CODEC = RegistryFileCodec.create(Registry.CONFIGURED_FEATURE_REGISTRY, DIRECT_CODEC);
	public static final Codec<List<Supplier<ConfiguredFeature<?, ?>>>> LIST_CODEC = RegistryFileCodec.homogeneousList(
		Registry.CONFIGURED_FEATURE_REGISTRY, DIRECT_CODEC
	);
	public static final Logger LOGGER = LogManager.getLogger();
	public final F feature;
	public final FC config;

	public ConfiguredFeature(F feature, FC featureConfiguration) {
		this.feature = feature;
		this.config = featureConfiguration;
	}

	public F feature() {
		return this.feature;
	}

	public FC config() {
		return this.config;
	}

	public PlacedFeature placed(List<PlacementModifier> list) {
		return new PlacedFeature(() -> this, list);
	}

	public PlacedFeature placed(PlacementModifier... placementModifiers) {
		return this.placed(List.of(placementModifiers));
	}

	public PlacedFeature filteredByBlockSurvival(Block block) {
		return this.filtered(BlockPredicate.wouldSurvive(block.defaultBlockState(), BlockPos.ZERO));
	}

	public PlacedFeature onlyWhenEmpty() {
		return this.filtered(BlockPredicate.matchesBlock(Blocks.AIR, BlockPos.ZERO));
	}

	public PlacedFeature filtered(BlockPredicate blockPredicate) {
		return this.placed(BlockPredicateFilter.forPredicate(blockPredicate));
	}

	public boolean place(WorldGenLevel worldGenLevel, ChunkGenerator chunkGenerator, Random random, BlockPos blockPos) {
		return worldGenLevel.ensureCanWrite(blockPos)
			? this.feature.place(new FeaturePlaceContext<>(Optional.empty(), worldGenLevel, chunkGenerator, random, blockPos, this.config))
			: false;
	}

	public Stream<ConfiguredFeature<?, ?>> getFeatures() {
		return Stream.concat(Stream.of(this), this.config.getFeatures());
	}

	public String toString() {
		return (String)BuiltinRegistries.CONFIGURED_FEATURE
			.getResourceKey(this)
			.map(Objects::toString)
			.orElseGet(() -> DIRECT_CODEC.encodeStart(JsonOps.INSTANCE, this).toString());
	}
}
