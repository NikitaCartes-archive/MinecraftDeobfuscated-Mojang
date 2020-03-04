package net.minecraft.world.level.levelgen.feature;

import com.mojang.datafixers.Dynamic;
import java.util.Random;
import java.util.function.Function;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.ChunkGeneratorSettings;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.configurations.RandomPatchConfiguration;

public class RandomPatchFeature extends Feature<RandomPatchConfiguration> {
	public RandomPatchFeature(Function<Dynamic<?>, ? extends RandomPatchConfiguration> function) {
		super(function);
	}

	public boolean place(
		LevelAccessor levelAccessor,
		ChunkGenerator<? extends ChunkGeneratorSettings> chunkGenerator,
		Random random,
		BlockPos blockPos,
		RandomPatchConfiguration randomPatchConfiguration
	) {
		BlockState blockState = randomPatchConfiguration.stateProvider.getState(random, blockPos);
		BlockPos blockPos2;
		if (randomPatchConfiguration.project) {
			blockPos2 = levelAccessor.getHeightmapPos(Heightmap.Types.WORLD_SURFACE_WG, blockPos);
		} else {
			blockPos2 = blockPos;
		}

		int i = 0;
		BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();

		for (int j = 0; j < randomPatchConfiguration.tries; j++) {
			mutableBlockPos.setWithOffset(
				blockPos2,
				random.nextInt(randomPatchConfiguration.xspread + 1) - random.nextInt(randomPatchConfiguration.xspread + 1),
				random.nextInt(randomPatchConfiguration.yspread + 1) - random.nextInt(randomPatchConfiguration.yspread + 1),
				random.nextInt(randomPatchConfiguration.zspread + 1) - random.nextInt(randomPatchConfiguration.zspread + 1)
			);
			BlockPos blockPos3 = mutableBlockPos.below();
			BlockState blockState2 = levelAccessor.getBlockState(blockPos3);
			if ((
					levelAccessor.isEmptyBlock(mutableBlockPos)
						|| randomPatchConfiguration.canReplace && levelAccessor.getBlockState(mutableBlockPos).getMaterial().isReplaceable()
				)
				&& blockState.canSurvive(levelAccessor, mutableBlockPos)
				&& (randomPatchConfiguration.whitelist.isEmpty() || randomPatchConfiguration.whitelist.contains(blockState2.getBlock()))
				&& !randomPatchConfiguration.blacklist.contains(blockState2)
				&& (
					!randomPatchConfiguration.needWater
						|| levelAccessor.getFluidState(blockPos3.west()).is(FluidTags.WATER)
						|| levelAccessor.getFluidState(blockPos3.east()).is(FluidTags.WATER)
						|| levelAccessor.getFluidState(blockPos3.north()).is(FluidTags.WATER)
						|| levelAccessor.getFluidState(blockPos3.south()).is(FluidTags.WATER)
				)) {
				randomPatchConfiguration.blockPlacer.place(levelAccessor, mutableBlockPos, blockState, random);
				i++;
			}
		}

		return i > 0;
	}
}
