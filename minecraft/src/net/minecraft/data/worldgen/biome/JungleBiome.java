package net.minecraft.data.worldgen.biome;

import net.minecraft.data.worldgen.BiomeDefaultFeatures;
import net.minecraft.data.worldgen.StructureFeatures;
import net.minecraft.data.worldgen.SurfaceBuilders;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.biome.AmbientMoodSettings;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeSpecialEffects;

public final class JungleBiome extends Biome {
	public JungleBiome() {
		super(
			new Biome.BiomeBuilder()
				.surfaceBuilder(SurfaceBuilders.GRASS)
				.precipitation(Biome.Precipitation.RAIN)
				.biomeCategory(Biome.BiomeCategory.JUNGLE)
				.depth(0.1F)
				.scale(0.2F)
				.temperature(0.95F)
				.downfall(0.9F)
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
		this.addStructureStart(StructureFeatures.JUNGLE_TEMPLE);
		BiomeDefaultFeatures.addDefaultOverworldLandStructures(this);
		this.addStructureStart(StructureFeatures.RUINED_PORTAL_JUNGLE);
		BiomeDefaultFeatures.addDefaultCarvers(this);
		BiomeDefaultFeatures.addDefaultLakes(this);
		BiomeDefaultFeatures.addDefaultMonsterRoom(this);
		BiomeDefaultFeatures.addDefaultUndergroundVariety(this);
		BiomeDefaultFeatures.addDefaultOres(this);
		BiomeDefaultFeatures.addDefaultSoftDisks(this);
		BiomeDefaultFeatures.addLightBambooVegetation(this);
		BiomeDefaultFeatures.addJungleTrees(this);
		BiomeDefaultFeatures.addWarmFlowers(this);
		BiomeDefaultFeatures.addJungleGrass(this);
		BiomeDefaultFeatures.addDefaultMushrooms(this);
		BiomeDefaultFeatures.addDefaultExtraVegetation(this);
		BiomeDefaultFeatures.addDefaultSprings(this);
		BiomeDefaultFeatures.addJungleExtraVegetation(this);
		BiomeDefaultFeatures.addSurfaceFreezing(this);
		BiomeDefaultFeatures.baseJungleSpawns(this);
		this.addSpawn(MobCategory.CREATURE, new Biome.SpawnerData(EntityType.PARROT, 40, 1, 2));
		this.addSpawn(MobCategory.MONSTER, new Biome.SpawnerData(EntityType.OCELOT, 2, 1, 3));
		this.addSpawn(MobCategory.CREATURE, new Biome.SpawnerData(EntityType.PANDA, 1, 1, 2));
	}
}
