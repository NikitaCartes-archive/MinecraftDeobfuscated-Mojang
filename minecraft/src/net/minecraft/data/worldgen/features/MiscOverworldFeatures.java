package net.minecraft.data.worldgen.features;

import java.util.List;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.blockpredicates.BlockPredicate;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.LakeFeature;
import net.minecraft.world.level.levelgen.feature.configurations.BlockStateConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.DiskConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.SpringConfiguration;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProvider;
import net.minecraft.world.level.levelgen.feature.stateproviders.RuleBasedBlockStateProvider;
import net.minecraft.world.level.material.Fluids;

public class MiscOverworldFeatures {
	public static final Holder<ConfiguredFeature<NoneFeatureConfiguration, ?>> ICE_SPIKE = FeatureUtils.register("ice_spike", Feature.ICE_SPIKE);
	public static final Holder<ConfiguredFeature<DiskConfiguration, ?>> ICE_PATCH = FeatureUtils.register(
		"ice_patch",
		Feature.DISK,
		new DiskConfiguration(
			RuleBasedBlockStateProvider.simple(Blocks.PACKED_ICE),
			BlockPredicate.matchesBlocks(List.of(Blocks.DIRT, Blocks.GRASS_BLOCK, Blocks.PODZOL, Blocks.COARSE_DIRT, Blocks.MYCELIUM, Blocks.SNOW_BLOCK, Blocks.ICE)),
			UniformInt.of(2, 3),
			1
		)
	);
	public static final Holder<ConfiguredFeature<BlockStateConfiguration, ?>> FOREST_ROCK = FeatureUtils.register(
		"forest_rock", Feature.FOREST_ROCK, new BlockStateConfiguration(Blocks.MOSSY_COBBLESTONE.defaultBlockState())
	);
	public static final Holder<ConfiguredFeature<BlockStateConfiguration, ?>> ICEBERG_PACKED = FeatureUtils.register(
		"iceberg_packed", Feature.ICEBERG, new BlockStateConfiguration(Blocks.PACKED_ICE.defaultBlockState())
	);
	public static final Holder<ConfiguredFeature<BlockStateConfiguration, ?>> ICEBERG_BLUE = FeatureUtils.register(
		"iceberg_blue", Feature.ICEBERG, new BlockStateConfiguration(Blocks.BLUE_ICE.defaultBlockState())
	);
	public static final Holder<ConfiguredFeature<NoneFeatureConfiguration, ?>> BLUE_ICE = FeatureUtils.register("blue_ice", Feature.BLUE_ICE);
	public static final Holder<ConfiguredFeature<LakeFeature.Configuration, ?>> LAKE_LAVA = FeatureUtils.register(
		"lake_lava",
		Feature.LAKE,
		new LakeFeature.Configuration(BlockStateProvider.simple(Blocks.LAVA.defaultBlockState()), BlockStateProvider.simple(Blocks.STONE.defaultBlockState()))
	);
	public static final Holder<ConfiguredFeature<DiskConfiguration, ?>> DISK_CLAY = FeatureUtils.register(
		"disk_clay",
		Feature.DISK,
		new DiskConfiguration(
			RuleBasedBlockStateProvider.simple(Blocks.CLAY), BlockPredicate.matchesBlocks(List.of(Blocks.DIRT, Blocks.CLAY)), UniformInt.of(2, 3), 1
		)
	);
	public static final Holder<ConfiguredFeature<DiskConfiguration, ?>> DISK_GRAVEL = FeatureUtils.register(
		"disk_gravel",
		Feature.DISK,
		new DiskConfiguration(
			RuleBasedBlockStateProvider.simple(Blocks.GRAVEL), BlockPredicate.matchesBlocks(List.of(Blocks.DIRT, Blocks.GRASS_BLOCK)), UniformInt.of(2, 5), 2
		)
	);
	public static final Holder<ConfiguredFeature<DiskConfiguration, ?>> DISK_SAND = FeatureUtils.register(
		"disk_sand",
		Feature.DISK,
		new DiskConfiguration(
			new RuleBasedBlockStateProvider(
				BlockStateProvider.simple(Blocks.SAND),
				List.of(
					new RuleBasedBlockStateProvider.Rule(BlockPredicate.matchesBlocks(Direction.DOWN.getNormal(), Blocks.AIR), BlockStateProvider.simple(Blocks.SANDSTONE))
				)
			),
			BlockPredicate.matchesBlocks(List.of(Blocks.DIRT, Blocks.GRASS_BLOCK)),
			UniformInt.of(2, 6),
			2
		)
	);
	public static final Holder<ConfiguredFeature<NoneFeatureConfiguration, ?>> FREEZE_TOP_LAYER = FeatureUtils.register(
		"freeze_top_layer", Feature.FREEZE_TOP_LAYER
	);
	public static final Holder<ConfiguredFeature<DiskConfiguration, ?>> DISK_GRASS = FeatureUtils.register(
		"disk_grass",
		Feature.DISK,
		new DiskConfiguration(
			new RuleBasedBlockStateProvider(
				BlockStateProvider.simple(Blocks.DIRT),
				List.of(
					new RuleBasedBlockStateProvider.Rule(
						BlockPredicate.not(
							BlockPredicate.anyOf(BlockPredicate.solid(Direction.UP.getNormal()), BlockPredicate.matchesFluids(Direction.UP.getNormal(), Fluids.WATER))
						),
						BlockStateProvider.simple(Blocks.GRASS_BLOCK)
					)
				)
			),
			BlockPredicate.matchesBlocks(List.of(Blocks.DIRT, Blocks.MUD)),
			UniformInt.of(2, 6),
			2
		)
	);
	public static final Holder<ConfiguredFeature<NoneFeatureConfiguration, ?>> BONUS_CHEST = FeatureUtils.register("bonus_chest", Feature.BONUS_CHEST);
	public static final Holder<ConfiguredFeature<NoneFeatureConfiguration, ?>> VOID_START_PLATFORM = FeatureUtils.register(
		"void_start_platform", Feature.VOID_START_PLATFORM
	);
	public static final Holder<ConfiguredFeature<NoneFeatureConfiguration, ?>> DESERT_WELL = FeatureUtils.register("desert_well", Feature.DESERT_WELL);
	public static final Holder<ConfiguredFeature<SpringConfiguration, ?>> SPRING_LAVA_OVERWORLD = FeatureUtils.register(
		"spring_lava_overworld",
		Feature.SPRING,
		new SpringConfiguration(
			Fluids.LAVA.defaultFluidState(),
			true,
			4,
			1,
			HolderSet.direct(
				Block::builtInRegistryHolder, Blocks.STONE, Blocks.GRANITE, Blocks.DIORITE, Blocks.ANDESITE, Blocks.DEEPSLATE, Blocks.TUFF, Blocks.CALCITE, Blocks.DIRT
			)
		)
	);
	public static final Holder<ConfiguredFeature<SpringConfiguration, ?>> SPRING_LAVA_FROZEN = FeatureUtils.register(
		"spring_lava_frozen",
		Feature.SPRING,
		new SpringConfiguration(
			Fluids.LAVA.defaultFluidState(), true, 4, 1, HolderSet.direct(Block::builtInRegistryHolder, Blocks.SNOW_BLOCK, Blocks.POWDER_SNOW, Blocks.PACKED_ICE)
		)
	);
	public static final Holder<ConfiguredFeature<SpringConfiguration, ?>> SPRING_WATER = FeatureUtils.register(
		"spring_water",
		Feature.SPRING,
		new SpringConfiguration(
			Fluids.WATER.defaultFluidState(),
			true,
			4,
			1,
			HolderSet.direct(
				Block::builtInRegistryHolder,
				Blocks.STONE,
				Blocks.GRANITE,
				Blocks.DIORITE,
				Blocks.ANDESITE,
				Blocks.DEEPSLATE,
				Blocks.TUFF,
				Blocks.CALCITE,
				Blocks.DIRT,
				Blocks.SNOW_BLOCK,
				Blocks.POWDER_SNOW,
				Blocks.PACKED_ICE
			)
		)
	);
}
