/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.core;

public interface HolderOwner<T> {
    default public boolean canSerializeIn(HolderOwner<T> holderOwner) {
        return holderOwner == this;
    }
}

