/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.network;

import net.minecraft.network.chat.Component;

public interface PacketListener {
    public void onDisconnect(Component var1);

    public boolean isAcceptingMessages();

    default public boolean shouldPropagateHandlingExceptions() {
        return true;
    }
}

