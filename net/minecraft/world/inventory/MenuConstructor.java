/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.inventory;

import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import org.jetbrains.annotations.Nullable;

@FunctionalInterface
public interface MenuConstructor {
    @Nullable
    public AbstractContainerMenu createMenu(int var1, Inventory var2, Player var3);
}

