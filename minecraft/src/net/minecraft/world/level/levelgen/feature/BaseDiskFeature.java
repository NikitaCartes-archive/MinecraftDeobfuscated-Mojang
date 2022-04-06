package net.minecraft.world.level.levelgen.feature;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.WorldGenLevel;
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
		int j = i + diskConfiguration.halfHeight();
		int k = i - diskConfiguration.halfHeight() - 1;
		int l = diskConfiguration.radius().sample(featurePlaceContext.random());
		BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();

		for (BlockPos blockPos2 : BlockPos.betweenClosed(blockPos.offset(-l, 0, -l), blockPos.offset(l, 0, l))) {
			int m = blockPos2.getX() - blockPos.getX();
			int n = blockPos2.getZ() - blockPos.getZ();
			if (m * m + n * n <= l * l) {
				bl |= this.placeColumn(diskConfiguration, worldGenLevel, j, k, mutableBlockPos.set(blockPos2));
			}
		}

		return bl;
	}

	protected boolean placeColumn(DiskConfiguration diskConfiguration, WorldGenLevel worldGenLevel, int i, int j, BlockPos.MutableBlockPos mutableBlockPos) {
		boolean bl = false;
		boolean bl2 = false;
		boolean bl3 = diskConfiguration.state().getBlock() instanceof FallingBlock;

		for (int k = i; k >= j; k--) {
			mutableBlockPos.setY(k);
			BlockState blockState = worldGenLevel.getBlockState(mutableBlockPos);
			boolean bl4 = false;
			if (k > j && this.matchesTargetBlock(diskConfiguration, blockState)) {
				worldGenLevel.setBlock(mutableBlockPos, diskConfiguration.state(), 2);
				this.markAboveForPostProcessing(worldGenLevel, mutableBlockPos);
				bl2 = true;
				bl4 = true;
			}

			if (bl3 && bl && blockState.isAir()) {
				worldGenLevel.setBlock(mutableBlockPos.move(Direction.UP), this.getSupportState(diskConfiguration), 2);
			}

			bl = bl4;
		}

		return bl2;
	}

	protected BlockState getSupportState(DiskConfiguration diskConfiguration) {
		return diskConfiguration.state().is(Blocks.RED_SAND) ? Blocks.RED_SANDSTONE.defaultBlockState() : Blocks.SANDSTONE.defaultBlockState();
	}

	protected boolean matchesTargetBlock(DiskConfiguration diskConfiguration, BlockState blockState) {
		for (BlockState blockState2 : diskConfiguration.targets()) {
			if (blockState2.is(blockState.getBlock())) {
				return true;
			}
		}

		return false;
	}
}
