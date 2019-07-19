/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.network.protocol;

import java.io.IOException;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.PacketListener;

public interface Packet<T extends PacketListener> {
    public void read(FriendlyByteBuf var1) throws IOException;

    public void write(FriendlyByteBuf var1) throws IOException;

    public void handle(T var1);

    default public boolean isSkippable() {
        return false;
    }
}

