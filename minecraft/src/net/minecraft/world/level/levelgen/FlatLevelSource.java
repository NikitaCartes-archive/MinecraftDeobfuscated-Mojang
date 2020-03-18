package net.minecraft.world.level.levelgen;

import com.google.common.collect.Lists;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.world.entity.npc.CatSpawner;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.NoiseColumn;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeManager;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.biome.BiomeSpecialEffects;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.StructureFeature;
import net.minecraft.world.level.levelgen.feature.configurations.DecoratedFeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.DecoratorConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.LayerConfiguration;
import net.minecraft.world.level.levelgen.flat.FlatLevelGeneratorSettings;
import net.minecraft.world.level.levelgen.placement.FeatureDecorator;
import net.minecraft.world.level.levelgen.surfacebuilders.ConfiguredSurfaceBuilder;

public class FlatLevelSource extends ChunkGenerator<FlatLevelGeneratorSettings> {
	private final Biome biomeWrapper;
	private final PhantomSpawner phantomSpawner = new PhantomSpawner();
	private final CatSpawner catSpawner = new CatSpawner();

	public FlatLevelSource(LevelAccessor levelAccessor, BiomeSource biomeSource, FlatLevelGeneratorSettings flatLevelGeneratorSettings) {
		super(levelAccessor, biomeSource, flatLevelGeneratorSettings);
		this.biomeWrapper = this.getBiomeFromSettings();
	}

	private Biome getBiomeFromSettings() {
		Biome biome = this.settings.getBiome();
		FlatLevelSource.FlatLevelBiomeWrapper flatLevelBiomeWrapper = new FlatLevelSource.FlatLevelBiomeWrapper(
			biome.getSurfaceBuilder(),
			biome.getPrecipitation(),
			biome.getBiomeCategory(),
			biome.getDepth(),
			biome.getScale(),
			biome.getTemperature(),
			biome.getDownfall(),
			biome.getSpecialEffects(),
			biome.getParent()
		);
		Map<String, Map<String, String>> map = this.settings.getStructuresOptions();

		for (String string : map.keySet()) {
			ConfiguredFeature<?, ?>[] configuredFeatures = (ConfiguredFeature<?, ?>[])FlatLevelGeneratorSettings.STRUCTURE_FEATURES.get(string);
			if (configuredFeatures != null) {
				for (ConfiguredFeature<?, ?> configuredFeature : configuredFeatures) {
					flatLevelBiomeWrapper.addFeature((GenerationStep.Decoration)FlatLevelGeneratorSettings.STRUCTURE_FEATURES_STEP.get(configuredFeature), configuredFeature);
					ConfiguredFeature<?, ?> configuredFeature2 = ((DecoratedFeatureConfiguration)configuredFeature.config).feature;
					if (configuredFeature2.feature instanceof StructureFeature) {
						StructureFeature<FeatureConfiguration> structureFeature = (StructureFeature<FeatureConfiguration>)configuredFeature2.feature;
						FeatureConfiguration featureConfiguration = biome.getStructureConfiguration(structureFeature);
						FeatureConfiguration featureConfiguration2 = featureConfiguration != null
							? featureConfiguration
							: (FeatureConfiguration)FlatLevelGeneratorSettings.STRUCTURE_FEATURES_DEFAULT.get(configuredFeature);
						flatLevelBiomeWrapper.addStructureStart(structureFeature.configured(featureConfiguration2));
					}
				}
			}
		}

		boolean bl = (!this.settings.isVoidGen() || biome == Biomes.THE_VOID) && map.containsKey("decoration");
		if (bl) {
			List<GenerationStep.Decoration> list = Lists.<GenerationStep.Decoration>newArrayList();
			list.add(GenerationStep.Decoration.UNDERGROUND_STRUCTURES);
			list.add(GenerationStep.Decoration.SURFACE_STRUCTURES);

			for (GenerationStep.Decoration decoration : GenerationStep.Decoration.values()) {
				if (!list.contains(decoration)) {
					for (ConfiguredFeature<?, ?> configuredFeature2 : biome.getFeaturesForStep(decoration)) {
						flatLevelBiomeWrapper.addFeature(decoration, configuredFeature2);
					}
				}
			}
		}

		BlockState[] blockStates = this.settings.getLayers();

		for (int i = 0; i < blockStates.length; i++) {
			BlockState blockState = blockStates[i];
			if (blockState != null && !Heightmap.Types.MOTION_BLOCKING.isOpaque().test(blockState)) {
				this.settings.deleteLayer(i);
				flatLevelBiomeWrapper.addFeature(
					GenerationStep.Decoration.TOP_LAYER_MODIFICATION,
					Feature.FILL_LAYER.configured(new LayerConfiguration(i, blockState)).decorated(FeatureDecorator.NOPE.configured(DecoratorConfiguration.NONE))
				);
			}
		}

		return flatLevelBiomeWrapper;
	}

