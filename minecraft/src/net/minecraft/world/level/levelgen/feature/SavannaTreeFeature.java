package net.minecraft.world.level.levelgen.feature;

import com.mojang.datafixers.Dynamic;
import java.util.Random;
import java.util.Set;
import java.util.function.Function;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.LevelSimulatedRW;
import net.minecraft.world.level.LevelWriter;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.BoundingBox;

public class SavannaTreeFeature extends AbstractTreeFeature<NoneFeatureConfiguration> {
	private static final BlockState TRUNK = Blocks.ACACIA_LOG.defaultBlockState();
	private static final BlockState LEAF = Blocks.ACACIA_LEAVES.defaultBlockState();

	public SavannaTreeFeature(Function<Dynamic<?>, ? extends NoneFeatureConfiguration> function, boolean bl) {
		super(function, bl);
	}

	@Override
	public boolean doPlace(Set<BlockPos> set, LevelSimulatedRW levelSimulatedRW, Random random, BlockPos blockPos, BoundingBox boundingBox) {
		int i = random.nextInt(3) + random.nextInt(3) + 5;
		boolean bl = true;
		if (blockPos.getY() >= 1 && blockPos.getY() + i + 1 <= 256) {
			for (int j = blockPos.getY(); j <= blockPos.getY() + 1 + i; j++) {
				int k = 1;
				if (j == blockPos.getY()) {
					k = 0;
				}

				if (j >= blockPos.getY() + 1 + i - 2) {
					k = 2;
				}

				BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();

				for (int l = blockPos.getX() - k; l <= blockPos.getX() + k && bl; l++) {
					for (int m = blockPos.getZ() - k; m <= blockPos.getZ() + k && bl; m++) {
						if (j < 0 || j >= 256) {
							bl = false;
						} else if (!isFree(levelSimulatedRW, mutableBlockPos.set(l, j, m))) {
							bl = false;
						}
					}
				}
			}

			if (!bl) {
				return false;
			} else if (isGrassOrDirt(levelSimulatedRW, blockPos.below()) && blockPos.getY() < 256 - i - 1) {
				this.setDirtAt(levelSimulatedRW, blockPos.below());
				Direction direction = Direction.Plane.HORIZONTAL.getRandomDirection(random);
				int kx = i - random.nextInt(4) - 1;
				int n = 3 - random.nextInt(3);
				int l = blockPos.getX();
				int mx = blockPos.getZ();
				int o = 0;

				for (int p = 0; p < i; p++) {
					int q = blockPos.getY() + p;
					if (p >= kx && n > 0) {
						l += direction.getStepX();
						mx += direction.getStepZ();
						n--;
					}

					BlockPos blockPos2 = new BlockPos(l, q, mx);
					if (isAirOrLeaves(levelSimulatedRW, blockPos2)) {
						this.placeLogAt(set, levelSimulatedRW, blockPos2, boundingBox);
						o = q;
					}
				}

				BlockPos blockPos3 = new BlockPos(l, o, mx);

				for (int qx = -3; qx <= 3; qx++) {
					for (int r = -3; r <= 3; r++) {
						if (Math.abs(qx) != 3 || Math.abs(r) != 3) {
							this.placeLeafAt(set, levelSimulatedRW, blockPos3.offset(qx, 0, r), boundingBox);
						}
					}
				}

				blockPos3 = blockPos3.above();

				for (int qx = -1; qx <= 1; qx++) {
					for (int rx = -1; rx <= 1; rx++) {
						this.placeLeafAt(set, levelSimulatedRW, blockPos3.offset(qx, 0, rx), boundingBox);
					}
				}

				this.placeLeafAt(set, levelSimulatedRW, blockPos3.east(2), boundingBox);
				this.placeLeafAt(set, levelSimulatedRW, blockPos3.west(2), boundingBox);
				this.placeLeafAt(set, levelSimulatedRW, blockPos3.south(2), boundingBox);
				this.placeLeafAt(set, levelSimulatedRW, blockPos3.north(2), boundingBox);
				l = blockPos.getX();
				mx = blockPos.getZ();
				Direction direction2 = Direction.Plane.HORIZONTAL.getRandomDirection(random);
				if (direction2 != direction) {
					int qx = kx - random.nextInt(2) - 1;
					int rx = 1 + random.nextInt(3);
					o = 0;

					for (int s = qx; s < i && rx > 0; rx--) {
						if (s >= 1) {
							int t = blockPos.getY() + s;
							l += direction2.getStepX();
							mx += direction2.getStepZ();
							BlockPos blockPos4 = new BlockPos(l, t, mx);
							if (isAirOrLeaves(levelSimulatedRW, blockPos4)) {
								this.placeLogAt(set, levelSimulatedRW, blockPos4, boundingBox);
								o = t;
							}
						}

						s++;
					}

					if (o > 0) {
						BlockPos blockPos5 = new BlockPos(l, o, mx);

						for (int t = -2; t <= 2; t++) {
							for (int u = -2; u <= 2; u++) {
								if (Math.abs(t) != 2 || Math.abs(u) != 2) {
									this.placeLeafAt(set, levelSimulatedRW, blockPos5.offset(t, 0, u), boundingBox);
								}
							}
						}

						blockPos5 = blockPos5.above();

						for (int t = -1; t <= 1; t++) {
							for (int ux = -1; ux <= 1; ux++) {
								this.placeLeafAt(set, levelSimulatedRW, blockPos5.offset(t, 0, ux), boundingBox);
							}
						}
					}
				}

				return true;
			} else {
				return false;
			}
		} else {
			return false;
		}
	}

	private void placeLogAt(Set<BlockPos> set, LevelWriter levelWriter, BlockPos blockPos, BoundingBox boundingBox) {
		this.setBlock(set, levelWriter, blockPos, TRUNK, boundingBox);
	}

	private void placeLeafAt(Set<BlockPos> set, LevelSimulatedRW levelSimulatedRW, BlockPos blockPos, BoundingBox boundingBox) {
		if (isAirOrLeaves(levelSimulatedRW, blockPos)) {
			this.setBlock(set, levelSimulatedRW, blockPos, LEAF, boundingBox);
		}
	}
}
