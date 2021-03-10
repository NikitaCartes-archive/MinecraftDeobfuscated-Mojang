/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.inventory;

import net.minecraft.core.NonNullList;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;

public interface ContainerSynchronizer {
    public void sendInitialData(AbstractContainerMenu var1, NonNullList<ItemStack> var2, ItemStack var3, int[] var4);

    public void sendSlotChange(AbstractContainerMenu var1, int var2, ItemStack var3);

    public void sendCarriedChange(AbstractContainerMenu var1, ItemStack var2);

    public void sendDataChange(AbstractContainerMenu var1, int var2, int var3);
}

