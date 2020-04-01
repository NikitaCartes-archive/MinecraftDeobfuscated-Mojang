package net.minecraft.world.level.dimension.special;

import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.AmbientMoodSettings;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeDefaultFeatures;
import net.minecraft.world.level.biome.BiomeSourceType;
import net.minecraft.world.level.biome.BiomeSpecialEffects;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.biome.FixedBiomeSource;
import net.minecraft.world.level.biome.FixedBiomeSourceSettings;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.chunk.ChunkGeneratorType;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.dimension.NormalDimension;
import net.minecraft.world.level.levelgen.ChunkGeneratorSettings;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.MineshaftFeature;
import net.minecraft.world.level.levelgen.feature.configurations.CountRangeDecoratorConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.MineshaftConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.OreConfiguration;
import net.minecraft.world.level.levelgen.placement.ConfiguredDecorator;
import net.minecraft.world.level.levelgen.placement.FeatureDecorator;
import net.minecraft.world.level.levelgen.surfacebuilders.SurfaceBuilder;

public class G33 extends NormalDimension {
	public G33(Level level, DimensionType dimensionType) {
		super(level, dimensionType);
	}

	@Override
	public ChunkGenerator<? extends ChunkGeneratorSettings> createRandomLevelGenerator() {
		FixedBiomeSourceSettings fixedBiomeSourceSettings = BiomeSourceType.FIXED.createSettings(0L).setBiome(Biomes.BUSY);
		return ChunkGeneratorType.SURFACE.create(this.level, new FixedBiomeSource(fixedBiomeSourceSettings), ChunkGeneratorType.SURFACE.createSettings());
	}

