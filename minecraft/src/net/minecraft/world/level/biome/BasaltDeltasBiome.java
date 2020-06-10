package net.minecraft.world.level.biome;

import com.google.common.collect.ImmutableList;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.Musics;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.carver.WorldCarver;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.ChanceRangeDecoratorConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.CountRangeDecoratorConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.OreConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.ProbabilityFeatureConfiguration;
import net.minecraft.world.level.levelgen.placement.FeatureDecorator;
import net.minecraft.world.level.levelgen.placement.FrequencyDecoratorConfiguration;
import net.minecraft.world.level.levelgen.surfacebuilders.SurfaceBuilder;

public class BasaltDeltasBiome extends Biome {
	protected BasaltDeltasBiome() {
		super(
			new Biome.BiomeBuilder()
				.surfaceBuilder(SurfaceBuilder.BASALT_DELTAS, SurfaceBuilder.CONFIG_BASALT_DELTAS)
				.precipitation(Biome.Precipitation.NONE)
				.biomeCategory(Biome.BiomeCategory.NETHER)
				.depth(0.1F)
				.scale(0.2F)
				.temperature(2.0F)
				.downfall(0.0F)
				.specialEffects(
					new BiomeSpecialEffects.Builder()
						.waterColor(4159204)
						.waterFogColor(4341314)
						.fogColor(6840176)
						.ambientParticle(new AmbientParticleSettings(ParticleTypes.WHITE_ASH, 0.118093334F))
						.ambientLoopSound(SoundEvents.AMBIENT_BASALT_DELTAS_LOOP)
						.ambientMoodSound(new AmbientMoodSettings(SoundEvents.AMBIENT_BASALT_DELTAS_MOOD, 6000, 8, 2.0))
						.ambientAdditionsSound(new AmbientAdditionsSettings(SoundEvents.AMBIENT_BASALT_DELTAS_ADDITIONS, 0.0111))
						.backgroundMusic(Musics.createGameMusic(SoundEvents.MUSIC_BIOME_BASALT_DELTAS))
						.build()
				)
				.parent(null)
				.optimalParameters(ImmutableList.of(new Biome.ClimateParameters(-0.5F, 0.0F, 0.0F, 0.0F, 0.175F)))
		);
		this.addStructureStart(BiomeDefaultFeatures.RUINED_PORTAL_NETHER);
		this.addCarver(GenerationStep.Carving.AIR, makeCarver(WorldCarver.NETHER_CAVE, new ProbabilityFeatureConfiguration(0.2F)));
		this.addStructureStart(BiomeDefaultFeatures.NETHER_BRIDGE);
		this.addFeature(
			GenerationStep.Decoration.SURFACE_STRUCTURES,
			Feature.DELTA_FEATURE
				.configured(BiomeDefaultFeatures.BASALT_DELTA_FEATURE_CONFIG)
				.decorated(FeatureDecorator.COUNT_HEIGHTMAP.configured(new FrequencyDecoratorConfiguration(40)))
		);
		this.addFeature(
			GenerationStep.Decoration.VEGETAL_DECORATION,
			Feature.SPRING
				.configured(BiomeDefaultFeatures.LAVA_SPRING_CONFIG)
				.decorated(FeatureDecorator.COUNT_VERY_BIASED_RANGE.configured(new CountRangeDecoratorConfiguration(40, 8, 16, 256)))
		);
		this.addFeature(
			GenerationStep.Decoration.SURFACE_STRUCTURES,
			Feature.BASALT_COLUMNS
				.configured(BiomeDefaultFeatures.SMALL_BASALT_COLUMN_FEATURE_CONFIG)
				.decorated(FeatureDecorator.COUNT_HEIGHTMAP.configured(new FrequencyDecoratorConfiguration(4)))
		);
		this.addFeature(
			GenerationStep.Decoration.SURFACE_STRUCTURES,
			Feature.BASALT_COLUMNS
				.configured(BiomeDefaultFeatures.LARGE_BASALT_COLUMN_FEATURE_CONFIG)
				.decorated(FeatureDecorator.COUNT_HEIGHTMAP.configured(new FrequencyDecoratorConfiguration(2)))
		);
		this.addFeature(
			GenerationStep.Decoration.UNDERGROUND_DECORATION,
			Feature.REPLACE_BLOBS
				.configured(BiomeDefaultFeatures.BASALT_BLOBS_FEATURE_CONFIG)
				.decorated(FeatureDecorator.COUNT_RANGE.configured(new CountRangeDecoratorConfiguration(75, 0, 0, 128)))
		);
		this.addFeature(
			GenerationStep.Decoration.UNDERGROUND_DECORATION,
			Feature.REPLACE_BLOBS
				.configured(BiomeDefaultFeatures.BLACKSTONE_BLOBS_FEATURE_CONFIG)
				.decorated(FeatureDecorator.COUNT_RANGE.configured(new CountRangeDecoratorConfiguration(25, 0, 0, 128)))
		);
		this.addFeature(
			GenerationStep.Decoration.UNDERGROUND_DECORATION,
			Feature.SPRING
				.configured(BiomeDefaultFeatures.BASALT_DELTA_SPRING_CONFIG)
				.decorated(FeatureDecorator.COUNT_RANGE.configured(new CountRangeDecoratorConfiguration(16, 4, 8, 128)))
		);
		this.addFeature(
			GenerationStep.Decoration.UNDERGROUND_DECORATION,
			Feature.RANDOM_PATCH.configured(BiomeDefaultFeatures.FIRE_CONFIG).decorated(FeatureDecorator.FIRE.configured(new FrequencyDecoratorConfiguration(10)))
		);
		this.addFeature(
			GenerationStep.Decoration.UNDERGROUND_DECORATION,
			Feature.RANDOM_PATCH.configured(BiomeDefaultFeatures.SOUL_FIRE_CONFIG).decorated(FeatureDecorator.FIRE.configured(new FrequencyDecoratorConfiguration(10)))
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
				.configured(new OreConfiguration(OreConfiguration.Predicates.NETHERRACK, Blocks.MAGMA_BLOCK.defaultBlockState(), 33))
				.decorated(FeatureDecorator.MAGMA.configured(new FrequencyDecoratorConfiguration(4)))
		);
		this.addFeature(
			GenerationStep.Decoration.UNDERGROUND_DECORATION,
			Feature.SPRING
				.configured(BiomeDefaultFeatures.CLOSED_NETHER_SPRING_CONFIG)
				.decorated(FeatureDecorator.COUNT_RANGE.configured(new CountRangeDecoratorConfiguration(32, 10, 20, 128)))
		);
		BiomeDefaultFeatures.addNetherOres(this, 20, 32);
		BiomeDefaultFeatures.addAncientDebris(this);
		this.addSpawn(MobCategory.MONSTER, new Biome.SpawnerData(EntityType.GHAST, 40, 1, 1));
		this.addSpawn(MobCategory.MONSTER, new Biome.SpawnerData(EntityType.MAGMA_CUBE, 100, 2, 5));
		this.addSpawn(MobCategory.CREATURE, new Biome.SpawnerData(EntityType.STRIDER, 60, 1, 2));
	}
}
