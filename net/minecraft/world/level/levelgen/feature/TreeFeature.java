/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.levelgen.feature;

import com.mojang.datafixers.Dynamic;
import java.util.Random;
import java.util.Set;
import java.util.function.Function;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.LevelSimulatedRW;
import net.minecraft.world.level.LevelWriter;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CocoaBlock;
import net.minecraft.world.level.block.VineBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.levelgen.feature.AbstractTreeFeature;
import net.minecraft.world.level.levelgen.feature.NoneFeatureConfiguration;
import net.minecraft.world.level.levelgen.structure.BoundingBox;

public class TreeFeature
extends AbstractTreeFeature<NoneFeatureConfiguration> {
    private static final BlockState DEFAULT_TRUNK = Blocks.OAK_LOG.defaultBlockState();
    private static final BlockState DEFAULT_LEAF = Blocks.OAK_LEAVES.defaultBlockState();
    protected final int baseHeight;
    private final boolean addJungleFeatures;
    private final BlockState trunk;
    private final BlockState leaf;

    public TreeFeature(Function<Dynamic<?>, ? extends NoneFeatureConfiguration> function, boolean bl) {
        this(function, bl, 4, DEFAULT_TRUNK, DEFAULT_LEAF, false);
    }

    public TreeFeature(Function<Dynamic<?>, ? extends NoneFeatureConfiguration> function, boolean bl, int i, BlockState blockState, BlockState blockState2, boolean bl2) {
        super(function, bl);
        this.baseHeight = i;
        this.trunk = blockState;
        this.leaf = blockState2;
        this.addJungleFeatures = bl2;
    }

    @Override
    public boolean doPlace(Set<BlockPos> set, LevelSimulatedRW levelSimulatedRW, Random random, BlockPos blockPos, BoundingBox boundingBox) {
        BlockPos blockPos2;
        int q;
        int p;
        int n;
        int m;
        int l;
        int k;
        int j;
        int i = this.getTreeHeight(random);
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
                k = 2;
            }
            BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();
            for (l = blockPos.getX() - k; l <= blockPos.getX() + k && bl; ++l) {
                for (m = blockPos.getZ() - k; m <= blockPos.getZ() + k && bl; ++m) {
                    if (j >= 0 && j < 256) {
                        if (TreeFeature.isFree(levelSimulatedRW, mutableBlockPos.set(l, j, m))) continue;
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
        if (!TreeFeature.isGrassOrDirtOrFarmland(levelSimulatedRW, blockPos.below()) || blockPos.getY() >= 256 - i - 1) {
            return false;
        }
        this.setDirtAt(levelSimulatedRW, blockPos.below());
        j = 3;
        k = 0;
        for (n = blockPos.getY() - 3 + i; n <= blockPos.getY() + i; ++n) {
            l = n - (blockPos.getY() + i);
            m = 1 - l / 2;
            for (int o = blockPos.getX() - m; o <= blockPos.getX() + m; ++o) {
                p = o - blockPos.getX();
                for (q = blockPos.getZ() - m; q <= blockPos.getZ() + m; ++q) {
                    int r = q - blockPos.getZ();
                    if (Math.abs(p) == m && Math.abs(r) == m && (random.nextInt(2) == 0 || l == 0) || !TreeFeature.isAirOrLeaves(levelSimulatedRW, blockPos2 = new BlockPos(o, n, q)) && !TreeFeature.isReplaceablePlant(levelSimulatedRW, blockPos2)) continue;
                    this.setBlock(set, levelSimulatedRW, blockPos2, this.leaf, boundingBox);
                }
            }
        }
        for (n = 0; n < i; ++n) {
            if (!TreeFeature.isAirOrLeaves(levelSimulatedRW, blockPos.above(n)) && !TreeFeature.isReplaceablePlant(levelSimulatedRW, blockPos.above(n))) continue;
            this.setBlock(set, levelSimulatedRW, blockPos.above(n), this.trunk, boundingBox);
            if (!this.addJungleFeatures || n <= 0) continue;
            if (random.nextInt(3) > 0 && TreeFeature.isAir(levelSimulatedRW, blockPos.offset(-1, n, 0))) {
                this.addVine(levelSimulatedRW, blockPos.offset(-1, n, 0), VineBlock.EAST);
            }
            if (random.nextInt(3) > 0 && TreeFeature.isAir(levelSimulatedRW, blockPos.offset(1, n, 0))) {
                this.addVine(levelSimulatedRW, blockPos.offset(1, n, 0), VineBlock.WEST);
            }
            if (random.nextInt(3) > 0 && TreeFeature.isAir(levelSimulatedRW, blockPos.offset(0, n, -1))) {
                this.addVine(levelSimulatedRW, blockPos.offset(0, n, -1), VineBlock.SOUTH);
            }
            if (random.nextInt(3) <= 0 || !TreeFeature.isAir(levelSimulatedRW, blockPos.offset(0, n, 1))) continue;
            this.addVine(levelSimulatedRW, blockPos.offset(0, n, 1), VineBlock.NORTH);
        }
        if (this.addJungleFeatures) {
            for (n = blockPos.getY() - 3 + i; n <= blockPos.getY() + i; ++n) {
                l = n - (blockPos.getY() + i);
                m = 2 - l / 2;
                BlockPos.MutableBlockPos mutableBlockPos2 = new BlockPos.MutableBlockPos();
                for (p = blockPos.getX() - m; p <= blockPos.getX() + m; ++p) {
                    for (q = blockPos.getZ() - m; q <= blockPos.getZ() + m; ++q) {
                        mutableBlockPos2.set(p, n, q);
                        if (!TreeFeature.isLeaves(levelSimulatedRW, mutableBlockPos2)) continue;
                        BlockPos blockPos3 = mutableBlockPos2.west();
                        blockPos2 = mutableBlockPos2.east();
                        BlockPos blockPos4 = mutableBlockPos2.north();
                        BlockPos blockPos5 = mutableBlockPos2.south();
                        if (random.nextInt(4) == 0 && TreeFeature.isAir(levelSimulatedRW, blockPos3)) {
                            this.addHangingVine(levelSimulatedRW, blockPos3, VineBlock.EAST);
                        }
                        if (random.nextInt(4) == 0 && TreeFeature.isAir(levelSimulatedRW, blockPos2)) {
                            this.addHangingVine(levelSimulatedRW, blockPos2, VineBlock.WEST);
                        }
                        if (random.nextInt(4) == 0 && TreeFeature.isAir(levelSimulatedRW, blockPos4)) {
                            this.addHangingVine(levelSimulatedRW, blockPos4, VineBlock.SOUTH);
                        }
                        if (random.nextInt(4) != 0 || !TreeFeature.isAir(levelSimulatedRW, blockPos5)) continue;
                        this.addHangingVine(levelSimulatedRW, blockPos5, VineBlock.NORTH);
                    }
                }
            }
            if (random.nextInt(5) == 0 && i > 5) {
                for (n = 0; n < 2; ++n) {
                    for (Direction direction : Direction.Plane.HORIZONTAL) {
                        if (random.nextInt(4 - n) != 0) continue;
                        Direction direction2 = direction.getOpposite();
                        this.placeCocoa(levelSimulatedRW, random.nextInt(3), blockPos.offset(direction2.getStepX(), i - 5 + n, direction2.getStepZ()), direction);
                    }
                }
            }
        }
        return true;
    }

    protected int getTreeHeight(Random random) {
        return this.baseHeight + random.nextInt(3);
    }

    private void placeCocoa(LevelWriter levelWriter, int i, BlockPos blockPos, Direction direction) {
        this.setBlock(levelWriter, blockPos, (BlockState)((BlockState)Blocks.COCOA.defaultBlockState().setValue(CocoaBlock.AGE, i)).setValue(CocoaBlock.FACING, direction));
    }

    private void addVine(LevelWriter levelWriter, BlockPos blockPos, BooleanProperty booleanProperty) {
        this.setBlock(levelWriter, blockPos, (BlockState)Blocks.VINE.defaultBlockState().setValue(booleanProperty, true));
    }

    private void addHangingVine(LevelSimulatedRW levelSimulatedRW, BlockPos blockPos, BooleanProperty booleanProperty) {
        this.addVine(levelSimulatedRW, blockPos, booleanProperty);
        blockPos = blockPos.below();
        for (int i = 4; TreeFeature.isAir(levelSimulatedRW, blockPos) && i > 0; --i) {
            this.addVine(levelSimulatedRW, blockPos, booleanProperty);
            blockPos = blockPos.below();
        }
    }
}

