package net.minecraft.world.level.levelgen.feature;

import com.google.common.collect.Lists;
import com.mojang.datafixers.Dynamic;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.Set;
import java.util.function.Function;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.level.LevelSimulatedRW;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LogBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.BoundingBox;

public class BigTreeFeature extends AbstractTreeFeature<NoneFeatureConfiguration> {
	private static final BlockState LOG = Blocks.OAK_LOG.defaultBlockState();
	private static final BlockState LEAVES = Blocks.OAK_LEAVES.defaultBlockState();

	public BigTreeFeature(Function<Dynamic<?>, ? extends NoneFeatureConfiguration> function, boolean bl) {
		super(function, bl);
	}

	private void crossSection(LevelSimulatedRW levelSimulatedRW, BlockPos blockPos, float f, BoundingBox boundingBox, Set<BlockPos> set) {
		int i = (int)((double)f + 0.618);

		for (int j = -i; j <= i; j++) {
			for (int k = -i; k <= i; k++) {
				if (Math.pow((double)Math.abs(j) + 0.5, 2.0) + Math.pow((double)Math.abs(k) + 0.5, 2.0) <= (double)(f * f)) {
					BlockPos blockPos2 = blockPos.offset(j, 0, k);
					if (isAirOrLeaves(levelSimulatedRW, blockPos2)) {
						this.setBlock(set, levelSimulatedRW, blockPos2, LEAVES, boundingBox);
					}
				}
			}
		}
	}

	private float treeShape(int i, int j) {
		if ((float)j < (float)i * 0.3F) {
			return -1.0F;
		} else {
			float f = (float)i / 2.0F;
			float g = f - (float)j;
			float h = Mth.sqrt(f * f - g * g);
			if (g == 0.0F) {
				h = f;
			} else if (Math.abs(g) >= f) {
				return 0.0F;
			}

			return h * 0.5F;
		}
	}

	private float foliageShape(int i) {
		if (i < 0 || i >= 5) {
			return -1.0F;
		} else {
			return i != 0 && i != 4 ? 3.0F : 2.0F;
		}
	}

	private void foliageCluster(LevelSimulatedRW levelSimulatedRW, BlockPos blockPos, BoundingBox boundingBox, Set<BlockPos> set) {
		for (int i = 0; i < 5; i++) {
			this.crossSection(levelSimulatedRW, blockPos.above(i), this.foliageShape(i), boundingBox, set);
		}
	}

	private int makeLimb(Set<BlockPos> set, LevelSimulatedRW levelSimulatedRW, BlockPos blockPos, BlockPos blockPos2, boolean bl, BoundingBox boundingBox) {
		if (!bl && Objects.equals(blockPos, blockPos2)) {
			return -1;
		} else {
			BlockPos blockPos3 = blockPos2.offset(-blockPos.getX(), -blockPos.getY(), -blockPos.getZ());
			int i = this.getSteps(blockPos3);
			float f = (float)blockPos3.getX() / (float)i;
			float g = (float)blockPos3.getY() / (float)i;
			float h = (float)blockPos3.getZ() / (float)i;

			for (int j = 0; j <= i; j++) {
				BlockPos blockPos4 = blockPos.offset((double)(0.5F + (float)j * f), (double)(0.5F + (float)j * g), (double)(0.5F + (float)j * h));
				if (bl) {
					this.setBlock(set, levelSimulatedRW, blockPos4, LOG.setValue(LogBlock.AXIS, this.getLogAxis(blockPos, blockPos4)), boundingBox);
				} else if (!isFree(levelSimulatedRW, blockPos4)) {
					return j;
				}
			}

			return -1;
		}
	}

	private int getSteps(BlockPos blockPos) {
		int i = Mth.abs(blockPos.getX());
		int j = Mth.abs(blockPos.getY());
		int k = Mth.abs(blockPos.getZ());
		if (k > i && k > j) {
			return k;
		} else {
			return j > i ? j : i;
		}
	}

	private Direction.Axis getLogAxis(BlockPos blockPos, BlockPos blockPos2) {
		Direction.Axis axis = Direction.Axis.Y;
		int i = Math.abs(blockPos2.getX() - blockPos.getX());
		int j = Math.abs(blockPos2.getZ() - blockPos.getZ());
		int k = Math.max(i, j);
		if (k > 0) {
			if (i == k) {
				axis = Direction.Axis.X;
			} else if (j == k) {
				axis = Direction.Axis.Z;
			}
		}

		return axis;
	}

	private void makeFoliage(
		LevelSimulatedRW levelSimulatedRW, int i, BlockPos blockPos, List<BigTreeFeature.FoliageCoords> list, BoundingBox boundingBox, Set<BlockPos> set
	) {
		for (BigTreeFeature.FoliageCoords foliageCoords : list) {
			if (this.trimBranches(i, foliageCoords.getBranchBase() - blockPos.getY())) {
				this.foliageCluster(levelSimulatedRW, foliageCoords, boundingBox, set);
			}
		}
	}

