/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.network.protocol.game;

import java.util.Optional;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ServerGamePacketListener;
import net.minecraft.world.effect.MobEffect;

public class ServerboundSetBeaconPacket
implements Packet<ServerGamePacketListener> {
    private final Optional<MobEffect> primary;
    private final Optional<MobEffect> secondary;

    public ServerboundSetBeaconPacket(Optional<MobEffect> optional, Optional<MobEffect> optional2) {
        this.primary = optional;
        this.secondary = optional2;
    }

    public ServerboundSetBeaconPacket(FriendlyByteBuf friendlyByteBuf2) {
        this.primary = friendlyByteBuf2.readOptional(friendlyByteBuf -> friendlyByteBuf.readById(BuiltInRegistries.MOB_EFFECT));
        this.secondary = friendlyByteBuf2.readOptional(friendlyByteBuf -> friendlyByteBuf.readById(BuiltInRegistries.MOB_EFFECT));
    }

    @Override
    public void write(FriendlyByteBuf friendlyByteBuf2) {
        friendlyByteBuf2.writeOptional(this.primary, (friendlyByteBuf, mobEffect) -> friendlyByteBuf.writeId(BuiltInRegistries.MOB_EFFECT, mobEffect));
        friendlyByteBuf2.writeOptional(this.secondary, (friendlyByteBuf, mobEffect) -> friendlyByteBuf.writeId(BuiltInRegistries.MOB_EFFECT, mobEffect));
    }

    @Override
    public void handle(ServerGamePacketListener serverGamePacketListener) {
        serverGamePacketListener.handleSetBeaconPacket(this);
    }

    public Optional<MobEffect> getPrimary() {
        return this.primary;
    }

    public Optional<MobEffect> getSecondary() {
        return this.secondary;
    }
}

