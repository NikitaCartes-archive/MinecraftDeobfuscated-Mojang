package net.minecraft.world.level.levelgen.feature;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.mojang.serialization.Codec;
import java.util.List;
import java.util.OptionalInt;
import java.util.Set;
import java.util.function.BiConsumer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelSimulatedReader;
import net.minecraft.world.level.LevelWriter;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.levelgen.feature.configurations.TreeConfiguration;
import net.minecraft.world.level.levelgen.feature.foliageplacers.FoliagePlacer;
import net.minecraft.world.level.levelgen.feature.rootplacers.RootPlacer;
import net.minecraft.world.level.levelgen.feature.treedecorators.TreeDecorator;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.phys.shapes.BitSetDiscreteVoxelShape;
import net.minecraft.world.phys.shapes.DiscreteVoxelShape;

public class TreeFeature extends Feature<TreeConfiguration> {
	private static final int BLOCK_UPDATE_FLAGS = 19;

	public TreeFeature(Codec<TreeConfiguration> codec) {
		super(codec);
	}

	private static boolean isVine(LevelSimulatedReader levelSimulatedReader, BlockPos blockPos) {
		return levelSimulatedReader.isStateAtPosition(blockPos, blockState -> blockState.is(Blocks.VINE));
	}

	public static boolean isBlockWater(LevelSimulatedReader levelSimulatedReader, BlockPos blockPos) {
		return levelSimulatedReader.isStateAtPosition(blockPos, blockState -> blockState.is(Blocks.WATER));
	}

	public static boolean isAirOrLeaves(LevelSimulatedReader levelSimulatedReader, BlockPos blockPos) {
		return levelSimulatedReader.isStateAtPosition(blockPos, blockState -> blockState.isAir() || blockState.is(BlockTags.LEAVES));
	}

	private static boolean isReplaceablePlant(LevelSimulatedReader levelSimulatedReader, BlockPos blockPos) {
		return levelSimulatedReader.isStateAtPosition(blockPos, blockState -> {
			Material material = blockState.getMaterial();
			return material == Material.REPLACEABLE_PLANT || material == Material.REPLACEABLE_WATER_PLANT || material == Material.REPLACEABLE_FIREPROOF_PLANT;
		});
	}

	private static void setBlockKnownShape(LevelWriter levelWriter, BlockPos blockPos, BlockState blockState) {
		levelWriter.setBlock(blockPos, blockState, 19);
	}

	public static boolean validTreePos(LevelSimulatedReader levelSimulatedReader, BlockPos blockPos) {
		return isAirOrLeaves(levelSimulatedReader, blockPos) || isReplaceablePlant(levelSimulatedReader, blockPos) || isBlockWater(levelSimulatedReader, blockPos);
	}

	private boolean doPlace(
		WorldGenLevel worldGenLevel,
		RandomSource randomSource,
		BlockPos blockPos,
		BiConsumer<BlockPos, BlockState> biConsumer,
		BiConsumer<BlockPos, BlockState> biConsumer2,
		BiConsumer<BlockPos, BlockState> biConsumer3,
		TreeConfiguration treeConfiguration
	) {
		int i = treeConfiguration.trunkPlacer.getTreeHeight(randomSource);
		int j = treeConfiguration.foliagePlacer.foliageHeight(randomSource, i, treeConfiguration);
		int k = i - j;
		int l = treeConfiguration.foliagePlacer.foliageRadius(randomSource, k);
		BlockPos blockPos2 = (BlockPos)treeConfiguration.rootPlacer.map(rootPlacer -> rootPlacer.getTrunkOrigin(blockPos, randomSource)).orElse(blockPos);
		int m = Math.min(blockPos.getY(), blockPos2.getY());
		int n = Math.max(blockPos.getY(), blockPos2.getY()) + i + 1;
		if (m >= worldGenLevel.getMinBuildHeight() + 1 && n <= worldGenLevel.getMaxBuildHeight()) {
			OptionalInt optionalInt = treeConfiguration.minimumSize.minClippedHeight();
			int o = this.getMaxFreeTreeHeight(worldGenLevel, i, blockPos2, treeConfiguration);
			if (o >= i || !optionalInt.isEmpty() && o >= optionalInt.getAsInt()) {
				if (treeConfiguration.rootPlacer.isPresent()
					&& !((RootPlacer)treeConfiguration.rootPlacer.get()).placeRoots(worldGenLevel, biConsumer, randomSource, blockPos, blockPos2, treeConfiguration)) {
					return false;
				} else {
					List<FoliagePlacer.FoliageAttachment> list = treeConfiguration.trunkPlacer
						.placeTrunk(worldGenLevel, biConsumer2, randomSource, o, blockPos2, treeConfiguration);
					list.forEach(
						foliageAttachment -> treeConfiguration.foliagePlacer
								.createFoliage(worldGenLevel, biConsumer3, randomSource, treeConfiguration, o, foliageAttachment, j, l)
					);
					return true;
				}
			} else {
				return false;
			}
		} else {
			return false;
		}
	}

