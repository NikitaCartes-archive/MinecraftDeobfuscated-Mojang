package net.minecraft.world.level.levelgen.feature;

import com.mojang.serialization.Codec;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import java.util.function.Predicate;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.data.models.blockstates.PropertyDispatch;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.Tag;
import net.minecraft.util.Mth;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.MultifaceBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.feature.configurations.SculkPatchConfiguration;
import net.minecraft.world.phys.shapes.CollisionContext;
import org.apache.logging.log4j.util.TriConsumer;

public class SculkPatchFeature extends Feature<SculkPatchConfiguration> {
	public SculkPatchFeature(Codec<SculkPatchConfiguration> codec) {
		super(codec);
	}

	private BlockPos calculateSurfacePos(WorldGenLevel worldGenLevel, BlockPos blockPos, Direction direction, Direction direction2, int i) {
		BlockPos.MutableBlockPos mutableBlockPos = blockPos.mutable();

		for (int j = 0;
			worldGenLevel.isStateAtPosition(mutableBlockPos, blockState -> this.allowsPlacementOfSculk(worldGenLevel, blockState, blockPos)) && j < i;
			j++
		) {
			mutableBlockPos.move(direction);
		}

		for (int var8 = 0;
			worldGenLevel.isStateAtPosition(mutableBlockPos, blockState -> !this.allowsPlacementOfSculk(worldGenLevel, blockState, blockPos)) && var8 < i;
			var8++
		) {
			mutableBlockPos.move(direction2);
		}

		return mutableBlockPos;
	}

	@Override
	public boolean place(FeaturePlaceContext<SculkPatchConfiguration> featurePlaceContext) {
		WorldGenLevel worldGenLevel = featurePlaceContext.level();
		SculkPatchConfiguration sculkPatchConfiguration = featurePlaceContext.config();
		Random random = featurePlaceContext.random();
		BlockPos blockPos = featurePlaceContext.origin();
		Predicate<BlockState> predicate = getReplaceableTag(sculkPatchConfiguration);
		int i = sculkPatchConfiguration.xzRadius.sample(random) + 1;
		int j = sculkPatchConfiguration.xzRadius.sample(random) + 1;
		Set<BlockPos> set = this.placeGroundPatch(worldGenLevel, sculkPatchConfiguration, random, blockPos, predicate, i, j);
		this.distributeGrowths(featurePlaceContext, worldGenLevel, sculkPatchConfiguration, random, set);
		return !set.isEmpty();
	}

	protected void distributeGrowths(
		FeaturePlaceContext<SculkPatchConfiguration> featurePlaceContext,
		WorldGenLevel worldGenLevel,
		SculkPatchConfiguration sculkPatchConfiguration,
		Random random,
		Set<BlockPos> set
	) {
		for (BlockPos blockPos : set) {
			if (sculkPatchConfiguration.growthChance > 0.0F && random.nextFloat() < sculkPatchConfiguration.growthChance) {
				this.placeGrowth(worldGenLevel, sculkPatchConfiguration, featurePlaceContext.chunkGenerator(), random, blockPos);
			}
		}
	}

	protected boolean placeGrowth(
		WorldGenLevel worldGenLevel, SculkPatchConfiguration sculkPatchConfiguration, ChunkGenerator chunkGenerator, Random random, BlockPos blockPos
	) {
		return ((ConfiguredFeature)sculkPatchConfiguration.growthFeature.get())
			.place(worldGenLevel, chunkGenerator, random, blockPos.relative(sculkPatchConfiguration.surface.getDirection().getOpposite()));
	}

