package net.minecraft.world.level.levelgen.feature;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.mojang.serialization.Codec;
import java.util.Iterator;
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
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.levelgen.feature.configurations.TreeConfiguration;
import net.minecraft.world.level.levelgen.feature.foliageplacers.FoliagePlacer;
import net.minecraft.world.level.levelgen.feature.rootplacers.RootPlacer;
import net.minecraft.world.level.levelgen.feature.treedecorators.TreeDecorator;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
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

	public static boolean isAirOrLeaves(LevelSimulatedReader levelSimulatedReader, BlockPos blockPos) {
		return levelSimulatedReader.isStateAtPosition(blockPos, blockState -> blockState.isAir() || blockState.is(BlockTags.LEAVES));
	}

	private static void setBlockKnownShape(LevelWriter levelWriter, BlockPos blockPos, BlockState blockState) {
		levelWriter.setBlock(blockPos, blockState, 19);
	}

	public static boolean validTreePos(LevelSimulatedReader levelSimulatedReader, BlockPos blockPos) {
		return levelSimulatedReader.isStateAtPosition(blockPos, blockState -> blockState.isAir() || blockState.is(BlockTags.REPLACEABLE_BY_TREES));
	}

	private boolean doPlace(
		WorldGenLevel worldGenLevel,
		RandomSource randomSource,
		BlockPos blockPos,
		BiConsumer<BlockPos, BlockState> biConsumer,
		BiConsumer<BlockPos, BlockState> biConsumer2,
		FoliagePlacer.FoliageSetter foliageSetter,
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
								.createFoliage(worldGenLevel, foliageSetter, randomSource, treeConfiguration, o, foliageAttachment, j, l)
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
		final WorldGenLevel worldGenLevel = featurePlaceContext.level();
		RandomSource randomSource = featurePlaceContext.random();
		BlockPos blockPos = featurePlaceContext.origin();
		TreeConfiguration treeConfiguration = featurePlaceContext.config();
		Set<BlockPos> set = Sets.<BlockPos>newHashSet();
		Set<BlockPos> set2 = Sets.<BlockPos>newHashSet();
		final Set<BlockPos> set3 = Sets.<BlockPos>newHashSet();
		Set<BlockPos> set4 = Sets.<BlockPos>newHashSet();
		BiConsumer<BlockPos, BlockState> biConsumer = (blockPosx, blockState) -> {
			set.add(blockPosx.immutable());
			worldGenLevel.setBlock(blockPosx, blockState, 19);
		};
		BiConsumer<BlockPos, BlockState> biConsumer2 = (blockPosx, blockState) -> {
			set2.add(blockPosx.immutable());
			worldGenLevel.setBlock(blockPosx, blockState, 19);
		};
		FoliagePlacer.FoliageSetter foliageSetter = new FoliagePlacer.FoliageSetter() {
			@Override
			public void set(BlockPos blockPos, BlockState blockState) {
				set3.add(blockPos.immutable());
				worldGenLevel.setBlock(blockPos, blockState, 19);
			}

			@Override
			public boolean isSet(BlockPos blockPos) {
				return set3.contains(blockPos);
			}
		};
		BiConsumer<BlockPos, BlockState> biConsumer3 = (blockPosx, blockState) -> {
			set4.add(blockPosx.immutable());
			worldGenLevel.setBlock(blockPosx, blockState, 19);
		};
		boolean bl = this.doPlace(worldGenLevel, randomSource, blockPos, biConsumer, biConsumer2, foliageSetter, treeConfiguration);
		if (bl && (!set2.isEmpty() || !set3.isEmpty())) {
			if (!treeConfiguration.decorators.isEmpty()) {
				TreeDecorator.Context context = new TreeDecorator.Context(worldGenLevel, biConsumer3, randomSource, set2, set3, set);
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
		DiscreteVoxelShape discreteVoxelShape = new BitSetDiscreteVoxelShape(boundingBox.getXSpan(), boundingBox.getYSpan(), boundingBox.getZSpan());
		int i = 7;
		List<Set<BlockPos>> list = Lists.<Set<BlockPos>>newArrayList();

		for (int j = 0; j < 7; j++) {
			list.add(Sets.newHashSet());
		}

		for (BlockPos blockPos : Lists.newArrayList(Sets.union(set2, set3))) {
			if (boundingBox.isInside(blockPos)) {
				discreteVoxelShape.fill(blockPos.getX() - boundingBox.minX(), blockPos.getY() - boundingBox.minY(), blockPos.getZ() - boundingBox.minZ());
			}
		}

		BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();
		int k = 0;
		((Set)list.get(0)).addAll(set);

		while (true) {
			while (k >= 7 || !((Set)list.get(k)).isEmpty()) {
				if (k >= 7) {
					return discreteVoxelShape;
				}

				Iterator<BlockPos> iterator = ((Set)list.get(k)).iterator();
				BlockPos blockPos2 = (BlockPos)iterator.next();
				iterator.remove();
				if (boundingBox.isInside(blockPos2)) {
					if (k != 0) {
						BlockState blockState = levelAccessor.getBlockState(blockPos2);
						setBlockKnownShape(levelAccessor, blockPos2, blockState.setValue(BlockStateProperties.DISTANCE, Integer.valueOf(k)));
					}

					discreteVoxelShape.fill(blockPos2.getX() - boundingBox.minX(), blockPos2.getY() - boundingBox.minY(), blockPos2.getZ() - boundingBox.minZ());

					for (Direction direction : Direction.values()) {
						mutableBlockPos.setWithOffset(blockPos2, direction);
						if (boundingBox.isInside(mutableBlockPos)) {
							int l = mutableBlockPos.getX() - boundingBox.minX();
							int m = mutableBlockPos.getY() - boundingBox.minY();
							int n = mutableBlockPos.getZ() - boundingBox.minZ();
							if (!discreteVoxelShape.isFull(l, m, n)) {
								BlockState blockState2 = levelAccessor.getBlockState(mutableBlockPos);
								OptionalInt optionalInt = LeavesBlock.getOptionalDistanceAt(blockState2);
								if (!optionalInt.isEmpty()) {
									int o = Math.min(optionalInt.getAsInt(), k + 1);
									if (o < 7) {
										((Set)list.get(o)).add(mutableBlockPos.immutable());
										k = Math.min(k, o);
									}
								}
							}
						}
					}
				}
			}

			k++;
		}
	}
}
