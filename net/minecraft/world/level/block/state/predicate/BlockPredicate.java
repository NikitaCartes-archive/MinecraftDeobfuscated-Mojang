/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.block.state.predicate;

import java.util.function.Predicate;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class BlockPredicate
implements Predicate<BlockState> {
    private final Block block;

    public BlockPredicate(Block block) {
        this.block = block;
    }

    public static BlockPredicate forBlock(Block block) {
        return new BlockPredicate(block);
    }

    @Override
    public boolean test(@Nullable BlockState blockState) {
        return blockState != null && blockState.is(this.block);
    }

    @Override
    public /* synthetic */ boolean test(@Nullable Object object) {
        return this.test((BlockState)object);
    }
}

