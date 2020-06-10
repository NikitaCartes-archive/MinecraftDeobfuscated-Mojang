package net.minecraft.world.level.levelgen.feature.trunkplacers;

import com.google.common.collect.Lists;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.level.LevelSimulatedRW;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.levelgen.feature.TreeFeature;
import net.minecraft.world.level.levelgen.feature.configurations.TreeConfiguration;
import net.minecraft.world.level.levelgen.feature.foliageplacers.FoliagePlacer;
import net.minecraft.world.level.levelgen.structure.BoundingBox;

public class FancyTrunkPlacer extends TrunkPlacer {
	public static final Codec<FancyTrunkPlacer> CODEC = RecordCodecBuilder.create(instance -> trunkPlacerParts(instance).apply(instance, FancyTrunkPlacer::new));

	public FancyTrunkPlacer(int i, int j, int k) {
		super(i, j, k);
	}

	@Override
	protected TrunkPlacerType<?> type() {
		return TrunkPlacerType.FANCY_TRUNK_PLACER;
	}

	@Override
	public List<FoliagePlacer.FoliageAttachment> placeTrunk(
		LevelSimulatedRW levelSimulatedRW, Random random, int i, BlockPos blockPos, Set<BlockPos> set, BoundingBox boundingBox, TreeConfiguration treeConfiguration
	) {
		int j = 5;
		int k = i + 2;
		int l = Mth.floor((double)k * 0.618);
		if (!treeConfiguration.fromSapling) {
			setDirtAt(levelSimulatedRW, blockPos.below());
		}

		double d = 1.0;
		int m = Math.min(1, Mth.floor(1.382 + Math.pow(1.0 * (double)k / 13.0, 2.0)));
		int n = blockPos.getY() + l;
		int o = k - 5;
		List<FancyTrunkPlacer.FoliageCoords> list = Lists.<FancyTrunkPlacer.FoliageCoords>newArrayList();
		list.add(new FancyTrunkPlacer.FoliageCoords(blockPos.above(o), n));

		for (; o >= 0; o--) {
			float f = this.treeShape(k, o);
			if (!(f < 0.0F)) {
				for (int p = 0; p < m; p++) {
					double e = 1.0;
					double g = 1.0 * (double)f * ((double)random.nextFloat() + 0.328);
					double h = (double)(random.nextFloat() * 2.0F) * Math.PI;
					double q = g * Math.sin(h) + 0.5;
					double r = g * Math.cos(h) + 0.5;
					BlockPos blockPos2 = blockPos.offset(q, (double)(o - 1), r);
					BlockPos blockPos3 = blockPos2.above(5);
					if (this.makeLimb(levelSimulatedRW, random, blockPos2, blockPos3, false, set, boundingBox, treeConfiguration)) {
						int s = blockPos.getX() - blockPos2.getX();
						int t = blockPos.getZ() - blockPos2.getZ();
						double u = (double)blockPos2.getY() - Math.sqrt((double)(s * s + t * t)) * 0.381;
						int v = u > (double)n ? n : (int)u;
						BlockPos blockPos4 = new BlockPos(blockPos.getX(), v, blockPos.getZ());
						if (this.makeLimb(levelSimulatedRW, random, blockPos4, blockPos2, false, set, boundingBox, treeConfiguration)) {
							list.add(new FancyTrunkPlacer.FoliageCoords(blockPos2, blockPos4.getY()));
						}
					}
				}
			}
		}

		this.makeLimb(levelSimulatedRW, random, blockPos, blockPos.above(l), true, set, boundingBox, treeConfiguration);
		this.makeBranches(levelSimulatedRW, random, k, blockPos, list, set, boundingBox, treeConfiguration);
		List<FoliagePlacer.FoliageAttachment> list2 = Lists.<FoliagePlacer.FoliageAttachment>newArrayList();

		for (FancyTrunkPlacer.FoliageCoords foliageCoords : list) {
			if (this.trimBranches(k, foliageCoords.getBranchBase() - blockPos.getY())) {
				list2.add(foliageCoords.attachment);
			}
		}

		return list2;
	}

	private boolean makeLimb(
		LevelSimulatedRW levelSimulatedRW,
		Random random,
		BlockPos blockPos,
		BlockPos blockPos2,
		boolean bl,
		Set<BlockPos> set,
		BoundingBox boundingBox,
		TreeConfiguration treeConfiguration
	) {
		if (!bl && Objects.equals(blockPos, blockPos2)) {
			return true;
		} else {
			BlockPos blockPos3 = blockPos2.offset(-blockPos.getX(), -blockPos.getY(), -blockPos.getZ());
			int i = this.getSteps(blockPos3);
			float f = (float)blockPos3.getX() / (float)i;
			float g = (float)blockPos3.getY() / (float)i;
			float h = (float)blockPos3.getZ() / (float)i;

			for (int j = 0; j <= i; j++) {
				BlockPos blockPos4 = blockPos.offset((double)(0.5F + (float)j * f), (double)(0.5F + (float)j * g), (double)(0.5F + (float)j * h));
				if (bl) {
					setBlock(
						levelSimulatedRW,
						blockPos4,
						treeConfiguration.trunkProvider.getState(random, blockPos4).setValue(RotatedPillarBlock.AXIS, this.getLogAxis(blockPos, blockPos4)),
						boundingBox
					);
					set.add(blockPos4.immutable());
				} else if (!TreeFeature.isFree(levelSimulatedRW, blockPos4)) {
					return false;
				}
			}

			return true;
		}
	}

	private int getSteps(BlockPos blockPos) {
		int i = Mth.abs(blockPos.getX());
		int j = Mth.abs(blockPos.getY());
		int k = Mth.abs(blockPos.getZ());
		return Math.max(i, Math.max(j, k));
	}

	private Direction.Axis getLogAxis(BlockPos blockPos, BlockPos blockPos2) {
		Direction.Axis axis = Direction.Axis.Y;
		int i = Math.abs(blockPos2.getX() - blockPos.getX());
		int j = Math.abs(blockPos2.getZ() - blockPos.getZ());
		int k = Math.max(i, j);
		if (k > 0) {
			if (i == k) {
				axis = Direction.Axis.X;
			} else {
				axis = Direction.Axis.Z;
			}
		}

		return axis;
	}

	private boolean trimBranches(int i, int j) {
		return (double)j >= (double)i * 0.2;
	}

	private void makeBranches(
		LevelSimulatedRW levelSimulatedRW,
		Random random,
		int i,
		BlockPos blockPos,
		List<FancyTrunkPlacer.FoliageCoords> list,
		Set<BlockPos> set,
		BoundingBox boundingBox,
		TreeConfiguration treeConfiguration
	) {
		for (FancyTrunkPlacer.FoliageCoords foliageCoords : list) {
			int j = foliageCoords.getBranchBase();
			BlockPos blockPos2 = new BlockPos(blockPos.getX(), j, blockPos.getZ());
			if (!blockPos2.equals(foliageCoords.attachment.foliagePos()) && this.trimBranches(i, j - blockPos.getY())) {
				this.makeLimb(levelSimulatedRW, random, blockPos2, foliageCoords.attachment.foliagePos(), true, set, boundingBox, treeConfiguration);
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

	static class FoliageCoords {
		private final FoliagePlacer.FoliageAttachment attachment;
		private final int branchBase;

		public FoliageCoords(BlockPos blockPos, int i) {
			this.attachment = new FoliagePlacer.FoliageAttachment(blockPos, 0, false);
			this.branchBase = i;
		}

		public int getBranchBase() {
			return this.branchBase;
		}
	}
}