	private int getMaxFreeTreeHeight(LevelSimulatedReader levelSimulatedReader, int i, BlockPos blockPos, TreeConfiguration treeConfiguration) {
		BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();

		for (int j = 0; j <= i + 1; j++) {
			int k = treeConfiguration.minimumSize.getSizeAtHeight(i, j);

			for (int l = -k; l <= k; l++) {
				for (int m = -k; m <= k; m++) {
					mutableBlockPos.setWithOffset(blockPos, l, j, m);
					if (!treeConfiguration.trunkPlacer.isFree(levelSimulatedReader, mutableBlockPos)
						|| !treeConfiguration.ignoreVines && isVine(levelSimulatedReader, mutableBlockPos)) {
						return j - 2;
					}
				}
			}
		}

		return i;
	}

	@Override
	protected void setBlock(LevelWriter levelWriter, BlockPos blockPos, BlockState blockState) {
		setBlockKnownShape(levelWriter, blockPos, blockState);
	}

	@Override
	public final boolean place(FeaturePlaceContext<TreeConfiguration> featurePlaceContext) {
		WorldGenLevel worldGenLevel = featurePlaceContext.level();
		RandomSource randomSource = featurePlaceContext.random();
		BlockPos blockPos = featurePlaceContext.origin();
		TreeConfiguration treeConfiguration = featurePlaceContext.config();
		Set<BlockPos> set = Sets.<BlockPos>newHashSet();
		Set<BlockPos> set2 = Sets.<BlockPos>newHashSet();
		Set<BlockPos> set3 = Sets.<BlockPos>newHashSet();
		Set<BlockPos> set4 = Sets.<BlockPos>newHashSet();
		BiConsumer<BlockPos, BlockState> biConsumer = (blockPosx, blockState) -> {
			set.add(blockPosx.immutable());
			worldGenLevel.setBlock(blockPosx, blockState, 19);
		};
		BiConsumer<BlockPos, BlockState> biConsumer2 = (blockPosx, blockState) -> {
			set2.add(blockPosx.immutable());
			worldGenLevel.setBlock(blockPosx, blockState, 19);
		};
		BiConsumer<BlockPos, BlockState> biConsumer3 = (blockPosx, blockState) -> {
			set3.add(blockPosx.immutable());
			worldGenLevel.setBlock(blockPosx, blockState, 19);
		};
		BiConsumer<BlockPos, BlockState> biConsumer4 = (blockPosx, blockState) -> {
			set4.add(blockPosx.immutable());
			worldGenLevel.setBlock(blockPosx, blockState, 19);
		};
		boolean bl = this.doPlace(worldGenLevel, randomSource, blockPos, biConsumer, biConsumer2, biConsumer3, treeConfiguration);
		if (bl && (!set2.isEmpty() || !set3.isEmpty())) {
			if (!treeConfiguration.decorators.isEmpty()) {
				TreeDecorator.Context context = new TreeDecorator.Context(worldGenLevel, biConsumer4, randomSource, set2, set3, set);
				treeConfiguration.decorators.forEach(treeDecorator -> treeDecorator.place(context));
			}

			return (Boolean)BoundingBox.encapsulatingPositions(Iterables.concat(set, set2, set3, set4)).map(boundingBox -> {
				DiscreteVoxelShape discreteVoxelShape = updateLeaves(worldGenLevel, boundingBox, set2, set4, set);
				StructureTemplate.updateShapeAtEdge(worldGenLevel, 3, discreteVoxelShape, boundingBox.minX(), boundingBox.minY(), boundingBox.minZ());
				return true;
			}).orElse(false);
		} else {
			return false;
		}
	}

