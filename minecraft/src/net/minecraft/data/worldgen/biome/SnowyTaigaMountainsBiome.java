package net.minecraft.data.worldgen.biome;

import net.minecraft.data.worldgen.BiomeDefaultFeatures;
import net.minecraft.data.worldgen.StructureFeatures;
import net.minecraft.data.worldgen.SurfaceBuilders;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.biome.AmbientMoodSettings;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeSpecialEffects;

public final class SnowyTaigaMountainsBiome extends Biome {
	public SnowyTaigaMountainsBiome() {
		super(
			new Biome.BiomeBuilder()
				.surfaceBuilder(SurfaceBuilders.GRASS)
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
		BiomeDefaultFeatures.addDefaultOverworldLandStructures(this);
		this.addStructureStart(StructureFeatures.RUINED_PORTAL_MOUNTAIN);
		BiomeDefaultFeatures.addDefaultCarvers(this);
		BiomeDefaultFeatures.addDefaultLakes(this);
		BiomeDefaultFeatures.addDefaultMonsterRoom(this);
		BiomeDefaultFeatures.addFerns(this);
		BiomeDefaultFeatures.addDefaultUndergroundVariety(this);
		BiomeDefaultFeatures.addDefaultOres(this);
		BiomeDefaultFeatures.addDefaultSoftDisks(this);
		BiomeDefaultFeatures.addTaigaTrees(this);
		BiomeDefaultFeatures.addDefaultFlowers(this);
		BiomeDefaultFeatures.addTaigaGrass(this);
		BiomeDefaultFeatures.addDefaultMushrooms(this);
		BiomeDefaultFeatures.addDefaultExtraVegetation(this);
		BiomeDefaultFeatures.addDefaultSprings(this);
		BiomeDefaultFeatures.addBerryBushes(this);
		BiomeDefaultFeatures.addSurfaceFreezing(this);
		BiomeDefaultFeatures.farmAnimals(this);
		this.addSpawn(MobCategory.CREATURE, new Biome.SpawnerData(EntityType.WOLF, 8, 4, 4));
		this.addSpawn(MobCategory.CREATURE, new Biome.SpawnerData(EntityType.RABBIT, 4, 2, 3));
		this.addSpawn(MobCategory.CREATURE, new Biome.SpawnerData(EntityType.FOX, 8, 2, 4));
		BiomeDefaultFeatures.commonSpawns(this);
	}
}
