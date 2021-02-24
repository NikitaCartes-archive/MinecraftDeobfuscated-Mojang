/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.network.protocol.game;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.core.Registry;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import org.apache.commons.lang3.Validate;

public class ClientboundSoundEntityPacket
implements Packet<ClientGamePacketListener> {
    private final SoundEvent sound;
    private final SoundSource source;
    private final int id;
    private final float volume;
    private final float pitch;

    public ClientboundSoundEntityPacket(SoundEvent soundEvent, SoundSource soundSource, Entity entity, float f, float g) {
        Validate.notNull(soundEvent, "sound", new Object[0]);
        this.sound = soundEvent;
        this.source = soundSource;
        this.id = entity.getId();
        this.volume = f;
        this.pitch = g;
    }

    public ClientboundSoundEntityPacket(FriendlyByteBuf friendlyByteBuf) {
        this.sound = (SoundEvent)Registry.SOUND_EVENT.byId(friendlyByteBuf.readVarInt());
        this.source = friendlyByteBuf.readEnum(SoundSource.class);
        this.id = friendlyByteBuf.readVarInt();
        this.volume = friendlyByteBuf.readFloat();
        this.pitch = friendlyByteBuf.readFloat();
    }

    @Override
    public void write(FriendlyByteBuf friendlyByteBuf) {
        friendlyByteBuf.writeVarInt(Registry.SOUND_EVENT.getId(this.sound));
        friendlyByteBuf.writeEnum(this.source);
        friendlyByteBuf.writeVarInt(this.id);
        friendlyByteBuf.writeFloat(this.volume);
        friendlyByteBuf.writeFloat(this.pitch);
    }

    @Environment(value=EnvType.CLIENT)
    public SoundEvent getSound() {
        return this.sound;
    }

    @Environment(value=EnvType.CLIENT)
    public SoundSource getSource() {
        return this.source;
    }

    @Environment(value=EnvType.CLIENT)
    public int getId() {
        return this.id;
    }

    @Environment(value=EnvType.CLIENT)
    public float getVolume() {
        return this.volume;
    }

    @Environment(value=EnvType.CLIENT)
    public float getPitch() {
        return this.pitch;
    }

    @Override
    public void handle(ClientGamePacketListener clientGamePacketListener) {
        clientGamePacketListener.handleSoundEntityEvent(this);
    }
}

