package net.minecraft.world.level.levelgen.feature;

import com.mojang.datafixers.Dynamic;
import java.util.Random;
import java.util.function.Function;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.ChunkGeneratorSettings;

public class RandomBooleanSelectorFeature extends Feature<RandomBooleanFeatureConfig> {
	public RandomBooleanSelectorFeature(Function<Dynamic<?>, ? extends RandomBooleanFeatureConfig> function) {
		super(function);
	}

	public boolean place(
		LevelAccessor levelAccessor,
		ChunkGenerator<? extends ChunkGeneratorSettings> chunkGenerator,
		Random random,
		BlockPos blockPos,
		RandomBooleanFeatureConfig randomBooleanFeatureConfig
	) {
		boolean bl = random.nextBoolean();
		return bl
			? randomBooleanFeatureConfig.featureTrue.place(levelAccessor, chunkGenerator, random, blockPos)
			: randomBooleanFeatureConfig.featureFalse.place(levelAccessor, chunkGenerator, random, blockPos);
	}
}
