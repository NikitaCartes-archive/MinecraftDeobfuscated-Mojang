/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world;

import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.MenuConstructor;

public interface MenuProvider
extends MenuConstructor {
    public Component getDisplayName();
}

