/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.item;

import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

public class ItemNameBlockItem
extends BlockItem {
    public ItemNameBlockItem(Block block, Item.Properties properties) {
        super(block, properties);
    }

    @Override
    public String getDescriptionId() {
        return this.getOrCreateDescriptionId();
    }
}

