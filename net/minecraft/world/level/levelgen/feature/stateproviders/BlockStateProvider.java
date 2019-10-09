/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.levelgen.feature.stateproviders;

import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Serializable;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProviderType;

public abstract class BlockStateProvider
implements Serializable {
    protected final BlockStateProviderType<?> type;

    protected BlockStateProvider(BlockStateProviderType<?> blockStateProviderType) {
        this.type = blockStateProviderType;
    }

    public abstract BlockState getState(Random var1, BlockPos var2);
}

