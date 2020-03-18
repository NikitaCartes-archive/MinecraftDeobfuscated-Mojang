package net.minecraft.world.level.biome;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.MineshaftFeature;
import net.minecraft.world.level.levelgen.feature.configurations.MineshaftConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.OceanRuinConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.ShipwreckConfiguration;
import net.minecraft.world.level.levelgen.structure.OceanRuinFeature;
import net.minecraft.world.level.levelgen.surfacebuilders.SurfaceBuilder;

public class LukeWarmOceanBiome extends Biome {
	public LukeWarmOceanBiome() {
		super(
			new Biome.BiomeBuilder()
				.surfaceBuilder(SurfaceBuilder.DEFAULT, SurfaceBuilder.CONFIG_OCEAN_SAND)
				.precipitation(Biome.Precipitation.RAIN)
				.biomeCategory(Biome.BiomeCategory.OCEAN)
				.depth(-1.0F)
				.scale(0.1F)
				.temperature(0.5F)
				.downfall(0.5F)
				.specialEffects(
					new BiomeSpecialEffects.Builder()
						.waterColor(4566514)
						.waterFogColor(267827)
						.fogColor(12638463)
						.ambientMoodSound(AmbientMoodSettings.LEGACY_CAVE_SETTINGS)
						.build()
				)
				.parent(null)
		);
		this.addStructureStart(Feature.OCEAN_RUIN.configured(new OceanRuinConfiguration(OceanRuinFeature.Type.WARM, 0.3F, 0.9F)));
		this.addStructureStart(Feature.MINESHAFT.configured(new MineshaftConfiguration(0.004, MineshaftFeature.Type.NORMAL)));
		this.addStructureStart(Feature.SHIPWRECK.configured(new ShipwreckConfiguration(false)));
		BiomeDefaultFeatures.addOceanCarvers(this);
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
		BiomeDefaultFeatures.addWarmSeagrass(this);
		BiomeDefaultFeatures.addLukeWarmKelp(this);
		BiomeDefaultFeatures.addSurfaceFreezing(this);
		this.addSpawn(MobCategory.WATER_CREATURE, new Biome.SpawnerData(EntityType.SQUID, 10, 1, 2));
		this.addSpawn(MobCategory.WATER_CREATURE, new Biome.SpawnerData(EntityType.COD, 15, 3, 6));
		this.addSpawn(MobCategory.WATER_CREATURE, new Biome.SpawnerData(EntityType.PUFFERFISH, 5, 1, 3));
		this.addSpawn(MobCategory.WATER_CREATURE, new Biome.SpawnerData(EntityType.TROPICAL_FISH, 25, 8, 8));
		this.addSpawn(MobCategory.WATER_CREATURE, new Biome.SpawnerData(EntityType.DOLPHIN, 2, 1, 2));
		this.addSpawn(MobCategory.AMBIENT, new Biome.SpawnerData(EntityType.BAT, 10, 8, 8));
		this.addSpawn(MobCategory.MONSTER, new Biome.SpawnerData(EntityType.SPIDER, 100, 4, 4));
		this.addSpawn(MobCategory.MONSTER, new Biome.SpawnerData(EntityType.ZOMBIE, 95, 4, 4));
		this.addSpawn(MobCategory.MONSTER, new Biome.SpawnerData(EntityType.DROWNED, 5, 1, 1));
		this.addSpawn(MobCategory.MONSTER, new Biome.SpawnerData(EntityType.ZOMBIE_VILLAGER, 5, 1, 1));
		this.addSpawn(MobCategory.MONSTER, new Biome.SpawnerData(EntityType.SKELETON, 100, 4, 4));
		this.addSpawn(MobCategory.MONSTER, new Biome.SpawnerData(EntityType.CREEPER, 100, 4, 4));
		this.addSpawn(MobCategory.MONSTER, new Biome.SpawnerData(EntityType.SLIME, 100, 4, 4));
		this.addSpawn(MobCategory.MONSTER, new Biome.SpawnerData(EntityType.ENDERMAN, 10, 1, 4));
		this.addSpawn(MobCategory.MONSTER, new Biome.SpawnerData(EntityType.WITCH, 5, 1, 1));
	}
}
