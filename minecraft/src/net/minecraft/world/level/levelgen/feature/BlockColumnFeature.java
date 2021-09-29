package net.minecraft.world.level.levelgen.feature;

import com.mojang.serialization.Codec;
import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.configurations.BlockColumnConfiguration;

public class BlockColumnFeature extends Feature<BlockColumnConfiguration> {
	public BlockColumnFeature(Codec<BlockColumnConfiguration> codec) {
		super(codec);
	}

	@Override
	public boolean place(FeaturePlaceContext<BlockColumnConfiguration> featurePlaceContext) {
		LevelAccessor levelAccessor = featurePlaceContext.level();
		BlockColumnConfiguration blockColumnConfiguration = featurePlaceContext.config();
		Random random = featurePlaceContext.random();
		int i = blockColumnConfiguration.layers().size();
		int[] is = new int[i];
		int j = 0;

		for (int k = 0; k < i; k++) {
			is[k] = ((BlockColumnConfiguration.Layer)blockColumnConfiguration.layers().get(k)).height().sample(random);
			j += is[k];
		}

		if (j == 0) {
			return false;
		} else {
			BlockPos.MutableBlockPos mutableBlockPos = featurePlaceContext.origin().mutable();
			BlockPos.MutableBlockPos mutableBlockPos2 = mutableBlockPos.mutable().move(blockColumnConfiguration.direction());
			BlockState blockState = levelAccessor.getBlockState(mutableBlockPos);

			for (int l = 0; l < j; l++) {
				if (!blockState.isAir() && !blockColumnConfiguration.allowWater() && !blockState.getFluidState().is(FluidTags.WATER)) {
					truncate(is, j, l, blockColumnConfiguration.prioritizeTip());
					break;
				}

				blockState = levelAccessor.getBlockState(mutableBlockPos2);
				mutableBlockPos2.move(blockColumnConfiguration.direction());
			}

			for (int l = 0; l < i; l++) {
				int m = is[l];
				if (m != 0) {
					BlockColumnConfiguration.Layer layer = (BlockColumnConfiguration.Layer)blockColumnConfiguration.layers().get(l);

					for (int n = 0; n < m; n++) {
						levelAccessor.setBlock(mutableBlockPos, layer.state().getState(random, mutableBlockPos), 2);
						mutableBlockPos.move(blockColumnConfiguration.direction());
					}
				}
			}

			return true;
		}
	}

	private static void truncate(int[] is, int i, int j, boolean bl) {
		int k = i - j;
		int l = bl ? -1 : 1;
		int m = bl ? is.length - 1 : 0;
		int n = bl ? -1 : is.length;

		for (int o = m; o != n && k > 0; o += l) {
			int p = is[o];
			int q = Math.min(p, k);
			k -= q;
			is[o] -= q;
		}
	}
}
