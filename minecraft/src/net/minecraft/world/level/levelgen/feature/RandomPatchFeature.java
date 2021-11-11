package net.minecraft.world.level.levelgen.feature;

import com.mojang.serialization.Codec;
import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.levelgen.feature.configurations.RandomPatchConfiguration;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;

public class RandomPatchFeature extends Feature<RandomPatchConfiguration> {
	public RandomPatchFeature(Codec<RandomPatchConfiguration> codec) {
		super(codec);
	}

	@Override
	public boolean place(FeaturePlaceContext<RandomPatchConfiguration> featurePlaceContext) {
		RandomPatchConfiguration randomPatchConfiguration = featurePlaceContext.config();
		Random random = featurePlaceContext.random();
		BlockPos blockPos = featurePlaceContext.origin();
		WorldGenLevel worldGenLevel = featurePlaceContext.level();
		int i = 0;
		BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();
		int j = randomPatchConfiguration.xzSpread() + 1;
		int k = randomPatchConfiguration.ySpread() + 1;

		for (int l = 0; l < randomPatchConfiguration.tries(); l++) {
			mutableBlockPos.setWithOffset(blockPos, random.nextInt(j) - random.nextInt(j), random.nextInt(k) - random.nextInt(k), random.nextInt(j) - random.nextInt(j));
			if (((PlacedFeature)randomPatchConfiguration.feature().get()).place(worldGenLevel, featurePlaceContext.chunkGenerator(), random, mutableBlockPos)) {
				i++;
			}
		}

		return i > 0;
	}
}
