package net.minecraft.world.level.biome;

import com.google.common.collect.ImmutableList;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.MineshaftFeature;
import net.minecraft.world.level.levelgen.feature.RuinedPortalFeature;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.MineshaftConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.RuinedPortalConfiguration;
import net.minecraft.world.level.levelgen.surfacebuilders.SurfaceBuilder;

public final class BadlandsBiome extends Biome {
	public BadlandsBiome() {
		super(
			new Biome.BiomeBuilder()
				.surfaceBuilder(SurfaceBuilder.BADLANDS, SurfaceBuilder.CONFIG_BADLANDS)
				.precipitation(Biome.Precipitation.NONE)
				.biomeCategory(Biome.BiomeCategory.MESA)
				.depth(0.1F)
				.scale(0.2F)
				.temperature(2.0F)
				.downfall(0.0F)
				.specialEffects(
					new BiomeSpecialEffects.Builder()
						.waterColor(4159204)
						.waterFogColor(329011)
						.fogColor(12638463)
						.ambientMoodSound(AmbientMoodSettings.LEGACY_CAVE_SETTINGS)
						.build()
				)
				.parent(null)
				.optimalParameters(ImmutableList.of(new Biome.ClimateParameters(-0.25F, -0.5F, 0.5F, 0.2F, 1.0F)))
		);
		this.addStructureStart(Feature.MINESHAFT.configured(new MineshaftConfiguration(0.004, MineshaftFeature.Type.MESA)));
		this.addStructureStart(Feature.STRONGHOLD.configured(FeatureConfiguration.NONE));
		this.addStructureStart(Feature.RUINED_PORTAL.configured(new RuinedPortalConfiguration(RuinedPortalFeature.Type.STANDARD)));
		BiomeDefaultFeatures.addDefaultCarvers(this);
		BiomeDefaultFeatures.addStructureFeaturePlacement(this);
		BiomeDefaultFeatures.addDefaultLakes(this);
		BiomeDefaultFeatures.addDefaultMonsterRoom(this);
		BiomeDefaultFeatures.addDefaultUndergroundVariety(this);
		BiomeDefaultFeatures.addDefaultOres(this);
		BiomeDefaultFeatures.addExtraGold(this);
		BiomeDefaultFeatures.addDefaultSoftDisks(this);
		BiomeDefaultFeatures.addBadlandGrass(this);
		BiomeDefaultFeatures.addDefaultMushrooms(this);
		BiomeDefaultFeatures.addBadlandExtraVegetation(this);
		BiomeDefaultFeatures.addDefaultSprings(this);
		BiomeDefaultFeatures.addSurfaceFreezing(this);
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

	@Environment(EnvType.CLIENT)
	@Override
	public int getFoliageColor() {
		return 10387789;
	}

	@Environment(EnvType.CLIENT)
	@Override
	public int getGrassColor(double d, double e) {
		return 9470285;
	}
}
