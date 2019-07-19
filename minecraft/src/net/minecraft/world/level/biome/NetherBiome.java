package net.minecraft.world.level.biome;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.carver.WorldCarver;
import net.minecraft.world.level.levelgen.feature.BushConfiguration;
import net.minecraft.world.level.levelgen.feature.DecoratorChanceRange;
import net.minecraft.world.level.levelgen.feature.DecoratorConfiguration;
import net.minecraft.world.level.levelgen.feature.DecoratorCountRange;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.HellSpringConfiguration;
import net.minecraft.world.level.levelgen.feature.OreConfiguration;
import net.minecraft.world.level.levelgen.feature.ProbabilityFeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.SpringConfiguration;
import net.minecraft.world.level.levelgen.placement.DecoratorFrequency;
import net.minecraft.world.level.levelgen.placement.FeatureDecorator;
import net.minecraft.world.level.levelgen.surfacebuilders.SurfaceBuilder;
import net.minecraft.world.level.material.Fluids;

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
		this.addStructureStart(Feature.NETHER_BRIDGE, FeatureConfiguration.NONE);
		this.addCarver(GenerationStep.Carving.AIR, makeCarver(WorldCarver.HELL_CAVE, new ProbabilityFeatureConfiguration(0.2F)));
		this.addFeature(
			GenerationStep.Decoration.VEGETAL_DECORATION,
			makeComposite(
				Feature.SPRING, new SpringConfiguration(Fluids.LAVA.defaultFluidState()), FeatureDecorator.COUNT_VERY_BIASED_RANGE, new DecoratorCountRange(20, 8, 16, 256)
			)
		);
		BiomeDefaultFeatures.addDefaultMushrooms(this);
		this.addFeature(
			GenerationStep.Decoration.UNDERGROUND_DECORATION,
			makeComposite(Feature.NETHER_BRIDGE, FeatureConfiguration.NONE, FeatureDecorator.NOPE, DecoratorConfiguration.NONE)
		);
		this.addFeature(
			GenerationStep.Decoration.UNDERGROUND_DECORATION,
			makeComposite(Feature.NETHER_SPRING, new HellSpringConfiguration(false), FeatureDecorator.COUNT_RANGE, new DecoratorCountRange(8, 4, 8, 128))
		);
		this.addFeature(
			GenerationStep.Decoration.UNDERGROUND_DECORATION,
			makeComposite(Feature.HELL_FIRE, FeatureConfiguration.NONE, FeatureDecorator.HELL_FIRE, new DecoratorFrequency(10))
		);
		this.addFeature(
			GenerationStep.Decoration.UNDERGROUND_DECORATION,
			makeComposite(Feature.GLOWSTONE_BLOB, FeatureConfiguration.NONE, FeatureDecorator.LIGHT_GEM_CHANCE, new DecoratorFrequency(10))
		);
		this.addFeature(
			GenerationStep.Decoration.UNDERGROUND_DECORATION,
			makeComposite(Feature.GLOWSTONE_BLOB, FeatureConfiguration.NONE, FeatureDecorator.COUNT_RANGE, new DecoratorCountRange(10, 0, 0, 128))
		);
		this.addFeature(
			GenerationStep.Decoration.UNDERGROUND_DECORATION,
			makeComposite(
				Feature.BUSH, new BushConfiguration(Blocks.BROWN_MUSHROOM.defaultBlockState()), FeatureDecorator.CHANCE_RANGE, new DecoratorChanceRange(0.5F, 0, 0, 128)
			)
		);
		this.addFeature(
			GenerationStep.Decoration.UNDERGROUND_DECORATION,
			makeComposite(
				Feature.BUSH, new BushConfiguration(Blocks.RED_MUSHROOM.defaultBlockState()), FeatureDecorator.CHANCE_RANGE, new DecoratorChanceRange(0.5F, 0, 0, 128)
			)
		);
		this.addFeature(
			GenerationStep.Decoration.UNDERGROUND_DECORATION,
			makeComposite(
				Feature.ORE,
				new OreConfiguration(OreConfiguration.Predicates.NETHERRACK, Blocks.NETHER_QUARTZ_ORE.defaultBlockState(), 14),
				FeatureDecorator.COUNT_RANGE,
				new DecoratorCountRange(16, 10, 20, 128)
			)
		);
		this.addFeature(
			GenerationStep.Decoration.UNDERGROUND_DECORATION,
			makeComposite(
				Feature.ORE,
				new OreConfiguration(OreConfiguration.Predicates.NETHERRACK, Blocks.MAGMA_BLOCK.defaultBlockState(), 33),
				FeatureDecorator.MAGMA,
				new DecoratorFrequency(4)
			)
		);
		this.addFeature(
			GenerationStep.Decoration.UNDERGROUND_DECORATION,
			makeComposite(Feature.NETHER_SPRING, new HellSpringConfiguration(true), FeatureDecorator.COUNT_RANGE, new DecoratorCountRange(16, 10, 20, 128))
		);
		this.addSpawn(MobCategory.MONSTER, new Biome.SpawnerData(EntityType.GHAST, 50, 4, 4));
		this.addSpawn(MobCategory.MONSTER, new Biome.SpawnerData(EntityType.ZOMBIE_PIGMAN, 100, 4, 4));
		this.addSpawn(MobCategory.MONSTER, new Biome.SpawnerData(EntityType.MAGMA_CUBE, 2, 4, 4));
		this.addSpawn(MobCategory.MONSTER, new Biome.SpawnerData(EntityType.ENDERMAN, 1, 4, 4));
	}
}
