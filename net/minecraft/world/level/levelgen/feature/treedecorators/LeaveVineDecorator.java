/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.levelgen.feature.treedecorators;

import com.mojang.serialization.Codec;
import java.util.List;
import java.util.Random;
import java.util.function.BiConsumer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelSimulatedReader;
import net.minecraft.world.level.block.VineBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.treedecorators.TreeDecorator;
import net.minecraft.world.level.levelgen.feature.treedecorators.TreeDecoratorType;

public class LeaveVineDecorator
extends TreeDecorator {
    public static final Codec<LeaveVineDecorator> CODEC = Codec.unit(() -> INSTANCE);
    public static final LeaveVineDecorator INSTANCE = new LeaveVineDecorator();

    @Override
    protected TreeDecoratorType<?> type() {
        return TreeDecoratorType.LEAVE_VINE;
    }

    @Override
    public void place(LevelSimulatedReader levelSimulatedReader, BiConsumer<BlockPos, BlockState> biConsumer, Random random, List<BlockPos> list, List<BlockPos> list2) {
        list2.forEach(blockPos -> {
            BlockPos blockPos2;
            if (random.nextInt(4) == 0 && Feature.isAir(levelSimulatedReader, blockPos2 = blockPos.west())) {
                LeaveVineDecorator.addHangingVine(levelSimulatedReader, blockPos2, VineBlock.EAST, biConsumer);
            }
            if (random.nextInt(4) == 0 && Feature.isAir(levelSimulatedReader, blockPos2 = blockPos.east())) {
                LeaveVineDecorator.addHangingVine(levelSimulatedReader, blockPos2, VineBlock.WEST, biConsumer);
            }
            if (random.nextInt(4) == 0 && Feature.isAir(levelSimulatedReader, blockPos2 = blockPos.north())) {
                LeaveVineDecorator.addHangingVine(levelSimulatedReader, blockPos2, VineBlock.SOUTH, biConsumer);
            }
            if (random.nextInt(4) == 0 && Feature.isAir(levelSimulatedReader, blockPos2 = blockPos.south())) {
                LeaveVineDecorator.addHangingVine(levelSimulatedReader, blockPos2, VineBlock.NORTH, biConsumer);
            }
        });
    }

    private static void addHangingVine(LevelSimulatedReader levelSimulatedReader, BlockPos blockPos, BooleanProperty booleanProperty, BiConsumer<BlockPos, BlockState> biConsumer) {
        LeaveVineDecorator.placeVine(biConsumer, blockPos, booleanProperty);
        blockPos = blockPos.below();
        for (int i = 4; Feature.isAir(levelSimulatedReader, blockPos) && i > 0; --i) {
            LeaveVineDecorator.placeVine(biConsumer, blockPos, booleanProperty);
            blockPos = blockPos.below();
        }
    }
}

