package net.minecraft.world.level.levelgen.feature;

import com.mojang.datafixers.Dynamic;
import java.util.Random;
import java.util.Set;
import java.util.function.Function;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelSimulatedRW;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.BoundingBox;

public class PineFeature extends AbstractTreeFeature<NoneFeatureConfiguration> {
	private static final BlockState TRUNK = Blocks.SPRUCE_LOG.defaultBlockState();
	private static final BlockState LEAF = Blocks.SPRUCE_LEAVES.defaultBlockState();

	public PineFeature(Function<Dynamic<?>, ? extends NoneFeatureConfiguration> function) {
		super(function, false);
	}

	@Override
	public boolean doPlace(Set<BlockPos> set, LevelSimulatedRW levelSimulatedRW, Random random, BlockPos blockPos, BoundingBox boundingBox) {
		int i = random.nextInt(5) + 7;
		int j = i - random.nextInt(2) - 3;
		int k = i - j;
		int l = 1 + random.nextInt(k + 1);
		if (blockPos.getY() >= 1 && blockPos.getY() + i + 1 <= 256) {
			boolean bl = true;

			for (int m = blockPos.getY(); m <= blockPos.getY() + 1 + i && bl; m++) {
				int n = 1;
				if (m - blockPos.getY() < j) {
					n = 0;
				} else {
					n = l;
				}

				BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();

				for (int o = blockPos.getX() - n; o <= blockPos.getX() + n && bl; o++) {
					for (int p = blockPos.getZ() - n; p <= blockPos.getZ() + n && bl; p++) {
						if (m < 0 || m >= 256) {
							bl = false;
						} else if (!isFree(levelSimulatedRW, mutableBlockPos.set(o, m, p))) {
							bl = false;
						}
					}
				}
			}

			if (!bl) {
				return false;
			} else if (isGrassOrDirt(levelSimulatedRW, blockPos.below()) && blockPos.getY() < 256 - i - 1) {
				this.setDirtAt(levelSimulatedRW, blockPos.below());
				int m = 0;

				for (int n = blockPos.getY() + i; n >= blockPos.getY() + j; n--) {
					for (int q = blockPos.getX() - m; q <= blockPos.getX() + m; q++) {
						int o = q - blockPos.getX();

						for (int px = blockPos.getZ() - m; px <= blockPos.getZ() + m; px++) {
							int r = px - blockPos.getZ();
							if (Math.abs(o) != m || Math.abs(r) != m || m <= 0) {
								BlockPos blockPos2 = new BlockPos(q, n, px);
								if (isAirOrLeaves(levelSimulatedRW, blockPos2)) {
									this.setBlock(set, levelSimulatedRW, blockPos2, LEAF, boundingBox);
								}
							}
						}
					}

					if (m >= 1 && n == blockPos.getY() + j + 1) {
						m--;
					} else if (m < l) {
						m++;
					}
				}

				for (int n = 0; n < i - 1; n++) {
					if (isAirOrLeaves(levelSimulatedRW, blockPos.above(n))) {
						this.setBlock(set, levelSimulatedRW, blockPos.above(n), TRUNK, boundingBox);
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
}
