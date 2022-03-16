/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.multiplayer.prediction;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ServerGamePacketListener;

@FunctionalInterface
@Environment(value=EnvType.CLIENT)
public interface PredictiveAction {
    public Packet<ServerGamePacketListener> predict(int var1);
}

