/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world;

import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;

public interface Nameable {
    public Component getName();

    default public boolean hasCustomName() {
        return this.getCustomName() != null;
    }

    default public Component getDisplayName() {
        return this.getName();
    }

    @Nullable
    default public Component getCustomName() {
        return null;
    }
}

