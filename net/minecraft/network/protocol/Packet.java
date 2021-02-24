/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.network.protocol;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.PacketListener;

public interface Packet<T extends PacketListener> {
    public void write(FriendlyByteBuf var1);

    public void handle(T var1);

    default public boolean isSkippable() {
        return false;
    }
}