	@Override
	public void buildSurfaceAndBedrock(WorldGenRegion worldGenRegion, ChunkAccess chunkAccess) {
	}

	@Override
	public int getSpawnHeight() {
		ChunkAccess chunkAccess = this.level.getChunk(0, 0);
		return chunkAccess.getHeight(Heightmap.Types.MOTION_BLOCKING, 8, 8);
	}

	@Override
	protected Biome getCarvingOrDecorationBiome(BiomeManager biomeManager, BlockPos blockPos) {
		return this.biomeWrapper;
	}

	@Override
	public void fillFromNoise(LevelAccessor levelAccessor, ChunkAccess chunkAccess) {
		BlockState[] blockStates = this.settings.getLayers();
		BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();
		Heightmap heightmap = chunkAccess.getOrCreateHeightmapUnprimed(Heightmap.Types.OCEAN_FLOOR_WG);
		Heightmap heightmap2 = chunkAccess.getOrCreateHeightmapUnprimed(Heightmap.Types.WORLD_SURFACE_WG);

		for (int i = 0; i < blockStates.length; i++) {
			BlockState blockState = blockStates[i];
			if (blockState != null) {
				for (int j = 0; j < 16; j++) {
					for (int k = 0; k < 16; k++) {
						chunkAccess.setBlockState(mutableBlockPos.set(j, i, k), blockState, false);
						heightmap.update(j, i, k, blockState);
						heightmap2.update(j, i, k, blockState);
					}
				}
			}
		}
	}

	@Override
	public int getBaseHeight(int i, int j, Heightmap.Types types) {
		BlockState[] blockStates = this.settings.getLayers();

		for (int k = blockStates.length - 1; k >= 0; k--) {
			BlockState blockState = blockStates[k];
			if (blockState != null && types.isOpaque().test(blockState)) {
				return k + 1;
			}
		}

		return 0;
	}

	@Override
	public BlockGetter getBaseColumn(int i, int j) {
		return new NoiseColumn(this.settings.getLayers());
	}

	@Override
	public void tickCustomSpawners(ServerLevel serverLevel, boolean bl, boolean bl2) {
		this.phantomSpawner.tick(serverLevel, bl, bl2);
		this.catSpawner.tick(serverLevel, bl, bl2);
	}

	@Override
	public boolean isBiomeValidStartForStructure(Biome biome, StructureFeature<? extends FeatureConfiguration> structureFeature) {
		return this.biomeWrapper.isValidStart(structureFeature);
	}

	@Nullable
	@Override
	public <C extends FeatureConfiguration> C getStructureConfiguration(Biome biome, StructureFeature<C> structureFeature) {
		return this.biomeWrapper.getStructureConfiguration(structureFeature);
	}

	@Nullable
	@Override
	public BlockPos findNearestMapFeature(Level level, String string, BlockPos blockPos, int i, boolean bl) {
		return !this.settings.getStructuresOptions().keySet().contains(string.toLowerCase(Locale.ROOT))
			? null
			: super.findNearestMapFeature(level, string, blockPos, i, bl);
	}

	class FlatLevelBiomeWrapper extends Biome {
		protected FlatLevelBiomeWrapper(
			ConfiguredSurfaceBuilder<?> configuredSurfaceBuilder,
			Biome.Precipitation precipitation,
			Biome.BiomeCategory biomeCategory,
			float f,
			float g,
			float h,
			float i,
			BiomeSpecialEffects biomeSpecialEffects,
			@Nullable String string
		) {
			super(
				new Biome.BiomeBuilder()
					.surfaceBuilder(configuredSurfaceBuilder)
					.precipitation(precipitation)
					.biomeCategory(biomeCategory)
					.depth(f)
					.scale(g)
					.temperature(h)
					.downfall(i)
					.specialEffects(biomeSpecialEffects)
					.parent(string)
			);
		}
	}
}
