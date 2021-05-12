/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.item;

import net.minecraft.tags.BlockTags;
import net.minecraft.world.item.DiggerItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Tier;

public class PickaxeItem
extends DiggerItem {
    protected PickaxeItem(Tier tier, int i, float f, Item.Properties properties) {
        super(i, f, tier, BlockTags.MINEABLE_WITH_PICKAXE, properties);
    }
}

