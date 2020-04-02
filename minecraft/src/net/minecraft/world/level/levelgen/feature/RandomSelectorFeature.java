package net.minecraft.world.level.levelgen.feature;

import com.mojang.datafixers.Dynamic;
import java.util.Random;
import java.util.function.Function;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.StructureFeatureManager;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.ChunkGeneratorSettings;
import net.minecraft.world.level.levelgen.feature.configurations.RandomFeatureConfiguration;

public class RandomSelectorFeature extends Feature<RandomFeatureConfiguration> {
	public RandomSelectorFeature(Function<Dynamic<?>, ? extends RandomFeatureConfiguration> function) {
		super(function);
	}

	public boolean place(
		LevelAccessor levelAccessor,
		StructureFeatureManager structureFeatureManager,
		ChunkGenerator<? extends ChunkGeneratorSettings> chunkGenerator,
		Random random,
		BlockPos blockPos,
		RandomFeatureConfiguration randomFeatureConfiguration
	) {
		for (WeightedConfiguredFeature<?> weightedConfiguredFeature : randomFeatureConfiguration.features) {
			if (random.nextFloat() < weightedConfiguredFeature.chance) {
				return weightedConfiguredFeature.place(levelAccessor, structureFeatureManager, chunkGenerator, random, blockPos);
			}
		}

		return randomFeatureConfiguration.defaultFeature.place(levelAccessor, structureFeatureManager, chunkGenerator, random, blockPos);
	}
}
