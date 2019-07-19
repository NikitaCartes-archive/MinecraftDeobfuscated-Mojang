/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.color.item;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.world.item.ItemStack;

@Environment(value=EnvType.CLIENT)
public interface ItemColor {
    public int getColor(ItemStack var1, int var2);
}

