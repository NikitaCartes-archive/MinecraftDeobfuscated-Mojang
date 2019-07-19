/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.item;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class SimpleFoiledItem
extends Item {
    public SimpleFoiledItem(Item.Properties properties) {
        super(properties);
    }

    @Override
    @Environment(value=EnvType.CLIENT)
    public boolean isFoil(ItemStack itemStack) {
        return true;
    }
}

