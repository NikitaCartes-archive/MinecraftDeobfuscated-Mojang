package net.minecraft.world.level.levelgen.feature;

import com.mojang.datafixers.Dynamic;
import java.util.Random;
import java.util.function.Function;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.StructureFeatureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;

public abstract class AbstractFlowerFeature<U extends FeatureConfiguration> extends Feature<U> {
	public AbstractFlowerFeature(Function<Dynamic<?>, ? extends U> function) {
		super(function);
	}

	@Override
	public boolean place(
		WorldGenLevel worldGenLevel,
		StructureFeatureManager structureFeatureManager,
		ChunkGenerator chunkGenerator,
		Random random,
		BlockPos blockPos,
		U featureConfiguration
	) {
		BlockState blockState = this.getRandomFlower(random, blockPos, featureConfiguration);
		int i = 0;

		for (int j = 0; j < this.getCount(featureConfiguration); j++) {
			BlockPos blockPos2 = this.getPos(random, blockPos, featureConfiguration);
			if (worldGenLevel.isEmptyBlock(blockPos2)
				&& blockPos2.getY() < 255
				&& blockState.canSurvive(worldGenLevel, blockPos2)
				&& this.isValid(worldGenLevel, blockPos2, featureConfiguration)) {
				worldGenLevel.setBlock(blockPos2, blockState, 2);
				i++;
			}
		}

		return i > 0;
	}

	public abstract boolean isValid(LevelAccessor levelAccessor, BlockPos blockPos, U featureConfiguration);

	public abstract int getCount(U featureConfiguration);

	public abstract BlockPos getPos(Random random, BlockPos blockPos, U featureConfiguration);

	public abstract BlockState getRandomFlower(Random random, BlockPos blockPos, U featureConfiguration);
}
