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
import java.util.function.Supplier;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.Util;
import net.minecraft.core.Registry;
import net.minecraft.data.worldgen.Features;
import net.minecraft.data.worldgen.StructureFeatures;
import net.minecraft.resources.RegistryLookupCodec;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeGenerationSettings;
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
import net.minecraft.world.level.levelgen.feature.configurations.LayerConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.StructureFeatureConfiguration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class FlatLevelGeneratorSettings {
	private static final Logger LOGGER = LogManager.getLogger();
	public static final Codec<FlatLevelGeneratorSettings> CODEC = RecordCodecBuilder.<FlatLevelGeneratorSettings>create(
			instance -> instance.group(
						RegistryLookupCodec.create(Registry.BIOME_REGISTRY).forGetter(flatLevelGeneratorSettings -> flatLevelGeneratorSettings.biomes),
						StructureSettings.CODEC.fieldOf("structures").forGetter(FlatLevelGeneratorSettings::structureSettings),
						FlatLayerInfo.CODEC.listOf().fieldOf("layers").forGetter(FlatLevelGeneratorSettings::getLayersInfo),
						Codec.BOOL.fieldOf("lakes").orElse(false).forGetter(flatLevelGeneratorSettings -> flatLevelGeneratorSettings.addLakes),
						Codec.BOOL.fieldOf("features").orElse(false).forGetter(flatLevelGeneratorSettings -> flatLevelGeneratorSettings.decoration),
						Biome.CODEC.optionalFieldOf("biome").orElseGet(Optional::empty).forGetter(flatLevelGeneratorSettings -> Optional.of(flatLevelGeneratorSettings.biome))
					)
					.apply(instance, FlatLevelGeneratorSettings::new)
		)
		.stable();
	private static final Map<StructureFeature<?>, ConfiguredStructureFeature<?, ?>> STRUCTURE_FEATURES = Util.make(
		Maps.<StructureFeature<?>, ConfiguredStructureFeature<?, ?>>newHashMap(), hashMap -> {
			hashMap.put(StructureFeature.MINESHAFT, StructureFeatures.MINESHAFT);
			hashMap.put(StructureFeature.VILLAGE, StructureFeatures.VILLAGE_PLAINS);
			hashMap.put(StructureFeature.STRONGHOLD, StructureFeatures.STRONGHOLD);
			hashMap.put(StructureFeature.SWAMP_HUT, StructureFeatures.SWAMP_HUT);
			hashMap.put(StructureFeature.DESERT_PYRAMID, StructureFeatures.DESERT_PYRAMID);
			hashMap.put(StructureFeature.JUNGLE_TEMPLE, StructureFeatures.JUNGLE_TEMPLE);
			hashMap.put(StructureFeature.IGLOO, StructureFeatures.IGLOO);
			hashMap.put(StructureFeature.OCEAN_RUIN, StructureFeatures.OCEAN_RUIN_COLD);
			hashMap.put(StructureFeature.SHIPWRECK, StructureFeatures.SHIPWRECK);
			hashMap.put(StructureFeature.OCEAN_MONUMENT, StructureFeatures.OCEAN_MONUMENT);
			hashMap.put(StructureFeature.END_CITY, StructureFeatures.END_CITY);
			hashMap.put(StructureFeature.WOODLAND_MANSION, StructureFeatures.WOODLAND_MANSION);
			hashMap.put(StructureFeature.NETHER_BRIDGE, StructureFeatures.NETHER_BRIDGE);
			hashMap.put(StructureFeature.PILLAGER_OUTPOST, StructureFeatures.PILLAGER_OUTPOST);
			hashMap.put(StructureFeature.RUINED_PORTAL, StructureFeatures.RUINED_PORTAL_STANDARD);
			hashMap.put(StructureFeature.BASTION_REMNANT, StructureFeatures.BASTION_REMNANT);
		}
	);
	private final Registry<Biome> biomes;
	private final StructureSettings structureSettings;
	private final List<FlatLayerInfo> layersInfo = Lists.<FlatLayerInfo>newArrayList();
	private Supplier<Biome> biome;
	private final BlockState[] layers = new BlockState[256];
	private boolean voidGen;
	private boolean decoration = false;
	private boolean addLakes = false;

	public FlatLevelGeneratorSettings(
		Registry<Biome> registry, StructureSettings structureSettings, List<FlatLayerInfo> list, boolean bl, boolean bl2, Optional<Supplier<Biome>> optional
	) {
		this(structureSettings, registry);
		if (bl) {
			this.setAddLakes();
		}

		if (bl2) {
			this.setDecoration();
		}

		this.layersInfo.addAll(list);
		this.updateLayers();
		if (!optional.isPresent()) {
			LOGGER.error("Unknown biome, defaulting to plains");
			this.biome = () -> registry.getOrThrow(Biomes.PLAINS);
		} else {
			this.biome = (Supplier<Biome>)optional.get();
		}
	}

	public FlatLevelGeneratorSettings(StructureSettings structureSettings, Registry<Biome> registry) {
		this.biomes = registry;
		this.structureSettings = structureSettings;
		this.biome = () -> registry.getOrThrow(Biomes.PLAINS);
	}

	@Environment(EnvType.CLIENT)
	public FlatLevelGeneratorSettings withStructureSettings(StructureSettings structureSettings) {
		return this.withLayers(this.layersInfo, structureSettings);
	}

	@Environment(EnvType.CLIENT)
	public FlatLevelGeneratorSettings withLayers(List<FlatLayerInfo> list, StructureSettings structureSettings) {
		FlatLevelGeneratorSettings flatLevelGeneratorSettings = new FlatLevelGeneratorSettings(structureSettings, this.biomes);

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
		BiomeGenerationSettings biomeGenerationSettings = biome.getGenerationSettings();
		BiomeGenerationSettings.Builder builder = new BiomeGenerationSettings.Builder().surfaceBuilder(biomeGenerationSettings.getSurfaceBuilder());
		if (this.addLakes) {
			builder.addFeature(GenerationStep.Decoration.LAKES, Features.LAKE_WATER);
			builder.addFeature(GenerationStep.Decoration.LAKES, Features.LAKE_LAVA);
		}

		for (Entry<StructureFeature<?>, StructureFeatureConfiguration> entry : this.structureSettings.structureConfig().entrySet()) {
			builder.addStructureStart(biomeGenerationSettings.withBiomeConfig((ConfiguredStructureFeature<?, ?>)STRUCTURE_FEATURES.get(entry.getKey())));
		}

		boolean bl = (!this.voidGen || this.biomes.getResourceKey(biome).equals(Optional.of(Biomes.THE_VOID))) && this.decoration;
		if (bl) {
			List<List<Supplier<ConfiguredFeature<?, ?>>>> list = biomeGenerationSettings.features();

			for (int i = 0; i < list.size(); i++) {
				if (i != GenerationStep.Decoration.UNDERGROUND_STRUCTURES.ordinal() && i != GenerationStep.Decoration.SURFACE_STRUCTURES.ordinal()) {
					for (Supplier<ConfiguredFeature<?, ?>> supplier : (List)list.get(i)) {
						builder.addFeature(i, supplier);
					}
				}
			}
		}

		BlockState[] blockStates = this.getLayers();

		for (int ix = 0; ix < blockStates.length; ix++) {
			BlockState blockState = blockStates[ix];
			if (blockState != null && !Heightmap.Types.MOTION_BLOCKING.isOpaque().test(blockState)) {
				this.layers[ix] = null;
				builder.addFeature(GenerationStep.Decoration.TOP_LAYER_MODIFICATION, Feature.FILL_LAYER.configured(new LayerConfiguration(ix, blockState)));
			}
		}

		return new Biome.BiomeBuilder()
			.precipitation(biome.getPrecipitation())
			.biomeCategory(biome.getBiomeCategory())
			.depth(biome.getDepth())
			.scale(biome.getScale())
			.temperature(biome.getBaseTemperature())
			.downfall(biome.getDownfall())
			.specialEffects(biome.getSpecialEffects())
			.generationSettings(builder.build())
			.mobSpawnSettings(biome.getMobSettings())
			.build();
	}

	public StructureSettings structureSettings() {
		return this.structureSettings;
	}

	public Biome getBiome() {
		return (Biome)this.biome.get();
	}

	@Environment(EnvType.CLIENT)
	public void setBiome(Supplier<Biome> supplier) {
		this.biome = supplier;
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

	public static FlatLevelGeneratorSettings getDefault(Registry<Biome> registry) {
		StructureSettings structureSettings = new StructureSettings(
			Optional.of(StructureSettings.DEFAULT_STRONGHOLD),
			Maps.<StructureFeature<?>, StructureFeatureConfiguration>newHashMap(
				ImmutableMap.of(StructureFeature.VILLAGE, StructureSettings.DEFAULTS.get(StructureFeature.VILLAGE))
			)
		);
		FlatLevelGeneratorSettings flatLevelGeneratorSettings = new FlatLevelGeneratorSettings(structureSettings, registry);
		flatLevelGeneratorSettings.biome = () -> registry.getOrThrow(Biomes.PLAINS);
		flatLevelGeneratorSettings.getLayersInfo().add(new FlatLayerInfo(1, Blocks.BEDROCK));
		flatLevelGeneratorSettings.getLayersInfo().add(new FlatLayerInfo(2, Blocks.DIRT));
		flatLevelGeneratorSettings.getLayersInfo().add(new FlatLayerInfo(1, Blocks.GRASS_BLOCK));
		flatLevelGeneratorSettings.updateLayers();
		return flatLevelGeneratorSettings;
	}
}
