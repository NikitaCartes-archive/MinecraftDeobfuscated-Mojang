package net.minecraft.world.level.biome;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.MineshaftFeature;
import net.minecraft.world.level.levelgen.feature.configurations.DecoratorConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.MineshaftConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.SeagrassFeatureConfiguration;
import net.minecraft.world.level.levelgen.placement.FeatureDecorator;
import net.minecraft.world.level.levelgen.surfacebuilders.SurfaceBuilder;

public final class RiverBiome extends Biome {
	public RiverBiome() {
		super(
			new Biome.BiomeBuilder()
				.surfaceBuilder(SurfaceBuilder.DEFAULT, SurfaceBuilder.CONFIG_GRASS)
				.precipitation(Biome.Precipitation.RAIN)
				.biomeCategory(Biome.BiomeCategory.RIVER)
				.depth(-0.5F)
				.scale(0.0F)
				.temperature(0.5F)
				.downfall(0.5F)
				.specialEffects(
					new BiomeSpecialEffects.Builder()
						.waterColor(4159204)
						.waterFogColor(329011)
						.fogColor(12638463)
						.ambientMoodSound(AmbientMoodSettings.LEGACY_CAVE_SETTINGS)
						.build()
				)
				.parent(null)
		);
		this.addStructureStart(Feature.MINESHAFT.configured(new MineshaftConfiguration(0.004, MineshaftFeature.Type.NORMAL)));
		BiomeDefaultFeatures.addDefaultCarvers(this);
		BiomeDefaultFeatures.addStructureFeaturePlacement(this);
		BiomeDefaultFeatures.addDefaultLakes(this);
		BiomeDefaultFeatures.addDefaultMonsterRoom(this);
		BiomeDefaultFeatures.addDefaultUndergroundVariety(this);
		BiomeDefaultFeatures.addDefaultOres(this);
		BiomeDefaultFeatures.addDefaultSoftDisks(this);
		BiomeDefaultFeatures.addWaterTrees(this);
		BiomeDefaultFeatures.addDefaultFlowers(this);
		BiomeDefaultFeatures.addDefaultGrass(this);
		BiomeDefaultFeatures.addDefaultMushrooms(this);
		BiomeDefaultFeatures.addDefaultExtraVegetation(this);
		BiomeDefaultFeatures.addDefaultSprings(this);
		this.addFeature(
			GenerationStep.Decoration.VEGETAL_DECORATION,
			Feature.SEAGRASS
				.configured(new SeagrassFeatureConfiguration(48, 0.4))
				.decorated(FeatureDecorator.TOP_SOLID_HEIGHTMAP.configured(DecoratorConfiguration.NONE))
		);
		BiomeDefaultFeatures.addSurfaceFreezing(this);
		this.addSpawn(MobCategory.WATER_CREATURE, new Biome.SpawnerData(EntityType.SQUID, 2, 1, 4));
		this.addSpawn(MobCategory.WATER_CREATURE, new Biome.SpawnerData(EntityType.SALMON, 5, 1, 5));
		this.addSpawn(MobCategory.AMBIENT, new Biome.SpawnerData(EntityType.BAT, 10, 8, 8));
		this.addSpawn(MobCategory.MONSTER, new Biome.SpawnerData(EntityType.SPIDER, 100, 4, 4));
		this.addSpawn(MobCategory.MONSTER, new Biome.SpawnerData(EntityType.ZOMBIE, 95, 4, 4));
		this.addSpawn(MobCategory.MONSTER, new Biome.SpawnerData(EntityType.DROWNED, 100, 1, 1));
		this.addSpawn(MobCategory.MONSTER, new Biome.SpawnerData(EntityType.ZOMBIE_VILLAGER, 5, 1, 1));
		this.addSpawn(MobCategory.MONSTER, new Biome.SpawnerData(EntityType.SKELETON, 100, 4, 4));
		this.addSpawn(MobCategory.MONSTER, new Biome.SpawnerData(EntityType.CREEPER, 100, 4, 4));
		this.addSpawn(MobCategory.MONSTER, new Biome.SpawnerData(EntityType.SLIME, 100, 4, 4));
		this.addSpawn(MobCategory.MONSTER, new Biome.SpawnerData(EntityType.ENDERMAN, 10, 1, 4));
		this.addSpawn(MobCategory.MONSTER, new Biome.SpawnerData(EntityType.WITCH, 5, 1, 1));
	}
}