	private static DiscreteVoxelShape updateLeaves(LevelAccessor levelAccessor, BoundingBox boundingBox, Set<BlockPos> set, Set<BlockPos> set2, Set<BlockPos> set3) {
		List<Set<BlockPos>> list = Lists.<Set<BlockPos>>newArrayList();
		DiscreteVoxelShape discreteVoxelShape = new BitSetDiscreteVoxelShape(boundingBox.getXSpan(), boundingBox.getYSpan(), boundingBox.getZSpan());
		int i = 6;

		for (int j = 0; j < 6; j++) {
			list.add(Sets.newHashSet());
		}

		BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();

		for (BlockPos blockPos : Lists.newArrayList(Sets.union(set2, set3))) {
			if (boundingBox.isInside(blockPos)) {
				discreteVoxelShape.fill(blockPos.getX() - boundingBox.minX(), blockPos.getY() - boundingBox.minY(), blockPos.getZ() - boundingBox.minZ());
			}
		}

		for (BlockPos blockPosx : Lists.newArrayList(set)) {
			if (boundingBox.isInside(blockPosx)) {
				discreteVoxelShape.fill(blockPosx.getX() - boundingBox.minX(), blockPosx.getY() - boundingBox.minY(), blockPosx.getZ() - boundingBox.minZ());
			}

			for (Direction direction : Direction.values()) {
				mutableBlockPos.setWithOffset(blockPosx, direction);
				if (!set.contains(mutableBlockPos)) {
					BlockState blockState = levelAccessor.getBlockState(mutableBlockPos);
					if (blockState.hasProperty(BlockStateProperties.DISTANCE)) {
						((Set)list.get(0)).add(mutableBlockPos.immutable());
						setBlockKnownShape(levelAccessor, mutableBlockPos, blockState.setValue(BlockStateProperties.DISTANCE, Integer.valueOf(1)));
						if (boundingBox.isInside(mutableBlockPos)) {
							discreteVoxelShape.fill(
								mutableBlockPos.getX() - boundingBox.minX(), mutableBlockPos.getY() - boundingBox.minY(), mutableBlockPos.getZ() - boundingBox.minZ()
							);
						}
					}
				}
			}
		}

		for (int k = 1; k < 6; k++) {
			Set<BlockPos> set4 = (Set<BlockPos>)list.get(k - 1);
			Set<BlockPos> set5 = (Set<BlockPos>)list.get(k);

			for (BlockPos blockPos2 : set4) {
				if (boundingBox.isInside(blockPos2)) {
					discreteVoxelShape.fill(blockPos2.getX() - boundingBox.minX(), blockPos2.getY() - boundingBox.minY(), blockPos2.getZ() - boundingBox.minZ());
				}

				for (Direction direction2 : Direction.values()) {
					mutableBlockPos.setWithOffset(blockPos2, direction2);
					if (!set4.contains(mutableBlockPos) && !set5.contains(mutableBlockPos)) {
						BlockState blockState2 = levelAccessor.getBlockState(mutableBlockPos);
						if (blockState2.hasProperty(BlockStateProperties.DISTANCE)) {
							int l = (Integer)blockState2.getValue(BlockStateProperties.DISTANCE);
							if (l > k + 1) {
								BlockState blockState3 = blockState2.setValue(BlockStateProperties.DISTANCE, Integer.valueOf(k + 1));
								setBlockKnownShape(levelAccessor, mutableBlockPos, blockState3);
								if (boundingBox.isInside(mutableBlockPos)) {
									discreteVoxelShape.fill(
										mutableBlockPos.getX() - boundingBox.minX(), mutableBlockPos.getY() - boundingBox.minY(), mutableBlockPos.getZ() - boundingBox.minZ()
									);
								}

								set5.add(mutableBlockPos.immutable());
							}
						}
					}
				}
			}
		}

		return discreteVoxelShape;
	}
}
