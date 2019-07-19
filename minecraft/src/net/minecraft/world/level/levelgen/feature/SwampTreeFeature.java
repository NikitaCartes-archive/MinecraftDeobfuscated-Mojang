package net.minecraft.world.level.levelgen.feature;

import com.mojang.datafixers.Dynamic;
import java.util.Random;
import java.util.Set;
import java.util.function.Function;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelSimulatedRW;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.VineBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.structure.BoundingBox;

public class SwampTreeFeature extends AbstractTreeFeature<NoneFeatureConfiguration> {
	private static final BlockState TRUNK = Blocks.OAK_LOG.defaultBlockState();
	private static final BlockState LEAF = Blocks.OAK_LEAVES.defaultBlockState();

	public SwampTreeFeature(Function<Dynamic<?>, ? extends NoneFeatureConfiguration> function) {
		super(function, false);
	}

	@Override
	public boolean doPlace(Set<BlockPos> set, LevelSimulatedRW levelSimulatedRW, Random random, BlockPos blockPos, BoundingBox boundingBox) {
		int i = random.nextInt(4) + 5;
		blockPos = levelSimulatedRW.getHeightmapPos(Heightmap.Types.OCEAN_FLOOR, blockPos);
		boolean bl = true;
		if (blockPos.getY() >= 1 && blockPos.getY() + i + 1 <= 256) {
			for (int j = blockPos.getY(); j <= blockPos.getY() + 1 + i; j++) {
				int k = 1;
				if (j == blockPos.getY()) {
					k = 0;
				}

				if (j >= blockPos.getY() + 1 + i - 2) {
					k = 3;
				}

				BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();

				for (int l = blockPos.getX() - k; l <= blockPos.getX() + k && bl; l++) {
					for (int m = blockPos.getZ() - k; m <= blockPos.getZ() + k && bl; m++) {
						if (j >= 0 && j < 256) {
							mutableBlockPos.set(l, j, m);
							if (!isAirOrLeaves(levelSimulatedRW, mutableBlockPos)) {
								if (isBlockWater(levelSimulatedRW, mutableBlockPos)) {
									if (j > blockPos.getY()) {
										bl = false;
									}
								} else {
									bl = false;
								}
							}
						} else {
							bl = false;
						}
					}
				}
			}

			if (!bl) {
				return false;
			} else if (isGrassOrDirt(levelSimulatedRW, blockPos.below()) && blockPos.getY() < 256 - i - 1) {
				this.setDirtAt(levelSimulatedRW, blockPos.below());

				for (int j = blockPos.getY() - 3 + i; j <= blockPos.getY() + i; j++) {
					int kx = j - (blockPos.getY() + i);
					int n = 2 - kx / 2;

					for (int l = blockPos.getX() - n; l <= blockPos.getX() + n; l++) {
						int mx = l - blockPos.getX();

						for (int o = blockPos.getZ() - n; o <= blockPos.getZ() + n; o++) {
							int p = o - blockPos.getZ();
							if (Math.abs(mx) != n || Math.abs(p) != n || random.nextInt(2) != 0 && kx != 0) {
								BlockPos blockPos2 = new BlockPos(l, j, o);
								if (isAirOrLeaves(levelSimulatedRW, blockPos2) || isReplaceablePlant(levelSimulatedRW, blockPos2)) {
									this.setBlock(set, levelSimulatedRW, blockPos2, LEAF, boundingBox);
								}
							}
						}
					}
				}

				for (int j = 0; j < i; j++) {
					BlockPos blockPos3 = blockPos.above(j);
					if (isAirOrLeaves(levelSimulatedRW, blockPos3) || isBlockWater(levelSimulatedRW, blockPos3)) {
						this.setBlock(set, levelSimulatedRW, blockPos3, TRUNK, boundingBox);
					}
				}

				for (int jx = blockPos.getY() - 3 + i; jx <= blockPos.getY() + i; jx++) {
					int kx = jx - (blockPos.getY() + i);
					int n = 2 - kx / 2;
					BlockPos.MutableBlockPos mutableBlockPos2 = new BlockPos.MutableBlockPos();

					for (int mx = blockPos.getX() - n; mx <= blockPos.getX() + n; mx++) {
						for (int ox = blockPos.getZ() - n; ox <= blockPos.getZ() + n; ox++) {
							mutableBlockPos2.set(mx, jx, ox);
							if (isLeaves(levelSimulatedRW, mutableBlockPos2)) {
								BlockPos blockPos4 = mutableBlockPos2.west();
								BlockPos blockPos2 = mutableBlockPos2.east();
								BlockPos blockPos5 = mutableBlockPos2.north();
								BlockPos blockPos6 = mutableBlockPos2.south();
								if (random.nextInt(4) == 0 && isAir(levelSimulatedRW, blockPos4)) {
									this.addVine(levelSimulatedRW, blockPos4, VineBlock.EAST);
								}

								if (random.nextInt(4) == 0 && isAir(levelSimulatedRW, blockPos2)) {
									this.addVine(levelSimulatedRW, blockPos2, VineBlock.WEST);
								}

								if (random.nextInt(4) == 0 && isAir(levelSimulatedRW, blockPos5)) {
									this.addVine(levelSimulatedRW, blockPos5, VineBlock.SOUTH);
								}

								if (random.nextInt(4) == 0 && isAir(levelSimulatedRW, blockPos6)) {
									this.addVine(levelSimulatedRW, blockPos6, VineBlock.NORTH);
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

	private void addVine(LevelSimulatedRW levelSimulatedRW, BlockPos blockPos, BooleanProperty booleanProperty) {
		BlockState blockState = Blocks.VINE.defaultBlockState().setValue(booleanProperty, Boolean.valueOf(true));
		this.setBlock(levelSimulatedRW, blockPos, blockState);
		int i = 4;

		for (BlockPos var6 = blockPos.below(); isAir(levelSimulatedRW, var6) && i > 0; i--) {
			this.setBlock(levelSimulatedRW, var6, blockState);
			var6 = var6.below();
		}
	}
}
