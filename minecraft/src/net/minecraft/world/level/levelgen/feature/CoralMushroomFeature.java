package net.minecraft.world.level.levelgen.feature;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

public class CoralMushroomFeature extends CoralFeature {
	public CoralMushroomFeature(Codec<NoneFeatureConfiguration> codec) {
		super(codec);
	}

	@Override
	protected boolean placeFeature(LevelAccessor levelAccessor, RandomSource randomSource, BlockPos blockPos, BlockState blockState) {
		int i = randomSource.nextInt(3) + 3;
		int j = randomSource.nextInt(3) + 3;
		int k = randomSource.nextInt(3) + 3;
		int l = randomSource.nextInt(3) + 1;
		BlockPos.MutableBlockPos mutableBlockPos = blockPos.mutable();

		for (int m = 0; m <= j; m++) {
			for (int n = 0; n <= i; n++) {
				for (int o = 0; o <= k; o++) {
					mutableBlockPos.set(m + blockPos.getX(), n + blockPos.getY(), o + blockPos.getZ());
					mutableBlockPos.move(Direction.DOWN, l);
					if ((m != 0 && m != j || n != 0 && n != i)
						&& (o != 0 && o != k || n != 0 && n != i)
						&& (m != 0 && m != j || o != 0 && o != k)
						&& (m == 0 || m == j || n == 0 || n == i || o == 0 || o == k)
						&& !(randomSource.nextFloat() < 0.1F)
						&& !this.placeCoralBlock(levelAccessor, randomSource, mutableBlockPos, blockState)) {
					}
				}
			}
		}

		return true;
	}
}
