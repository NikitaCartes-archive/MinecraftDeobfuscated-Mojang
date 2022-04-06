/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.levelgen.feature;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.mojang.serialization.Codec;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.Set;
import java.util.function.BiConsumer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelSimulatedReader;
import net.minecraft.world.level.LevelWriter;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.TreeConfiguration;
import net.minecraft.world.level.levelgen.feature.foliageplacers.FoliagePlacer;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.phys.shapes.BitSetDiscreteVoxelShape;
import net.minecraft.world.phys.shapes.DiscreteVoxelShape;

public class TreeFeature
extends Feature<TreeConfiguration> {
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
            return material == Material.REPLACEABLE_PLANT || material == Material.REPLACEABLE_WATER_PLANT;
        });
    }

    private static void setBlockKnownShape(LevelWriter levelWriter, BlockPos blockPos, BlockState blockState) {
        levelWriter.setBlock(blockPos, blockState, 19);
    }

    public static boolean validTreePos(LevelSimulatedReader levelSimulatedReader, BlockPos blockPos) {
        return TreeFeature.isAirOrLeaves(levelSimulatedReader, blockPos) || TreeFeature.isReplaceablePlant(levelSimulatedReader, blockPos) || TreeFeature.isBlockWater(levelSimulatedReader, blockPos);
    }

    private boolean doPlace(WorldGenLevel worldGenLevel, RandomSource randomSource, BlockPos blockPos, BiConsumer<BlockPos, BlockState> biConsumer, BiConsumer<BlockPos, BlockState> biConsumer2, BiConsumer<BlockPos, BlockState> biConsumer3, TreeConfiguration treeConfiguration) {
        int i = treeConfiguration.trunkPlacer.getTreeHeight(randomSource);
        int j = treeConfiguration.foliagePlacer.foliageHeight(randomSource, i, treeConfiguration);
        int k = i - j;
        int l = treeConfiguration.foliagePlacer.foliageRadius(randomSource, k);
        if (blockPos.getY() < worldGenLevel.getMinBuildHeight() + 1 || blockPos.getY() + i + 1 > worldGenLevel.getMaxBuildHeight()) {
            return false;
        }
        OptionalInt optionalInt = treeConfiguration.minimumSize.minClippedHeight();
        int m = this.getMaxFreeTreeHeight(worldGenLevel, i, blockPos, treeConfiguration);
        if (!(m >= i || optionalInt.isPresent() && m >= optionalInt.getAsInt())) {
            return false;
        }
        BlockPos blockPos2 = blockPos;
        if (treeConfiguration.rootPlacer.isPresent()) {
            Optional<BlockPos> optional = treeConfiguration.rootPlacer.get().placeRoots(worldGenLevel, biConsumer, randomSource, blockPos, treeConfiguration);
            if (optional.isEmpty()) {
                return false;
            }
            blockPos2 = optional.get();
        }
        List<FoliagePlacer.FoliageAttachment> list = treeConfiguration.trunkPlacer.placeTrunk(worldGenLevel, biConsumer2, randomSource, m, blockPos2, treeConfiguration);
        list.forEach(foliageAttachment -> treeConfiguration.foliagePlacer.createFoliage(worldGenLevel, biConsumer3, randomSource, treeConfiguration, m, (FoliagePlacer.FoliageAttachment)foliageAttachment, j, l));
        return true;
    }

    private int getMaxFreeTreeHeight(LevelSimulatedReader levelSimulatedReader, int i, BlockPos blockPos, TreeConfiguration treeConfiguration) {
        BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();
        for (int j = 0; j <= i + 1; ++j) {
            int k = treeConfiguration.minimumSize.getSizeAtHeight(i, j);
            for (int l = -k; l <= k; ++l) {
                for (int m = -k; m <= k; ++m) {
                    mutableBlockPos.setWithOffset(blockPos, l, j, m);
                    if (treeConfiguration.trunkPlacer.isFree(levelSimulatedReader, mutableBlockPos) && (treeConfiguration.ignoreVines || !TreeFeature.isVine(levelSimulatedReader, mutableBlockPos))) continue;
                    return j - 2;
                }
            }
        }
        return i;
    }

    @Override
    protected void setBlock(LevelWriter levelWriter, BlockPos blockPos, BlockState blockState) {
        TreeFeature.setBlockKnownShape(levelWriter, blockPos, blockState);
    }

    @Override
    public final boolean place(FeaturePlaceContext<TreeConfiguration> featurePlaceContext) {
        WorldGenLevel worldGenLevel = featurePlaceContext.level();
        RandomSource randomSource = featurePlaceContext.random();
        BlockPos blockPos2 = featurePlaceContext.origin();
        TreeConfiguration treeConfiguration = featurePlaceContext.config();
        HashSet set = Sets.newHashSet();
        HashSet set2 = Sets.newHashSet();
        HashSet set3 = Sets.newHashSet();
        HashSet set4 = Sets.newHashSet();
        BiConsumer<BlockPos, BlockState> biConsumer = (blockPos, blockState) -> {
            set.add(blockPos.immutable());
            worldGenLevel.setBlock((BlockPos)blockPos, (BlockState)blockState, 19);
        };
        BiConsumer<BlockPos, BlockState> biConsumer2 = (blockPos, blockState) -> {
            set2.add(blockPos.immutable());
            worldGenLevel.setBlock((BlockPos)blockPos, (BlockState)blockState, 19);
        };
        BiConsumer<BlockPos, BlockState> biConsumer3 = (blockPos, blockState) -> {
            set3.add(blockPos.immutable());
            worldGenLevel.setBlock((BlockPos)blockPos, (BlockState)blockState, 19);
        };
        BiConsumer<BlockPos, BlockState> biConsumer4 = (blockPos, blockState) -> {
            set4.add(blockPos.immutable());
            worldGenLevel.setBlock((BlockPos)blockPos, (BlockState)blockState, 19);
        };
        boolean bl = this.doPlace(worldGenLevel, randomSource, blockPos2, biConsumer, biConsumer2, biConsumer3, treeConfiguration);
        if (!bl || set2.isEmpty() && set3.isEmpty()) {
            return false;
        }
        if (!treeConfiguration.decorators.isEmpty()) {
            ArrayList<BlockPos> list = Lists.newArrayList(set);
            ArrayList<BlockPos> list2 = Lists.newArrayList(set2);
            ArrayList<BlockPos> list3 = Lists.newArrayList(set3);
            list2.sort(Comparator.comparingInt(Vec3i::getY));
            list3.sort(Comparator.comparingInt(Vec3i::getY));
            list.sort(Comparator.comparingInt(Vec3i::getY));
            treeConfiguration.decorators.forEach(treeDecorator -> treeDecorator.place(worldGenLevel, biConsumer4, randomSource, list2, list3, list));
        }
        return BoundingBox.encapsulatingPositions(Iterables.concat(set2, set3, set4)).map(boundingBox -> {
            DiscreteVoxelShape discreteVoxelShape = TreeFeature.updateLeaves(worldGenLevel, boundingBox, set2, set4);
            StructureTemplate.updateShapeAtEdge(worldGenLevel, 3, discreteVoxelShape, boundingBox.minX(), boundingBox.minY(), boundingBox.minZ());
            return true;
        }).orElse(false);
    }

    private static DiscreteVoxelShape updateLeaves(LevelAccessor levelAccessor, BoundingBox boundingBox, Set<BlockPos> set, Set<BlockPos> set2) {
        ArrayList list = Lists.newArrayList();
        BitSetDiscreteVoxelShape discreteVoxelShape = new BitSetDiscreteVoxelShape(boundingBox.getXSpan(), boundingBox.getYSpan(), boundingBox.getZSpan());
        int i = 6;
        for (int j = 0; j < 6; ++j) {
            list.add(Sets.newHashSet());
        }
        BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();
        for (BlockPos blockPos : Lists.newArrayList(set2)) {
            if (!boundingBox.isInside(blockPos)) continue;
            ((DiscreteVoxelShape)discreteVoxelShape).fill(blockPos.getX() - boundingBox.minX(), blockPos.getY() - boundingBox.minY(), blockPos.getZ() - boundingBox.minZ());
        }
        for (BlockPos blockPos : Lists.newArrayList(set)) {
            if (boundingBox.isInside(blockPos)) {
                ((DiscreteVoxelShape)discreteVoxelShape).fill(blockPos.getX() - boundingBox.minX(), blockPos.getY() - boundingBox.minY(), blockPos.getZ() - boundingBox.minZ());
            }
            for (Direction direction : Direction.values()) {
                BlockState blockState;
                mutableBlockPos.setWithOffset((Vec3i)blockPos, direction);
                if (set.contains(mutableBlockPos) || !(blockState = levelAccessor.getBlockState(mutableBlockPos)).hasProperty(BlockStateProperties.DISTANCE)) continue;
                ((Set)list.get(0)).add(mutableBlockPos.immutable());
                TreeFeature.setBlockKnownShape(levelAccessor, mutableBlockPos, (BlockState)blockState.setValue(BlockStateProperties.DISTANCE, 1));
                if (!boundingBox.isInside(mutableBlockPos)) continue;
                ((DiscreteVoxelShape)discreteVoxelShape).fill(mutableBlockPos.getX() - boundingBox.minX(), mutableBlockPos.getY() - boundingBox.minY(), mutableBlockPos.getZ() - boundingBox.minZ());
            }
        }
        for (int k = 1; k < 6; ++k) {
            Set set3 = (Set)list.get(k - 1);
            Set set4 = (Set)list.get(k);
            for (BlockPos blockPos2 : set3) {
                if (boundingBox.isInside(blockPos2)) {
                    ((DiscreteVoxelShape)discreteVoxelShape).fill(blockPos2.getX() - boundingBox.minX(), blockPos2.getY() - boundingBox.minY(), blockPos2.getZ() - boundingBox.minZ());
                }
                for (Direction direction2 : Direction.values()) {
                    int l;
                    BlockState blockState2;
                    mutableBlockPos.setWithOffset((Vec3i)blockPos2, direction2);
                    if (set3.contains(mutableBlockPos) || set4.contains(mutableBlockPos) || !(blockState2 = levelAccessor.getBlockState(mutableBlockPos)).hasProperty(BlockStateProperties.DISTANCE) || (l = blockState2.getValue(BlockStateProperties.DISTANCE).intValue()) <= k + 1) continue;
                    BlockState blockState3 = (BlockState)blockState2.setValue(BlockStateProperties.DISTANCE, k + 1);
                    TreeFeature.setBlockKnownShape(levelAccessor, mutableBlockPos, blockState3);
                    if (boundingBox.isInside(mutableBlockPos)) {
                        ((DiscreteVoxelShape)discreteVoxelShape).fill(mutableBlockPos.getX() - boundingBox.minX(), mutableBlockPos.getY() - boundingBox.minY(), mutableBlockPos.getZ() - boundingBox.minZ());
                    }
                    set4.add(mutableBlockPos.immutable());
                }
            }
        }
        return discreteVoxelShape;
    }
}

