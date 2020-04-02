package net.minecraft.world.level.levelgen.feature;

import com.mojang.datafixers.Dynamic;
import java.util.Random;
import java.util.function.Function;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.StructureFeatureManager;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.ChunkGeneratorSettings;
import net.minecraft.world.level.levelgen.feature.configurations.RandomRandomFeatureConfiguration;

public class RandomRandomFeature extends Feature<RandomRandomFeatureConfiguration> {
	public RandomRandomFeature(Function<Dynamic<?>, ? extends RandomRandomFeatureConfiguration> function) {
		super(function);
	}

	public boolean place(
		LevelAccessor levelAccessor,
		StructureFeatureManager structureFeatureManager,
		ChunkGenerator<? extends ChunkGeneratorSettings> chunkGenerator,
		Random random,
		BlockPos blockPos,
		RandomRandomFeatureConfiguration randomRandomFeatureConfiguration
	) {
		int i = random.nextInt(5) - 3 + randomRandomFeatureConfiguration.count;

		for (int j = 0; j < i; j++) {
			int k = random.nextInt(randomRandomFeatureConfiguration.features.size());
			ConfiguredFeature<?, ?> configuredFeature = (ConfiguredFeature<?, ?>)randomRandomFeatureConfiguration.features.get(k);
			configuredFeature.place(levelAccessor, structureFeatureManager, chunkGenerator, random, blockPos);
		}

		return true;
	}
}
