/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.levelgen.feature;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.mojang.serialization.Codec;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.OptionalInt;
import java.util.Random;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelSimulatedReader;
import net.minecraft.world.level.LevelWriter;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.levelgen.Heightmap;
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
    public TreeFeature(Codec<TreeConfiguration> codec) {
        super(codec);
    }

    public static boolean isFree(LevelSimulatedReader levelSimulatedReader, BlockPos blockPos) {
        return TreeFeature.validTreePos(levelSimulatedReader, blockPos) || levelSimulatedReader.isStateAtPosition(blockPos, blockState -> blockState.is(BlockTags.LOGS));
    }

    private static boolean isVine(LevelSimulatedReader levelSimulatedReader, BlockPos blockPos) {
        return levelSimulatedReader.isStateAtPosition(blockPos, blockState -> blockState.is(Blocks.VINE));
    }

    private static boolean isBlockWater(LevelSimulatedReader levelSimulatedReader, BlockPos blockPos) {
        return levelSimulatedReader.isStateAtPosition(blockPos, blockState -> blockState.is(Blocks.WATER));
    }

    public static boolean isAirOrLeaves(LevelSimulatedReader levelSimulatedReader, BlockPos blockPos) {
        return levelSimulatedReader.isStateAtPosition(blockPos, blockState -> blockState.isAir() || blockState.is(BlockTags.LEAVES));
    }

    private static boolean isGrassOrDirtOrFarmland(LevelSimulatedReader levelSimulatedReader, BlockPos blockPos) {
        return levelSimulatedReader.isStateAtPosition(blockPos, blockState -> TreeFeature.isDirt(blockState) || blockState.is(Blocks.FARMLAND));
    }

    private static boolean isReplaceablePlant(LevelSimulatedReader levelSimulatedReader, BlockPos blockPos) {
        return levelSimulatedReader.isStateAtPosition(blockPos, blockState -> {
            Material material = blockState.getMaterial();
            return material == Material.REPLACEABLE_PLANT;
        });
    }

    public static void setBlockKnownShape(LevelWriter levelWriter, BlockPos blockPos, BlockState blockState) {
        levelWriter.setBlock(blockPos, blockState, 19);
    }

    public static boolean validTreePos(LevelSimulatedReader levelSimulatedReader, BlockPos blockPos) {
        return TreeFeature.isAirOrLeaves(levelSimulatedReader, blockPos) || TreeFeature.isReplaceablePlant(levelSimulatedReader, blockPos) || TreeFeature.isBlockWater(levelSimulatedReader, blockPos);
    }

    private boolean doPlace(WorldGenLevel worldGenLevel, Random random, BlockPos blockPos, Set<BlockPos> set, Set<BlockPos> set2, BoundingBox boundingBox, TreeConfiguration treeConfiguration) {
        BlockPos blockPos2;
        int n;
        int i = treeConfiguration.trunkPlacer.getTreeHeight(random);
        int j = treeConfiguration.foliagePlacer.foliageHeight(random, i, treeConfiguration);
        int k = i - j;
        int l = treeConfiguration.foliagePlacer.foliageRadius(random, k);
        if (!treeConfiguration.fromSapling) {
            int m = worldGenLevel.getHeightmapPos(Heightmap.Types.OCEAN_FLOOR, blockPos).getY();
            n = worldGenLevel.getHeightmapPos(Heightmap.Types.WORLD_SURFACE, blockPos).getY();
            if (n - m > treeConfiguration.maxWaterDepth) {
                return false;
            }
            int o = treeConfiguration.heightmap == Heightmap.Types.OCEAN_FLOOR ? m : (treeConfiguration.heightmap == Heightmap.Types.WORLD_SURFACE ? n : worldGenLevel.getHeightmapPos(treeConfiguration.heightmap, blockPos).getY());
            blockPos2 = new BlockPos(blockPos.getX(), o, blockPos.getZ());
        } else {
            blockPos2 = blockPos;
        }
        if (blockPos2.getY() < worldGenLevel.getMinBuildHeight() + 1 || blockPos2.getY() + i + 1 > worldGenLevel.getMaxBuildHeight()) {
            return false;
        }
        if (!TreeFeature.isGrassOrDirtOrFarmland(worldGenLevel, blockPos2.below())) {
            return false;
        }
        OptionalInt optionalInt = treeConfiguration.minimumSize.minClippedHeight();
        n = this.getMaxFreeTreeHeight(worldGenLevel, i, blockPos2, treeConfiguration);
        if (!(n >= i || optionalInt.isPresent() && n >= optionalInt.getAsInt())) {
            return false;
        }
        List<FoliagePlacer.FoliageAttachment> list = treeConfiguration.trunkPlacer.placeTrunk(worldGenLevel, random, n, blockPos2, set, boundingBox, treeConfiguration);
        list.forEach(foliageAttachment -> treeConfiguration.foliagePlacer.createFoliage(worldGenLevel, random, treeConfiguration, n, (FoliagePlacer.FoliageAttachment)foliageAttachment, j, l, set2, boundingBox));
        return true;
    }

    private int getMaxFreeTreeHeight(LevelSimulatedReader levelSimulatedReader, int i, BlockPos blockPos, TreeConfiguration treeConfiguration) {
        BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();
        for (int j = 0; j <= i + 1; ++j) {
            int k = treeConfiguration.minimumSize.getSizeAtHeight(i, j);
            for (int l = -k; l <= k; ++l) {
                for (int m = -k; m <= k; ++m) {
                    mutableBlockPos.setWithOffset(blockPos, l, j, m);
                    if (TreeFeature.isFree(levelSimulatedReader, mutableBlockPos) && (treeConfiguration.ignoreVines || !TreeFeature.isVine(levelSimulatedReader, mutableBlockPos))) continue;
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
        Random random = featurePlaceContext.random();
        BlockPos blockPos = featurePlaceContext.origin();
        TreeConfiguration treeConfiguration = featurePlaceContext.config();
        HashSet<BlockPos> set = Sets.newHashSet();
        HashSet<BlockPos> set2 = Sets.newHashSet();
        HashSet<BlockPos> set3 = Sets.newHashSet();
        BoundingBox boundingBox = BoundingBox.getUnknownBox();
        boolean bl = this.doPlace(worldGenLevel, random, blockPos, set, set2, boundingBox, treeConfiguration);
        if (boundingBox.x0 > boundingBox.x1 || !bl || set.isEmpty()) {
            return false;
        }
        if (!treeConfiguration.decorators.isEmpty()) {
            ArrayList<BlockPos> list = Lists.newArrayList(set);
            ArrayList<BlockPos> list2 = Lists.newArrayList(set2);
            list.sort(Comparator.comparingInt(Vec3i::getY));
            list2.sort(Comparator.comparingInt(Vec3i::getY));
            treeConfiguration.decorators.forEach(treeDecorator -> treeDecorator.place(worldGenLevel, random, list, list2, set3, boundingBox));
        }
        DiscreteVoxelShape discreteVoxelShape = this.updateLeaves(worldGenLevel, boundingBox, set, set3);
        StructureTemplate.updateShapeAtEdge(worldGenLevel, 3, discreteVoxelShape, boundingBox.x0, boundingBox.y0, boundingBox.z0);
        return true;
    }

    private DiscreteVoxelShape updateLeaves(LevelAccessor levelAccessor, BoundingBox boundingBox, Set<BlockPos> set, Set<BlockPos> set2) {
        ArrayList list = Lists.newArrayList();
        BitSetDiscreteVoxelShape discreteVoxelShape = new BitSetDiscreteVoxelShape(boundingBox.getXSpan(), boundingBox.getYSpan(), boundingBox.getZSpan());
        int i = 6;
        for (int j = 0; j < 6; ++j) {
            list.add(Sets.newHashSet());
        }
        BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();
        for (BlockPos blockPos : Lists.newArrayList(set2)) {
            if (!boundingBox.isInside(blockPos)) continue;
            ((DiscreteVoxelShape)discreteVoxelShape).fill(blockPos.getX() - boundingBox.x0, blockPos.getY() - boundingBox.y0, blockPos.getZ() - boundingBox.z0);
        }
        for (BlockPos blockPos : Lists.newArrayList(set)) {
            if (boundingBox.isInside(blockPos)) {
                ((DiscreteVoxelShape)discreteVoxelShape).fill(blockPos.getX() - boundingBox.x0, blockPos.getY() - boundingBox.y0, blockPos.getZ() - boundingBox.z0);
            }
            for (Direction direction : Direction.values()) {
                BlockState blockState;
                mutableBlockPos.setWithOffset(blockPos, direction);
                if (set.contains(mutableBlockPos) || !(blockState = levelAccessor.getBlockState(mutableBlockPos)).hasProperty(BlockStateProperties.DISTANCE)) continue;
                ((Set)list.get(0)).add(mutableBlockPos.immutable());
                TreeFeature.setBlockKnownShape(levelAccessor, mutableBlockPos, (BlockState)blockState.setValue(BlockStateProperties.DISTANCE, 1));
                if (!boundingBox.isInside(mutableBlockPos)) continue;
                ((DiscreteVoxelShape)discreteVoxelShape).fill(mutableBlockPos.getX() - boundingBox.x0, mutableBlockPos.getY() - boundingBox.y0, mutableBlockPos.getZ() - boundingBox.z0);
            }
        }
        for (int k = 1; k < 6; ++k) {
            Set set3 = (Set)list.get(k - 1);
            Set set4 = (Set)list.get(k);
            for (BlockPos blockPos2 : set3) {
                if (boundingBox.isInside(blockPos2)) {
                    ((DiscreteVoxelShape)discreteVoxelShape).fill(blockPos2.getX() - boundingBox.x0, blockPos2.getY() - boundingBox.y0, blockPos2.getZ() - boundingBox.z0);
                }
                for (Direction direction2 : Direction.values()) {
                    int l;
                    BlockState blockState2;
                    mutableBlockPos.setWithOffset(blockPos2, direction2);
                    if (set3.contains(mutableBlockPos) || set4.contains(mutableBlockPos) || !(blockState2 = levelAccessor.getBlockState(mutableBlockPos)).hasProperty(BlockStateProperties.DISTANCE) || (l = blockState2.getValue(BlockStateProperties.DISTANCE).intValue()) <= k + 1) continue;
                    BlockState blockState3 = (BlockState)blockState2.setValue(BlockStateProperties.DISTANCE, k + 1);
                    TreeFeature.setBlockKnownShape(levelAccessor, mutableBlockPos, blockState3);
                    if (boundingBox.isInside(mutableBlockPos)) {
                        ((DiscreteVoxelShape)discreteVoxelShape).fill(mutableBlockPos.getX() - boundingBox.x0, mutableBlockPos.getY() - boundingBox.y0, mutableBlockPos.getZ() - boundingBox.z0);
                    }
                    set4.add(mutableBlockPos.immutable());
                }
            }
        }
        return discreteVoxelShape;
    }
}

