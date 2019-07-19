package net.minecraft.world.level.levelgen.feature;

import com.mojang.datafixers.Dynamic;
import java.util.Random;
import java.util.Set;
import java.util.function.Function;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.LevelSimulatedRW;
import net.minecraft.world.level.LevelSimulatedReader;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.BoundingBox;

public class DarkOakFeature extends AbstractTreeFeature<NoneFeatureConfiguration> {
	private static final BlockState LOG = Blocks.DARK_OAK_LOG.defaultBlockState();
	private static final BlockState LEAVES = Blocks.DARK_OAK_LEAVES.defaultBlockState();

	public DarkOakFeature(Function<Dynamic<?>, ? extends NoneFeatureConfiguration> function, boolean bl) {
		super(function, bl);
	}

	@Override
	public boolean doPlace(Set<BlockPos> set, LevelSimulatedRW levelSimulatedRW, Random random, BlockPos blockPos, BoundingBox boundingBox) {
		int i = random.nextInt(3) + random.nextInt(2) + 6;
		int j = blockPos.getX();
		int k = blockPos.getY();
		int l = blockPos.getZ();
		if (k >= 1 && k + i + 1 < 256) {
			BlockPos blockPos2 = blockPos.below();
			if (!isGrassOrDirt(levelSimulatedRW, blockPos2)) {
				return false;
			} else if (!this.canPlaceTreeOfHeight(levelSimulatedRW, blockPos, i)) {
				return false;
			} else {
				this.setDirtAt(levelSimulatedRW, blockPos2);
				this.setDirtAt(levelSimulatedRW, blockPos2.east());
				this.setDirtAt(levelSimulatedRW, blockPos2.south());
				this.setDirtAt(levelSimulatedRW, blockPos2.south().east());
				Direction direction = Direction.Plane.HORIZONTAL.getRandomDirection(random);
				int m = i - random.nextInt(4);
				int n = 2 - random.nextInt(3);
				int o = j;
				int p = l;
				int q = k + i - 1;

				for (int r = 0; r < i; r++) {
					if (r >= m && n > 0) {
						o += direction.getStepX();
						p += direction.getStepZ();
						n--;
					}

					int s = k + r;
					BlockPos blockPos3 = new BlockPos(o, s, p);
					if (isAirOrLeaves(levelSimulatedRW, blockPos3)) {
						this.placeLogAt(set, levelSimulatedRW, blockPos3, boundingBox);
						this.placeLogAt(set, levelSimulatedRW, blockPos3.east(), boundingBox);
						this.placeLogAt(set, levelSimulatedRW, blockPos3.south(), boundingBox);
						this.placeLogAt(set, levelSimulatedRW, blockPos3.east().south(), boundingBox);
					}
				}

				for (int r = -2; r <= 0; r++) {
					for (int s = -2; s <= 0; s++) {
						int t = -1;
						this.placeLeafAt(levelSimulatedRW, o + r, q + t, p + s, boundingBox, set);
						this.placeLeafAt(levelSimulatedRW, 1 + o - r, q + t, p + s, boundingBox, set);
						this.placeLeafAt(levelSimulatedRW, o + r, q + t, 1 + p - s, boundingBox, set);
						this.placeLeafAt(levelSimulatedRW, 1 + o - r, q + t, 1 + p - s, boundingBox, set);
						if ((r > -2 || s > -1) && (r != -1 || s != -2)) {
							int var29 = 1;
							this.placeLeafAt(levelSimulatedRW, o + r, q + var29, p + s, boundingBox, set);
							this.placeLeafAt(levelSimulatedRW, 1 + o - r, q + var29, p + s, boundingBox, set);
							this.placeLeafAt(levelSimulatedRW, o + r, q + var29, 1 + p - s, boundingBox, set);
							this.placeLeafAt(levelSimulatedRW, 1 + o - r, q + var29, 1 + p - s, boundingBox, set);
						}
					}
				}

				if (random.nextBoolean()) {
					this.placeLeafAt(levelSimulatedRW, o, q + 2, p, boundingBox, set);
					this.placeLeafAt(levelSimulatedRW, o + 1, q + 2, p, boundingBox, set);
					this.placeLeafAt(levelSimulatedRW, o + 1, q + 2, p + 1, boundingBox, set);
					this.placeLeafAt(levelSimulatedRW, o, q + 2, p + 1, boundingBox, set);
				}

				for (int r = -3; r <= 4; r++) {
					for (int sx = -3; sx <= 4; sx++) {
						if ((r != -3 || sx != -3) && (r != -3 || sx != 4) && (r != 4 || sx != -3) && (r != 4 || sx != 4) && (Math.abs(r) < 3 || Math.abs(sx) < 3)) {
							this.placeLeafAt(levelSimulatedRW, o + r, q, p + sx, boundingBox, set);
						}
					}
				}

				for (int r = -1; r <= 2; r++) {
					for (int sxx = -1; sxx <= 2; sxx++) {
						if ((r < 0 || r > 1 || sxx < 0 || sxx > 1) && random.nextInt(3) <= 0) {
							int t = random.nextInt(3) + 2;

							for (int u = 0; u < t; u++) {
								this.placeLogAt(set, levelSimulatedRW, new BlockPos(j + r, q - u - 1, l + sxx), boundingBox);
							}

							for (int u = -1; u <= 1; u++) {
								for (int v = -1; v <= 1; v++) {
									this.placeLeafAt(levelSimulatedRW, o + r + u, q, p + sxx + v, boundingBox, set);
								}
							}

							for (int u = -2; u <= 2; u++) {
								for (int v = -2; v <= 2; v++) {
									if (Math.abs(u) != 2 || Math.abs(v) != 2) {
										this.placeLeafAt(levelSimulatedRW, o + r + u, q - 1, p + sxx + v, boundingBox, set);
									}
								}
							}
						}
					}
				}

				return true;
			}
		} else {
			return false;
		}
	}

	private boolean canPlaceTreeOfHeight(LevelSimulatedReader levelSimulatedReader, BlockPos blockPos, int i) {
		int j = blockPos.getX();
		int k = blockPos.getY();
		int l = blockPos.getZ();
		BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();

		for (int m = 0; m <= i + 1; m++) {
			int n = 1;
			if (m == 0) {
				n = 0;
			}

			if (m >= i - 1) {
				n = 2;
			}

			for (int o = -n; o <= n; o++) {
				for (int p = -n; p <= n; p++) {
					if (!isFree(levelSimulatedReader, mutableBlockPos.set(j + o, k + m, l + p))) {
						return false;
					}
				}
			}
		}

		return true;
	}

	private void placeLogAt(Set<BlockPos> set, LevelSimulatedRW levelSimulatedRW, BlockPos blockPos, BoundingBox boundingBox) {
		if (isFree(levelSimulatedRW, blockPos)) {
			this.setBlock(set, levelSimulatedRW, blockPos, LOG, boundingBox);
		}
	}

	private void placeLeafAt(LevelSimulatedRW levelSimulatedRW, int i, int j, int k, BoundingBox boundingBox, Set<BlockPos> set) {
		BlockPos blockPos = new BlockPos(i, j, k);
		if (isAir(levelSimulatedRW, blockPos)) {
			this.setBlock(set, levelSimulatedRW, blockPos, LEAVES, boundingBox);
		}
	}
}
