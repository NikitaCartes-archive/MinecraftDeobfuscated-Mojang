package net.minecraft.world.level.levelgen.flat;

import com.google.common.collect.Lists;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.HolderSet;
import net.minecraft.core.RegistryCodecs;
import net.minecraft.core.registries.Registries;
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
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.LayerConfiguration;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.minecraft.world.level.levelgen.structure.BuiltinStructureSets;
import net.minecraft.world.level.levelgen.structure.StructureSet;
import org.slf4j.Logger;

public class FlatLevelGeneratorSettings {
	private static final Logger LOGGER = LogUtils.getLogger();
	public static final Codec<FlatLevelGeneratorSettings> CODEC = RecordCodecBuilder.create(
			instance -> instance.group(
						RegistryCodecs.homogeneousList(Registries.STRUCTURE_SET)
							.lenientOptionalFieldOf("structure_overrides")
							.forGetter(flatLevelGeneratorSettings -> flatLevelGeneratorSettings.structureOverrides),
						FlatLayerInfo.CODEC.listOf().fieldOf("layers").forGetter(FlatLevelGeneratorSettings::getLayersInfo),
						Codec.BOOL.fieldOf("lakes").orElse(false).forGetter(flatLevelGeneratorSettings -> flatLevelGeneratorSettings.addLakes),
						Codec.BOOL.fieldOf("features").orElse(false).forGetter(flatLevelGeneratorSettings -> flatLevelGeneratorSettings.decoration),
						Biome.CODEC
							.lenientOptionalFieldOf("biome")
							.orElseGet(Optional::empty)
							.forGetter(flatLevelGeneratorSettings -> Optional.of(flatLevelGeneratorSettings.biome)),
						RegistryOps.retrieveElement(Biomes.PLAINS),
						RegistryOps.retrieveElement(MiscOverworldPlacements.LAKE_LAVA_UNDERGROUND),
						RegistryOps.retrieveElement(MiscOverworldPlacements.LAKE_LAVA_SURFACE)
					)
					.apply(instance, FlatLevelGeneratorSettings::new)
		)
		.<FlatLevelGeneratorSettings>comapFlatMap(FlatLevelGeneratorSettings::validateHeight, Function.identity())
		.stable();
	private final Optional<HolderSet<StructureSet>> structureOverrides;
	private final List<FlatLayerInfo> layersInfo = Lists.<FlatLayerInfo>newArrayList();
	private final Holder<Biome> biome;
	private final List<BlockState> layers;
	private boolean voidGen;
	private boolean decoration;
	private boolean addLakes;
	private final List<Holder<PlacedFeature>> lakes;

	private static DataResult<FlatLevelGeneratorSettings> validateHeight(FlatLevelGeneratorSettings flatLevelGeneratorSettings) {
		int i = flatLevelGeneratorSettings.layersInfo.stream().mapToInt(FlatLayerInfo::getHeight).sum();
		return i > DimensionType.Y_SIZE
			? DataResult.error(() -> "Sum of layer heights is > " + DimensionType.Y_SIZE, flatLevelGeneratorSettings)
			: DataResult.success(flatLevelGeneratorSettings);
	}

	private FlatLevelGeneratorSettings(
		Optional<HolderSet<StructureSet>> optional,
		List<FlatLayerInfo> list,
		boolean bl,
		boolean bl2,
		Optional<Holder<Biome>> optional2,
		Holder.Reference<Biome> reference,
		Holder<PlacedFeature> holder,
		Holder<PlacedFeature> holder2
	) {
		this(optional, getBiome(optional2, reference), List.of(holder, holder2));
		if (bl) {
			this.setAddLakes();
		}

		if (bl2) {
			this.setDecoration();
		}

		this.layersInfo.addAll(list);
		this.updateLayers();
	}

	private static Holder<Biome> getBiome(Optional<? extends Holder<Biome>> optional, Holder<Biome> holder) {
		if (optional.isEmpty()) {
			LOGGER.error("Unknown biome, defaulting to plains");
			return holder;
		} else {
			return (Holder<Biome>)optional.get();
		}
	}

	public FlatLevelGeneratorSettings(Optional<HolderSet<StructureSet>> optional, Holder<Biome> holder, List<Holder<PlacedFeature>> list) {
		this.structureOverrides = optional;
		this.biome = holder;
		this.layers = Lists.<BlockState>newArrayList();
		this.lakes = list;
	}

