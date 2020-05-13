package net.minecraft.world.level.levelgen.flat;

import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.mojang.datafixers.Dynamic;
import com.mojang.datafixers.OptionalDynamic;
import com.mojang.datafixers.types.DynamicOps;
import com.mojang.datafixers.util.Pair;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.Util;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.ChunkGeneratorSettings;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.feature.BastionPieces;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.MineshaftFeature;
import net.minecraft.world.level.levelgen.feature.StructureFeature;
import net.minecraft.world.level.levelgen.feature.configurations.BlockStateConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.JigsawConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.MineshaftConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.MultiJigsawConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.OceanRuinConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.RuinedPortalConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.ShipwreckConfiguration;
import net.minecraft.world.level.levelgen.placement.ChanceDecoratorConfiguration;
import net.minecraft.world.level.levelgen.placement.FeatureDecorator;
import net.minecraft.world.level.levelgen.structure.OceanRuinFeature;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class FlatLevelGeneratorSettings {
	private static final Logger LOGGER = LogManager.getLogger();
	private static final ConfiguredFeature<?, ? extends StructureFeature<?>> MINESHAFT_COMPOSITE_FEATURE = Feature.MINESHAFT
		.configured(new MineshaftConfiguration(0.004, MineshaftFeature.Type.NORMAL));
	private static final ConfiguredFeature<?, ? extends StructureFeature<?>> VILLAGE_COMPOSITE_FEATURE = Feature.VILLAGE
		.configured(new JigsawConfiguration("village/plains/town_centers", 6));
	private static final ConfiguredFeature<?, ? extends StructureFeature<?>> STRONGHOLD_COMPOSITE_FEATURE = Feature.STRONGHOLD
		.configured(FeatureConfiguration.NONE);
	private static final ConfiguredFeature<?, ? extends StructureFeature<?>> SWAMPHUT_COMPOSITE_FEATURE = Feature.SWAMP_HUT.configured(FeatureConfiguration.NONE);
	private static final ConfiguredFeature<?, ? extends StructureFeature<?>> DESERT_PYRAMID_COMPOSITE_FEATURE = Feature.DESERT_PYRAMID
		.configured(FeatureConfiguration.NONE);
	private static final ConfiguredFeature<?, ? extends StructureFeature<?>> JUNGLE_PYRAMID_COMPOSITE_FEATURE = Feature.JUNGLE_TEMPLE
		.configured(FeatureConfiguration.NONE);
	private static final ConfiguredFeature<?, ? extends StructureFeature<?>> IGLOO_COMPOSITE_FEATURE = Feature.IGLOO.configured(FeatureConfiguration.NONE);
	private static final ConfiguredFeature<?, ? extends StructureFeature<?>> SHIPWRECK_COMPOSITE_FEATURE = Feature.SHIPWRECK
		.configured(new ShipwreckConfiguration(false));
	private static final ConfiguredFeature<?, ? extends StructureFeature<?>> OCEAN_MONUMENT_COMPOSITE_FEATURE = Feature.OCEAN_MONUMENT
		.configured(FeatureConfiguration.NONE);
	private static final ConfiguredFeature<?, ? extends StructureFeature<?>> ENDCITY_COMPOSITE_FEATURE = Feature.END_CITY.configured(FeatureConfiguration.NONE);
	private static final ConfiguredFeature<?, ? extends StructureFeature<?>> WOOLAND_MANSION_COMPOSITE_FEATURE = Feature.WOODLAND_MANSION
		.configured(FeatureConfiguration.NONE);
	private static final ConfiguredFeature<?, ? extends StructureFeature<?>> FORTRESS_COMPOSITE_FEATURE = Feature.NETHER_BRIDGE
		.configured(FeatureConfiguration.NONE);
	private static final ConfiguredFeature<?, ? extends StructureFeature<?>> RUINED_PORTAL_COMPOSITE_FEATURE = Feature.RUINED_PORTAL
		.configured(new RuinedPortalConfiguration());
	private static final ConfiguredFeature<?, ? extends StructureFeature<?>> OCEAN_RUIN_COMPOSITE_FEATURE = Feature.OCEAN_RUIN
		.configured(new OceanRuinConfiguration(OceanRuinFeature.Type.COLD, 0.3F, 0.1F));
	private static final ConfiguredFeature<?, ? extends StructureFeature<?>> PILLAGER_OUTPOST_COMPOSITE_FEATURE = Feature.PILLAGER_OUTPOST
		.configured(FeatureConfiguration.NONE);
	private static final ConfiguredFeature<?, ? extends StructureFeature<?>> BASTION_REMNANT_COMMPOSITE_FEATURE = Feature.BASTION_REMNANT
		.configured(new MultiJigsawConfiguration(BastionPieces.POOLS));
	private static final ConfiguredFeature<?, ?> WATER_LAKE_COMPOSITE_FEATURE = Feature.LAKE
		.configured(new BlockStateConfiguration(Blocks.WATER.defaultBlockState()))
		.decorated(FeatureDecorator.WATER_LAKE.configured(new ChanceDecoratorConfiguration(4)));
	private static final ConfiguredFeature<?, ?> LAVA_LAKE_COMPOSITE_FEATURE = Feature.LAKE
		.configured(new BlockStateConfiguration(Blocks.LAVA.defaultBlockState()))
		.decorated(FeatureDecorator.LAVA_LAKE.configured(new ChanceDecoratorConfiguration(80)));
	public static final Map<ConfiguredFeature<?, ?>, GenerationStep.Decoration> STRUCTURE_FEATURES_STEP = Util.make(
		Maps.<ConfiguredFeature<?, ?>, GenerationStep.Decoration>newHashMap(), hashMap -> {
			hashMap.put(MINESHAFT_COMPOSITE_FEATURE, GenerationStep.Decoration.UNDERGROUND_STRUCTURES);
			hashMap.put(VILLAGE_COMPOSITE_FEATURE, GenerationStep.Decoration.SURFACE_STRUCTURES);
			hashMap.put(STRONGHOLD_COMPOSITE_FEATURE, GenerationStep.Decoration.UNDERGROUND_STRUCTURES);
			hashMap.put(SWAMPHUT_COMPOSITE_FEATURE, GenerationStep.Decoration.SURFACE_STRUCTURES);
			hashMap.put(DESERT_PYRAMID_COMPOSITE_FEATURE, GenerationStep.Decoration.SURFACE_STRUCTURES);
			hashMap.put(JUNGLE_PYRAMID_COMPOSITE_FEATURE, GenerationStep.Decoration.SURFACE_STRUCTURES);
			hashMap.put(IGLOO_COMPOSITE_FEATURE, GenerationStep.Decoration.SURFACE_STRUCTURES);
			hashMap.put(RUINED_PORTAL_COMPOSITE_FEATURE, GenerationStep.Decoration.SURFACE_STRUCTURES);
			hashMap.put(SHIPWRECK_COMPOSITE_FEATURE, GenerationStep.Decoration.SURFACE_STRUCTURES);
			hashMap.put(OCEAN_RUIN_COMPOSITE_FEATURE, GenerationStep.Decoration.SURFACE_STRUCTURES);
			hashMap.put(WATER_LAKE_COMPOSITE_FEATURE, GenerationStep.Decoration.LOCAL_MODIFICATIONS);
			hashMap.put(LAVA_LAKE_COMPOSITE_FEATURE, GenerationStep.Decoration.LOCAL_MODIFICATIONS);
			hashMap.put(ENDCITY_COMPOSITE_FEATURE, GenerationStep.Decoration.SURFACE_STRUCTURES);
			hashMap.put(WOOLAND_MANSION_COMPOSITE_FEATURE, GenerationStep.Decoration.SURFACE_STRUCTURES);
			hashMap.put(FORTRESS_COMPOSITE_FEATURE, GenerationStep.Decoration.UNDERGROUND_STRUCTURES);
			hashMap.put(OCEAN_MONUMENT_COMPOSITE_FEATURE, GenerationStep.Decoration.SURFACE_STRUCTURES);
			hashMap.put(PILLAGER_OUTPOST_COMPOSITE_FEATURE, GenerationStep.Decoration.SURFACE_STRUCTURES);
			hashMap.put(BASTION_REMNANT_COMMPOSITE_FEATURE, GenerationStep.Decoration.SURFACE_STRUCTURES);
		}
	);
	public static final Map<String, ConfiguredFeature<?, ?>[]> STRUCTURE_FEATURES = Util.make(
		Maps.<String, ConfiguredFeature<?, ?>[]>newHashMap(),
		hashMap -> {
			hashMap.put("mineshaft", new ConfiguredFeature[]{MINESHAFT_COMPOSITE_FEATURE});
			hashMap.put("village", new ConfiguredFeature[]{VILLAGE_COMPOSITE_FEATURE});
			hashMap.put("stronghold", new ConfiguredFeature[]{STRONGHOLD_COMPOSITE_FEATURE});
			hashMap.put(
				"biome_1",
				new ConfiguredFeature[]{
					SWAMPHUT_COMPOSITE_FEATURE,
					DESERT_PYRAMID_COMPOSITE_FEATURE,
					JUNGLE_PYRAMID_COMPOSITE_FEATURE,
					IGLOO_COMPOSITE_FEATURE,
					OCEAN_RUIN_COMPOSITE_FEATURE,
					SHIPWRECK_COMPOSITE_FEATURE
				}
			);
			hashMap.put("oceanmonument", new ConfiguredFeature[]{OCEAN_MONUMENT_COMPOSITE_FEATURE});
			hashMap.put("lake", new ConfiguredFeature[]{WATER_LAKE_COMPOSITE_FEATURE});
			hashMap.put("lava_lake", new ConfiguredFeature[]{LAVA_LAKE_COMPOSITE_FEATURE});
			hashMap.put("endcity", new ConfiguredFeature[]{ENDCITY_COMPOSITE_FEATURE});
			hashMap.put("mansion", new ConfiguredFeature[]{WOOLAND_MANSION_COMPOSITE_FEATURE});
			hashMap.put("fortress", new ConfiguredFeature[]{FORTRESS_COMPOSITE_FEATURE});
			hashMap.put("pillager_outpost", new ConfiguredFeature[]{PILLAGER_OUTPOST_COMPOSITE_FEATURE});
			hashMap.put("ruined_portal", new ConfiguredFeature[]{RUINED_PORTAL_COMPOSITE_FEATURE});
			hashMap.put("bastion_remnant", new ConfiguredFeature[]{BASTION_REMNANT_COMMPOSITE_FEATURE});
		}
	);
	public static final Map<ConfiguredFeature<?, ? extends StructureFeature<?>>, FeatureConfiguration> STRUCTURE_FEATURES_DEFAULT = Util.make(
		Maps.<ConfiguredFeature<?, ? extends StructureFeature<?>>, FeatureConfiguration>newHashMap(), hashMap -> {
			hashMap.put(MINESHAFT_COMPOSITE_FEATURE, new MineshaftConfiguration(0.004, MineshaftFeature.Type.NORMAL));
			hashMap.put(VILLAGE_COMPOSITE_FEATURE, new JigsawConfiguration("village/plains/town_centers", 6));
			hashMap.put(STRONGHOLD_COMPOSITE_FEATURE, FeatureConfiguration.NONE);
			hashMap.put(SWAMPHUT_COMPOSITE_FEATURE, FeatureConfiguration.NONE);
			hashMap.put(DESERT_PYRAMID_COMPOSITE_FEATURE, FeatureConfiguration.NONE);
			hashMap.put(JUNGLE_PYRAMID_COMPOSITE_FEATURE, FeatureConfiguration.NONE);
			hashMap.put(IGLOO_COMPOSITE_FEATURE, FeatureConfiguration.NONE);
			hashMap.put(OCEAN_RUIN_COMPOSITE_FEATURE, new OceanRuinConfiguration(OceanRuinFeature.Type.COLD, 0.3F, 0.9F));
			hashMap.put(SHIPWRECK_COMPOSITE_FEATURE, new ShipwreckConfiguration(false));
			hashMap.put(OCEAN_MONUMENT_COMPOSITE_FEATURE, FeatureConfiguration.NONE);
			hashMap.put(ENDCITY_COMPOSITE_FEATURE, FeatureConfiguration.NONE);
			hashMap.put(WOOLAND_MANSION_COMPOSITE_FEATURE, FeatureConfiguration.NONE);
			hashMap.put(FORTRESS_COMPOSITE_FEATURE, FeatureConfiguration.NONE);
			hashMap.put(PILLAGER_OUTPOST_COMPOSITE_FEATURE, FeatureConfiguration.NONE);
			hashMap.put(BASTION_REMNANT_COMMPOSITE_FEATURE, new MultiJigsawConfiguration(BastionPieces.POOLS));
		}
	);
	private final ChunkGeneratorSettings structureSettings;
	private final List<FlatLayerInfo> layersInfo = Lists.<FlatLayerInfo>newArrayList();
	private final Map<String, Map<String, String>> structuresOptions = Maps.<String, Map<String, String>>newHashMap();
	private Biome biome;
	private final BlockState[] layers = new BlockState[256];
	private boolean voidGen;
	private int seaLevel;

	public FlatLevelGeneratorSettings() {
		this(new ChunkGeneratorSettings());
	}

	public FlatLevelGeneratorSettings(ChunkGeneratorSettings chunkGeneratorSettings) {
		this.structureSettings = chunkGeneratorSettings;
	}

	public ChunkGeneratorSettings structureSettings() {
		return this.structureSettings;
	}

	@Nullable
	public static Block byString(String string) {
		try {
			ResourceLocation resourceLocation = new ResourceLocation(string);
			return (Block)Registry.BLOCK.getOptional(resourceLocation).orElse(null);
		} catch (IllegalArgumentException var2) {
			LOGGER.warn("Invalid blockstate: {}", string, var2);
			return null;
		}
	}

	public Biome getBiome() {
		return this.biome;
	}

	public void setBiome(Biome biome) {
		this.biome = biome;
	}

	public Map<String, Map<String, String>> getStructuresOptions() {
		return this.structuresOptions;
	}

	public List<FlatLayerInfo> getLayersInfo() {
		return this.layersInfo;
	}

	public void updateLayers() {
		int i = 0;

		for (FlatLayerInfo flatLayerInfo : this.layersInfo) {
			flatLayerInfo.setStart(i);
			i += flatLayerInfo.getHeight();
		}

		this.seaLevel = 0;
		this.voidGen = true;
		i = 0;

		for (FlatLayerInfo flatLayerInfo : this.layersInfo) {
			for (int j = flatLayerInfo.getStart(); j < flatLayerInfo.getStart() + flatLayerInfo.getHeight(); j++) {
				BlockState blockState = flatLayerInfo.getBlockState();
				if (!blockState.is(Blocks.AIR)) {
					this.voidGen = false;
					this.layers[j] = blockState;
				}
			}

			if (flatLayerInfo.getBlockState().is(Blocks.AIR)) {
				i += flatLayerInfo.getHeight();
			} else {
				this.seaLevel = this.seaLevel + flatLayerInfo.getHeight() + i;
				i = 0;
			}
		}
	}

	public String toString() {
		StringBuilder stringBuilder = new StringBuilder();

		for (int i = 0; i < this.layersInfo.size(); i++) {
			if (i > 0) {
				stringBuilder.append(",");
			}

			stringBuilder.append(this.layersInfo.get(i));
		}

		stringBuilder.append(";");
		stringBuilder.append(Registry.BIOME.getKey(this.biome));
		stringBuilder.append(";");
		if (!this.structuresOptions.isEmpty()) {
			int i = 0;

			for (Entry<String, Map<String, String>> entry : this.structuresOptions.entrySet()) {
				if (i++ > 0) {
					stringBuilder.append(",");
				}

				stringBuilder.append(((String)entry.getKey()).toLowerCase(Locale.ROOT));
				Map<String, String> map = (Map<String, String>)entry.getValue();
				if (!map.isEmpty()) {
					stringBuilder.append("(");
					int j = 0;

					for (Entry<String, String> entry2 : map.entrySet()) {
						if (j++ > 0) {
							stringBuilder.append(" ");
						}

						stringBuilder.append((String)entry2.getKey());
						stringBuilder.append("=");
						stringBuilder.append((String)entry2.getValue());
					}

					stringBuilder.append(")");
				}
			}
		}

		return stringBuilder.toString();
	}

	@Nullable
	@Environment(EnvType.CLIENT)
	private static FlatLayerInfo getLayerInfoFromString(String string, int i) {
		String[] strings = string.split("\\*", 2);
		int j;
		if (strings.length == 2) {
			try {
				j = Math.max(Integer.parseInt(strings[0]), 0);
			} catch (NumberFormatException var9) {
				LOGGER.error("Error while parsing flat world string => {}", var9.getMessage());
				return null;
			}
		} else {
			j = 1;
		}

		int k = Math.min(i + j, 256);
		int l = k - i;

		Block block;
		try {
			block = byString(strings[strings.length - 1]);
		} catch (Exception var8) {
			LOGGER.error("Error while parsing flat world string => {}", var8.getMessage());
			return null;
		}

		if (block == null) {
			LOGGER.error("Error while parsing flat world string => Unknown block, {}", strings[strings.length - 1]);
			return null;
		} else {
			FlatLayerInfo flatLayerInfo = new FlatLayerInfo(l, block);
			flatLayerInfo.setStart(i);
			return flatLayerInfo;
		}
	}

	@Environment(EnvType.CLIENT)
	private static List<FlatLayerInfo> getLayersInfoFromString(String string) {
		List<FlatLayerInfo> list = Lists.<FlatLayerInfo>newArrayList();
		String[] strings = string.split(",");
		int i = 0;

		for (String string2 : strings) {
			FlatLayerInfo flatLayerInfo = getLayerInfoFromString(string2, i);
			if (flatLayerInfo == null) {
				return Collections.emptyList();
			}

			list.add(flatLayerInfo);
			i += flatLayerInfo.getHeight();
		}

		return list;
	}

	@Environment(EnvType.CLIENT)
	public <T> Dynamic<T> toObject(DynamicOps<T> dynamicOps) {
		T object = dynamicOps.createList(
			this.layersInfo
				.stream()
				.map(
					flatLayerInfo -> dynamicOps.createMap(
							ImmutableMap.of(
								dynamicOps.createString("height"),
								dynamicOps.createInt(flatLayerInfo.getHeight()),
								dynamicOps.createString("block"),
								dynamicOps.createString(Registry.BLOCK.getKey(flatLayerInfo.getBlockState().getBlock()).toString())
							)
						)
				)
		);
		T object2 = dynamicOps.createMap(
			(Map<T, T>)this.structuresOptions
				.entrySet()
				.stream()
				.map(
					entry -> Pair.of(
							dynamicOps.createString(((String)entry.getKey()).toLowerCase(Locale.ROOT)),
							dynamicOps.createMap(
								(Map<T, T>)((Map)entry.getValue())
									.entrySet()
									.stream()
									.map(entryx -> Pair.of(dynamicOps.createString((String)entryx.getKey()), dynamicOps.createString((String)entryx.getValue())))
									.collect(Collectors.toMap(Pair::getFirst, Pair::getSecond))
							)
						)
				)
				.collect(Collectors.toMap(Pair::getFirst, Pair::getSecond))
		);
		return new Dynamic<>(
			dynamicOps,
			dynamicOps.createMap(
				ImmutableMap.of(
					dynamicOps.createString("layers"),
					object,
					dynamicOps.createString("biome"),
					dynamicOps.createString(Registry.BIOME.getKey(this.biome).toString()),
					dynamicOps.createString("structures"),
					object2
				)
			)
		);
	}

	public static FlatLevelGeneratorSettings fromObject(Dynamic<?> dynamic) {
		FlatLevelGeneratorSettings flatLevelGeneratorSettings = new FlatLevelGeneratorSettings();
		List<Pair<Integer, Block>> list = dynamic.get("layers")
			.asList(dynamicx -> Pair.of(dynamicx.get("height").asInt(1), byString(dynamicx.get("block").asString(""))));
		if (list.stream().anyMatch(pair -> pair.getSecond() == null)) {
			return getDefault();
		} else {
			List<FlatLayerInfo> list2 = (List<FlatLayerInfo>)list.stream()
				.map(pair -> new FlatLayerInfo((Integer)pair.getFirst(), (Block)pair.getSecond()))
				.collect(Collectors.toList());
			if (list2.isEmpty()) {
				return getDefault();
			} else {
				flatLevelGeneratorSettings.getLayersInfo().addAll(list2);
				flatLevelGeneratorSettings.updateLayers();
				OptionalDynamic<?> optionalDynamic = dynamic.get("biome");
				flatLevelGeneratorSettings.setBiome((Biome)Registry.BIOME.getOptional(new ResourceLocation(optionalDynamic.asString(""))).orElseGet(() -> {
					LOGGER.error("Unknown biome, defaulting to plains: " + optionalDynamic);
					return Biomes.PLAINS;
				}));
				dynamic.get("structures")
					.flatMap(Dynamic::getMapValues)
					.ifPresent(
						map -> map.keySet()
								.forEach(dynamicx -> dynamicx.asString().map(string -> (Map)flatLevelGeneratorSettings.getStructuresOptions().put(string, Maps.newHashMap())))
					);
				return flatLevelGeneratorSettings;
			}
		}
	}

	@Environment(EnvType.CLIENT)
	public static FlatLevelGeneratorSettings fromString(String string) {
		Iterator<String> iterator = Splitter.on(';').split(string).iterator();
		if (!iterator.hasNext()) {
			return getDefault();
		} else {
			FlatLevelGeneratorSettings flatLevelGeneratorSettings = new FlatLevelGeneratorSettings();
			List<FlatLayerInfo> list = getLayersInfoFromString((String)iterator.next());
			if (list.isEmpty()) {
				return getDefault();
			} else {
				flatLevelGeneratorSettings.getLayersInfo().addAll(list);
				flatLevelGeneratorSettings.updateLayers();
				Biome biome = Biomes.PLAINS;
				if (iterator.hasNext()) {
					try {
						ResourceLocation resourceLocation = new ResourceLocation((String)iterator.next());
						biome = (Biome)Registry.BIOME.getOptional(resourceLocation).orElseThrow(() -> new IllegalArgumentException("Invalid Biome: " + resourceLocation));
					} catch (Exception var17) {
						LOGGER.error("Error while parsing flat world string => {}", var17.getMessage());
					}
				}

				flatLevelGeneratorSettings.setBiome(biome);
				if (iterator.hasNext()) {
					String[] strings = ((String)iterator.next()).toLowerCase(Locale.ROOT).split(",");

					for (String string2 : strings) {
						String[] strings2 = string2.split("\\(", 2);
						if (!strings2[0].isEmpty()) {
							flatLevelGeneratorSettings.addStructure(strings2[0]);
							if (strings2.length > 1 && strings2[1].endsWith(")") && strings2[1].length() > 1) {
								String[] strings3 = strings2[1].substring(0, strings2[1].length() - 1).split(" ");

								for (String string3 : strings3) {
									String[] strings4 = string3.split("=", 2);
									if (strings4.length == 2) {
										flatLevelGeneratorSettings.addStructureOption(strings2[0], strings4[0], strings4[1]);
									}
								}
							}
						}
					}
				} else {
					flatLevelGeneratorSettings.getStructuresOptions().put("village", Maps.newHashMap());
				}

				return flatLevelGeneratorSettings;
			}
		}
	}

	@Environment(EnvType.CLIENT)
	private void addStructure(String string) {
		Map<String, String> map = Maps.<String, String>newHashMap();
		this.structuresOptions.put(string, map);
	}

	@Environment(EnvType.CLIENT)
	private void addStructureOption(String string, String string2, String string3) {
		((Map)this.structuresOptions.get(string)).put(string2, string3);
		this.structureSettings.setOption(string, string2, string3);
	}

	public static FlatLevelGeneratorSettings getDefault() {
		FlatLevelGeneratorSettings flatLevelGeneratorSettings = new FlatLevelGeneratorSettings();
		flatLevelGeneratorSettings.setBiome(Biomes.PLAINS);
		flatLevelGeneratorSettings.getLayersInfo().add(new FlatLayerInfo(1, Blocks.BEDROCK));
		flatLevelGeneratorSettings.getLayersInfo().add(new FlatLayerInfo(2, Blocks.DIRT));
		flatLevelGeneratorSettings.getLayersInfo().add(new FlatLayerInfo(1, Blocks.GRASS_BLOCK));
		flatLevelGeneratorSettings.updateLayers();
		flatLevelGeneratorSettings.getStructuresOptions().put("village", Maps.newHashMap());
		return flatLevelGeneratorSettings;
	}

	public boolean isVoidGen() {
		return this.voidGen;
	}

	public BlockState[] getLayers() {
		return this.layers;
	}

	public void deleteLayer(int i) {
		this.layers[i] = null;
	}
}
