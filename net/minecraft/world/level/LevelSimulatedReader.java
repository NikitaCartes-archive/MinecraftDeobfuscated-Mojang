/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level;

import java.util.function.Predicate;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

public interface LevelSimulatedReader {
    public boolean isStateAtPosition(BlockPos var1, Predicate<BlockState> var2);
}

