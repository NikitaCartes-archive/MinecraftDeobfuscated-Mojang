/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world;

import org.jetbrains.annotations.Nullable;

public interface Clearable {
    public void clearContent();

    public static void tryClear(@Nullable Object object) {
        if (object instanceof Clearable) {
            ((Clearable)object).clearContent();
        }
    }
}