	public static class BusyBiome extends Biome {
		public BusyBiome() {
			super(
				new Biome.BiomeBuilder()
					.surfaceBuilder(SurfaceBuilder.DEFAULT, SurfaceBuilder.CONFIG_GRASS)
					.precipitation(Biome.Precipitation.SNOW)
					.biomeCategory(Biome.BiomeCategory.TAIGA)
					.depth(0.3F)
					.scale(0.4F)
					.temperature(-0.5F)
					.downfall(0.4F)
					.specialEffects(
						new BiomeSpecialEffects.Builder()
							.waterColor(4020182)
							.waterFogColor(329011)
							.fogColor(12638463)
							.ambientMoodSound(AmbientMoodSettings.LEGACY_CAVE_SETTINGS)
							.build()
					)
					.parent("snowy_taiga")
			);
			this.addStructureStart(Feature.MINESHAFT.configured(new MineshaftConfiguration(0.004, MineshaftFeature.Type.NORMAL)));
			this.addStructureStart(Feature.STRONGHOLD.configured(FeatureConfiguration.NONE));
			BiomeDefaultFeatures.addDefaultCarvers(this);
			BiomeDefaultFeatures.addStructureFeaturePlacement(this);
			BiomeDefaultFeatures.addDefaultLakes(this);
			BiomeDefaultFeatures.addDefaultMonsterRoom(this);
			BiomeDefaultFeatures.addFerns(this);
			BiomeDefaultFeatures.addDefaultUndergroundVariety(this);
			BiomeDefaultFeatures.addDefaultSoftDisks(this);
			BiomeDefaultFeatures.addTaigaTrees(this);
			BiomeDefaultFeatures.addDefaultFlowers(this);
			BiomeDefaultFeatures.addTaigaGrass(this);
			BiomeDefaultFeatures.addDefaultMushrooms(this);
			BiomeDefaultFeatures.addDefaultExtraVegetation(this);
			BiomeDefaultFeatures.addDefaultSprings(this);
			BiomeDefaultFeatures.addBerryBushes(this);
			BiomeDefaultFeatures.addSurfaceFreezing(this);
			ConfiguredDecorator<CountRangeDecoratorConfiguration> configuredDecorator = FeatureDecorator.COUNT_RANGE
				.configured(new CountRangeDecoratorConfiguration(20, 0, 0, 128));
			this.addFeature(
				GenerationStep.Decoration.UNDERGROUND_ORES,
				Feature.ORE
					.configured(new OreConfiguration(OreConfiguration.Predicates.NATURAL_STONE, Blocks.COAL_BLOCK.defaultBlockState(), 17))
					.decorated(configuredDecorator)
			);
			this.addFeature(
				GenerationStep.Decoration.UNDERGROUND_ORES,
				Feature.ORE
					.configured(new OreConfiguration(OreConfiguration.Predicates.NATURAL_STONE, Blocks.IRON_BLOCK.defaultBlockState(), 9))
					.decorated(configuredDecorator)
			);
			this.addFeature(
				GenerationStep.Decoration.UNDERGROUND_ORES,
				Feature.ORE
					.configured(new OreConfiguration(OreConfiguration.Predicates.NATURAL_STONE, Blocks.GOLD_BLOCK.defaultBlockState(), 9))
					.decorated(configuredDecorator)
			);
			this.addFeature(
				GenerationStep.Decoration.UNDERGROUND_ORES,
				Feature.ORE
					.configured(new OreConfiguration(OreConfiguration.Predicates.NATURAL_STONE, Blocks.REDSTONE_BLOCK.defaultBlockState(), 8))
					.decorated(configuredDecorator)
			);
			this.addFeature(
				GenerationStep.Decoration.UNDERGROUND_ORES,
				Feature.ORE
					.configured(new OreConfiguration(OreConfiguration.Predicates.NATURAL_STONE, Blocks.DIAMOND_BLOCK.defaultBlockState(), 8))
					.decorated(configuredDecorator)
			);
			this.addFeature(
				GenerationStep.Decoration.UNDERGROUND_ORES,
				Feature.ORE
					.configured(new OreConfiguration(OreConfiguration.Predicates.NATURAL_STONE, Blocks.LAPIS_BLOCK.defaultBlockState(), 7))
					.decorated(configuredDecorator)
			);
			this.addFeature(
				GenerationStep.Decoration.UNDERGROUND_ORES,
				Feature.ORE
					.configured(new OreConfiguration(OreConfiguration.Predicates.NATURAL_STONE, Blocks.PISTON.defaultBlockState(), 17))
					.decorated(configuredDecorator)
			);
			this.addFeature(
				GenerationStep.Decoration.UNDERGROUND_ORES,
				Feature.ORE
					.configured(new OreConfiguration(OreConfiguration.Predicates.NATURAL_STONE, Blocks.STICKY_PISTON.defaultBlockState(), 9))
					.decorated(configuredDecorator)
			);
			this.addFeature(
				GenerationStep.Decoration.UNDERGROUND_ORES,
				Feature.ORE
					.configured(new OreConfiguration(OreConfiguration.Predicates.NATURAL_STONE, Blocks.DISPENSER.defaultBlockState(), 9))
					.decorated(configuredDecorator)
			);
			this.addFeature(
				GenerationStep.Decoration.UNDERGROUND_ORES,
				Feature.ORE
					.configured(new OreConfiguration(OreConfiguration.Predicates.NATURAL_STONE, Blocks.DROPPER.defaultBlockState(), 8))
					.decorated(configuredDecorator)
			);
			this.addFeature(
				GenerationStep.Decoration.UNDERGROUND_ORES,
				Feature.ORE
					.configured(new OreConfiguration(OreConfiguration.Predicates.NATURAL_STONE, Blocks.OBSERVER.defaultBlockState(), 8))
					.decorated(configuredDecorator)
			);
			this.addFeature(
				GenerationStep.Decoration.UNDERGROUND_ORES,
				Feature.ORE
					.configured(new OreConfiguration(OreConfiguration.Predicates.NATURAL_STONE, Blocks.HOPPER.defaultBlockState(), 7))
					.decorated(configuredDecorator)
			);
		}
	}
}
