/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.network.protocol.game;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import org.jetbrains.annotations.Nullable;

public class ClientboundUpdateMobEffectPacket
implements Packet<ClientGamePacketListener> {
    private static final short LONG_DURATION_THRESHOLD = Short.MAX_VALUE;
    private static final int FLAG_AMBIENT = 1;
    private static final int FLAG_VISIBLE = 2;
    private static final int FLAG_SHOW_ICON = 4;
    private final int entityId;
    private final MobEffect effect;
    private final byte effectAmplifier;
    private final int effectDurationTicks;
    private final byte flags;
    @Nullable
    private final MobEffectInstance.FactorData factorData;

    public ClientboundUpdateMobEffectPacket(int i, MobEffectInstance mobEffectInstance) {
        this.entityId = i;
        this.effect = mobEffectInstance.getEffect();
        this.effectAmplifier = (byte)(mobEffectInstance.getAmplifier() & 0xFF);
        this.effectDurationTicks = mobEffectInstance.getDuration();
        byte b = 0;
        if (mobEffectInstance.isAmbient()) {
            b = (byte)(b | 1);
        }
        if (mobEffectInstance.isVisible()) {
            b = (byte)(b | 2);
        }
        if (mobEffectInstance.showIcon()) {
            b = (byte)(b | 4);
        }
        this.flags = b;
        this.factorData = mobEffectInstance.getFactorData().orElse(null);
    }

    public ClientboundUpdateMobEffectPacket(FriendlyByteBuf friendlyByteBuf2) {
        this.entityId = friendlyByteBuf2.readVarInt();
        this.effect = friendlyByteBuf2.readById(BuiltInRegistries.MOB_EFFECT);
        this.effectAmplifier = friendlyByteBuf2.readByte();
        this.effectDurationTicks = friendlyByteBuf2.readVarInt();
        this.flags = friendlyByteBuf2.readByte();
        this.factorData = (MobEffectInstance.FactorData)friendlyByteBuf2.readNullable(friendlyByteBuf -> friendlyByteBuf.readWithCodec(MobEffectInstance.FactorData.CODEC));
    }

    @Override
    public void write(FriendlyByteBuf friendlyByteBuf2) {
        friendlyByteBuf2.writeVarInt(this.entityId);
        friendlyByteBuf2.writeId(BuiltInRegistries.MOB_EFFECT, this.effect);
        friendlyByteBuf2.writeByte(this.effectAmplifier);
        friendlyByteBuf2.writeVarInt(this.effectDurationTicks);
        friendlyByteBuf2.writeByte(this.flags);
        friendlyByteBuf2.writeNullable(this.factorData, (friendlyByteBuf, factorData) -> friendlyByteBuf.writeWithCodec(MobEffectInstance.FactorData.CODEC, factorData));
    }

    public boolean isSuperLongDuration() {
        return this.effectDurationTicks >= Short.MAX_VALUE;
    }

    @Override
    public void handle(ClientGamePacketListener clientGamePacketListener) {
        clientGamePacketListener.handleUpdateMobEffect(this);
    }

    public int getEntityId() {
        return this.entityId;
    }

    public MobEffect getEffect() {
        return this.effect;
    }

    public byte getEffectAmplifier() {
        return this.effectAmplifier;
    }

    public int getEffectDurationTicks() {
        return this.effectDurationTicks;
    }

    public boolean isEffectVisible() {
        return (this.flags & 2) == 2;
    }

    public boolean isEffectAmbient() {
        return (this.flags & 1) == 1;
    }

    public boolean effectShowsIcon() {
        return (this.flags & 4) == 4;
    }

    @Nullable
    public MobEffectInstance.FactorData getFactorData() {
        return this.factorData;
    }
}

