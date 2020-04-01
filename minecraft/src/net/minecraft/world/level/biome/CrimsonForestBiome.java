package net.minecraft.world.level.biome;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.carver.WorldCarver;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.CountRangeDecoratorConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.DecoratorConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.OreConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.ProbabilityFeatureConfiguration;
import net.minecraft.world.level.levelgen.placement.FeatureDecorator;
import net.minecraft.world.level.levelgen.placement.FrequencyDecoratorConfiguration;
import net.minecraft.world.level.levelgen.surfacebuilders.SurfaceBuilder;

public class CrimsonForestBiome extends Biome {
	protected CrimsonForestBiome() {
		super(
			new Biome.BiomeBuilder()
				.surfaceBuilder(SurfaceBuilder.NETHER_FOREST, SurfaceBuilder.CONFIG_CRIMSON_FOREST)
				.precipitation(Biome.Precipitation.NONE)
				.biomeCategory(Biome.BiomeCategory.NETHER)
				.depth(0.1F)
				.scale(0.2F)
				.temperature(2.0F)
				.downfall(0.0F)
				.specialEffects(
					new BiomeSpecialEffects.Builder()
						.waterColor(4159204)
						.waterFogColor(329011)
						.fogColor(3343107)
						.ambientParticle(new AmbientParticleSettings(ParticleTypes.CRIMSON_SPORE, 0.025F, 1.0E-6F, 1.0E-4F, 1.0E-6F))
						.ambientLoopSound(SoundEvents.AMBIENT_CRIMSON_FOREST_LOOP)
						.ambientMoodSound(new AmbientMoodSettings(SoundEvents.AMBIENT_CRIMSON_FOREST_MOOD, 6000, 8, 2.0))
						.ambientAdditionsSound(new AmbientAdditionsSettings(SoundEvents.AMBIENT_CRIMSON_FOREST_ADDITIONS, 0.0111))
						.build()
				)
				.parent(null)
		);
		this.addCarver(GenerationStep.Carving.AIR, makeCarver(WorldCarver.NETHER_CAVE, new ProbabilityFeatureConfiguration(0.2F)));
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
			Feature.RANDOM_PATCH.configured(BiomeDefaultFeatures.FIRE_CONFIG).decorated(FeatureDecorator.FIRE.configured(new FrequencyDecoratorConfiguration(10)))
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
		this.addFeature(
			GenerationStep.Decoration.VEGETAL_DECORATION,
			Feature.WEEPING_VINES
				.configured(FeatureConfiguration.NONE)
				.decorated(FeatureDecorator.COUNT_RANGE.configured(new CountRangeDecoratorConfiguration(10, 0, 0, 128)))
		);
		BiomeDefaultFeatures.addCrimsonForestVegetation(this);
		BiomeDefaultFeatures.addNetherDefaultOres(this);
		this.addSpawn(MobCategory.MONSTER, new Biome.SpawnerData(EntityType.ZOMBIFIED_PIGLIN, 1, 2, 4));
		this.addSpawn(MobCategory.MONSTER, new Biome.SpawnerData(EntityType.HOGLIN, 9, 3, 4));
		this.addSpawn(MobCategory.MONSTER, new Biome.SpawnerData(EntityType.PIGLIN, 5, 3, 4));
		this.addSpawn(MobCategory.CREATURE, new Biome.SpawnerData(EntityType.STRIDER, 60, 2, 4));
	}
}