	public FlatLevelGeneratorSettings withBiomeAndLayers(List<FlatLayerInfo> list, Optional<HolderSet<StructureSet>> optional, Holder<Biome> holder) {
		FlatLevelGeneratorSettings flatLevelGeneratorSettings = new FlatLevelGeneratorSettings(optional, holder, this.lakes);

		for (FlatLayerInfo flatLayerInfo : list) {
			flatLevelGeneratorSettings.layersInfo.add(new FlatLayerInfo(flatLayerInfo.getHeight(), flatLayerInfo.getBlockState().getBlock()));
			flatLevelGeneratorSettings.updateLayers();
		}

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

	public BiomeGenerationSettings adjustGenerationSettings(Holder<Biome> holder) {
		if (!holder.equals(this.biome)) {
			return holder.value().getGenerationSettings();
		} else {
			BiomeGenerationSettings biomeGenerationSettings = this.getBiome().value().getGenerationSettings();
			BiomeGenerationSettings.PlainBuilder plainBuilder = new BiomeGenerationSettings.PlainBuilder();
			if (this.addLakes) {
				for (Holder<PlacedFeature> holder2 : this.lakes) {
					plainBuilder.addFeature(GenerationStep.Decoration.LAKES, holder2);
				}
			}

			boolean bl = (!this.voidGen || holder.is(Biomes.THE_VOID)) && this.decoration;
			if (bl) {
				List<HolderSet<PlacedFeature>> list = biomeGenerationSettings.features();

				for (int i = 0; i < list.size(); i++) {
					if (i != GenerationStep.Decoration.UNDERGROUND_STRUCTURES.ordinal()
						&& i != GenerationStep.Decoration.SURFACE_STRUCTURES.ordinal()
						&& (!this.addLakes || i != GenerationStep.Decoration.LAKES.ordinal())) {
						for (Holder<PlacedFeature> holder3 : (HolderSet)list.get(i)) {
							plainBuilder.addFeature(i, holder3);
						}
					}
				}
			}

			List<BlockState> list = this.getLayers();

			for (int ix = 0; ix < list.size(); ix++) {
				BlockState blockState = (BlockState)list.get(ix);
				if (!Heightmap.Types.MOTION_BLOCKING.isOpaque().test(blockState)) {
					list.set(ix, null);
					plainBuilder.addFeature(
						GenerationStep.Decoration.TOP_LAYER_MODIFICATION, PlacementUtils.inlinePlaced(Feature.FILL_LAYER, new LayerConfiguration(ix, blockState))
					);
				}
			}

			return plainBuilder.build();
		}
	}

	public Optional<HolderSet<StructureSet>> structureOverrides() {
		return this.structureOverrides;
	}

	public Holder<Biome> getBiome() {
		return this.biome;
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

	public static FlatLevelGeneratorSettings getDefault(
		HolderGetter<Biome> holderGetter, HolderGetter<StructureSet> holderGetter2, HolderGetter<PlacedFeature> holderGetter3
	) {
		HolderSet<StructureSet> holderSet = HolderSet.direct(
			holderGetter2.getOrThrow(BuiltinStructureSets.STRONGHOLDS), holderGetter2.getOrThrow(BuiltinStructureSets.VILLAGES)
		);
		FlatLevelGeneratorSettings flatLevelGeneratorSettings = new FlatLevelGeneratorSettings(
			Optional.of(holderSet), getDefaultBiome(holderGetter), createLakesList(holderGetter3)
		);
		flatLevelGeneratorSettings.getLayersInfo().add(new FlatLayerInfo(1, Blocks.BEDROCK));
		flatLevelGeneratorSettings.getLayersInfo().add(new FlatLayerInfo(2, Blocks.DIRT));
		flatLevelGeneratorSettings.getLayersInfo().add(new FlatLayerInfo(1, Blocks.GRASS_BLOCK));
		flatLevelGeneratorSettings.updateLayers();
		return flatLevelGeneratorSettings;
	}

	public static Holder<Biome> getDefaultBiome(HolderGetter<Biome> holderGetter) {
		return holderGetter.getOrThrow(Biomes.PLAINS);
	}

	public static List<Holder<PlacedFeature>> createLakesList(HolderGetter<PlacedFeature> holderGetter) {
		return List.of(holderGetter.getOrThrow(MiscOverworldPlacements.LAKE_LAVA_UNDERGROUND), holderGetter.getOrThrow(MiscOverworldPlacements.LAKE_LAVA_SURFACE));
	}
}
