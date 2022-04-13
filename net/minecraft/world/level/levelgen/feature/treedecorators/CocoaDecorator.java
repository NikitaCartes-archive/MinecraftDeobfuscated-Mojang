/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.levelgen.feature.treedecorators;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CocoaBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.treedecorators.TreeDecorator;
import net.minecraft.world.level.levelgen.feature.treedecorators.TreeDecoratorType;

public class CocoaDecorator
extends TreeDecorator {
    public static final Codec<CocoaDecorator> CODEC = ((MapCodec)Codec.floatRange(0.0f, 1.0f).fieldOf("probability")).xmap(CocoaDecorator::new, cocoaDecorator -> Float.valueOf(cocoaDecorator.probability)).codec();
    private final float probability;

    public CocoaDecorator(float f) {
        this.probability = f;
    }

    @Override
    protected TreeDecoratorType<?> type() {
        return TreeDecoratorType.COCOA;
    }

    @Override
    public void place(TreeDecorator.Context context) {
        RandomSource randomSource = context.random();
        if (randomSource.nextFloat() >= this.probability) {
            return;
        }
        ObjectArrayList<BlockPos> list = context.logs();
        int i = ((BlockPos)list.get(0)).getY();
        list.stream().filter(blockPos -> blockPos.getY() - i <= 2).forEach(blockPos -> {
            for (Direction direction : Direction.Plane.HORIZONTAL) {
                Direction direction2;
                BlockPos blockPos2;
                if (!(randomSource.nextFloat() <= 0.25f) || !context.isAir(blockPos2 = blockPos.offset((direction2 = direction.getOpposite()).getStepX(), 0, direction2.getStepZ()))) continue;
                context.setBlock(blockPos2, (BlockState)((BlockState)Blocks.COCOA.defaultBlockState().setValue(CocoaBlock.AGE, randomSource.nextInt(3))).setValue(CocoaBlock.FACING, direction));
            }
        });
    }
}

