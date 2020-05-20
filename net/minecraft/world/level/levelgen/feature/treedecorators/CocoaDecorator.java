/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.levelgen.feature.treedecorators;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import java.util.List;
import java.util.Random;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CocoaBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.treedecorators.TreeDecorator;
import net.minecraft.world.level.levelgen.feature.treedecorators.TreeDecoratorType;
import net.minecraft.world.level.levelgen.structure.BoundingBox;

public class CocoaDecorator
extends TreeDecorator {
    public static final Codec<CocoaDecorator> CODEC = ((MapCodec)Codec.FLOAT.fieldOf("probability")).xmap(CocoaDecorator::new, cocoaDecorator -> Float.valueOf(cocoaDecorator.probability)).codec();
    private final float probability;

    public CocoaDecorator(float f) {
        this.probability = f;
    }

    @Override
    protected TreeDecoratorType<?> type() {
        return TreeDecoratorType.COCOA;
    }

    @Override
    public void place(LevelAccessor levelAccessor, Random random, List<BlockPos> list, List<BlockPos> list2, Set<BlockPos> set, BoundingBox boundingBox) {
        if (random.nextFloat() >= this.probability) {
            return;
        }
        int i = list.get(0).getY();
        list.stream().filter(blockPos -> blockPos.getY() - i <= 2).forEach(blockPos -> {
            for (Direction direction : Direction.Plane.HORIZONTAL) {
                Direction direction2;
                BlockPos blockPos2;
                if (!(random.nextFloat() <= 0.25f) || !Feature.isAir(levelAccessor, blockPos2 = blockPos.offset((direction2 = direction.getOpposite()).getStepX(), 0, direction2.getStepZ()))) continue;
                BlockState blockState = (BlockState)((BlockState)Blocks.COCOA.defaultBlockState().setValue(CocoaBlock.AGE, random.nextInt(3))).setValue(CocoaBlock.FACING, direction);
                this.setBlock(levelAccessor, blockPos2, blockState, set, boundingBox);
            }
        });
    }
}

