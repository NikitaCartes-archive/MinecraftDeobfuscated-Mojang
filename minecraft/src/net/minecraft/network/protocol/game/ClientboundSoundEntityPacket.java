package net.minecraft.network.protocol.game;

import net.minecraft.core.Holder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;

public class ClientboundSoundEntityPacket implements Packet<ClientGamePacketListener> {
	public static final StreamCodec<RegistryFriendlyByteBuf, ClientboundSoundEntityPacket> STREAM_CODEC = Packet.codec(
		ClientboundSoundEntityPacket::write, ClientboundSoundEntityPacket::new
	);
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

	private ClientboundSoundEntityPacket(RegistryFriendlyByteBuf registryFriendlyByteBuf) {
		this.sound = SoundEvent.STREAM_CODEC.decode(registryFriendlyByteBuf);
		this.source = registryFriendlyByteBuf.readEnum(SoundSource.class);
		this.id = registryFriendlyByteBuf.readVarInt();
		this.volume = registryFriendlyByteBuf.readFloat();
		this.pitch = registryFriendlyByteBuf.readFloat();
		this.seed = registryFriendlyByteBuf.readLong();
	}

	private void write(RegistryFriendlyByteBuf registryFriendlyByteBuf) {
		SoundEvent.STREAM_CODEC.encode(registryFriendlyByteBuf, this.sound);
		registryFriendlyByteBuf.writeEnum(this.source);
		registryFriendlyByteBuf.writeVarInt(this.id);
		registryFriendlyByteBuf.writeFloat(this.volume);
		registryFriendlyByteBuf.writeFloat(this.pitch);
		registryFriendlyByteBuf.writeLong(this.seed);
	}

	@Override
	public PacketType<ClientboundSoundEntityPacket> type() {
		return GamePacketTypes.CLIENTBOUND_SOUND_ENTITY;
	}

	public void handle(ClientGamePacketListener clientGamePacketListener) {
		clientGamePacketListener.handleSoundEntityEvent(this);
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
}
