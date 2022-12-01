/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.network.protocol.game;

import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;

public class ClientboundSoundEntityPacket
implements Packet<ClientGamePacketListener> {
    private final Holder<SoundEvent> sound;
    private final SoundSource source;
    private final int id;
    private final float volume;
    private final float pitch;
    private final long seed;

    public ClientboundSoundEntityPacket(Holder<SoundEvent> holder, SoundSource soundSource, Entity entity, float f, float g, long l) {
        this.sound = holder;
        this.source = soundSource;
        this.id = entity.getId();
        this.volume = f;
        this.pitch = g;
        this.seed = l;
    }

    public ClientboundSoundEntityPacket(FriendlyByteBuf friendlyByteBuf) {
        this.sound = friendlyByteBuf.readById(BuiltInRegistries.SOUND_EVENT.asHolderIdMap(), SoundEvent::readFromNetwork);
        this.source = friendlyByteBuf.readEnum(SoundSource.class);
        this.id = friendlyByteBuf.readVarInt();
        this.volume = friendlyByteBuf.readFloat();
        this.pitch = friendlyByteBuf.readFloat();
        this.seed = friendlyByteBuf.readLong();
    }

    @Override
    public void write(FriendlyByteBuf friendlyByteBuf2) {
        friendlyByteBuf2.writeId(BuiltInRegistries.SOUND_EVENT.asHolderIdMap(), this.sound, (friendlyByteBuf, soundEvent) -> soundEvent.writeToNetwork((FriendlyByteBuf)friendlyByteBuf));
        friendlyByteBuf2.writeEnum(this.source);
        friendlyByteBuf2.writeVarInt(this.id);
        friendlyByteBuf2.writeFloat(this.volume);
        friendlyByteBuf2.writeFloat(this.pitch);
        friendlyByteBuf2.writeLong(this.seed);
    }

    public Holder<SoundEvent> getSound() {
        return this.sound;
    }

    public SoundSource getSource() {
        return this.source;
    }

    public int getId() {
        return this.id;
    }

    public float getVolume() {
        return this.volume;
    }

    public float getPitch() {
        return this.pitch;
    }

    public long getSeed() {
        return this.seed;
    }

    @Override
    public void handle(ClientGamePacketListener clientGamePacketListener) {
        clientGamePacketListener.handleSoundEntityEvent(this);
    }
}

