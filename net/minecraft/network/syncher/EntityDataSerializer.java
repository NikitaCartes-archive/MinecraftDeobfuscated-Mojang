/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.network.syncher;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.syncher.EntityDataAccessor;

public interface EntityDataSerializer<T> {
    public void write(FriendlyByteBuf var1, T var2);

    public T read(FriendlyByteBuf var1);

    default public EntityDataAccessor<T> createAccessor(int i) {
        return new EntityDataAccessor(i, this);
    }

    public T copy(T var1);
}

