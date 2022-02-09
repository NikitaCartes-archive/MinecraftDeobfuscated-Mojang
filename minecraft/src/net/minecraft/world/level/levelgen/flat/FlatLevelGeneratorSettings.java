package net.minecraft.world.level.levelgen.flat;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.Registry;
import net.minecraft.data.worldgen.placement.MiscOverworldPlacements;
import net.minecraft.data.worldgen.placement.PlacementUtils;
import net.minecraft.resources.RegistryOps;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeGenerationSettings;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.StructureSettings;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.StructureFeature;
import net.minecraft.world.level.levelgen.feature.configurations.LayerConfiguration;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import org.slf4j.Logger;

public class FlatLevelGeneratorSettings {
	private static final Logger LOGGER = LogUtils.getLogger();
	public static final Codec<FlatLevelGeneratorSettings> CODEC = RecordCodecBuilder.create(
			instance -> instance.group(
						RegistryOps.retrieveRegistry(Registry.BIOME_REGISTRY).forGetter(flatLevelGeneratorSettings -> flatLevelGeneratorSettings.biomes),
						StructureSettings.CODEC.fieldOf("structures").forGetter(FlatLevelGeneratorSettings::structureSettings),
						FlatLayerInfo.CODEC.listOf().fieldOf("layers").forGetter(FlatLevelGeneratorSettings::getLayersInfo),
						Codec.BOOL.fieldOf("lakes").orElse(false).forGetter(flatLevelGeneratorSettings -> flatLevelGeneratorSettings.addLakes),
						Codec.BOOL.fieldOf("features").orElse(false).forGetter(flatLevelGeneratorSettings -> flatLevelGeneratorSettings.decoration),
						Biome.CODEC.optionalFieldOf("biome").orElseGet(Optional::empty).forGetter(flatLevelGeneratorSettings -> Optional.of(flatLevelGeneratorSettings.biome))
					)
					.apply(instance, FlatLevelGeneratorSettings::new)
		)
		.<FlatLevelGeneratorSettings>comapFlatMap(FlatLevelGeneratorSettings::validateHeight, Function.identity())
		.stable();
	private final Registry<Biome> biomes;
	private final StructureSettings structureSettings;
	private final List<FlatLayerInfo> layersInfo = Lists.<FlatLayerInfo>newArrayList();
	private Holder<Biome> biome;
	private final List<BlockState> layers;
	private boolean voidGen;
	private boolean decoration;
	private boolean addLakes;

	private static DataResult<FlatLevelGeneratorSettings> validateHeight(FlatLevelGeneratorSettings flatLevelGeneratorSettings) {
		int i = flatLevelGeneratorSettings.layersInfo.stream().mapToInt(FlatLayerInfo::getHeight).sum();
		return i > DimensionType.Y_SIZE
			? DataResult.error("Sum of layer heights is > " + DimensionType.Y_SIZE, flatLevelGeneratorSettings)
			: DataResult.success(flatLevelGeneratorSettings);
	}

	private FlatLevelGeneratorSettings(
		Registry<Biome> registry, StructureSettings structureSettings, List<FlatLayerInfo> list, boolean bl, boolean bl2, Optional<Holder<Biome>> optional
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
		if (optional.isEmpty()) {
			LOGGER.error("Unknown biome, defaulting to plains");
			this.biome = registry.getOrCreateHolder(Biomes.PLAINS);
		} else {
			this.biome = (Holder<Biome>)optional.get();
		}
	}

	public FlatLevelGeneratorSettings(StructureSettings structureSettings, Registry<Biome> registry) {
		this.biomes = registry;
		this.structureSettings = structureSettings;
		this.biome = registry.getOrCreateHolder(Biomes.PLAINS);
		this.layers = Lists.<BlockState>newArrayList();
	}

	public FlatLevelGeneratorSettings withStructureSettings(StructureSettings structureSettings) {
		return this.withLayers(this.layersInfo, structureSettings);
	}

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

