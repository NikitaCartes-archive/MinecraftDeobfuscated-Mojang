/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
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
import net.minecraft.world.level.levelgen.feature.AbstractTreeFeature;
import net.minecraft.world.level.levelgen.feature.NoneFeatureConfiguration;
import net.minecraft.world.level.levelgen.structure.BoundingBox;

public class SwampTreeFeature
extends AbstractTreeFeature<NoneFeatureConfiguration> {
    private static final BlockState TRUNK = Blocks.OAK_LOG.defaultBlockState();
    private static final BlockState LEAF = Blocks.OAK_LEAVES.defaultBlockState();

    public SwampTreeFeature(Function<Dynamic<?>, ? extends NoneFeatureConfiguration> function) {
        super(function, false);
    }

    @Override
    public boolean doPlace(Set<BlockPos> set, LevelSimulatedRW levelSimulatedRW, Random random, BlockPos blockPos, BoundingBox boundingBox) {
        BlockPos blockPos2;
        int o;
        int m;
        int l;
        int k;
        int j;
        int i = random.nextInt(4) + 5;
        blockPos = levelSimulatedRW.getHeightmapPos(Heightmap.Types.OCEAN_FLOOR, blockPos);
        boolean bl = true;
        if (blockPos.getY() < 1 || blockPos.getY() + i + 1 > 256) {
            return false;
        }
        for (j = blockPos.getY(); j <= blockPos.getY() + 1 + i; ++j) {
            k = 1;
            if (j == blockPos.getY()) {
                k = 0;
            }
            if (j >= blockPos.getY() + 1 + i - 2) {
                k = 3;
            }
            BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();
            for (l = blockPos.getX() - k; l <= blockPos.getX() + k && bl; ++l) {
                for (m = blockPos.getZ() - k; m <= blockPos.getZ() + k && bl; ++m) {
                    if (j >= 0 && j < 256) {
                        mutableBlockPos.set(l, j, m);
                        if (SwampTreeFeature.isAirOrLeaves(levelSimulatedRW, mutableBlockPos)) continue;
                        if (SwampTreeFeature.isBlockWater(levelSimulatedRW, mutableBlockPos)) {
                            if (j <= blockPos.getY()) continue;
                            bl = false;
                            continue;
                        }
                        bl = false;
                        continue;
                    }
                    bl = false;
                }
            }
        }
        if (!bl) {
            return false;
        }
        if (!SwampTreeFeature.isGrassOrDirt(levelSimulatedRW, blockPos.below()) || blockPos.getY() >= 256 - i - 1) {
            return false;
        }
        this.setDirtAt(levelSimulatedRW, blockPos.below());
        for (j = blockPos.getY() - 3 + i; j <= blockPos.getY() + i; ++j) {
            k = j - (blockPos.getY() + i);
            int n = 2 - k / 2;
            for (l = blockPos.getX() - n; l <= blockPos.getX() + n; ++l) {
                m = l - blockPos.getX();
                for (o = blockPos.getZ() - n; o <= blockPos.getZ() + n; ++o) {
                    int p = o - blockPos.getZ();
                    if (Math.abs(m) == n && Math.abs(p) == n && (random.nextInt(2) == 0 || k == 0) || !SwampTreeFeature.isAirOrLeaves(levelSimulatedRW, blockPos2 = new BlockPos(l, j, o)) && !SwampTreeFeature.isReplaceablePlant(levelSimulatedRW, blockPos2)) continue;
                    this.setBlock(set, levelSimulatedRW, blockPos2, LEAF, boundingBox);
                }
            }
        }
        for (j = 0; j < i; ++j) {
            BlockPos blockPos3 = blockPos.above(j);
            if (!SwampTreeFeature.isAirOrLeaves(levelSimulatedRW, blockPos3) && !SwampTreeFeature.isBlockWater(levelSimulatedRW, blockPos3)) continue;
            this.setBlock(set, levelSimulatedRW, blockPos3, TRUNK, boundingBox);
        }
        for (j = blockPos.getY() - 3 + i; j <= blockPos.getY() + i; ++j) {
            int k2 = j - (blockPos.getY() + i);
            int n = 2 - k2 / 2;
            BlockPos.MutableBlockPos mutableBlockPos2 = new BlockPos.MutableBlockPos();
            for (m = blockPos.getX() - n; m <= blockPos.getX() + n; ++m) {
                for (o = blockPos.getZ() - n; o <= blockPos.getZ() + n; ++o) {
                    mutableBlockPos2.set(m, j, o);
                    if (!SwampTreeFeature.isLeaves(levelSimulatedRW, mutableBlockPos2)) continue;
                    BlockPos blockPos4 = mutableBlockPos2.west();
                    blockPos2 = mutableBlockPos2.east();
                    BlockPos blockPos5 = mutableBlockPos2.north();
                    BlockPos blockPos6 = mutableBlockPos2.south();
                    if (random.nextInt(4) == 0 && SwampTreeFeature.isAir(levelSimulatedRW, blockPos4)) {
                        this.addVine(levelSimulatedRW, blockPos4, VineBlock.EAST);
                    }
                    if (random.nextInt(4) == 0 && SwampTreeFeature.isAir(levelSimulatedRW, blockPos2)) {
                        this.addVine(levelSimulatedRW, blockPos2, VineBlock.WEST);
                    }
                    if (random.nextInt(4) == 0 && SwampTreeFeature.isAir(levelSimulatedRW, blockPos5)) {
                        this.addVine(levelSimulatedRW, blockPos5, VineBlock.SOUTH);
                    }
                    if (random.nextInt(4) != 0 || !SwampTreeFeature.isAir(levelSimulatedRW, blockPos6)) continue;
                    this.addVine(levelSimulatedRW, blockPos6, VineBlock.NORTH);
                }
            }
        }
        return true;
    }

    private void addVine(LevelSimulatedRW levelSimulatedRW, BlockPos blockPos, BooleanProperty booleanProperty) {
        BlockState blockState = (BlockState)Blocks.VINE.defaultBlockState().setValue(booleanProperty, true);
        this.setBlock(levelSimulatedRW, blockPos, blockState);
        blockPos = blockPos.below();
        for (int i = 4; SwampTreeFeature.isAir(levelSimulatedRW, blockPos) && i > 0; --i) {
            this.setBlock(levelSimulatedRW, blockPos, blockState);
            blockPos = blockPos.below();
        }
    }
}

