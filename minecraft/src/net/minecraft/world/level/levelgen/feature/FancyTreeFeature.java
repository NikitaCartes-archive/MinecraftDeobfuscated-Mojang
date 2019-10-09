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
import net.minecraft.world.level.block.LogBlock;
import net.minecraft.world.level.levelgen.feature.configurations.SmallTreeConfiguration;
import net.minecraft.world.level.levelgen.structure.BoundingBox;

public class FancyTreeFeature extends AbstractTreeFeature<SmallTreeConfiguration> {
	public FancyTreeFeature(Function<Dynamic<?>, ? extends SmallTreeConfiguration> function) {
		super(function);
	}

	private void crossSection(
		LevelSimulatedRW levelSimulatedRW,
		Random random,
		BlockPos blockPos,
		float f,
		Set<BlockPos> set,
		BoundingBox boundingBox,
		SmallTreeConfiguration smallTreeConfiguration
	) {
		int i = (int)((double)f + 0.618);

		for (int j = -i; j <= i; j++) {
			for (int k = -i; k <= i; k++) {
				if (Math.pow((double)Math.abs(j) + 0.5, 2.0) + Math.pow((double)Math.abs(k) + 0.5, 2.0) <= (double)(f * f)) {
					this.placeLeaf(levelSimulatedRW, random, blockPos.offset(j, 0, k), set, boundingBox, smallTreeConfiguration);
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

	private void foliageCluster(
		LevelSimulatedRW levelSimulatedRW,
		Random random,
		BlockPos blockPos,
		Set<BlockPos> set,
		BoundingBox boundingBox,
		SmallTreeConfiguration smallTreeConfiguration
	) {
		for (int i = 0; i < 5; i++) {
			this.crossSection(levelSimulatedRW, random, blockPos.above(i), this.foliageShape(i), set, boundingBox, smallTreeConfiguration);
		}
	}

	private int makeLimb(
		LevelSimulatedRW levelSimulatedRW,
		Random random,
		BlockPos blockPos,
		BlockPos blockPos2,
		boolean bl,
		Set<BlockPos> set,
		BoundingBox boundingBox,
		SmallTreeConfiguration smallTreeConfiguration
	) {
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
					this.setBlock(
						levelSimulatedRW,
						blockPos4,
						smallTreeConfiguration.trunkProvider.getState(random, blockPos4).setValue(LogBlock.AXIS, this.getLogAxis(blockPos, blockPos4)),
						boundingBox
					);
					set.add(blockPos4);
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
		LevelSimulatedRW levelSimulatedRW,
		Random random,
		int i,
		BlockPos blockPos,
		List<FancyTreeFeature.FoliageCoords> list,
		Set<BlockPos> set,
		BoundingBox boundingBox,
		SmallTreeConfiguration smallTreeConfiguration
	) {
		for (FancyTreeFeature.FoliageCoords foliageCoords : list) {
			if (this.trimBranches(i, foliageCoords.getBranchBase() - blockPos.getY())) {
				this.foliageCluster(levelSimulatedRW, random, foliageCoords, set, boundingBox, smallTreeConfiguration);
			}
		}
	}

	private boolean trimBranches(int i, int j) {
		return (double)j >= (double)i * 0.2;
	}

	private void makeTrunk(
		LevelSimulatedRW levelSimulatedRW,
		Random random,
		BlockPos blockPos,
		int i,
		Set<BlockPos> set,
		BoundingBox boundingBox,
		SmallTreeConfiguration smallTreeConfiguration
	) {
		this.makeLimb(levelSimulatedRW, random, blockPos, blockPos.above(i), true, set, boundingBox, smallTreeConfiguration);
	}

	private void makeBranches(
		LevelSimulatedRW levelSimulatedRW,
		Random random,
		int i,
		BlockPos blockPos,
		List<FancyTreeFeature.FoliageCoords> list,
		Set<BlockPos> set,
		BoundingBox boundingBox,
		SmallTreeConfiguration smallTreeConfiguration
	) {
		for (FancyTreeFeature.FoliageCoords foliageCoords : list) {
			int j = foliageCoords.getBranchBase();
			BlockPos blockPos2 = new BlockPos(blockPos.getX(), j, blockPos.getZ());
			if (!blockPos2.equals(foliageCoords) && this.trimBranches(i, j - blockPos.getY())) {
				this.makeLimb(levelSimulatedRW, random, blockPos2, foliageCoords, true, set, boundingBox, smallTreeConfiguration);
			}
		}
	}

	public boolean doPlace(
		LevelSimulatedRW levelSimulatedRW,
		Random random,
		BlockPos blockPos,
		Set<BlockPos> set,
		Set<BlockPos> set2,
		BoundingBox boundingBox,
		SmallTreeConfiguration smallTreeConfiguration
	) {
		Random random2 = new Random(random.nextLong());
		int i = this.checkLocation(levelSimulatedRW, random, blockPos, 5 + random2.nextInt(12), set, boundingBox, smallTreeConfiguration);
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
			List<FancyTreeFeature.FoliageCoords> list = Lists.<FancyTreeFeature.FoliageCoords>newArrayList();
			list.add(new FancyTreeFeature.FoliageCoords(blockPos.above(m), l));

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
						if (this.makeLimb(levelSimulatedRW, random, blockPos2, blockPos3, false, set, boundingBox, smallTreeConfiguration) == -1) {
							int q = blockPos.getX() - blockPos2.getX();
							int r = blockPos.getZ() - blockPos2.getZ();
							double s = (double)blockPos2.getY() - Math.sqrt((double)(q * q + r * r)) * 0.381;
							int t = s > (double)l ? l : (int)s;
							BlockPos blockPos4 = new BlockPos(blockPos.getX(), t, blockPos.getZ());
							if (this.makeLimb(levelSimulatedRW, random, blockPos4, blockPos2, false, set, boundingBox, smallTreeConfiguration) == -1) {
								list.add(new FancyTreeFeature.FoliageCoords(blockPos2, blockPos4.getY()));
							}
						}
					}
				}
			}

			this.makeFoliage(levelSimulatedRW, random, i, blockPos, list, set2, boundingBox, smallTreeConfiguration);
			this.makeTrunk(levelSimulatedRW, random, blockPos, j, set, boundingBox, smallTreeConfiguration);
			this.makeBranches(levelSimulatedRW, random, i, blockPos, list, set, boundingBox, smallTreeConfiguration);
			return true;
		}
	}

	private int checkLocation(
		LevelSimulatedRW levelSimulatedRW,
		Random random,
		BlockPos blockPos,
		int i,
		Set<BlockPos> set,
		BoundingBox boundingBox,
		SmallTreeConfiguration smallTreeConfiguration
	) {
		if (!isGrassOrDirtOrFarmland(levelSimulatedRW, blockPos.below())) {
			return -1;
		} else {
			int j = this.makeLimb(levelSimulatedRW, random, blockPos, blockPos.above(i - 1), false, set, boundingBox, smallTreeConfiguration);
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
