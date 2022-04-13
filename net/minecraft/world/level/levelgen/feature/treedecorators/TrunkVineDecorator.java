/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.levelgen.feature.treedecorators;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.VineBlock;
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
    public void place(TreeDecorator.Context context) {
        RandomSource randomSource = context.random();
        context.logs().forEach(blockPos -> {
            BlockPos blockPos2;
            if (randomSource.nextInt(3) > 0 && context.isAir(blockPos2 = blockPos.west())) {
                context.placeVine(blockPos2, VineBlock.EAST);
            }
            if (randomSource.nextInt(3) > 0 && context.isAir(blockPos2 = blockPos.east())) {
                context.placeVine(blockPos2, VineBlock.WEST);
            }
            if (randomSource.nextInt(3) > 0 && context.isAir(blockPos2 = blockPos.north())) {
                context.placeVine(blockPos2, VineBlock.SOUTH);
            }
            if (randomSource.nextInt(3) > 0 && context.isAir(blockPos2 = blockPos.south())) {
                context.placeVine(blockPos2, VineBlock.NORTH);
            }
        });
    }
}

