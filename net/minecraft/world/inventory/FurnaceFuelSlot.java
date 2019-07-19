/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.inventory;

import net.minecraft.world.Container;
import net.minecraft.world.inventory.AbstractFurnaceMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public class FurnaceFuelSlot
extends Slot {
    private final AbstractFurnaceMenu menu;

    public FurnaceFuelSlot(AbstractFurnaceMenu abstractFurnaceMenu, Container container, int i, int j, int k) {
        super(container, i, j, k);
        this.menu = abstractFurnaceMenu;
    }

    @Override
    public boolean mayPlace(ItemStack itemStack) {
        return this.menu.isFuel(itemStack) || FurnaceFuelSlot.isBucket(itemStack);
    }

    @Override
    public int getMaxStackSize(ItemStack itemStack) {
        return FurnaceFuelSlot.isBucket(itemStack) ? 1 : super.getMaxStackSize(itemStack);
    }

    public static boolean isBucket(ItemStack itemStack) {
        return itemStack.getItem() == Items.BUCKET;
    }
}

