package net.minecraft.data.worldgen.biome;

import net.minecraft.data.worldgen.BiomeDefaultFeatures;
import net.minecraft.data.worldgen.Features;
import net.minecraft.data.worldgen.StructureFeatures;
import net.minecraft.data.worldgen.SurfaceBuilders;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.biome.AmbientMoodSettings;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeSpecialEffects;
import net.minecraft.world.level.levelgen.GenerationStep;

public class DeepLukeWarmOceanBiome extends Biome {
	public DeepLukeWarmOceanBiome() {
		super(
			new Biome.BiomeBuilder()
				.surfaceBuilder(SurfaceBuilders.OCEAN_SAND)
				.precipitation(Biome.Precipitation.RAIN)
				.biomeCategory(Biome.BiomeCategory.OCEAN)
				.depth(-1.8F)
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
		this.addStructureStart(StructureFeatures.OCEAN_RUIN_WARM);
		this.addStructureStart(StructureFeatures.OCEAN_MONUMENT);
		BiomeDefaultFeatures.addDefaultOverworldOceanStructures(this);
		this.addStructureStart(StructureFeatures.RUINED_PORTAL_OCEAN);
		BiomeDefaultFeatures.addOceanCarvers(this);
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
		this.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, Features.SEAGRASS_DEEP_WARM);
		BiomeDefaultFeatures.addDefaultSeagrass(this);
		BiomeDefaultFeatures.addLukeWarmKelp(this);
		BiomeDefaultFeatures.addSurfaceFreezing(this);
		this.addSpawn(MobCategory.WATER_CREATURE, new Biome.SpawnerData(EntityType.SQUID, 8, 1, 4));
		this.addSpawn(MobCategory.WATER_AMBIENT, new Biome.SpawnerData(EntityType.COD, 8, 3, 6));
		this.addSpawn(MobCategory.WATER_AMBIENT, new Biome.SpawnerData(EntityType.PUFFERFISH, 5, 1, 3));
		this.addSpawn(MobCategory.WATER_AMBIENT, new Biome.SpawnerData(EntityType.TROPICAL_FISH, 25, 8, 8));
		this.addSpawn(MobCategory.WATER_CREATURE, new Biome.SpawnerData(EntityType.DOLPHIN, 2, 1, 2));
		BiomeDefaultFeatures.commonSpawns(this);
		this.addSpawn(MobCategory.MONSTER, new Biome.SpawnerData(EntityType.DROWNED, 5, 1, 1));
	}
}