	protected Set<BlockPos> placeGroundPatch(
		WorldGenLevel worldGenLevel, SculkPatchConfiguration sculkPatchConfiguration, Random random, BlockPos blockPos, Predicate<BlockState> predicate, int i, int j
	) {
		int k = (i - 1) * (i - 1);
		int l = (j - 1) * (j - 1);
		Direction direction = sculkPatchConfiguration.surface.getDirection();
		Direction direction2 = direction.getOpposite();
		Set<BlockPos> set = new HashSet();
		int m = 2 * i + 1;
		int n = 2 * j + 1;
		SculkPatchFeature.PatchCellData[][] patchCellDatas = new SculkPatchFeature.PatchCellData[m][n];

		for (int o = 0; o < m; o++) {
			for (int p = 0; p < n; p++) {
				patchCellDatas[o][p] = new SculkPatchFeature.PatchCellData();
			}
		}

		for (int o = -i + 1; o <= i - 1; o++) {
			for (int p = -j + 1; p <= j - 1; p++) {
				float f = (float)(o * o);
				float g = (float)(p * p);
				float h = f / (float)k + g / (float)l + Mth.randomBetween(random, -0.5F, 0.5F);
				if (h <= 1.0F) {
					SculkPatchFeature.PatchCellData patchCellData = patchCellDatas[o + i][p + j];
					patchCellData.variant = SculkPatchFeature.PatchCellVariant.Sculk;
					patchCellData.surfaceHeight = this.calculateSurfacePos(
							worldGenLevel, blockPos.offset(o, 0, p), direction, direction2, sculkPatchConfiguration.verticalRange
						)
						.getY();
				}
			}
		}

		TriConsumer<SculkPatchFeature.PatchCellData, Integer, Integer> triConsumer = (patchCellData, integer, integer2) -> {
			SculkPatchFeature.PatchCellData patchCellData2 = patchCellDatas[integer][integer2];
			if (patchCellData2.variant == SculkPatchFeature.PatchCellVariant.Empty) {
				if (patchCellData2.minNeightbourHeight == Integer.MAX_VALUE) {
					patchCellData2.surfaceHeight = this.calculateSurfacePos(
							worldGenLevel, blockPos.offset(integer - i, 0, integer2 - j), direction, direction2, sculkPatchConfiguration.verticalRange
						)
						.getY();
				}

				if (patchCellData2.surfaceHeight <= patchCellData.surfaceHeight) {
					patchCellData2.variant = SculkPatchFeature.PatchCellVariant.SculkVein;
				}
			}

			if (direction == Direction.DOWN) {
				patchCellData.minNeightbourHeight = Math.min(patchCellData.minNeightbourHeight, patchCellData2.surfaceHeight);
			} else {
				patchCellData.minNeightbourHeight = Math.max(patchCellData.minNeightbourHeight, patchCellData2.surfaceHeight);
			}
		};

		for (int px = 0; px < m; px++) {
			for (int q = 0; q < n; q++) {
				SculkPatchFeature.PatchCellData patchCellData2 = patchCellDatas[px][q];
				if (patchCellDatas[px][q].variant == SculkPatchFeature.PatchCellVariant.Sculk) {
					if (px > 0) {
						triConsumer.accept(patchCellData2, px - 1, q);
					}

					if (px < 2 * i) {
						triConsumer.accept(patchCellData2, px + 1, q);
					}

					if (q > 0) {
						triConsumer.accept(patchCellData2, px, q - 1);
					}

					if (q < 2 * j) {
						triConsumer.accept(patchCellData2, px, q + 1);
					}
				}
			}
		}

		PropertyDispatch.TriFunction<BlockState, BlockPos, Direction, BlockState> triFunction = (blockStatex, blockPosx, directionx) -> {
			BlockState blockState2x = worldGenLevel.getBlockState(blockPosx.relative(directionx));
			if (!blockState2x.is(Blocks.SCULK) && !blockState2x.is(Blocks.SCULK_CATALYST)) {
				MultifaceBlock multifaceBlock = (MultifaceBlock)Blocks.SCULK_VEIN;
				BlockState blockState3x = multifaceBlock.getStateForPlacement(blockStatex, worldGenLevel, blockPosx, directionx);
				if (blockState3x != null) {
					return blockState3x;
				}
			}

			return blockStatex;
		};
		BlockPos.MutableBlockPos mutableBlockPos = blockPos.mutable();
		BlockPos.MutableBlockPos mutableBlockPos2 = mutableBlockPos.mutable();

		for (int r = 0; r < m; r++) {
			for (int s = 0; s < n; s++) {
				SculkPatchFeature.PatchCellData patchCellData3 = patchCellDatas[r][s];
				if (patchCellData3.variant != SculkPatchFeature.PatchCellVariant.Empty) {
					mutableBlockPos.setWithOffset(blockPos, r - i, 0, s - j);
					mutableBlockPos.setY(patchCellData3.surfaceHeight);
					mutableBlockPos2.setWithOffset(mutableBlockPos, sculkPatchConfiguration.surface.getDirection());
					BlockState blockState = worldGenLevel.getBlockState(mutableBlockPos2);
					if (this.allowsPlacementOfSculk(worldGenLevel, worldGenLevel.getBlockState(mutableBlockPos), mutableBlockPos)
						|| !blockState.isFaceSturdy(worldGenLevel, mutableBlockPos2, sculkPatchConfiguration.surface.getDirection().getOpposite())) {
						if (patchCellData3.variant == SculkPatchFeature.PatchCellVariant.Sculk) {
							int t = patchCellData3.surfaceHeight - patchCellData3.minNeightbourHeight;
							int u = Mth.clamp(direction == Direction.DOWN ? t : -t, 1, sculkPatchConfiguration.verticalRange);
							BlockPos blockPos2 = mutableBlockPos2.immutable();
							boolean bl = this.placeGround(worldGenLevel, sculkPatchConfiguration, predicate, random, mutableBlockPos2, u);
							if (bl) {
								set.add(blockPos2);
							}
						}

						if (patchCellData3.variant == SculkPatchFeature.PatchCellVariant.SculkVein || patchCellData3.variant == SculkPatchFeature.PatchCellVariant.Sculk) {
							BlockState blockState2 = worldGenLevel.getBlockState(mutableBlockPos);
							if (blockState2.isAir()
								&& !blockState2.is(Blocks.SCULK)
								&& !blockState2.is(Blocks.SCULK_CATALYST)
								&& blockState.isFaceSturdy(worldGenLevel, mutableBlockPos2, direction2)) {
								BlockState blockState3 = blockState2;
								if (r > 0 && patchCellDatas[r - 1][s].variant != SculkPatchFeature.PatchCellVariant.Sculk) {
									blockState3 = triFunction.apply(blockState2, mutableBlockPos, Direction.WEST);
								}

								if (r < 2 * i && patchCellDatas[r + 1][s].variant != SculkPatchFeature.PatchCellVariant.Sculk) {
									blockState3 = triFunction.apply(blockState3, mutableBlockPos, Direction.EAST);
								}

								if (s > 0 && patchCellDatas[r][s - 1].variant != SculkPatchFeature.PatchCellVariant.Sculk) {
									blockState3 = triFunction.apply(blockState3, mutableBlockPos, Direction.NORTH);
								}

								if (s < 2 * j && patchCellDatas[r][s + 1].variant != SculkPatchFeature.PatchCellVariant.Sculk) {
									blockState3 = triFunction.apply(blockState3, mutableBlockPos, Direction.SOUTH);
								}

								blockState3 = triFunction.apply(blockState3, mutableBlockPos, Direction.UP);
								blockState3 = triFunction.apply(blockState3, mutableBlockPos, Direction.DOWN);
								if (blockState3 != blockState2 && blockState3 != Blocks.SCULK_VEIN.defaultBlockState()) {
									worldGenLevel.setBlock(mutableBlockPos, blockState3, 3);
								}
							}
						}
					}
				}
			}
		}

		return set;
	}

