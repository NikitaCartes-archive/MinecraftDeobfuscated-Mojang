package net.minecraft.world.level.levelgen.feature;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.levelgen.feature.configurations.BlockColumnConfiguration;

public class BlockColumnFeature extends Feature<BlockColumnConfiguration> {
	public BlockColumnFeature(Codec<BlockColumnConfiguration> codec) {
		super(codec);
	}

	@Override
	public boolean place(FeaturePlaceContext<BlockColumnConfiguration> featurePlaceContext) {
		WorldGenLevel worldGenLevel = featurePlaceContext.level();
		BlockColumnConfiguration blockColumnConfiguration = featurePlaceContext.config();
		RandomSource randomSource = featurePlaceContext.random();
		int i = blockColumnConfiguration.layers().size();
		int[] is = new int[i];
		int j = 0;

		for (int k = 0; k < i; k++) {
			is[k] = ((BlockColumnConfiguration.Layer)blockColumnConfiguration.layers().get(k)).height().sample(randomSource);
			j += is[k];
		}

		if (j == 0) {
			return false;
		} else {
			BlockPos.MutableBlockPos mutableBlockPos = featurePlaceContext.origin().mutable();
			BlockPos.MutableBlockPos mutableBlockPos2 = mutableBlockPos.mutable().move(blockColumnConfiguration.direction());

			for (int l = 0; l < j; l++) {
				if (!blockColumnConfiguration.allowedPlacement().test(worldGenLevel, mutableBlockPos2)) {
					truncate(is, j, l, blockColumnConfiguration.prioritizeTip());
					break;
				}

				mutableBlockPos2.move(blockColumnConfiguration.direction());
			}

			for (int l = 0; l < i; l++) {
				int m = is[l];
				if (m != 0) {
					BlockColumnConfiguration.Layer layer = (BlockColumnConfiguration.Layer)blockColumnConfiguration.layers().get(l);

					for (int n = 0; n < m; n++) {
						worldGenLevel.setBlock(mutableBlockPos, layer.state().getState(randomSource, mutableBlockPos), 2);
						mutableBlockPos.move(blockColumnConfiguration.direction());
					}
				}
			}

			return true;
		}
	}

	private static void truncate(int[] is, int i, int j, boolean bl) {
		int k = i - j;
		int l = bl ? 1 : -1;
		int m = bl ? 0 : is.length - 1;
		int n = bl ? is.length : -1;

		for (int o = m; o != n && k > 0; o += l) {
			int p = is[o];
			int q = Math.min(p, k);
			k -= q;
			is[o] -= q;
		}
	}
}
