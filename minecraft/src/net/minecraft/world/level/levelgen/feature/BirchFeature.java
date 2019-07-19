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

public class BirchFeature extends AbstractTreeFeature<NoneFeatureConfiguration> {
	private static final BlockState LOG = Blocks.BIRCH_LOG.defaultBlockState();
	private static final BlockState LEAF = Blocks.BIRCH_LEAVES.defaultBlockState();
	private final boolean superBirch;

	public BirchFeature(Function<Dynamic<?>, ? extends NoneFeatureConfiguration> function, boolean bl, boolean bl2) {
		super(function, bl);
		this.superBirch = bl2;
	}

	@Override
	public boolean doPlace(Set<BlockPos> set, LevelSimulatedRW levelSimulatedRW, Random random, BlockPos blockPos, BoundingBox boundingBox) {
		int i = random.nextInt(3) + 5;
		if (this.superBirch) {
			i += random.nextInt(7);
		}

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

				for (int j = blockPos.getY() - 3 + i; j <= blockPos.getY() + i; j++) {
					int kx = j - (blockPos.getY() + i);
					int n = 1 - kx / 2;

					for (int l = blockPos.getX() - n; l <= blockPos.getX() + n; l++) {
						int mx = l - blockPos.getX();

						for (int o = blockPos.getZ() - n; o <= blockPos.getZ() + n; o++) {
							int p = o - blockPos.getZ();
							if (Math.abs(mx) != n || Math.abs(p) != n || random.nextInt(2) != 0 && kx != 0) {
								BlockPos blockPos2 = new BlockPos(l, j, o);
								if (isAirOrLeaves(levelSimulatedRW, blockPos2)) {
									this.setBlock(set, levelSimulatedRW, blockPos2, LEAF, boundingBox);
								}
							}
						}
					}
				}

				for (int j = 0; j < i; j++) {
					if (isAirOrLeaves(levelSimulatedRW, blockPos.above(j))) {
						this.setBlock(set, levelSimulatedRW, blockPos.above(j), LOG, boundingBox);
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
