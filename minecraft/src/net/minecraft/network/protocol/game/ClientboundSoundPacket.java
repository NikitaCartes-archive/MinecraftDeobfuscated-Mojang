package net.minecraft.network.protocol.game;

import net.minecraft.core.Holder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;

public class ClientboundSoundPacket implements Packet<ClientGamePacketListener> {
	public static final StreamCodec<RegistryFriendlyByteBuf, ClientboundSoundPacket> STREAM_CODEC = Packet.codec(
		ClientboundSoundPacket::write, ClientboundSoundPacket::new
	);
	public static final float LOCATION_ACCURACY = 8.0F;
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

	private ClientboundSoundPacket(RegistryFriendlyByteBuf registryFriendlyByteBuf) {
		this.sound = SoundEvent.STREAM_CODEC.decode(registryFriendlyByteBuf);
		this.source = registryFriendlyByteBuf.readEnum(SoundSource.class);
		this.x = registryFriendlyByteBuf.readInt();
		this.y = registryFriendlyByteBuf.readInt();
		this.z = registryFriendlyByteBuf.readInt();
		this.volume = registryFriendlyByteBuf.readFloat();
		this.pitch = registryFriendlyByteBuf.readFloat();
		this.seed = registryFriendlyByteBuf.readLong();
	}

	private void write(RegistryFriendlyByteBuf registryFriendlyByteBuf) {
		SoundEvent.STREAM_CODEC.encode(registryFriendlyByteBuf, this.sound);
		registryFriendlyByteBuf.writeEnum(this.source);
		registryFriendlyByteBuf.writeInt(this.x);
		registryFriendlyByteBuf.writeInt(this.y);
		registryFriendlyByteBuf.writeInt(this.z);
		registryFriendlyByteBuf.writeFloat(this.volume);
		registryFriendlyByteBuf.writeFloat(this.pitch);
		registryFriendlyByteBuf.writeLong(this.seed);
	}

	@Override
	public PacketType<ClientboundSoundPacket> type() {
		return GamePacketTypes.CLIENTBOUND_SOUND;
	}

	public void handle(ClientGamePacketListener clientGamePacketListener) {
		clientGamePacketListener.handleSoundEvent(this);
	}

	public Holder<SoundEvent> getSound() {
		return this.sound;
	}

	public SoundSource getSource() {
		return this.source;
	}

	public double getX() {
		return (double)((float)this.x / 8.0F);
	}

	public double getY() {
		return (double)((float)this.y / 8.0F);
	}

	public double getZ() {
		return (double)((float)this.z / 8.0F);
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
