package net.minecraft.world.level.biome;

import com.google.common.collect.ImmutableList;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.MineshaftFeature;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.MineshaftConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.RandomFeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.RandomRandomFeatureConfiguration;
import net.minecraft.world.level.levelgen.placement.FeatureDecorator;
import net.minecraft.world.level.levelgen.placement.FrequencyDecoratorConfiguration;
import net.minecraft.world.level.levelgen.placement.FrequencyWithExtraChanceDecoratorConfiguration;
import net.minecraft.world.level.levelgen.surfacebuilders.SurfaceBuilder;

public final class ForestFlowerBiome extends Biome {
	public ForestFlowerBiome() {
		super(
			new Biome.BiomeBuilder()
				.surfaceBuilder(SurfaceBuilder.DEFAULT, SurfaceBuilder.CONFIG_GRASS)
				.precipitation(Biome.Precipitation.RAIN)
				.biomeCategory(Biome.BiomeCategory.FOREST)
				.depth(0.1F)
				.scale(0.4F)
				.temperature(0.7F)
				.downfall(0.8F)
				.specialEffects(
					new BiomeSpecialEffects.Builder().waterColor(4159204).waterFogColor(329011).fogColor(12638463).ambientMoodSound(SoundEvents.AMBIENT_CAVE).build()
				)
				.parent("forest")
		);
		this.addStructureStart(Feature.MINESHAFT.configured(new MineshaftConfiguration(0.004, MineshaftFeature.Type.NORMAL)));
		this.addStructureStart(Feature.STRONGHOLD.configured(FeatureConfiguration.NONE));
		BiomeDefaultFeatures.addDefaultCarvers(this);
		BiomeDefaultFeatures.addStructureFeaturePlacement(this);
		BiomeDefaultFeatures.addDefaultLakes(this);
		BiomeDefaultFeatures.addDefaultMonsterRoom(this);
		this.addFeature(
			GenerationStep.Decoration.VEGETAL_DECORATION,
			Feature.RANDOM_RANDOM_SELECTOR
				.configured(
					new RandomRandomFeatureConfiguration(
						ImmutableList.of(
							Feature.RANDOM_PATCH.configured(BiomeDefaultFeatures.DOUBLE_LILAC_CONFIG),
							Feature.RANDOM_PATCH.configured(BiomeDefaultFeatures.DOUBLE_ROSE_BUSH_CONFIG),
							Feature.RANDOM_PATCH.configured(BiomeDefaultFeatures.DOUBLE_PEONY_CONFIG),
							Feature.FLOWER.configured(BiomeDefaultFeatures.GENERAL_FOREST_FLOWER_CONFIG)
						),
						2
					)
				)
				.decorated(FeatureDecorator.COUNT_HEIGHTMAP_32.configured(new FrequencyDecoratorConfiguration(5)))
		);
		BiomeDefaultFeatures.addDefaultUndergroundVariety(this);
		BiomeDefaultFeatures.addDefaultOres(this);
		BiomeDefaultFeatures.addDefaultSoftDisks(this);
		this.addFeature(
			GenerationStep.Decoration.VEGETAL_DECORATION,
			Feature.RANDOM_SELECTOR
				.configured(
					new RandomFeatureConfiguration(
						ImmutableList.of(
							Feature.NORMAL_TREE.configured(BiomeDefaultFeatures.BIRCH_TREE_WITH_BEES_002_CONFIG).weighted(0.2F),
							Feature.FANCY_TREE.configured(BiomeDefaultFeatures.FANCY_TREE_WITH_BEES_002_CONFIG).weighted(0.1F)
						),
						Feature.NORMAL_TREE.configured(BiomeDefaultFeatures.NORMAL_TREE_WITH_BEES_002_CONFIG)
					)
				)
				.decorated(FeatureDecorator.COUNT_EXTRA_HEIGHTMAP.configured(new FrequencyWithExtraChanceDecoratorConfiguration(6, 0.1F, 1)))
		);
		this.addFeature(
			GenerationStep.Decoration.VEGETAL_DECORATION,
			Feature.FLOWER
				.configured(BiomeDefaultFeatures.FOREST_FLOWER_CONFIG)
				.decorated(FeatureDecorator.COUNT_HEIGHTMAP_32.configured(new FrequencyDecoratorConfiguration(100)))
		);
		BiomeDefaultFeatures.addDefaultGrass(this);
		BiomeDefaultFeatures.addDefaultMushrooms(this);
		BiomeDefaultFeatures.addDefaultExtraVegetation(this);
		BiomeDefaultFeatures.addDefaultSprings(this);
		BiomeDefaultFeatures.addSurfaceFreezing(this);
		this.addSpawn(MobCategory.CREATURE, new Biome.SpawnerData(EntityType.SHEEP, 12, 4, 4));
		this.addSpawn(MobCategory.CREATURE, new Biome.SpawnerData(EntityType.PIG, 10, 4, 4));
		this.addSpawn(MobCategory.CREATURE, new Biome.SpawnerData(EntityType.CHICKEN, 10, 4, 4));
		this.addSpawn(MobCategory.CREATURE, new Biome.SpawnerData(EntityType.COW, 8, 4, 4));
		this.addSpawn(MobCategory.CREATURE, new Biome.SpawnerData(EntityType.RABBIT, 4, 2, 3));
		this.addSpawn(MobCategory.AMBIENT, new Biome.SpawnerData(EntityType.BAT, 10, 8, 8));
		this.addSpawn(MobCategory.MONSTER, new Biome.SpawnerData(EntityType.SPIDER, 100, 4, 4));
		this.addSpawn(MobCategory.MONSTER, new Biome.SpawnerData(EntityType.ZOMBIE, 95, 4, 4));
		this.addSpawn(MobCategory.MONSTER, new Biome.SpawnerData(EntityType.ZOMBIE_VILLAGER, 5, 1, 1));
		this.addSpawn(MobCategory.MONSTER, new Biome.SpawnerData(EntityType.SKELETON, 100, 4, 4));
		this.addSpawn(MobCategory.MONSTER, new Biome.SpawnerData(EntityType.CREEPER, 100, 4, 4));
		this.addSpawn(MobCategory.MONSTER, new Biome.SpawnerData(EntityType.SLIME, 100, 4, 4));
		this.addSpawn(MobCategory.MONSTER, new Biome.SpawnerData(EntityType.ENDERMAN, 10, 1, 4));
		this.addSpawn(MobCategory.MONSTER, new Biome.SpawnerData(EntityType.WITCH, 5, 1, 1));
	}
}
