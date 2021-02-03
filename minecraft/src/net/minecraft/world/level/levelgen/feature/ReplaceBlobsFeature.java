package net.minecraft.world.level.levelgen.feature;

import com.mojang.serialization.Codec;
import java.util.Random;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.configurations.ReplaceSphereConfiguration;

public class ReplaceBlobsFeature extends Feature<ReplaceSphereConfiguration> {
	public ReplaceBlobsFeature(Codec<ReplaceSphereConfiguration> codec) {
		super(codec);
	}

	@Override
	public boolean place(FeaturePlaceContext<ReplaceSphereConfiguration> featurePlaceContext) {
		ReplaceSphereConfiguration replaceSphereConfiguration = featurePlaceContext.config();
		WorldGenLevel worldGenLevel = featurePlaceContext.level();
		Random random = featurePlaceContext.random();
		Block block = replaceSphereConfiguration.targetState.getBlock();
		BlockPos blockPos = findTarget(
			worldGenLevel,
			featurePlaceContext.origin().mutable().clamp(Direction.Axis.Y, worldGenLevel.getMinBuildHeight() + 1, worldGenLevel.getMaxBuildHeight() - 1),
			block
		);
		if (blockPos == null) {
			return false;
		} else {
			int i = replaceSphereConfiguration.radius().sample(random);
			int j = replaceSphereConfiguration.radius().sample(random);
			int k = replaceSphereConfiguration.radius().sample(random);
			int l = Math.max(i, Math.max(j, k));
			boolean bl = false;

			for (BlockPos blockPos2 : BlockPos.withinManhattan(blockPos, i, j, k)) {
				if (blockPos2.distManhattan(blockPos) > l) {
					break;
				}

				BlockState blockState = worldGenLevel.getBlockState(blockPos2);
				if (blockState.is(block)) {
					this.setBlock(worldGenLevel, blockPos2, replaceSphereConfiguration.replaceState);
					bl = true;
				}
			}

			return bl;
		}
	}

	@Nullable
	private static BlockPos findTarget(LevelAccessor levelAccessor, BlockPos.MutableBlockPos mutableBlockPos, Block block) {
		while (mutableBlockPos.getY() > levelAccessor.getMinBuildHeight() + 1) {
			BlockState blockState = levelAccessor.getBlockState(mutableBlockPos);
			if (blockState.is(block)) {
				return mutableBlockPos;
			}

			mutableBlockPos.move(Direction.DOWN);
		}

		return null;
	}
}
