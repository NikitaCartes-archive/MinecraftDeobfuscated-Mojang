/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.network.protocol.game;

import net.minecraft.core.Registry;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import org.apache.commons.lang3.Validate;

public class ClientboundSoundPacket
implements Packet<ClientGamePacketListener> {
    public static final float LOCATION_ACCURACY = 8.0f;
    private final SoundEvent sound;
    private final SoundSource source;
    private final int x;
    private final int y;
    private final int z;
    private final float volume;
    private final float pitch;

    public ClientboundSoundPacket(SoundEvent soundEvent, SoundSource soundSource, double d, double e, double f, float g, float h) {
        Validate.notNull(soundEvent, "sound", new Object[0]);
        this.sound = soundEvent;
        this.source = soundSource;
        this.x = (int)(d * 8.0);
        this.y = (int)(e * 8.0);
        this.z = (int)(f * 8.0);
        this.volume = g;
        this.pitch = h;
    }

    public ClientboundSoundPacket(FriendlyByteBuf friendlyByteBuf) {
        this.sound = (SoundEvent)Registry.SOUND_EVENT.byId(friendlyByteBuf.readVarInt());
        this.source = friendlyByteBuf.readEnum(SoundSource.class);
        this.x = friendlyByteBuf.readInt();
        this.y = friendlyByteBuf.readInt();
        this.z = friendlyByteBuf.readInt();
        this.volume = friendlyByteBuf.readFloat();
        this.pitch = friendlyByteBuf.readFloat();
    }

    @Override
    public void write(FriendlyByteBuf friendlyByteBuf) {
        friendlyByteBuf.writeVarInt(Registry.SOUND_EVENT.getId(this.sound));
        friendlyByteBuf.writeEnum(this.source);
        friendlyByteBuf.writeInt(this.x);
        friendlyByteBuf.writeInt(this.y);
        friendlyByteBuf.writeInt(this.z);
        friendlyByteBuf.writeFloat(this.volume);
        friendlyByteBuf.writeFloat(this.pitch);
    }

    public SoundEvent getSound() {
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

    @Override
    public void handle(ClientGamePacketListener clientGamePacketListener) {
        clientGamePacketListener.handleSoundEvent(this);
    }
}

