/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level;

import java.util.function.Supplier;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ServerLevelAccessor;
import org.jetbrains.annotations.Nullable;

public interface WorldGenLevel
extends ServerLevelAccessor {
    public long getSeed();

    default public boolean ensureCanWrite(BlockPos blockPos) {
        return true;
    }

    default public void setCurrentlyGenerating(@Nullable Supplier<String> supplier) {
    }
}

