package net.minecraft.world.level.biome;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.carver.WorldCarver;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.ChanceRangeDecoratorConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.CountRangeDecoratorConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.DecoratorConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.OreConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.ProbabilityFeatureConfiguration;
import net.minecraft.world.level.levelgen.placement.FeatureDecorator;
import net.minecraft.world.level.levelgen.placement.FrequencyDecoratorConfiguration;
import net.minecraft.world.level.levelgen.surfacebuilders.SurfaceBuilder;

public final class NetherBiome extends Biome {
	protected NetherBiome() {
		super(
			new Biome.BiomeBuilder()
				.surfaceBuilder(SurfaceBuilder.NETHER, SurfaceBuilder.CONFIG_HELL)
				.precipitation(Biome.Precipitation.NONE)
				.biomeCategory(Biome.BiomeCategory.NETHER)
				.depth(0.1F)
				.scale(0.2F)
				.temperature(2.0F)
				.downfall(0.0F)
				.waterColor(4159204)
				.waterFogColor(329011)
				.parent(null)
		);
		this.addStructureStart(Feature.NETHER_BRIDGE.configured(FeatureConfiguration.NONE));
		this.addCarver(GenerationStep.Carving.AIR, makeCarver(WorldCarver.HELL_CAVE, new ProbabilityFeatureConfiguration(0.2F)));
		this.addFeature(
			GenerationStep.Decoration.VEGETAL_DECORATION,
			Feature.SPRING
				.configured(BiomeDefaultFeatures.LAVA_SPRING_CONFIG)
				.decorated(FeatureDecorator.COUNT_VERY_BIASED_RANGE.configured(new CountRangeDecoratorConfiguration(20, 8, 16, 256)))
		);
		BiomeDefaultFeatures.addDefaultMushrooms(this);
		this.addFeature(
			GenerationStep.Decoration.UNDERGROUND_DECORATION,
			Feature.NETHER_BRIDGE.configured(FeatureConfiguration.NONE).decorated(FeatureDecorator.NOPE.configured(DecoratorConfiguration.NONE))
		);
		this.addFeature(
			GenerationStep.Decoration.UNDERGROUND_DECORATION,
			Feature.SPRING
				.configured(BiomeDefaultFeatures.OPEN_NETHER_SPRING_CONFIG)
				.decorated(FeatureDecorator.COUNT_RANGE.configured(new CountRangeDecoratorConfiguration(8, 4, 8, 128)))
		);
		this.addFeature(
			GenerationStep.Decoration.UNDERGROUND_DECORATION,
			Feature.RANDOM_PATCH
				.configured(BiomeDefaultFeatures.HELL_FIRE_CONFIG)
				.decorated(FeatureDecorator.HELL_FIRE.configured(new FrequencyDecoratorConfiguration(10)))
		);
		this.addFeature(
			GenerationStep.Decoration.UNDERGROUND_DECORATION,
			Feature.GLOWSTONE_BLOB
				.configured(FeatureConfiguration.NONE)
				.decorated(FeatureDecorator.LIGHT_GEM_CHANCE.configured(new FrequencyDecoratorConfiguration(10)))
		);
		this.addFeature(
			GenerationStep.Decoration.UNDERGROUND_DECORATION,
			Feature.GLOWSTONE_BLOB
				.configured(FeatureConfiguration.NONE)
				.decorated(FeatureDecorator.COUNT_RANGE.configured(new CountRangeDecoratorConfiguration(10, 0, 0, 128)))
		);
		this.addFeature(
			GenerationStep.Decoration.UNDERGROUND_DECORATION,
			Feature.RANDOM_PATCH
				.configured(BiomeDefaultFeatures.BROWN_MUSHROOM_CONFIG)
				.decorated(FeatureDecorator.CHANCE_RANGE.configured(new ChanceRangeDecoratorConfiguration(0.5F, 0, 0, 128)))
		);
		this.addFeature(
			GenerationStep.Decoration.UNDERGROUND_DECORATION,
			Feature.RANDOM_PATCH
				.configured(BiomeDefaultFeatures.RED_MUSHROOM_CONFIG)
				.decorated(FeatureDecorator.CHANCE_RANGE.configured(new ChanceRangeDecoratorConfiguration(0.5F, 0, 0, 128)))
		);
		this.addFeature(
			GenerationStep.Decoration.UNDERGROUND_DECORATION,
			Feature.ORE
				.configured(new OreConfiguration(OreConfiguration.Predicates.NETHERRACK, Blocks.NETHER_QUARTZ_ORE.defaultBlockState(), 14))
				.decorated(FeatureDecorator.COUNT_RANGE.configured(new CountRangeDecoratorConfiguration(16, 10, 20, 128)))
		);
		this.addFeature(
			GenerationStep.Decoration.UNDERGROUND_DECORATION,
			Feature.ORE
				.configured(new OreConfiguration(OreConfiguration.Predicates.NETHERRACK, Blocks.MAGMA_BLOCK.defaultBlockState(), 33))
				.decorated(FeatureDecorator.MAGMA.configured(new FrequencyDecoratorConfiguration(4)))
		);
		this.addFeature(
			GenerationStep.Decoration.UNDERGROUND_DECORATION,
			Feature.SPRING
				.configured(BiomeDefaultFeatures.CLOSED_NETHER_SPRING_CONFIG)
				.decorated(FeatureDecorator.COUNT_RANGE.configured(new CountRangeDecoratorConfiguration(16, 10, 20, 128)))
		);
		this.addSpawn(MobCategory.MONSTER, new Biome.SpawnerData(EntityType.GHAST, 50, 4, 4));
		this.addSpawn(MobCategory.MONSTER, new Biome.SpawnerData(EntityType.ZOMBIE_PIGMAN, 100, 4, 4));
		this.addSpawn(MobCategory.MONSTER, new Biome.SpawnerData(EntityType.MAGMA_CUBE, 2, 4, 4));
		this.addSpawn(MobCategory.MONSTER, new Biome.SpawnerData(EntityType.ENDERMAN, 1, 4, 4));
	}
}
