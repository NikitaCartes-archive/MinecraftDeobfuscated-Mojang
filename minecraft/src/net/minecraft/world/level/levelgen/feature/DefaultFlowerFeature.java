package net.minecraft.world.level.levelgen.feature;

import com.mojang.datafixers.Dynamic;
import java.util.Random;
import java.util.function.Function;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.configurations.RandomPatchConfiguration;

public class DefaultFlowerFeature extends AbstractFlowerFeature<RandomPatchConfiguration> {
	public DefaultFlowerFeature(Function<Dynamic<?>, ? extends RandomPatchConfiguration> function, Function<Random, ? extends RandomPatchConfiguration> function2) {
		super(function, function2);
	}

	public boolean isValid(LevelAccessor levelAccessor, BlockPos blockPos, RandomPatchConfiguration randomPatchConfiguration) {
		return !randomPatchConfiguration.blacklist.contains(levelAccessor.getBlockState(blockPos));
	}

	public int getCount(RandomPatchConfiguration randomPatchConfiguration) {
		return randomPatchConfiguration.tries;
	}

	public BlockPos getPos(Random random, BlockPos blockPos, RandomPatchConfiguration randomPatchConfiguration) {
		return blockPos.offset(
			random.nextInt(randomPatchConfiguration.xspread) - random.nextInt(randomPatchConfiguration.xspread),
			random.nextInt(randomPatchConfiguration.yspread) - random.nextInt(randomPatchConfiguration.yspread),
			random.nextInt(randomPatchConfiguration.zspread) - random.nextInt(randomPatchConfiguration.zspread)
		);
	}

	public BlockState getRandomFlower(Random random, BlockPos blockPos, RandomPatchConfiguration randomPatchConfiguration) {
		return randomPatchConfiguration.stateProvider.getState(random, blockPos);
	}
}
