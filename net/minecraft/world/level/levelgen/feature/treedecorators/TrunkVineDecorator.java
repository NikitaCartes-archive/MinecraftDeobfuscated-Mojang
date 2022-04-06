/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.levelgen.feature.treedecorators;

import com.mojang.serialization.Codec;
import java.util.List;
import java.util.function.BiConsumer;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.LevelSimulatedReader;
import net.minecraft.world.level.block.VineBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.treedecorators.TreeDecorator;
import net.minecraft.world.level.levelgen.feature.treedecorators.TreeDecoratorType;

public class TrunkVineDecorator
extends TreeDecorator {
    public static final Codec<TrunkVineDecorator> CODEC = Codec.unit(() -> INSTANCE);
    public static final TrunkVineDecorator INSTANCE = new TrunkVineDecorator();

    @Override
    protected TreeDecoratorType<?> type() {
        return TreeDecoratorType.TRUNK_VINE;
    }

    @Override
    public void place(LevelSimulatedReader levelSimulatedReader, BiConsumer<BlockPos, BlockState> biConsumer, RandomSource randomSource, List<BlockPos> list, List<BlockPos> list2, List<BlockPos> list3) {
        list.forEach(blockPos -> {
            BlockPos blockPos2;
            if (randomSource.nextInt(3) > 0 && Feature.isAir(levelSimulatedReader, blockPos2 = blockPos.west())) {
                TrunkVineDecorator.placeVine(biConsumer, blockPos2, VineBlock.EAST);
            }
            if (randomSource.nextInt(3) > 0 && Feature.isAir(levelSimulatedReader, blockPos2 = blockPos.east())) {
                TrunkVineDecorator.placeVine(biConsumer, blockPos2, VineBlock.WEST);
            }
            if (randomSource.nextInt(3) > 0 && Feature.isAir(levelSimulatedReader, blockPos2 = blockPos.north())) {
                TrunkVineDecorator.placeVine(biConsumer, blockPos2, VineBlock.SOUTH);
            }
            if (randomSource.nextInt(3) > 0 && Feature.isAir(levelSimulatedReader, blockPos2 = blockPos.south())) {
                TrunkVineDecorator.placeVine(biConsumer, blockPos2, VineBlock.NORTH);
            }
        });
    }
}