	protected boolean placeGround(
		WorldGenLevel worldGenLevel,
		SculkPatchConfiguration sculkPatchConfiguration,
		Predicate<BlockState> predicate,
		Random random,
		BlockPos.MutableBlockPos mutableBlockPos,
		int i
	) {
		BlockState blockState = sculkPatchConfiguration.groundState.getState(random, mutableBlockPos);

		for (int j = 0; j < i; j++) {
			BlockState blockState2 = worldGenLevel.getBlockState(mutableBlockPos);
			if (blockState.is(blockState2.getBlock()) || !predicate.test(blockState2)) {
				return false;
			}

			BlockState blockState3 = worldGenLevel.getBlockState(mutableBlockPos.above());
			if (blockState3.is(Blocks.SCULK_VEIN)) {
				worldGenLevel.setBlock(mutableBlockPos.above(), Blocks.AIR.defaultBlockState(), 2);
			}

			worldGenLevel.setBlock(mutableBlockPos, blockState, 2);
			mutableBlockPos.move(sculkPatchConfiguration.surface.getDirection());
		}

		return true;
	}

	protected boolean allowsPlacementOfSculk(LevelAccessor levelAccessor, BlockState blockState, BlockPos blockPos) {
		return !Block.isFaceFull(blockState.getCollisionShape(levelAccessor, blockPos, CollisionContext.empty()), Direction.DOWN);
	}

	private static Predicate<BlockState> getReplaceableTag(SculkPatchConfiguration sculkPatchConfiguration) {
		Tag<Block> tag = BlockTags.getAllTags().getTag(sculkPatchConfiguration.replaceable);
		return tag == null ? blockState -> true : blockState -> blockState.is(tag);
	}

	static class PatchCellData {
		int surfaceHeight = Integer.MAX_VALUE;
		int minNeightbourHeight = Integer.MAX_VALUE;
		SculkPatchFeature.PatchCellVariant variant = SculkPatchFeature.PatchCellVariant.Empty;
	}

	static enum PatchCellVariant {
		Empty,
		Sculk,
		SculkVein;
	}
}
