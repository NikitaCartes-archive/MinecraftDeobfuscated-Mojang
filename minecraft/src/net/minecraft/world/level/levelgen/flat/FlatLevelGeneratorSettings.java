package net.minecraft.world.level.levelgen.flat;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Map.Entry;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.Util;
import net.minecraft.core.Registry;
import net.minecraft.util.Codecs;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeDefaultFeatures;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.StructureSettings;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.ConfiguredStructureFeature;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.StructureFeature;
import net.minecraft.world.level.levelgen.feature.configurations.BlockStateConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.LayerConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.StructureFeatureConfiguration;
import net.minecraft.world.level.levelgen.placement.ChanceDecoratorConfiguration;
import net.minecraft.world.level.levelgen.placement.FeatureDecorator;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class FlatLevelGeneratorSettings {
	private static final Logger LOGGER = LogManager.getLogger();
	public static final Codec<FlatLevelGeneratorSettings> CODEC = RecordCodecBuilder.<FlatLevelGeneratorSettings>create(
			instance -> instance.group(
						StructureSettings.CODEC.fieldOf("structures").forGetter(FlatLevelGeneratorSettings::structureSettings),
						FlatLayerInfo.CODEC.listOf().fieldOf("layers").forGetter(FlatLevelGeneratorSettings::getLayersInfo),
						Codec.BOOL.fieldOf("lakes").withDefault(false).forGetter(flatLevelGeneratorSettings -> flatLevelGeneratorSettings.addLakes),
						Codec.BOOL.fieldOf("features").withDefault(false).forGetter(flatLevelGeneratorSettings -> flatLevelGeneratorSettings.decoration),
						Codecs.withDefault(Registry.BIOME.fieldOf("biome"), Util.prefix("Unknown biome, defaulting to plains", LOGGER::error), () -> Biomes.PLAINS)
							.forGetter(flatLevelGeneratorSettings -> flatLevelGeneratorSettings.biome)
					)
					.apply(instance, FlatLevelGeneratorSettings::new)
		)
		.stable();
	private static final ConfiguredFeature<?, ?> WATER_LAKE_COMPOSITE_FEATURE = Feature.LAKE
		.configured(new BlockStateConfiguration(Blocks.WATER.defaultBlockState()))
		.decorated(FeatureDecorator.WATER_LAKE.configured(new ChanceDecoratorConfiguration(4)));
	private static final ConfiguredFeature<?, ?> LAVA_LAKE_COMPOSITE_FEATURE = Feature.LAKE
		.configured(new BlockStateConfiguration(Blocks.LAVA.defaultBlockState()))
		.decorated(FeatureDecorator.LAVA_LAKE.configured(new ChanceDecoratorConfiguration(80)));
	private static final Map<StructureFeature<?>, ConfiguredStructureFeature<?, ?>> STRUCTURE_FEATURES = Util.make(
		Maps.<StructureFeature<?>, ConfiguredStructureFeature<?, ?>>newHashMap(), hashMap -> {
			hashMap.put(StructureFeature.MINESHAFT, BiomeDefaultFeatures.MINESHAFT);
			hashMap.put(StructureFeature.VILLAGE, BiomeDefaultFeatures.VILLAGE_PLAINS);
			hashMap.put(StructureFeature.STRONGHOLD, BiomeDefaultFeatures.STRONGHOLD);
			hashMap.put(StructureFeature.SWAMP_HUT, BiomeDefaultFeatures.SWAMP_HUT);
			hashMap.put(StructureFeature.DESERT_PYRAMID, BiomeDefaultFeatures.DESERT_PYRAMID);
			hashMap.put(StructureFeature.JUNGLE_TEMPLE, BiomeDefaultFeatures.JUNGLE_TEMPLE);
			hashMap.put(StructureFeature.IGLOO, BiomeDefaultFeatures.IGLOO);
			hashMap.put(StructureFeature.OCEAN_RUIN, BiomeDefaultFeatures.OCEAN_RUIN_COLD);
			hashMap.put(StructureFeature.SHIPWRECK, BiomeDefaultFeatures.SHIPWRECK);
			hashMap.put(StructureFeature.OCEAN_MONUMENT, BiomeDefaultFeatures.OCEAN_MONUMENT);
			hashMap.put(StructureFeature.END_CITY, BiomeDefaultFeatures.END_CITY);
			hashMap.put(StructureFeature.WOODLAND_MANSION, BiomeDefaultFeatures.WOODLAND_MANSION);
			hashMap.put(StructureFeature.NETHER_BRIDGE, BiomeDefaultFeatures.NETHER_BRIDGE);
			hashMap.put(StructureFeature.PILLAGER_OUTPOST, BiomeDefaultFeatures.PILLAGER_OUTPOST);
			hashMap.put(StructureFeature.RUINED_PORTAL, BiomeDefaultFeatures.RUINED_PORTAL_STANDARD);
			hashMap.put(StructureFeature.BASTION_REMNANT, BiomeDefaultFeatures.BASTION_REMNANT);
		}
	);
	private final StructureSettings structureSettings;
	private final List<FlatLayerInfo> layersInfo = Lists.<FlatLayerInfo>newArrayList();
	private Biome biome;
	private final BlockState[] layers = new BlockState[256];
	private boolean voidGen;
	private boolean decoration = false;
	private boolean addLakes = false;

	public FlatLevelGeneratorSettings(StructureSettings structureSettings, List<FlatLayerInfo> list, boolean bl, boolean bl2, Biome biome) {
		this(structureSettings);
		if (bl) {
			this.setAddLakes();
		}

		if (bl2) {
			this.setDecoration();
		}

		this.layersInfo.addAll(list);
		this.updateLayers();
		this.biome = biome;
	}

	public FlatLevelGeneratorSettings(StructureSettings structureSettings) {
		this.structureSettings = structureSettings;
	}

	@Environment(EnvType.CLIENT)
	public FlatLevelGeneratorSettings withStructureSettings(StructureSettings structureSettings) {
		return this.withLayers(this.layersInfo, structureSettings);
	}

	@Environment(EnvType.CLIENT)
	public FlatLevelGeneratorSettings withLayers(List<FlatLayerInfo> list, StructureSettings structureSettings) {
		FlatLevelGeneratorSettings flatLevelGeneratorSettings = new FlatLevelGeneratorSettings(structureSettings);

		for (FlatLayerInfo flatLayerInfo : list) {
			flatLevelGeneratorSettings.layersInfo.add(new FlatLayerInfo(flatLayerInfo.getHeight(), flatLayerInfo.getBlockState().getBlock()));
			flatLevelGeneratorSettings.updateLayers();
		}

		flatLevelGeneratorSettings.setBiome(this.biome);
		if (this.decoration) {
			flatLevelGeneratorSettings.setDecoration();
		}

		if (this.addLakes) {
			flatLevelGeneratorSettings.setAddLakes();
		}

		return flatLevelGeneratorSettings;
	}

	public void setDecoration() {
		this.decoration = true;
	}

	public void setAddLakes() {
		this.addLakes = true;
	}

	public Biome getBiomeFromSettings() {
		Biome biome = this.getBiome();
		Biome biome2 = new Biome(
			new Biome.BiomeBuilder()
				.surfaceBuilder(biome.getSurfaceBuilder())
				.precipitation(biome.getPrecipitation())
				.biomeCategory(biome.getBiomeCategory())
				.depth(biome.getDepth())
				.scale(biome.getScale())
				.temperature(biome.getTemperature())
				.downfall(biome.getDownfall())
				.specialEffects(biome.getSpecialEffects())
				.parent(biome.getParent())
		) {
		};
		if (this.addLakes) {
			biome2.addFeature(GenerationStep.Decoration.LAKES, WATER_LAKE_COMPOSITE_FEATURE);
			biome2.addFeature(GenerationStep.Decoration.LAKES, LAVA_LAKE_COMPOSITE_FEATURE);
		}

		for (Entry<StructureFeature<?>, StructureFeatureConfiguration> entry : this.structureSettings.structureConfig().entrySet()) {
			biome2.addStructureStart(biome.withBiomeConfig((ConfiguredStructureFeature<?, ?>)STRUCTURE_FEATURES.get(entry.getKey())));
		}

		boolean bl = (!this.voidGen || biome == Biomes.THE_VOID) && this.decoration;
		if (bl) {
			List<GenerationStep.Decoration> list = Lists.<GenerationStep.Decoration>newArrayList();
			list.add(GenerationStep.Decoration.UNDERGROUND_STRUCTURES);
			list.add(GenerationStep.Decoration.SURFACE_STRUCTURES);

			for (GenerationStep.Decoration decoration : GenerationStep.Decoration.values()) {
				if (!list.contains(decoration)) {
					for (ConfiguredFeature<?, ?> configuredFeature : biome.getFeaturesForStep(decoration)) {
						biome2.addFeature(decoration, configuredFeature);
					}
				}
			}
		}

		BlockState[] blockStates = this.getLayers();

		for (int i = 0; i < blockStates.length; i++) {
			BlockState blockState = blockStates[i];
			if (blockState != null && !Heightmap.Types.MOTION_BLOCKING.isOpaque().test(blockState)) {
				this.layers[i] = null;
				biome2.addFeature(GenerationStep.Decoration.TOP_LAYER_MODIFICATION, Feature.FILL_LAYER.configured(new LayerConfiguration(i, blockState)));
			}
		}

		return biome2;
	}

	public StructureSettings structureSettings() {
		return this.structureSettings;
	}

	public Biome getBiome() {
		return this.biome;
	}

	public void setBiome(Biome biome) {
		this.biome = biome;
	}

	public List<FlatLayerInfo> getLayersInfo() {
		return this.layersInfo;
	}

	public BlockState[] getLayers() {
		return this.layers;
	}

	public void updateLayers() {
		Arrays.fill(this.layers, 0, this.layers.length, null);
		int i = 0;

		for (FlatLayerInfo flatLayerInfo : this.layersInfo) {
			flatLayerInfo.setStart(i);
			i += flatLayerInfo.getHeight();
		}

		this.voidGen = true;

		for (FlatLayerInfo flatLayerInfo2 : this.layersInfo) {
			for (int j = flatLayerInfo2.getStart(); j < flatLayerInfo2.getStart() + flatLayerInfo2.getHeight(); j++) {
				BlockState blockState = flatLayerInfo2.getBlockState();
				if (!blockState.is(Blocks.AIR)) {
					this.voidGen = false;
					this.layers[j] = blockState;
				}
			}
		}
	}

	public static FlatLevelGeneratorSettings getDefault() {
		StructureSettings structureSettings = new StructureSettings(
			Optional.of(StructureSettings.DEFAULT_STRONGHOLD),
			Maps.<StructureFeature<?>, StructureFeatureConfiguration>newHashMap(
				ImmutableMap.of(StructureFeature.VILLAGE, StructureSettings.DEFAULTS.get(StructureFeature.VILLAGE))
			)
		);
		FlatLevelGeneratorSettings flatLevelGeneratorSettings = new FlatLevelGeneratorSettings(structureSettings);
		flatLevelGeneratorSettings.setBiome(Biomes.PLAINS);
		flatLevelGeneratorSettings.getLayersInfo().add(new FlatLayerInfo(1, Blocks.BEDROCK));
		flatLevelGeneratorSettings.getLayersInfo().add(new FlatLayerInfo(2, Blocks.DIRT));
		flatLevelGeneratorSettings.getLayersInfo().add(new FlatLayerInfo(1, Blocks.GRASS_BLOCK));
		flatLevelGeneratorSettings.updateLayers();
		return flatLevelGeneratorSettings;
	}
}
