/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.levelgen.feature.treedecorators;

import com.mojang.serialization.Codec;
import java.util.List;
import java.util.Random;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelSimulatedRW;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.VineBlock;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.treedecorators.TreeDecorator;
import net.minecraft.world.level.levelgen.feature.treedecorators.TreeDecoratorType;
import net.minecraft.world.level.levelgen.structure.BoundingBox;

public class LeaveVineDecorator
extends TreeDecorator {
    public static final Codec<LeaveVineDecorator> CODEC = Codec.unit(() -> INSTANCE);
    public static final LeaveVineDecorator INSTANCE = new LeaveVineDecorator();

    @Override
    protected TreeDecoratorType<?> type() {
        return TreeDecoratorType.LEAVE_VINE;
    }

    @Override
    public void place(WorldGenLevel worldGenLevel, Random random, List<BlockPos> list, List<BlockPos> list2, Set<BlockPos> set, BoundingBox boundingBox) {
        list2.forEach(blockPos -> {
            BlockPos blockPos2;
            if (random.nextInt(4) == 0 && Feature.isAir(worldGenLevel, blockPos2 = blockPos.west())) {
                this.addHangingVine(worldGenLevel, blockPos2, VineBlock.EAST, set, boundingBox);
            }
            if (random.nextInt(4) == 0 && Feature.isAir(worldGenLevel, blockPos2 = blockPos.east())) {
                this.addHangingVine(worldGenLevel, blockPos2, VineBlock.WEST, set, boundingBox);
            }
            if (random.nextInt(4) == 0 && Feature.isAir(worldGenLevel, blockPos2 = blockPos.north())) {
                this.addHangingVine(worldGenLevel, blockPos2, VineBlock.SOUTH, set, boundingBox);
            }
            if (random.nextInt(4) == 0 && Feature.isAir(worldGenLevel, blockPos2 = blockPos.south())) {
                this.addHangingVine(worldGenLevel, blockPos2, VineBlock.NORTH, set, boundingBox);
            }
        });
    }

    private void addHangingVine(LevelSimulatedRW levelSimulatedRW, BlockPos blockPos, BooleanProperty booleanProperty, Set<BlockPos> set, BoundingBox boundingBox) {
        this.placeVine(levelSimulatedRW, blockPos, booleanProperty, set, boundingBox);
        blockPos = blockPos.below();
        for (int i = 4; Feature.isAir(levelSimulatedRW, blockPos) && i > 0; --i) {
            this.placeVine(levelSimulatedRW, blockPos, booleanProperty, set, boundingBox);
            blockPos = blockPos.below();
        }
    }
}

