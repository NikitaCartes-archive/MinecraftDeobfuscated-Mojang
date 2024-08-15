package net.minecraft.world.level.levelgen.feature;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.configurations.DiskConfiguration;

public class DiskFeature extends Feature<DiskConfiguration> {
	public DiskFeature(Codec<DiskConfiguration> codec) {
		super(codec);
	}

	@Override
	public boolean place(FeaturePlaceContext<DiskConfiguration> featurePlaceContext) {
		DiskConfiguration diskConfiguration = featurePlaceContext.config();
		BlockPos blockPos = featurePlaceContext.origin();
		WorldGenLevel worldGenLevel = featurePlaceContext.level();
		RandomSource randomSource = featurePlaceContext.random();
		boolean bl = false;
		int i = blockPos.getY();
		int j = i + diskConfiguration.halfHeight();
		int k = i - diskConfiguration.halfHeight() - 1;
		int l = diskConfiguration.radius().sample(randomSource);
		BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();

		for (BlockPos blockPos2 : BlockPos.betweenClosed(blockPos.offset(-l, 0, -l), blockPos.offset(l, 0, l))) {
			int m = blockPos2.getX() - blockPos.getX();
			int n = blockPos2.getZ() - blockPos.getZ();
			if (m * m + n * n <= l * l) {
				bl |= this.placeColumn(diskConfiguration, worldGenLevel, randomSource, j, k, mutableBlockPos.set(blockPos2));
			}
		}

		return bl;
	}

	protected boolean placeColumn(
		DiskConfiguration diskConfiguration, WorldGenLevel worldGenLevel, RandomSource randomSource, int i, int j, BlockPos.MutableBlockPos mutableBlockPos
	) {
		boolean bl = false;
		boolean bl2 = false;

		for (int k = i; k > j; k--) {
			mutableBlockPos.setY(k);
			if (diskConfiguration.target().test(worldGenLevel, mutableBlockPos)) {
				BlockState blockState = diskConfiguration.stateProvider().getState(worldGenLevel, randomSource, mutableBlockPos);
				worldGenLevel.setBlock(mutableBlockPos, blockState, 2);
				if (!bl2) {
					this.markAboveForPostProcessing(worldGenLevel, mutableBlockPos);
				}

				bl = true;
				bl2 = true;
			} else {
				bl2 = false;
			}
		}

		return bl;
	}
}
