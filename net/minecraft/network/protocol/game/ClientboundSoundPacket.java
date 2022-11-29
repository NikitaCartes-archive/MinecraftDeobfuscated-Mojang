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

public class ClientboundSoundPacket
implements Packet<ClientGamePacketListener> {
    public static final float LOCATION_ACCURACY = 8.0f;
    private final Holder<SoundEvent> sound;
    private final SoundSource source;
    private final int x;
    private final int y;
    private final int z;
    private final float volume;
    private final float pitch;
    private final long seed;

    public ClientboundSoundPacket(Holder<SoundEvent> holder, SoundSource soundSource, double d, double e, double f, float g, float h, long l) {
        this.sound = holder;
        this.source = soundSource;
        this.x = (int)(d * 8.0);
        this.y = (int)(e * 8.0);
        this.z = (int)(f * 8.0);
        this.volume = g;
        this.pitch = h;
        this.seed = l;
    }

    public ClientboundSoundPacket(FriendlyByteBuf friendlyByteBuf) {
        this.sound = friendlyByteBuf.readById(BuiltInRegistries.SOUND_EVENT.asHolderIdMap(), SoundEvent::readFromNetwork);
        this.source = friendlyByteBuf.readEnum(SoundSource.class);
        this.x = friendlyByteBuf.readInt();
        this.y = friendlyByteBuf.readInt();
        this.z = friendlyByteBuf.readInt();
        this.volume = friendlyByteBuf.readFloat();
        this.pitch = friendlyByteBuf.readFloat();
        this.seed = friendlyByteBuf.readLong();
    }

    @Override
    public void write(FriendlyByteBuf friendlyByteBuf2) {
        friendlyByteBuf2.writeId(BuiltInRegistries.SOUND_EVENT.asHolderIdMap(), this.sound, (friendlyByteBuf, soundEvent) -> soundEvent.writeToNetwork((FriendlyByteBuf)friendlyByteBuf));
        friendlyByteBuf2.writeEnum(this.source);
        friendlyByteBuf2.writeInt(this.x);
        friendlyByteBuf2.writeInt(this.y);
        friendlyByteBuf2.writeInt(this.z);
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

    public double getX() {
        return (float)this.x / 8.0f;
    }

    public double getY() {
        return (float)this.y / 8.0f;
    }

    public double getZ() {
        return (float)this.z / 8.0f;
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
        clientGamePacketListener.handleSoundEvent(this);
    }
}

