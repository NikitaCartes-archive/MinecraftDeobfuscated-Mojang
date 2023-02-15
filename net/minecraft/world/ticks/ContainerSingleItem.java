/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.ticks;

import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;

public interface ContainerSingleItem
extends Container {
    @Override
    default public int getContainerSize() {
        return 1;
    }

    @Override
    default public boolean isEmpty() {
        return this.getFirstItem().isEmpty();
    }

    @Override
    default public void clearContent() {
        this.removeFirstItem();
    }

    default public ItemStack getFirstItem() {
        return this.getItem(0);
    }

    default public ItemStack removeFirstItem() {
        return this.removeItemNoUpdate(0);
    }

    default public void setFirstItem(ItemStack itemStack) {
        this.setItem(0, itemStack);
    }

    @Override
    default public ItemStack removeItemNoUpdate(int i) {
        return this.removeItem(i, this.getMaxStackSize());
    }
}

