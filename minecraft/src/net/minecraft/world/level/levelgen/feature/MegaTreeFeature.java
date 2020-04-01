package net.minecraft.world.level.levelgen.feature;

import com.mojang.datafixers.Dynamic;
import java.util.Random;
import java.util.Set;
import java.util.function.Function;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelSimulatedRW;
import net.minecraft.world.level.LevelSimulatedReader;
import net.minecraft.world.level.levelgen.feature.configurations.MegaTreeConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.TreeConfiguration;
import net.minecraft.world.level.levelgen.structure.BoundingBox;

public abstract class MegaTreeFeature<T extends TreeConfiguration> extends AbstractTreeFeature<T> {
	public MegaTreeFeature(Function<Dynamic<?>, ? extends T> function, Function<Random, ? extends T> function2) {
		super(function, function2);
	}

	protected int calcTreeHeigth(Random random, MegaTreeConfiguration megaTreeConfiguration) {
		int i = random.nextInt(3) + megaTreeConfiguration.baseHeight;
		if (megaTreeConfiguration.heightInterval > 1) {
			i += random.nextInt(megaTreeConfiguration.heightInterval);
		}

		return i;
	}

	private boolean checkIsFree(LevelSimulatedReader levelSimulatedReader, BlockPos blockPos, int i) {
		boolean bl = true;
		if (blockPos.getY() >= 1 && blockPos.getY() + i + 1 <= 256) {
			for (int j = 0; j <= 1 + i; j++) {
				int k = 2;
				if (j == 0) {
					k = 1;
				} else if (j >= 1 + i - 2) {
					k = 2;
				}

				for (int l = -k; l <= k && bl; l++) {
					for (int m = -k; m <= k && bl; m++) {
						if (blockPos.getY() + j < 0 || blockPos.getY() + j >= 256 || !isFree(levelSimulatedReader, blockPos.offset(l, j, m))) {
							bl = false;
						}
					}
				}
			}

			return bl;
		} else {
			return false;
		}
	}

	private boolean makeDirtFloor(LevelSimulatedRW levelSimulatedRW, BlockPos blockPos) {
		BlockPos blockPos2 = blockPos.below();
		if (isGrassOrDirt(levelSimulatedRW, blockPos2) && blockPos.getY() >= 2) {
			this.setDirtAt(levelSimulatedRW, blockPos2);
			this.setDirtAt(levelSimulatedRW, blockPos2.east());
			this.setDirtAt(levelSimulatedRW, blockPos2.south());
			this.setDirtAt(levelSimulatedRW, blockPos2.south().east());
			return true;
		} else {
			return false;
		}
	}

	protected boolean prepareTree(LevelSimulatedRW levelSimulatedRW, BlockPos blockPos, int i) {
		return this.checkIsFree(levelSimulatedRW, blockPos, i) && this.makeDirtFloor(levelSimulatedRW, blockPos);
	}

	protected void placeDoubleTrunkLeaves(
		LevelSimulatedRW levelSimulatedRW, Random random, BlockPos blockPos, int i, Set<BlockPos> set, BoundingBox boundingBox, TreeConfiguration treeConfiguration
	) {
		int j = i * i;

		for (int k = -i; k <= i + 1; k++) {
			for (int l = -i; l <= i + 1; l++) {
				int m = Math.min(Math.abs(k), Math.abs(k - 1));
				int n = Math.min(Math.abs(l), Math.abs(l - 1));
				if (m + n < 7 && m * m + n * n <= j) {
					this.placeLeaf(levelSimulatedRW, random, blockPos.offset(k, 0, l), set, boundingBox, treeConfiguration);
				}
			}
		}
	}

	protected void placeSingleTrunkLeaves(
		LevelSimulatedRW levelSimulatedRW, Random random, BlockPos blockPos, int i, Set<BlockPos> set, BoundingBox boundingBox, TreeConfiguration treeConfiguration
	) {
		int j = i * i;

		for (int k = -i; k <= i; k++) {
			for (int l = -i; l <= i; l++) {
				if (k * k + l * l <= j) {
					this.placeLeaf(levelSimulatedRW, random, blockPos.offset(k, 0, l), set, boundingBox, treeConfiguration);
				}
			}
		}
	}

	protected void placeTrunk(
		LevelSimulatedRW levelSimulatedRW,
		Random random,
		BlockPos blockPos,
		int i,
		Set<BlockPos> set,
		BoundingBox boundingBox,
		MegaTreeConfiguration megaTreeConfiguration
	) {
		BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();

		for (int j = 0; j < i; j++) {
			mutableBlockPos.setWithOffset(blockPos, 0, j, 0);
			if (isFree(levelSimulatedRW, mutableBlockPos)) {
				this.placeLog(levelSimulatedRW, random, mutableBlockPos, set, boundingBox, megaTreeConfiguration);
			}

			if (j < i - 1) {
				mutableBlockPos.setWithOffset(blockPos, 1, j, 0);
				if (isFree(levelSimulatedRW, mutableBlockPos)) {
					this.placeLog(levelSimulatedRW, random, mutableBlockPos, set, boundingBox, megaTreeConfiguration);
				}

				mutableBlockPos.setWithOffset(blockPos, 1, j, 1);
				if (isFree(levelSimulatedRW, mutableBlockPos)) {
					this.placeLog(levelSimulatedRW, random, mutableBlockPos, set, boundingBox, megaTreeConfiguration);
				}

				mutableBlockPos.setWithOffset(blockPos, 0, j, 1);
				if (isFree(levelSimulatedRW, mutableBlockPos)) {
					this.placeLog(levelSimulatedRW, random, mutableBlockPos, set, boundingBox, megaTreeConfiguration);
				}
			}
		}
	}
}