	public Holder<Biome> getBiomeFromSettings() {
		Biome biome = this.getBiome().value();
		BiomeGenerationSettings biomeGenerationSettings = biome.getGenerationSettings();
		BiomeGenerationSettings.Builder builder = new BiomeGenerationSettings.Builder();
		if (this.addLakes) {
			builder.addFeature(GenerationStep.Decoration.LAKES, MiscOverworldPlacements.LAKE_LAVA_UNDERGROUND);
			builder.addFeature(GenerationStep.Decoration.LAKES, MiscOverworldPlacements.LAKE_LAVA_SURFACE);
		}

		boolean bl = (!this.voidGen || this.biome.is(Biomes.THE_VOID)) && this.decoration;
		if (bl) {
			List<HolderSet<PlacedFeature>> list = biomeGenerationSettings.features();

			for (int i = 0; i < list.size(); i++) {
				if (i != GenerationStep.Decoration.UNDERGROUND_STRUCTURES.ordinal() && i != GenerationStep.Decoration.SURFACE_STRUCTURES.ordinal()) {
					for (Holder<PlacedFeature> holder : (HolderSet)list.get(i)) {
						builder.addFeature(i, holder);
					}
				}
			}
		}

		List<BlockState> list = this.getLayers();

		for (int ix = 0; ix < list.size(); ix++) {
			BlockState blockState = (BlockState)list.get(ix);
			if (!Heightmap.Types.MOTION_BLOCKING.isOpaque().test(blockState)) {
				list.set(ix, null);
				builder.addFeature(
					GenerationStep.Decoration.TOP_LAYER_MODIFICATION, PlacementUtils.inlinePlaced(Feature.FILL_LAYER, new LayerConfiguration(ix, blockState))
				);
			}
		}

		return Holder.direct(Biome.BiomeBuilder.from(biome).generationSettings(builder.build()).build());
	}

	public StructureSettings structureSettings() {
		return this.structureSettings;
	}

	public Holder<Biome> getBiome() {
		return this.biome;
	}

	public void setBiome(Holder<Biome> holder) {
		this.biome = holder;
	}

	public List<FlatLayerInfo> getLayersInfo() {
		return this.layersInfo;
	}

	public List<BlockState> getLayers() {
		return this.layers;
	}

	public void updateLayers() {
		this.layers.clear();

		for (FlatLayerInfo flatLayerInfo : this.layersInfo) {
			for (int i = 0; i < flatLayerInfo.getHeight(); i++) {
				this.layers.add(flatLayerInfo.getBlockState());
			}
		}

		this.voidGen = this.layers.stream().allMatch(blockState -> blockState.is(Blocks.AIR));
	}

	public static FlatLevelGeneratorSettings getDefault(Registry<Biome> registry) {
		StructureSettings structureSettings = new StructureSettings(
			ImmutableMap.of(
				StructureFeature.STRONGHOLD, StructureSettings.DEFAULT_STRONGHOLD, StructureFeature.VILLAGE, StructureSettings.DEFAULTS.get(StructureFeature.VILLAGE)
			)
		);
		FlatLevelGeneratorSettings flatLevelGeneratorSettings = new FlatLevelGeneratorSettings(structureSettings, registry);
		flatLevelGeneratorSettings.biome = registry.getOrCreateHolder(Biomes.PLAINS);
		flatLevelGeneratorSettings.getLayersInfo().add(new FlatLayerInfo(1, Blocks.BEDROCK));
		flatLevelGeneratorSettings.getLayersInfo().add(new FlatLayerInfo(2, Blocks.DIRT));
		flatLevelGeneratorSettings.getLayersInfo().add(new FlatLayerInfo(1, Blocks.GRASS_BLOCK));
		flatLevelGeneratorSettings.updateLayers();
		return flatLevelGeneratorSettings;
	}
}
