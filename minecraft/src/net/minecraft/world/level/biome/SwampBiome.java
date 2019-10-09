package net.minecraft.world.level.biome;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.MineshaftFeature;
import net.minecraft.world.level.levelgen.feature.configurations.DecoratorConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.MineshaftConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.SeagrassFeatureConfiguration;
import net.minecraft.world.level.levelgen.placement.FeatureDecorator;
import net.minecraft.world.level.levelgen.surfacebuilders.SurfaceBuilder;

public final class SwampBiome extends Biome {
	protected SwampBiome() {
		super(
			new Biome.BiomeBuilder()
				.surfaceBuilder(SurfaceBuilder.SWAMP, SurfaceBuilder.CONFIG_GRASS)
				.precipitation(Biome.Precipitation.RAIN)
				.biomeCategory(Biome.BiomeCategory.SWAMP)
				.depth(-0.2F)
				.scale(0.1F)
				.temperature(0.8F)
				.downfall(0.9F)
				.waterColor(6388580)
				.waterFogColor(2302743)
				.parent(null)
		);
		this.addStructureStart(Feature.SWAMP_HUT.configured(FeatureConfiguration.NONE));
		this.addStructureStart(Feature.MINESHAFT.configured(new MineshaftConfiguration(0.004, MineshaftFeature.Type.NORMAL)));
		BiomeDefaultFeatures.addDefaultCarvers(this);
		BiomeDefaultFeatures.addStructureFeaturePlacement(this);
		BiomeDefaultFeatures.addDefaultLakes(this);
		BiomeDefaultFeatures.addDefaultMonsterRoom(this);
		BiomeDefaultFeatures.addDefaultUndergroundVariety(this);
		BiomeDefaultFeatures.addDefaultOres(this);
		BiomeDefaultFeatures.addSwampClayDisk(this);
		BiomeDefaultFeatures.addSwampVegetation(this);
		BiomeDefaultFeatures.addDefaultMushrooms(this);
		BiomeDefaultFeatures.addSwampExtraVegetation(this);
		BiomeDefaultFeatures.addDefaultSprings(this);
		this.addFeature(
			GenerationStep.Decoration.VEGETAL_DECORATION,
			Feature.SEAGRASS
				.configured(new SeagrassFeatureConfiguration(64, 0.6))
				.decorated(FeatureDecorator.TOP_SOLID_HEIGHTMAP.configured(DecoratorConfiguration.NONE))
		);
		BiomeDefaultFeatures.addSwampExtraDecoration(this);
		BiomeDefaultFeatures.addSurfaceFreezing(this);
		this.addSpawn(MobCategory.CREATURE, new Biome.SpawnerData(EntityType.SHEEP, 12, 4, 4));
		this.addSpawn(MobCategory.CREATURE, new Biome.SpawnerData(EntityType.PIG, 10, 4, 4));
		this.addSpawn(MobCategory.CREATURE, new Biome.SpawnerData(EntityType.CHICKEN, 10, 4, 4));
		this.addSpawn(MobCategory.CREATURE, new Biome.SpawnerData(EntityType.COW, 8, 4, 4));
		this.addSpawn(MobCategory.AMBIENT, new Biome.SpawnerData(EntityType.BAT, 10, 8, 8));
		this.addSpawn(MobCategory.MONSTER, new Biome.SpawnerData(EntityType.SPIDER, 100, 4, 4));
		this.addSpawn(MobCategory.MONSTER, new Biome.SpawnerData(EntityType.ZOMBIE, 95, 4, 4));
		this.addSpawn(MobCategory.MONSTER, new Biome.SpawnerData(EntityType.ZOMBIE_VILLAGER, 5, 1, 1));
		this.addSpawn(MobCategory.MONSTER, new Biome.SpawnerData(EntityType.SKELETON, 100, 4, 4));
		this.addSpawn(MobCategory.MONSTER, new Biome.SpawnerData(EntityType.CREEPER, 100, 4, 4));
		this.addSpawn(MobCategory.MONSTER, new Biome.SpawnerData(EntityType.SLIME, 100, 4, 4));
		this.addSpawn(MobCategory.MONSTER, new Biome.SpawnerData(EntityType.ENDERMAN, 10, 1, 4));
		this.addSpawn(MobCategory.MONSTER, new Biome.SpawnerData(EntityType.WITCH, 5, 1, 1));
		this.addSpawn(MobCategory.MONSTER, new Biome.SpawnerData(EntityType.SLIME, 1, 1, 1));
	}

	@Environment(EnvType.CLIENT)
	@Override
	public int getGrassColor(BlockPos blockPos) {
		double d = BIOME_INFO_NOISE.getValue((double)blockPos.getX() * 0.0225, (double)blockPos.getZ() * 0.0225, false);
		return d < -0.1 ? 5011004 : 6975545;
	}

	@Environment(EnvType.CLIENT)
	@Override
	public int getFoliageColor(BlockPos blockPos) {
		return 6975545;
	}
}
