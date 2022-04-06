/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.levelgen.feature.treedecorators;

import com.mojang.serialization.Codec;
import java.util.List;
import java.util.function.BiConsumer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.LevelSimulatedReader;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.levelgen.feature.treedecorators.TreeDecoratorType;

public abstract class TreeDecorator {
    public static final Codec<TreeDecorator> CODEC = Registry.TREE_DECORATOR_TYPES.byNameCodec().dispatch(TreeDecorator::type, TreeDecoratorType::codec);

    protected abstract TreeDecoratorType<?> type();

    public abstract void place(LevelSimulatedReader var1, BiConsumer<BlockPos, BlockState> var2, RandomSource var3, List<BlockPos> var4, List<BlockPos> var5, List<BlockPos> var6);

    protected static void placeVine(BiConsumer<BlockPos, BlockState> biConsumer, BlockPos blockPos, BooleanProperty booleanProperty) {
        biConsumer.accept(blockPos, (BlockState)Blocks.VINE.defaultBlockState().setValue(booleanProperty, true));
    }
}