	private boolean trimBranches(int i, int j) {
		return (double)j >= (double)i * 0.2;
	}

	private void makeTrunk(Set<BlockPos> set, LevelSimulatedRW levelSimulatedRW, BlockPos blockPos, int i, BoundingBox boundingBox) {
		this.makeLimb(set, levelSimulatedRW, blockPos, blockPos.above(i), true, boundingBox);
	}

	private void makeBranches(
		Set<BlockPos> set, LevelSimulatedRW levelSimulatedRW, int i, BlockPos blockPos, List<BigTreeFeature.FoliageCoords> list, BoundingBox boundingBox
	) {
		for (BigTreeFeature.FoliageCoords foliageCoords : list) {
			int j = foliageCoords.getBranchBase();
			BlockPos blockPos2 = new BlockPos(blockPos.getX(), j, blockPos.getZ());
			if (!blockPos2.equals(foliageCoords) && this.trimBranches(i, j - blockPos.getY())) {
				this.makeLimb(set, levelSimulatedRW, blockPos2, foliageCoords, true, boundingBox);
			}
		}
	}

	@Override
	public boolean doPlace(Set<BlockPos> set, LevelSimulatedRW levelSimulatedRW, Random random, BlockPos blockPos, BoundingBox boundingBox) {
		Random random2 = new Random(random.nextLong());
		int i = this.checkLocation(set, levelSimulatedRW, blockPos, 5 + random2.nextInt(12), boundingBox);
		if (i == -1) {
			return false;
		} else {
			this.setDirtAt(levelSimulatedRW, blockPos.below());
			int j = (int)((double)i * 0.618);
			if (j >= i) {
				j = i - 1;
			}

			double d = 1.0;
			int k = (int)(1.382 + Math.pow(1.0 * (double)i / 13.0, 2.0));
			if (k < 1) {
				k = 1;
			}

			int l = blockPos.getY() + j;
			int m = i - 5;
			List<BigTreeFeature.FoliageCoords> list = Lists.<BigTreeFeature.FoliageCoords>newArrayList();
			list.add(new BigTreeFeature.FoliageCoords(blockPos.above(m), l));

			for (; m >= 0; m--) {
				float f = this.treeShape(i, m);
				if (!(f < 0.0F)) {
					for (int n = 0; n < k; n++) {
						double e = 1.0;
						double g = 1.0 * (double)f * ((double)random2.nextFloat() + 0.328);
						double h = (double)(random2.nextFloat() * 2.0F) * Math.PI;
						double o = g * Math.sin(h) + 0.5;
						double p = g * Math.cos(h) + 0.5;
						BlockPos blockPos2 = blockPos.offset(o, (double)(m - 1), p);
						BlockPos blockPos3 = blockPos2.above(5);
						if (this.makeLimb(set, levelSimulatedRW, blockPos2, blockPos3, false, boundingBox) == -1) {
							int q = blockPos.getX() - blockPos2.getX();
							int r = blockPos.getZ() - blockPos2.getZ();
							double s = (double)blockPos2.getY() - Math.sqrt((double)(q * q + r * r)) * 0.381;
							int t = s > (double)l ? l : (int)s;
							BlockPos blockPos4 = new BlockPos(blockPos.getX(), t, blockPos.getZ());
							if (this.makeLimb(set, levelSimulatedRW, blockPos4, blockPos2, false, boundingBox) == -1) {
								list.add(new BigTreeFeature.FoliageCoords(blockPos2, blockPos4.getY()));
							}
						}
					}
				}
			}

			this.makeFoliage(levelSimulatedRW, i, blockPos, list, boundingBox, set);
			this.makeTrunk(set, levelSimulatedRW, blockPos, j, boundingBox);
			this.makeBranches(set, levelSimulatedRW, i, blockPos, list, boundingBox);
			return true;
		}
	}

	private int checkLocation(Set<BlockPos> set, LevelSimulatedRW levelSimulatedRW, BlockPos blockPos, int i, BoundingBox boundingBox) {
		if (!isGrassOrDirtOrFarmland(levelSimulatedRW, blockPos.below())) {
			return -1;
		} else {
			int j = this.makeLimb(set, levelSimulatedRW, blockPos, blockPos.above(i - 1), false, boundingBox);
			if (j == -1) {
				return i;
			} else {
				return j < 6 ? -1 : j;
			}
		}
	}

	static class FoliageCoords extends BlockPos {
		private final int branchBase;

		public FoliageCoords(BlockPos blockPos, int i) {
			super(blockPos.getX(), blockPos.getY(), blockPos.getZ());
			this.branchBase = i;
		}

		public int getBranchBase() {
			return this.branchBase;
		}
	}
}
