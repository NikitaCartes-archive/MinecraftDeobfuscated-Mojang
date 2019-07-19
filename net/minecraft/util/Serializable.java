/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.util;

import com.mojang.datafixers.types.DynamicOps;

public interface Serializable {
    public <T> T serialize(DynamicOps<T> var1);
}

