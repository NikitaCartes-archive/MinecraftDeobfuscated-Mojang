/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.network.protocol.game;

import java.io.IOException;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;

public class ClientboundUpdateMobEffectPacket
implements Packet<ClientGamePacketListener> {
    private int entityId;
    private byte effectId;
    private byte effectAmplifier;
    private int effectDurationTicks;
    private byte flags;

    public ClientboundUpdateMobEffectPacket() {
    }

    public ClientboundUpdateMobEffectPacket(int i, MobEffectInstance mobEffectInstance) {
        this.entityId = i;
        this.effectId = (byte)(MobEffect.getId(mobEffectInstance.getEffect()) & 0xFF);
        this.effectAmplifier = (byte)(mobEffectInstance.getAmplifier() & 0xFF);
        this.effectDurationTicks = mobEffectInstance.getDuration() > Short.MAX_VALUE ? Short.MAX_VALUE : mobEffectInstance.getDuration();
        this.flags = 0;
        if (mobEffectInstance.isAmbient()) {
            this.flags = (byte)(this.flags | 1);
        }
        if (mobEffectInstance.isVisible()) {
            this.flags = (byte)(this.flags | 2);
        }
        if (mobEffectInstance.showIcon()) {
            this.flags = (byte)(this.flags | 4);
        }
    }

    @Override
    public void read(FriendlyByteBuf friendlyByteBuf) throws IOException {
        this.entityId = friendlyByteBuf.readVarInt();
        this.effectId = friendlyByteBuf.readByte();
        this.effectAmplifier = friendlyByteBuf.readByte();
        this.effectDurationTicks = friendlyByteBuf.readVarInt();
        this.flags = friendlyByteBuf.readByte();
    }

    @Override
    public void write(FriendlyByteBuf friendlyByteBuf) throws IOException {
        friendlyByteBuf.writeVarInt(this.entityId);
        friendlyByteBuf.writeByte(this.effectId);
        friendlyByteBuf.writeByte(this.effectAmplifier);
        friendlyByteBuf.writeVarInt(this.effectDurationTicks);
        friendlyByteBuf.writeByte(this.flags);
    }

    @Environment(value=EnvType.CLIENT)
    public boolean isSuperLongDuration() {
        return this.effectDurationTicks == Short.MAX_VALUE;
    }

    @Override
    public void handle(ClientGamePacketListener clientGamePacketListener) {
        clientGamePacketListener.handleUpdateMobEffect(this);
    }

    @Environment(value=EnvType.CLIENT)
    public int getEntityId() {
        return this.entityId;
    }

    @Environment(value=EnvType.CLIENT)
    public byte getEffectId() {
        return this.effectId;
    }

    @Environment(value=EnvType.CLIENT)
    public byte getEffectAmplifier() {
        return this.effectAmplifier;
    }

    @Environment(value=EnvType.CLIENT)
    public int getEffectDurationTicks() {
        return this.effectDurationTicks;
    }

    @Environment(value=EnvType.CLIENT)
    public boolean isEffectVisible() {
        return (this.flags & 2) == 2;
    }

    @Environment(value=EnvType.CLIENT)
    public boolean isEffectAmbient() {
        return (this.flags & 1) == 1;
    }

    @Environment(value=EnvType.CLIENT)
    public boolean effectShowsIcon() {
        return (this.flags & 4) == 4;
    }
}

