package net.minecraft.world.level.biome;

import java.util.Random;
import net.minecraft.Util;
import net.minecraft.core.Registry;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.StructureFeature;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;

public class BiomeGenerator {
	public static Biome magicize(int i) {
		Random random = new Random((long)i);
		return new BiomeGenerator.GeneratedBiome(random);
	}

	static class GeneratedBiome extends Biome {
		public GeneratedBiome(Random random) {
			super(Biome.random(random));
			Util.randomObjectStream(random, 4, Registry.CARVER)
				.forEach(worldCarver -> this.addCarver(Util.randomEnum(GenerationStep.Carving.class, random), Biome.makeRandomCarver(worldCarver, random)));
			Util.randomObjectStream(random, 32, Registry.ENTITY_TYPE).forEach(entityType -> {
				int ix = random.nextInt(4);
				int j = ix + random.nextInt(4);
				this.addSpawn(entityType.getCategory(), new Biome.SpawnerData(entityType, random.nextInt(20) + 1, ix, j));
			});
			Util.randomObjectStream(random, 5, Registry.STRUCTURE_FEATURE)
				.forEach(structureFeature -> this.addFeatureStart(Util.randomEnum(GenerationStep.Decoration.class, random), structureFeature.random2(random)));

			for (int i = 0; i < 32; i++) {
				this.addFeature(
					Util.randomEnum(GenerationStep.Decoration.class, random),
					Registry.FEATURE.getRandom(random).random(random).decorated(Registry.DECORATOR.getRandom(random).random(random))
				);
			}
		}

		private <C extends FeatureConfiguration> void addFeatureStart(
			GenerationStep.Decoration decoration, ConfiguredFeature<C, ? extends StructureFeature<C>> configuredFeature
		) {
			this.addStructureStart(configuredFeature);
			this.addFeature(decoration, configuredFeature);
		}
	}
}
