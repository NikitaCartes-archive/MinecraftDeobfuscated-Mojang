package net.minecraft.world.level.levelgen.feature;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.FallingBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.configurations.DiskConfiguration;

public class BaseDiskFeature extends Feature<DiskConfiguration> {
	public BaseDiskFeature(Codec<DiskConfiguration> codec) {
		super(codec);
	}

	@Override
	public boolean place(FeaturePlaceContext<DiskConfiguration> featurePlaceContext) {
		DiskConfiguration diskConfiguration = featurePlaceContext.config();
		BlockPos blockPos = featurePlaceContext.origin();
		WorldGenLevel worldGenLevel = featurePlaceContext.level();
		boolean bl = false;
		int i = blockPos.getY();
		int j = i + diskConfiguration.halfHeight;
		int k = i - diskConfiguration.halfHeight - 1;
		boolean bl2 = diskConfiguration.state.getBlock() instanceof FallingBlock;
		int l = diskConfiguration.radius.sample(featurePlaceContext.random());

		for (int m = blockPos.getX() - l; m <= blockPos.getX() + l; m++) {
			for (int n = blockPos.getZ() - l; n <= blockPos.getZ() + l; n++) {
				int o = m - blockPos.getX();
				int p = n - blockPos.getZ();
				if (o * o + p * p <= l * l) {
					boolean bl3 = false;

					for (int q = j; q >= k; q--) {
						BlockPos blockPos2 = new BlockPos(m, q, n);
						BlockState blockState = worldGenLevel.getBlockState(blockPos2);
						Block block = blockState.getBlock();
						boolean bl4 = false;
						if (q > k) {
							for (BlockState blockState2 : diskConfiguration.targets) {
								if (blockState2.is(block)) {
									worldGenLevel.setBlock(blockPos2, diskConfiguration.state, 2);
									this.markAboveForPostProcessing(worldGenLevel, blockPos2);
									bl = true;
									bl4 = true;
									break;
								}
							}
						}

						if (bl2 && bl3 && blockState.isAir()) {
							BlockState blockState3 = diskConfiguration.state.is(Blocks.RED_SAND) ? Blocks.RED_SANDSTONE.defaultBlockState() : Blocks.SANDSTONE.defaultBlockState();
							worldGenLevel.setBlock(new BlockPos(m, q + 1, n), blockState3, 2);
						}

						bl3 = bl4;
					}
				}
			}
		}

		return bl;
	}
}
