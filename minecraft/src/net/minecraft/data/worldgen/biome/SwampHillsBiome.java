package net.minecraft.data.worldgen.biome;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.data.worldgen.BiomeDefaultFeatures;
import net.minecraft.data.worldgen.StructureFeatures;
import net.minecraft.data.worldgen.SurfaceBuilders;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.biome.AmbientMoodSettings;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeSpecialEffects;

public final class SwampHillsBiome extends Biome {
	public SwampHillsBiome() {
		super(
			new Biome.BiomeBuilder()
				.surfaceBuilder(SurfaceBuilders.SWAMP)
				.precipitation(Biome.Precipitation.RAIN)
				.biomeCategory(Biome.BiomeCategory.SWAMP)
				.depth(-0.1F)
				.scale(0.3F)
				.temperature(0.8F)
				.downfall(0.9F)
				.specialEffects(
					new BiomeSpecialEffects.Builder()
						.waterColor(6388580)
						.waterFogColor(2302743)
						.fogColor(12638463)
						.ambientMoodSound(AmbientMoodSettings.LEGACY_CAVE_SETTINGS)
						.build()
				)
				.parent("swamp")
		);
		this.addStructureStart(StructureFeatures.MINESHAFT);
		this.addStructureStart(StructureFeatures.RUINED_PORTAL_SWAMP);
		BiomeDefaultFeatures.addDefaultCarvers(this);
		BiomeDefaultFeatures.addDefaultLakes(this);
		BiomeDefaultFeatures.addDefaultMonsterRoom(this);
		BiomeDefaultFeatures.addDefaultUndergroundVariety(this);
		BiomeDefaultFeatures.addDefaultOres(this);
		BiomeDefaultFeatures.addSwampClayDisk(this);
		BiomeDefaultFeatures.addSwampVegetation(this);
		BiomeDefaultFeatures.addDefaultMushrooms(this);
		BiomeDefaultFeatures.addSwampExtraVegetation(this);
		BiomeDefaultFeatures.addDefaultSprings(this);
		BiomeDefaultFeatures.addFossilDecoration(this);
		BiomeDefaultFeatures.addSurfaceFreezing(this);
		BiomeDefaultFeatures.farmAnimals(this);
		BiomeDefaultFeatures.commonSpawns(this);
		this.addSpawn(MobCategory.MONSTER, new Biome.SpawnerData(EntityType.SLIME, 1, 1, 1));
	}

	@Environment(EnvType.CLIENT)
	@Override
	public int getGrassColor(double d, double e) {
		double f = BIOME_INFO_NOISE.getValue(d * 0.0225, e * 0.0225, false);
		return f < -0.1 ? 5011004 : 6975545;
	}

	@Environment(EnvType.CLIENT)
	@Override
	public int getFoliageColor() {
		return 6975545;
	}
}
