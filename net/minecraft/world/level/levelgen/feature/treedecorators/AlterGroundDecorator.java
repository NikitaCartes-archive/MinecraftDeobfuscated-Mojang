/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.levelgen.feature.treedecorators;

import com.google.common.collect.Lists;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.ArrayList;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProvider;
import net.minecraft.world.level.levelgen.feature.treedecorators.TreeDecorator;
import net.minecraft.world.level.levelgen.feature.treedecorators.TreeDecoratorType;

public class AlterGroundDecorator
extends TreeDecorator {
    public static final Codec<AlterGroundDecorator> CODEC = ((MapCodec)BlockStateProvider.CODEC.fieldOf("provider")).xmap(AlterGroundDecorator::new, alterGroundDecorator -> alterGroundDecorator.provider).codec();
    private final BlockStateProvider provider;

    public AlterGroundDecorator(BlockStateProvider blockStateProvider) {
        this.provider = blockStateProvider;
    }

    @Override
    protected TreeDecoratorType<?> type() {
        return TreeDecoratorType.ALTER_GROUND;
    }

    @Override
    public void place(TreeDecorator.Context context) {
        ArrayList<BlockPos> list = Lists.newArrayList();
        ObjectArrayList<BlockPos> list2 = context.roots();
        ObjectArrayList<BlockPos> list3 = context.logs();
        if (list2.isEmpty()) {
            list.addAll(list3);
        } else if (!list3.isEmpty() && ((BlockPos)list2.get(0)).getY() == ((BlockPos)list3.get(0)).getY()) {
            list.addAll(list3);
            list.addAll(list2);
        } else {
            list.addAll(list2);
        }
        if (list.isEmpty()) {
            return;
        }
        int i = ((BlockPos)list.get(0)).getY();
        list.stream().filter(blockPos -> blockPos.getY() == i).forEach(blockPos -> {
            this.placeCircle(context, blockPos.west().north());
            this.placeCircle(context, blockPos.east(2).north());
            this.placeCircle(context, blockPos.west().south(2));
            this.placeCircle(context, blockPos.east(2).south(2));
            for (int i = 0; i < 5; ++i) {
                int j = context.random().nextInt(64);
                int k = j % 8;
                int l = j / 8;
                if (k != 0 && k != 7 && l != 0 && l != 7) continue;
                this.placeCircle(context, blockPos.offset(-3 + k, 0, -3 + l));
            }
        });
    }

    private void placeCircle(TreeDecorator.Context context, BlockPos blockPos) {
        for (int i = -2; i <= 2; ++i) {
            for (int j = -2; j <= 2; ++j) {
                if (Math.abs(i) == 2 && Math.abs(j) == 2) continue;
                this.placeBlockAt(context, blockPos.offset(i, 0, j));
            }
        }
    }

    private void placeBlockAt(TreeDecorator.Context context, BlockPos blockPos) {
        for (int i = 2; i >= -3; --i) {
            BlockPos blockPos2 = blockPos.above(i);
            if (Feature.isGrassOrDirt(context.level(), blockPos2)) {
                context.setBlock(blockPos2, this.provider.getState(context.random(), blockPos));
                break;
            }
            if (!context.isAir(blockPos2) && i < 0) break;
        }
    }
}

