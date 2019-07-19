package net.minecraft.world.level.levelgen.feature;

import com.mojang.datafixers.Dynamic;
import java.util.Random;
import java.util.Set;
import java.util.function.Function;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelSimulatedRW;
import net.minecraft.world.level.LevelSimulatedReader;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.BoundingBox;

public abstract class MegaTreeFeature<T extends FeatureConfiguration> extends AbstractTreeFeature<T> {
	protected final int baseHeight;
	protected final BlockState trunk;
	protected final BlockState leaf;
	protected final int heightInterval;

	public MegaTreeFeature(Function<Dynamic<?>, ? extends T> function, boolean bl, int i, int j, BlockState blockState, BlockState blockState2) {
		super(function, bl);
		this.baseHeight = i;
		this.heightInterval = j;
		this.trunk = blockState;
		this.leaf = blockState2;
	}

	protected int calcTreeHeigth(Random random) {
		int i = random.nextInt(3) + this.baseHeight;
		if (this.heightInterval > 1) {
			i += random.nextInt(this.heightInterval);
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

	protected void placeDoubleTrunkLeaves(LevelSimulatedRW levelSimulatedRW, BlockPos blockPos, int i, BoundingBox boundingBox, Set<BlockPos> set) {
		int j = i * i;

		for (int k = -i; k <= i + 1; k++) {
			for (int l = -i; l <= i + 1; l++) {
				int m = Math.min(Math.abs(k), Math.abs(k - 1));
				int n = Math.min(Math.abs(l), Math.abs(l - 1));
				if (m + n < 7 && m * m + n * n <= j) {
					BlockPos blockPos2 = blockPos.offset(k, 0, l);
					if (isAirOrLeaves(levelSimulatedRW, blockPos2)) {
						this.setBlock(set, levelSimulatedRW, blockPos2, this.leaf, boundingBox);
					}
				}
			}
		}
	}

	protected void placeSingleTrunkLeaves(LevelSimulatedRW levelSimulatedRW, BlockPos blockPos, int i, BoundingBox boundingBox, Set<BlockPos> set) {
		int j = i * i;

		for (int k = -i; k <= i; k++) {
			for (int l = -i; l <= i; l++) {
				if (k * k + l * l <= j) {
					BlockPos blockPos2 = blockPos.offset(k, 0, l);
					if (isAirOrLeaves(levelSimulatedRW, blockPos2)) {
						this.setBlock(set, levelSimulatedRW, blockPos2, this.leaf, boundingBox);
					}
				}
			}
		}
	}
}
