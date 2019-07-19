package net.minecraft.world.level.levelgen.feature;

import com.mojang.datafixers.Dynamic;
import java.util.Random;
import java.util.Set;
import java.util.function.Function;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.level.LevelSimulatedRW;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.BoundingBox;

public class MegaPineTreeFeature extends MegaTreeFeature<NoneFeatureConfiguration> {
	private static final BlockState TRUNK = Blocks.SPRUCE_LOG.defaultBlockState();
	private static final BlockState LEAF = Blocks.SPRUCE_LEAVES.defaultBlockState();
	private static final BlockState PODZOL = Blocks.PODZOL.defaultBlockState();
	private final boolean isSpruce;

	public MegaPineTreeFeature(Function<Dynamic<?>, ? extends NoneFeatureConfiguration> function, boolean bl, boolean bl2) {
		super(function, bl, 13, 15, TRUNK, LEAF);
		this.isSpruce = bl2;
	}

	@Override
	public boolean doPlace(Set<BlockPos> set, LevelSimulatedRW levelSimulatedRW, Random random, BlockPos blockPos, BoundingBox boundingBox) {
		int i = this.calcTreeHeigth(random);
		if (!this.prepareTree(levelSimulatedRW, blockPos, i)) {
			return false;
		} else {
			this.createCrown(levelSimulatedRW, blockPos.getX(), blockPos.getZ(), blockPos.getY() + i, 0, random, boundingBox, set);

			for (int j = 0; j < i; j++) {
				if (isAirOrLeaves(levelSimulatedRW, blockPos.above(j))) {
					this.setBlock(set, levelSimulatedRW, blockPos.above(j), this.trunk, boundingBox);
				}

				if (j < i - 1) {
					if (isAirOrLeaves(levelSimulatedRW, blockPos.offset(1, j, 0))) {
						this.setBlock(set, levelSimulatedRW, blockPos.offset(1, j, 0), this.trunk, boundingBox);
					}

					if (isAirOrLeaves(levelSimulatedRW, blockPos.offset(1, j, 1))) {
						this.setBlock(set, levelSimulatedRW, blockPos.offset(1, j, 1), this.trunk, boundingBox);
					}

					if (isAirOrLeaves(levelSimulatedRW, blockPos.offset(0, j, 1))) {
						this.setBlock(set, levelSimulatedRW, blockPos.offset(0, j, 1), this.trunk, boundingBox);
					}
				}
			}

			this.postPlaceTree(levelSimulatedRW, random, blockPos);
			return true;
		}
	}

	private void createCrown(LevelSimulatedRW levelSimulatedRW, int i, int j, int k, int l, Random random, BoundingBox boundingBox, Set<BlockPos> set) {
		int m = random.nextInt(5) + (this.isSpruce ? this.baseHeight : 3);
		int n = 0;

		for (int o = k - m; o <= k; o++) {
			int p = k - o;
			int q = l + Mth.floor((float)p / (float)m * 3.5F);
			this.placeDoubleTrunkLeaves(levelSimulatedRW, new BlockPos(i, o, j), q + (p > 0 && q == n && (o & 1) == 0 ? 1 : 0), boundingBox, set);
			n = q;
		}
	}

	public void postPlaceTree(LevelSimulatedRW levelSimulatedRW, Random random, BlockPos blockPos) {
		this.placePodzolCircle(levelSimulatedRW, blockPos.west().north());
		this.placePodzolCircle(levelSimulatedRW, blockPos.east(2).north());
		this.placePodzolCircle(levelSimulatedRW, blockPos.west().south(2));
		this.placePodzolCircle(levelSimulatedRW, blockPos.east(2).south(2));

		for (int i = 0; i < 5; i++) {
			int j = random.nextInt(64);
			int k = j % 8;
			int l = j / 8;
			if (k == 0 || k == 7 || l == 0 || l == 7) {
				this.placePodzolCircle(levelSimulatedRW, blockPos.offset(-3 + k, 0, -3 + l));
			}
		}
	}

	private void placePodzolCircle(LevelSimulatedRW levelSimulatedRW, BlockPos blockPos) {
		for (int i = -2; i <= 2; i++) {
			for (int j = -2; j <= 2; j++) {
				if (Math.abs(i) != 2 || Math.abs(j) != 2) {
					this.placePodzolAt(levelSimulatedRW, blockPos.offset(i, 0, j));
				}
			}
		}
	}

	private void placePodzolAt(LevelSimulatedRW levelSimulatedRW, BlockPos blockPos) {
		for (int i = 2; i >= -3; i--) {
			BlockPos blockPos2 = blockPos.above(i);
			if (isGrassOrDirt(levelSimulatedRW, blockPos2)) {
				this.setBlock(levelSimulatedRW, blockPos2, PODZOL);
				break;
			}

			if (!isAir(levelSimulatedRW, blockPos2) && i < 0) {
				break;
			}
		}
	}
}
