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

public class SpruceFeature extends AbstractTreeFeature<NoneFeatureConfiguration> {
	private static final BlockState TRUNK = Blocks.SPRUCE_LOG.defaultBlockState();
	private static final BlockState LEAF = Blocks.SPRUCE_LEAVES.defaultBlockState();

	public SpruceFeature(Function<Dynamic<?>, ? extends NoneFeatureConfiguration> function, boolean bl) {
		super(function, bl);
	}

	@Override
	public boolean doPlace(Set<BlockPos> set, LevelSimulatedRW levelSimulatedRW, Random random, BlockPos blockPos, BoundingBox boundingBox) {
		int i = random.nextInt(4) + 6;
		int j = 1 + random.nextInt(2);
		int k = i - j;
		int l = 2 + random.nextInt(2);
		boolean bl = true;
		if (blockPos.getY() >= 1 && blockPos.getY() + i + 1 <= 256) {
			for (int m = blockPos.getY(); m <= blockPos.getY() + 1 + i && bl; m++) {
				int n;
				if (m - blockPos.getY() < j) {
					n = 0;
				} else {
					n = l;
				}

				BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();

				for (int o = blockPos.getX() - n; o <= blockPos.getX() + n && bl; o++) {
					for (int p = blockPos.getZ() - n; p <= blockPos.getZ() + n && bl; p++) {
						if (m >= 0 && m < 256) {
							mutableBlockPos.set(o, m, p);
							if (!isAirOrLeaves(levelSimulatedRW, mutableBlockPos)) {
								bl = false;
							}
						} else {
							bl = false;
						}
					}
				}
			}

			if (!bl) {
				return false;
			} else if (isGrassOrDirtOrFarmland(levelSimulatedRW, blockPos.below()) && blockPos.getY() < 256 - i - 1) {
				this.setDirtAt(levelSimulatedRW, blockPos.below());
				int m = random.nextInt(2);
				int n = 1;
				int q = 0;

				for (int o = 0; o <= k; o++) {
					int px = blockPos.getY() + i - o;

					for (int r = blockPos.getX() - m; r <= blockPos.getX() + m; r++) {
						int s = r - blockPos.getX();

						for (int t = blockPos.getZ() - m; t <= blockPos.getZ() + m; t++) {
							int u = t - blockPos.getZ();
							if (Math.abs(s) != m || Math.abs(u) != m || m <= 0) {
								BlockPos blockPos2 = new BlockPos(r, px, t);
								if (isAirOrLeaves(levelSimulatedRW, blockPos2) || isReplaceablePlant(levelSimulatedRW, blockPos2)) {
									this.setBlock(set, levelSimulatedRW, blockPos2, LEAF, boundingBox);
								}
							}
						}
					}

					if (m >= n) {
						m = q;
						q = 1;
						if (++n > l) {
							n = l;
						}
					} else {
						m++;
					}
				}

				int o = random.nextInt(3);

				for (int px = 0; px < i - o; px++) {
					if (isAirOrLeaves(levelSimulatedRW, blockPos.above(px))) {
						this.setBlock(set, levelSimulatedRW, blockPos.above(px), TRUNK, boundingBox);
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
