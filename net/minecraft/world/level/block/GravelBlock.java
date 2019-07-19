/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.block;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.FallingBlock;
import net.minecraft.world.level.block.state.BlockState;

public class GravelBlock
extends FallingBlock {
    public GravelBlock(Block.Properties properties) {
        super(properties);
    }

    @Override
    @Environment(value=EnvType.CLIENT)
    public int getDustColor(BlockState blockState) {
        return -8356741;
    }
}

