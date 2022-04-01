package net.minecraft.network.protocol.game;

import net.minecraft.core.Registry;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import org.apache.commons.lang3.Validate;

public class ClientboundSoundEntityPacket implements Packet<ClientGamePacketListener> {
	private final SoundEvent sound;
	private final SoundSource source;
	private final int id;
	private final float volume;
	private final float pitch;

	public ClientboundSoundEntityPacket(SoundEvent soundEvent, SoundSource soundSource, Entity entity, float f, float g) {
		Validate.notNull(soundEvent, "sound");
		this.sound = soundEvent;
		this.source = soundSource;
		this.id = entity.getId();
		this.volume = f;
		this.pitch = g;
	}

	public ClientboundSoundEntityPacket(FriendlyByteBuf friendlyByteBuf) {
		this.sound = Registry.SOUND_EVENT.byId(friendlyByteBuf.readVarInt());
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

	public SoundEvent getSound() {
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

	public void handle(ClientGamePacketListener clientGamePacketListener) {
		clientGamePacketListener.handleSoundEntityEvent(this);
	}
}
