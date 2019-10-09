/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.levelgen.feature.blockplacers;

import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Serializable;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.blockplacers.BlockPlacerType;

public abstract class BlockPlacer
implements Serializable {
    protected final BlockPlacerType<?> type;

    protected BlockPlacer(BlockPlacerType<?> blockPlacerType) {
        this.type = blockPlacerType;
    }

    public abstract void place(LevelAccessor var1, BlockPos var2, BlockState var3, Random var4);
}

