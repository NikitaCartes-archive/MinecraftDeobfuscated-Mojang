package net.minecraft.world.level.levelgen.feature;

import com.mojang.datafixers.Dynamic;
import java.util.Random;
import java.util.function.Function;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.ChunkGeneratorSettings;
import net.minecraft.world.level.levelgen.feature.configurations.RandomBooleanFeatureConfiguration;

public class RandomBooleanSelectorFeature extends Feature<RandomBooleanFeatureConfiguration> {
	public RandomBooleanSelectorFeature(
		Function<Dynamic<?>, ? extends RandomBooleanFeatureConfiguration> function, Function<Random, ? extends RandomBooleanFeatureConfiguration> function2
	) {
		super(function, function2);
	}

	public boolean place(
		LevelAccessor levelAccessor,
		ChunkGenerator<? extends ChunkGeneratorSettings> chunkGenerator,
		Random random,
		BlockPos blockPos,
		RandomBooleanFeatureConfiguration randomBooleanFeatureConfiguration
	) {
		boolean bl = random.nextBoolean();
		return bl
			? randomBooleanFeatureConfiguration.featureTrue.place(levelAccessor, chunkGenerator, random, blockPos)
			: randomBooleanFeatureConfiguration.featureFalse.place(levelAccessor, chunkGenerator, random, blockPos);
	}
}
