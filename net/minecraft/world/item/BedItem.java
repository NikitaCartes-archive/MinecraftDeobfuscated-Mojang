/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.item;

import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.BlockPlaceContext;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public class BedItem
extends BlockItem {
    public BedItem(Block block, Item.Properties properties) {
        super(block, properties);
    }

    @Override
    protected boolean placeBlock(BlockPlaceContext blockPlaceContext, BlockState blockState) {
        return blockPlaceContext.getLevel().setBlock(blockPlaceContext.getClickedPos(), blockState, 26);
    }
}

