/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.block;

import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.BlockLayer;
import net.minecraft.world.level.block.BeaconBeamBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.IronBarsBlock;
import net.minecraft.world.level.block.state.BlockState;

public class StainedGlassPaneBlock
extends IronBarsBlock
implements BeaconBeamBlock {
    private final DyeColor color;

    public StainedGlassPaneBlock(DyeColor dyeColor, Block.Properties properties) {
        super(properties);
        this.color = dyeColor;
        this.registerDefaultState((BlockState)((BlockState)((BlockState)((BlockState)((BlockState)((BlockState)this.stateDefinition.any()).setValue(NORTH, false)).setValue(EAST, false)).setValue(SOUTH, false)).setValue(WEST, false)).setValue(WATERLOGGED, false));
    }

    @Override
    public DyeColor getColor() {
        return this.color;
    }

    @Override
    public BlockLayer getRenderLayer() {
        return BlockLayer.TRANSLUCENT;
    }
}

