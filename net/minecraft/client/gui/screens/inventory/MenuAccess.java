/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.gui.screens.inventory;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.world.inventory.AbstractContainerMenu;

@Environment(value=EnvType.CLIENT)
public interface MenuAccess<T extends AbstractContainerMenu> {
    public T getMenu();
}

