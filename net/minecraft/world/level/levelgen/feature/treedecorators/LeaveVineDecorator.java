/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.levelgen.feature.treedecorators;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.VineBlock;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.levelgen.feature.treedecorators.TreeDecorator;
import net.minecraft.world.level.levelgen.feature.treedecorators.TreeDecoratorType;

public class LeaveVineDecorator
extends TreeDecorator {
    public static final Codec<LeaveVineDecorator> CODEC = ((MapCodec)Codec.floatRange(0.0f, 1.0f).fieldOf("probability")).xmap(LeaveVineDecorator::new, leaveVineDecorator -> Float.valueOf(leaveVineDecorator.probability)).codec();
    private final float probability;

    @Override
    protected TreeDecoratorType<?> type() {
        return TreeDecoratorType.LEAVE_VINE;
    }

    public LeaveVineDecorator(float f) {
        this.probability = f;
    }

    @Override
    public void place(TreeDecorator.Context context) {
        RandomSource randomSource = context.random();
        context.leaves().forEach(blockPos -> {
            BlockPos blockPos2;
            if (randomSource.nextFloat() < this.probability && context.isAir(blockPos2 = blockPos.west())) {
                LeaveVineDecorator.addHangingVine(blockPos2, VineBlock.EAST, context);
            }
            if (randomSource.nextFloat() < this.probability && context.isAir(blockPos2 = blockPos.east())) {
                LeaveVineDecorator.addHangingVine(blockPos2, VineBlock.WEST, context);
            }
            if (randomSource.nextFloat() < this.probability && context.isAir(blockPos2 = blockPos.north())) {
                LeaveVineDecorator.addHangingVine(blockPos2, VineBlock.SOUTH, context);
            }
            if (randomSource.nextFloat() < this.probability && context.isAir(blockPos2 = blockPos.south())) {
                LeaveVineDecorator.addHangingVine(blockPos2, VineBlock.NORTH, context);
            }
        });
    }

    private static void addHangingVine(BlockPos blockPos, BooleanProperty booleanProperty, TreeDecorator.Context context) {
        context.placeVine(blockPos, booleanProperty);
        blockPos = blockPos.below();
        for (int i = 4; context.isAir(blockPos) && i > 0; --i) {
            context.placeVine(blockPos, booleanProperty);
            blockPos = blockPos.below();
        }
    }
}

