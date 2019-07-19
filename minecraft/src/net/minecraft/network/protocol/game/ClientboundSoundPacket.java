package net.minecraft.network.protocol.game;

import java.io.IOException;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.core.Registry;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import org.apache.commons.lang3.Validate;

public class ClientboundSoundPacket implements Packet<ClientGamePacketListener> {
	private SoundEvent sound;
	private SoundSource source;
	private int x;
	private int y;
	private int z;
	private float volume;
	private float pitch;

	public ClientboundSoundPacket() {
	}

	public ClientboundSoundPacket(SoundEvent soundEvent, SoundSource soundSource, double d, double e, double f, float g, float h) {
		Validate.notNull(soundEvent, "sound");
		this.sound = soundEvent;
		this.source = soundSource;
		this.x = (int)(d * 8.0);
		this.y = (int)(e * 8.0);
		this.z = (int)(f * 8.0);
		this.volume = g;
		this.pitch = h;
	}

	@Override
	public void read(FriendlyByteBuf friendlyByteBuf) throws IOException {
		this.sound = Registry.SOUND_EVENT.byId(friendlyByteBuf.readVarInt());
		this.source = friendlyByteBuf.readEnum(SoundSource.class);
		this.x = friendlyByteBuf.readInt();
		this.y = friendlyByteBuf.readInt();
		this.z = friendlyByteBuf.readInt();
		this.volume = friendlyByteBuf.readFloat();
		this.pitch = friendlyByteBuf.readFloat();
	}

	@Override
	public void write(FriendlyByteBuf friendlyByteBuf) throws IOException {
		friendlyByteBuf.writeVarInt(Registry.SOUND_EVENT.getId(this.sound));
		friendlyByteBuf.writeEnum(this.source);
		friendlyByteBuf.writeInt(this.x);
		friendlyByteBuf.writeInt(this.y);
		friendlyByteBuf.writeInt(this.z);
		friendlyByteBuf.writeFloat(this.volume);
		friendlyByteBuf.writeFloat(this.pitch);
	}

	@Environment(EnvType.CLIENT)
	public SoundEvent getSound() {
		return this.sound;
	}

	@Environment(EnvType.CLIENT)
	public SoundSource getSource() {
		return this.source;
	}

	@Environment(EnvType.CLIENT)
	public double getX() {
		return (double)((float)this.x / 8.0F);
	}

	@Environment(EnvType.CLIENT)
	public double getY() {
		return (double)((float)this.y / 8.0F);
	}

	@Environment(EnvType.CLIENT)
	public double getZ() {
		return (double)((float)this.z / 8.0F);
	}

	@Environment(EnvType.CLIENT)
	public float getVolume() {
		return this.volume;
	}

	@Environment(EnvType.CLIENT)
	public float getPitch() {
		return this.pitch;
	}

	public void handle(ClientGamePacketListener clientGamePacketListener) {
		clientGamePacketListener.handleSoundEvent(this);
	}
}
