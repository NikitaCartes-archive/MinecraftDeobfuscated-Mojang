/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.block;

import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.AbstractGlassBlock;
import net.minecraft.world.level.block.BeaconBeamBlock;
import net.minecraft.world.level.block.Block;

public class StainedGlassBlock
extends AbstractGlassBlock
implements BeaconBeamBlock {
    private final DyeColor color;

    public StainedGlassBlock(DyeColor dyeColor, Block.Properties properties) {
        super(properties);
        this.color = dyeColor;
    }

    @Override
    public DyeColor getColor() {
        return this.color;
    }
}

