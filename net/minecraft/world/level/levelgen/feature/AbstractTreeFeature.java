/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.levelgen.feature;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.mojang.datafixers.Dynamic;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import java.util.function.Function;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelSimulatedRW;
import net.minecraft.world.level.LevelSimulatedReader;
import net.minecraft.world.level.LevelWriter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.ChunkGeneratorSettings;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.TreeConfiguration;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.phys.shapes.BitSetDiscreteVoxelShape;
import net.minecraft.world.phys.shapes.DiscreteVoxelShape;

public abstract class AbstractTreeFeature<T extends TreeConfiguration>
extends Feature<T> {
    public AbstractTreeFeature(Function<Dynamic<?>, ? extends T> function) {
        super(function);
    }

    protected static boolean isFree(LevelSimulatedReader levelSimulatedReader, BlockPos blockPos) {
        return levelSimulatedReader.isStateAtPosition(blockPos, blockState -> {
            Block block = blockState.getBlock();
            return blockState.isAir() || blockState.is(BlockTags.LEAVES) || AbstractTreeFeature.isDirt(block) || block.is(BlockTags.LOGS) || block.is(BlockTags.SAPLINGS) || block == Blocks.VINE;
        });
    }

    public static boolean isAir(LevelSimulatedReader levelSimulatedReader, BlockPos blockPos) {
        return levelSimulatedReader.isStateAtPosition(blockPos, BlockState::isAir);
    }

    protected static boolean isDirt(LevelSimulatedReader levelSimulatedReader, BlockPos blockPos) {
        return levelSimulatedReader.isStateAtPosition(blockPos, blockState -> {
            Block block = blockState.getBlock();
            return AbstractTreeFeature.isDirt(block) && block != Blocks.GRASS_BLOCK && block != Blocks.MYCELIUM;
        });
    }

    protected static boolean isVine(LevelSimulatedReader levelSimulatedReader, BlockPos blockPos) {
        return levelSimulatedReader.isStateAtPosition(blockPos, blockState -> blockState.getBlock() == Blocks.VINE);
    }

    public static boolean isBlockWater(LevelSimulatedReader levelSimulatedReader, BlockPos blockPos) {
        return levelSimulatedReader.isStateAtPosition(blockPos, blockState -> blockState.getBlock() == Blocks.WATER);
    }

    public static boolean isAirOrLeaves(LevelSimulatedReader levelSimulatedReader, BlockPos blockPos) {
        return levelSimulatedReader.isStateAtPosition(blockPos, blockState -> blockState.isAir() || blockState.is(BlockTags.LEAVES));
    }

    public static boolean isGrassOrDirt(LevelSimulatedReader levelSimulatedReader, BlockPos blockPos) {
        return levelSimulatedReader.isStateAtPosition(blockPos, blockState -> AbstractTreeFeature.isDirt(blockState.getBlock()));
    }

    protected static boolean isGrassOrDirtOrFarmland(LevelSimulatedReader levelSimulatedReader, BlockPos blockPos) {
        return levelSimulatedReader.isStateAtPosition(blockPos, blockState -> {
            Block block = blockState.getBlock();
            return AbstractTreeFeature.isDirt(block) || block == Blocks.FARMLAND;
        });
    }

    public static boolean isReplaceablePlant(LevelSimulatedReader levelSimulatedReader, BlockPos blockPos) {
        return levelSimulatedReader.isStateAtPosition(blockPos, blockState -> {
            Material material = blockState.getMaterial();
            return material == Material.REPLACEABLE_PLANT;
        });
    }

    protected void setDirtAt(LevelSimulatedRW levelSimulatedRW, BlockPos blockPos) {
        if (!AbstractTreeFeature.isDirt(levelSimulatedRW, blockPos)) {
            this.setBlock(levelSimulatedRW, blockPos, Blocks.DIRT.defaultBlockState());
        }
    }

    protected boolean placeLog(LevelSimulatedRW levelSimulatedRW, Random random, BlockPos blockPos, Set<BlockPos> set, BoundingBox boundingBox, TreeConfiguration treeConfiguration) {
        if (AbstractTreeFeature.isAirOrLeaves(levelSimulatedRW, blockPos) || AbstractTreeFeature.isReplaceablePlant(levelSimulatedRW, blockPos) || AbstractTreeFeature.isBlockWater(levelSimulatedRW, blockPos)) {
            this.setBlock(levelSimulatedRW, blockPos, treeConfiguration.trunkProvider.getState(random, blockPos), boundingBox);
            set.add(blockPos.immutable());
            return true;
        }
        return false;
    }

    protected boolean placeLeaf(LevelSimulatedRW levelSimulatedRW, Random random, BlockPos blockPos, Set<BlockPos> set, BoundingBox boundingBox, TreeConfiguration treeConfiguration) {
        if (AbstractTreeFeature.isAirOrLeaves(levelSimulatedRW, blockPos) || AbstractTreeFeature.isReplaceablePlant(levelSimulatedRW, blockPos) || AbstractTreeFeature.isBlockWater(levelSimulatedRW, blockPos)) {
            this.setBlock(levelSimulatedRW, blockPos, treeConfiguration.leavesProvider.getState(random, blockPos), boundingBox);
            set.add(blockPos.immutable());
            return true;
        }
        return false;
    }

    @Override
    protected void setBlock(LevelWriter levelWriter, BlockPos blockPos, BlockState blockState) {
        this.setBlockKnownShape(levelWriter, blockPos, blockState);
    }

    protected final void setBlock(LevelWriter levelWriter, BlockPos blockPos, BlockState blockState, BoundingBox boundingBox) {
        this.setBlockKnownShape(levelWriter, blockPos, blockState);
        boundingBox.expand(new BoundingBox(blockPos, blockPos));
    }

    private void setBlockKnownShape(LevelWriter levelWriter, BlockPos blockPos, BlockState blockState) {
        levelWriter.setBlock(blockPos, blockState, 19);
    }

    @Override
    public final boolean place(LevelAccessor levelAccessor, ChunkGenerator<? extends ChunkGeneratorSettings> chunkGenerator, Random random, BlockPos blockPos, T treeConfiguration) {
        HashSet<BlockPos> set = Sets.newHashSet();
        HashSet<BlockPos> set2 = Sets.newHashSet();
        HashSet<BlockPos> set3 = Sets.newHashSet();
        BoundingBox boundingBox = BoundingBox.getUnknownBox();
        boolean bl = this.doPlace(levelAccessor, random, blockPos, set, set2, boundingBox, treeConfiguration);
        if (boundingBox.x0 > boundingBox.x1 || !bl || set.isEmpty()) {
            return false;
        }
        if (!((TreeConfiguration)treeConfiguration).decorators.isEmpty()) {
            ArrayList<BlockPos> list = Lists.newArrayList(set);
            ArrayList<BlockPos> list2 = Lists.newArrayList(set2);
            list.sort(Comparator.comparingInt(Vec3i::getY));
            list2.sort(Comparator.comparingInt(Vec3i::getY));
            ((TreeConfiguration)treeConfiguration).decorators.forEach(treeDecorator -> treeDecorator.place(levelAccessor, random, list, list2, set3, boundingBox));
        }
        DiscreteVoxelShape discreteVoxelShape = this.updateLeaves(levelAccessor, boundingBox, set, set3);
        StructureTemplate.updateShapeAtEdge(levelAccessor, 3, discreteVoxelShape, boundingBox.x0, boundingBox.y0, boundingBox.z0);
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
            ((DiscreteVoxelShape)discreteVoxelShape).setFull(blockPos.getX() - boundingBox.x0, blockPos.getY() - boundingBox.y0, blockPos.getZ() - boundingBox.z0, true, true);
        }
        for (BlockPos blockPos : Lists.newArrayList(set)) {
            if (boundingBox.isInside(blockPos)) {
                ((DiscreteVoxelShape)discreteVoxelShape).setFull(blockPos.getX() - boundingBox.x0, blockPos.getY() - boundingBox.y0, blockPos.getZ() - boundingBox.z0, true, true);
            }
            for (Direction direction : Direction.values()) {
                BlockState blockState;
                mutableBlockPos.setWithOffset(blockPos, direction);
                if (set.contains(mutableBlockPos) || !(blockState = levelAccessor.getBlockState(mutableBlockPos)).hasProperty(BlockStateProperties.DISTANCE)) continue;
                ((Set)list.get(0)).add(mutableBlockPos.immutable());
                this.setBlockKnownShape(levelAccessor, mutableBlockPos, (BlockState)blockState.setValue(BlockStateProperties.DISTANCE, 1));
                if (!boundingBox.isInside(mutableBlockPos)) continue;
                ((DiscreteVoxelShape)discreteVoxelShape).setFull(mutableBlockPos.getX() - boundingBox.x0, mutableBlockPos.getY() - boundingBox.y0, mutableBlockPos.getZ() - boundingBox.z0, true, true);
            }
        }
        for (int k = 1; k < 6; ++k) {
            Set set3 = (Set)list.get(k - 1);
            Set set4 = (Set)list.get(k);
            for (BlockPos blockPos2 : set3) {
                if (boundingBox.isInside(blockPos2)) {
                    ((DiscreteVoxelShape)discreteVoxelShape).setFull(blockPos2.getX() - boundingBox.x0, blockPos2.getY() - boundingBox.y0, blockPos2.getZ() - boundingBox.z0, true, true);
                }
                for (Direction direction2 : Direction.values()) {
                    int l;
                    BlockState blockState2;
                    mutableBlockPos.setWithOffset(blockPos2, direction2);
                    if (set3.contains(mutableBlockPos) || set4.contains(mutableBlockPos) || !(blockState2 = levelAccessor.getBlockState(mutableBlockPos)).hasProperty(BlockStateProperties.DISTANCE) || (l = blockState2.getValue(BlockStateProperties.DISTANCE).intValue()) <= k + 1) continue;
                    BlockState blockState3 = (BlockState)blockState2.setValue(BlockStateProperties.DISTANCE, k + 1);
                    this.setBlockKnownShape(levelAccessor, mutableBlockPos, blockState3);
                    if (boundingBox.isInside(mutableBlockPos)) {
                        ((DiscreteVoxelShape)discreteVoxelShape).setFull(mutableBlockPos.getX() - boundingBox.x0, mutableBlockPos.getY() - boundingBox.y0, mutableBlockPos.getZ() - boundingBox.z0, true, true);
                    }
                    set4.add(mutableBlockPos.immutable());
                }
            }
        }
        return discreteVoxelShape;
    }

    protected abstract boolean doPlace(LevelSimulatedRW var1, Random var2, BlockPos var3, Set<BlockPos> var4, Set<BlockPos> var5, BoundingBox var6, T var7);
}

