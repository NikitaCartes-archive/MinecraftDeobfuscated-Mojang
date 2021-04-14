/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.levelgen;

import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.BaseStoneSource;

public class SingleBaseStoneSource
implements BaseStoneSource {
    private final BlockState state;

    public SingleBaseStoneSource(BlockState blockState) {
        this.state = blockState;
    }

    @Override
    public BlockState getBaseStone(int i, int j, int k) {
        return this.state;
    }
}

