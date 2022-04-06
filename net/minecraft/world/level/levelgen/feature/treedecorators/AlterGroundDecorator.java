/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.levelgen.feature.treedecorators;

import com.google.common.collect.Lists;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.LevelSimulatedReader;
import net.minecraft.world.level.block.state.BlockState;
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
    public void place(LevelSimulatedReader levelSimulatedReader, BiConsumer<BlockPos, BlockState> biConsumer, RandomSource randomSource, List<BlockPos> list, List<BlockPos> list2, List<BlockPos> list3) {
        ArrayList<BlockPos> list4 = Lists.newArrayList();
        if (list3.isEmpty()) {
            list4.addAll(list);
        } else if (!list.isEmpty() && list3.get(0).getY() == list.get(0).getY()) {
            list4.addAll(list);
            list4.addAll(list3);
        } else {
            list4.addAll(list3);
        }
        if (list4.isEmpty()) {
            return;
        }
        int i = ((BlockPos)list4.get(0)).getY();
        list4.stream().filter(blockPos -> blockPos.getY() == i).forEach(blockPos -> {
            this.placeCircle(levelSimulatedReader, biConsumer, randomSource, blockPos.west().north());
            this.placeCircle(levelSimulatedReader, biConsumer, randomSource, blockPos.east(2).north());
            this.placeCircle(levelSimulatedReader, biConsumer, randomSource, blockPos.west().south(2));
            this.placeCircle(levelSimulatedReader, biConsumer, randomSource, blockPos.east(2).south(2));
            for (int i = 0; i < 5; ++i) {
                int j = randomSource.nextInt(64);
                int k = j % 8;
                int l = j / 8;
                if (k != 0 && k != 7 && l != 0 && l != 7) continue;
                this.placeCircle(levelSimulatedReader, biConsumer, randomSource, blockPos.offset(-3 + k, 0, -3 + l));
            }
        });
    }

    private void placeCircle(LevelSimulatedReader levelSimulatedReader, BiConsumer<BlockPos, BlockState> biConsumer, RandomSource randomSource, BlockPos blockPos) {
        for (int i = -2; i <= 2; ++i) {
            for (int j = -2; j <= 2; ++j) {
                if (Math.abs(i) == 2 && Math.abs(j) == 2) continue;
                this.placeBlockAt(levelSimulatedReader, biConsumer, randomSource, blockPos.offset(i, 0, j));
            }
        }
    }

    private void placeBlockAt(LevelSimulatedReader levelSimulatedReader, BiConsumer<BlockPos, BlockState> biConsumer, RandomSource randomSource, BlockPos blockPos) {
        for (int i = 2; i >= -3; --i) {
            BlockPos blockPos2 = blockPos.above(i);
            if (Feature.isGrassOrDirt(levelSimulatedReader, blockPos2)) {
                biConsumer.accept(blockPos2, this.provider.getState(randomSource, blockPos));
                break;
            }
            if (!Feature.isAir(levelSimulatedReader, blockPos2) && i < 0) break;
        }
    }
}

