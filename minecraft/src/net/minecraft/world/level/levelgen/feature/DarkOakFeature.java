package net.minecraft.world.level.levelgen.feature;

import com.mojang.datafixers.Dynamic;
import java.util.Random;
import java.util.Set;
import java.util.function.Function;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.LevelSimulatedRW;
import net.minecraft.world.level.LevelSimulatedReader;
import net.minecraft.world.level.levelgen.feature.configurations.MegaTreeConfiguration;
import net.minecraft.world.level.levelgen.structure.BoundingBox;

public class DarkOakFeature extends AbstractTreeFeature<MegaTreeConfiguration> {
	public DarkOakFeature(Function<Dynamic<?>, ? extends MegaTreeConfiguration> function, Function<Random, ? extends MegaTreeConfiguration> function2) {
		super(function, function2);
	}

	public boolean doPlace(
		LevelSimulatedRW levelSimulatedRW,
		Random random,
		BlockPos blockPos,
		Set<BlockPos> set,
		Set<BlockPos> set2,
		BoundingBox boundingBox,
		MegaTreeConfiguration megaTreeConfiguration
	) {
		int i = random.nextInt(3) + random.nextInt(2) + megaTreeConfiguration.baseHeight;
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
						this.placeLog(levelSimulatedRW, random, blockPos3, set, boundingBox, megaTreeConfiguration);
						this.placeLog(levelSimulatedRW, random, blockPos3.east(), set, boundingBox, megaTreeConfiguration);
						this.placeLog(levelSimulatedRW, random, blockPos3.south(), set, boundingBox, megaTreeConfiguration);
						this.placeLog(levelSimulatedRW, random, blockPos3.east().south(), set, boundingBox, megaTreeConfiguration);
					}
				}

				for (int r = -2; r <= 0; r++) {
					for (int s = -2; s <= 0; s++) {
						int t = -1;
						this.placeLeaf(levelSimulatedRW, random, new BlockPos(o + r, q + t, p + s), set2, boundingBox, megaTreeConfiguration);
						this.placeLeaf(levelSimulatedRW, random, new BlockPos(1 + o - r, q + t, p + s), set2, boundingBox, megaTreeConfiguration);
						this.placeLeaf(levelSimulatedRW, random, new BlockPos(o + r, q + t, 1 + p - s), set2, boundingBox, megaTreeConfiguration);
						this.placeLeaf(levelSimulatedRW, random, new BlockPos(1 + o - r, q + t, 1 + p - s), set2, boundingBox, megaTreeConfiguration);
						if ((r > -2 || s > -1) && (r != -1 || s != -2)) {
							int var31 = 1;
							this.placeLeaf(levelSimulatedRW, random, new BlockPos(o + r, q + var31, p + s), set2, boundingBox, megaTreeConfiguration);
							this.placeLeaf(levelSimulatedRW, random, new BlockPos(1 + o - r, q + var31, p + s), set2, boundingBox, megaTreeConfiguration);
							this.placeLeaf(levelSimulatedRW, random, new BlockPos(o + r, q + var31, 1 + p - s), set2, boundingBox, megaTreeConfiguration);
							this.placeLeaf(levelSimulatedRW, random, new BlockPos(1 + o - r, q + var31, 1 + p - s), set2, boundingBox, megaTreeConfiguration);
						}
					}
				}

				if (random.nextBoolean()) {
					this.placeLeaf(levelSimulatedRW, random, new BlockPos(o, q + 2, p), set2, boundingBox, megaTreeConfiguration);
					this.placeLeaf(levelSimulatedRW, random, new BlockPos(o + 1, q + 2, p), set2, boundingBox, megaTreeConfiguration);
					this.placeLeaf(levelSimulatedRW, random, new BlockPos(o + 1, q + 2, p + 1), set2, boundingBox, megaTreeConfiguration);
					this.placeLeaf(levelSimulatedRW, random, new BlockPos(o, q + 2, p + 1), set2, boundingBox, megaTreeConfiguration);
				}

				for (int r = -3; r <= 4; r++) {
					for (int sx = -3; sx <= 4; sx++) {
						if ((r != -3 || sx != -3) && (r != -3 || sx != 4) && (r != 4 || sx != -3) && (r != 4 || sx != 4) && (Math.abs(r) < 3 || Math.abs(sx) < 3)) {
							this.placeLeaf(levelSimulatedRW, random, new BlockPos(o + r, q, p + sx), set2, boundingBox, megaTreeConfiguration);
						}
					}
				}

				for (int r = -1; r <= 2; r++) {
					for (int sxx = -1; sxx <= 2; sxx++) {
						if ((r < 0 || r > 1 || sxx < 0 || sxx > 1) && random.nextInt(3) <= 0) {
							int t = random.nextInt(3) + 2;

							for (int u = 0; u < t; u++) {
								this.placeLog(levelSimulatedRW, random, new BlockPos(j + r, q - u - 1, l + sxx), set, boundingBox, megaTreeConfiguration);
							}

							for (int u = -1; u <= 1; u++) {
								for (int v = -1; v <= 1; v++) {
									this.placeLeaf(levelSimulatedRW, random, new BlockPos(o + r + u, q, p + sxx + v), set2, boundingBox, megaTreeConfiguration);
								}
							}

							for (int u = -2; u <= 2; u++) {
								for (int v = -2; v <= 2; v++) {
									if (Math.abs(u) != 2 || Math.abs(v) != 2) {
										this.placeLeaf(levelSimulatedRW, random, new BlockPos(o + r + u, q - 1, p + sxx + v), set2, boundingBox, megaTreeConfiguration);
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
}
