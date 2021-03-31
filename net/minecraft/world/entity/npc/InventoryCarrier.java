/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.entity.npc;

import net.minecraft.util.VisibleForDebug;
import net.minecraft.world.Container;

public interface InventoryCarrier {
    @VisibleForDebug
    public Container getInventory();
}

