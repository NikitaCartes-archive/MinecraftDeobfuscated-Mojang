package net.minecraft.world.level.levelgen.feature;

import com.google.common.collect.Lists;
import com.mojang.datafixers.Dynamic;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.function.Function;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

public class CoralTreeFeature extends CoralFeature {
	public CoralTreeFeature(Function<Dynamic<?>, ? extends NoneFeatureConfiguration> function) {
		super(function);
	}

	@Override
	protected boolean placeFeature(LevelAccessor levelAccessor, Random random, BlockPos blockPos, BlockState blockState) {
		BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos(blockPos);
		int i = random.nextInt(3) + 1;

		for (int j = 0; j < i; j++) {
			if (!this.placeCoralBlock(levelAccessor, random, mutableBlockPos, blockState)) {
				return true;
			}

			mutableBlockPos.move(Direction.UP);
		}

		BlockPos blockPos2 = mutableBlockPos.immutable();
		int k = random.nextInt(3) + 2;
		List<Direction> list = Lists.<Direction>newArrayList(Direction.Plane.HORIZONTAL);
		Collections.shuffle(list, random);

		for (Direction direction : list.subList(0, k)) {
			mutableBlockPos.set(blockPos2);
			mutableBlockPos.move(direction);
			int l = random.nextInt(5) + 2;
			int m = 0;

			for (int n = 0; n < l && this.placeCoralBlock(levelAccessor, random, mutableBlockPos, blockState); n++) {
				m++;
				mutableBlockPos.move(Direction.UP);
				if (n == 0 || m >= 2 && random.nextFloat() < 0.25F) {
					mutableBlockPos.move(direction);
					m = 0;
				}
			}
		}

		return true;
	}
}
