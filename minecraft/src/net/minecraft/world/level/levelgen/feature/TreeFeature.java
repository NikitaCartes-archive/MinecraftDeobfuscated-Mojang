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
import net.minecraft.world.level.block.CocoaBlock;
import net.minecraft.world.level.block.VineBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.levelgen.structure.BoundingBox;

public class TreeFeature extends AbstractTreeFeature<NoneFeatureConfiguration> {
	private static final BlockState DEFAULT_TRUNK = Blocks.OAK_LOG.defaultBlockState();
	private static final BlockState DEFAULT_LEAF = Blocks.OAK_LEAVES.defaultBlockState();
	protected final int baseHeight;
	private final boolean addJungleFeatures;
	private final BlockState trunk;
	private final BlockState leaf;

	public TreeFeature(Function<Dynamic<?>, ? extends NoneFeatureConfiguration> function, boolean bl) {
		this(function, bl, 4, DEFAULT_TRUNK, DEFAULT_LEAF, false);
	}

	public TreeFeature(
		Function<Dynamic<?>, ? extends NoneFeatureConfiguration> function, boolean bl, int i, BlockState blockState, BlockState blockState2, boolean bl2
	) {
		super(function, bl);
		this.baseHeight = i;
		this.trunk = blockState;
		this.leaf = blockState2;
		this.addJungleFeatures = bl2;
	}

	@Override
	public boolean doPlace(Set<BlockPos> set, LevelSimulatedRW levelSimulatedRW, Random random, BlockPos blockPos, BoundingBox boundingBox) {
		int i = this.getTreeHeight(random);
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
			} else if (isGrassOrDirtOrFarmland(levelSimulatedRW, blockPos.below()) && blockPos.getY() < 256 - i - 1) {
				this.setDirtAt(levelSimulatedRW, blockPos.below());
				int j = 3;
				int kx = 0;

				for (int n = blockPos.getY() - 3 + i; n <= blockPos.getY() + i; n++) {
					int l = n - (blockPos.getY() + i);
					int mx = 1 - l / 2;

					for (int o = blockPos.getX() - mx; o <= blockPos.getX() + mx; o++) {
						int p = o - blockPos.getX();

						for (int q = blockPos.getZ() - mx; q <= blockPos.getZ() + mx; q++) {
							int r = q - blockPos.getZ();
							if (Math.abs(p) != mx || Math.abs(r) != mx || random.nextInt(2) != 0 && l != 0) {
								BlockPos blockPos2 = new BlockPos(o, n, q);
								if (isAirOrLeaves(levelSimulatedRW, blockPos2) || isReplaceablePlant(levelSimulatedRW, blockPos2)) {
									this.setBlock(set, levelSimulatedRW, blockPos2, this.leaf, boundingBox);
								}
							}
						}
					}
				}

				for (int n = 0; n < i; n++) {
					if (isAirOrLeaves(levelSimulatedRW, blockPos.above(n)) || isReplaceablePlant(levelSimulatedRW, blockPos.above(n))) {
						this.setBlock(set, levelSimulatedRW, blockPos.above(n), this.trunk, boundingBox);
						if (this.addJungleFeatures && n > 0) {
							if (random.nextInt(3) > 0 && isAir(levelSimulatedRW, blockPos.offset(-1, n, 0))) {
								this.addVine(levelSimulatedRW, blockPos.offset(-1, n, 0), VineBlock.EAST);
							}

							if (random.nextInt(3) > 0 && isAir(levelSimulatedRW, blockPos.offset(1, n, 0))) {
								this.addVine(levelSimulatedRW, blockPos.offset(1, n, 0), VineBlock.WEST);
							}

							if (random.nextInt(3) > 0 && isAir(levelSimulatedRW, blockPos.offset(0, n, -1))) {
								this.addVine(levelSimulatedRW, blockPos.offset(0, n, -1), VineBlock.SOUTH);
							}

							if (random.nextInt(3) > 0 && isAir(levelSimulatedRW, blockPos.offset(0, n, 1))) {
								this.addVine(levelSimulatedRW, blockPos.offset(0, n, 1), VineBlock.NORTH);
							}
						}
					}
				}

				if (this.addJungleFeatures) {
					for (int nx = blockPos.getY() - 3 + i; nx <= blockPos.getY() + i; nx++) {
						int l = nx - (blockPos.getY() + i);
						int mx = 2 - l / 2;
						BlockPos.MutableBlockPos mutableBlockPos2 = new BlockPos.MutableBlockPos();

						for (int p = blockPos.getX() - mx; p <= blockPos.getX() + mx; p++) {
							for (int qx = blockPos.getZ() - mx; qx <= blockPos.getZ() + mx; qx++) {
								mutableBlockPos2.set(p, nx, qx);
								if (isLeaves(levelSimulatedRW, mutableBlockPos2)) {
									BlockPos blockPos3 = mutableBlockPos2.west();
									BlockPos blockPos2 = mutableBlockPos2.east();
									BlockPos blockPos4 = mutableBlockPos2.north();
									BlockPos blockPos5 = mutableBlockPos2.south();
									if (random.nextInt(4) == 0 && isAir(levelSimulatedRW, blockPos3)) {
										this.addHangingVine(levelSimulatedRW, blockPos3, VineBlock.EAST);
									}

									if (random.nextInt(4) == 0 && isAir(levelSimulatedRW, blockPos2)) {
										this.addHangingVine(levelSimulatedRW, blockPos2, VineBlock.WEST);
									}

									if (random.nextInt(4) == 0 && isAir(levelSimulatedRW, blockPos4)) {
										this.addHangingVine(levelSimulatedRW, blockPos4, VineBlock.SOUTH);
									}

									if (random.nextInt(4) == 0 && isAir(levelSimulatedRW, blockPos5)) {
										this.addHangingVine(levelSimulatedRW, blockPos5, VineBlock.NORTH);
									}
								}
							}
						}
					}

					if (random.nextInt(5) == 0 && i > 5) {
						for (int nx = 0; nx < 2; nx++) {
							for (Direction direction : Direction.Plane.HORIZONTAL) {
								if (random.nextInt(4 - nx) == 0) {
									Direction direction2 = direction.getOpposite();
									this.placeCocoa(levelSimulatedRW, random.nextInt(3), blockPos.offset(direction2.getStepX(), i - 5 + nx, direction2.getStepZ()), direction);
								}
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

	protected int getTreeHeight(Random random) {
		return this.baseHeight + random.nextInt(3);
	}

	private void placeCocoa(LevelWriter levelWriter, int i, BlockPos blockPos, Direction direction) {
		this.setBlock(levelWriter, blockPos, Blocks.COCOA.defaultBlockState().setValue(CocoaBlock.AGE, Integer.valueOf(i)).setValue(CocoaBlock.FACING, direction));
	}

	private void addVine(LevelWriter levelWriter, BlockPos blockPos, BooleanProperty booleanProperty) {
		this.setBlock(levelWriter, blockPos, Blocks.VINE.defaultBlockState().setValue(booleanProperty, Boolean.valueOf(true)));
	}

	private void addHangingVine(LevelSimulatedRW levelSimulatedRW, BlockPos blockPos, BooleanProperty booleanProperty) {
		this.addVine(levelSimulatedRW, blockPos, booleanProperty);
		int i = 4;

		for (BlockPos var5 = blockPos.below(); isAir(levelSimulatedRW, var5) && i > 0; i--) {
			this.addVine(levelSimulatedRW, var5, booleanProperty);
			var5 = var5.below();
		}
	}
}
