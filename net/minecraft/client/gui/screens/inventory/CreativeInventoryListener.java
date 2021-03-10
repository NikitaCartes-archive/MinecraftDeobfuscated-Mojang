/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.gui.screens.inventory;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerListener;
import net.minecraft.world.item.ItemStack;

@Environment(value=EnvType.CLIENT)
public class CreativeInventoryListener
implements ContainerListener {
    private final Minecraft minecraft;

    public CreativeInventoryListener(Minecraft minecraft) {
        this.minecraft = minecraft;
    }

    @Override
    public void slotChanged(AbstractContainerMenu abstractContainerMenu, int i, ItemStack itemStack) {
        this.minecraft.gameMode.handleCreativeModeItemAdd(itemStack, i);
    }

    @Override
    public void dataChanged(AbstractContainerMenu abstractContainerMenu, int i, int j) {
    }
}

