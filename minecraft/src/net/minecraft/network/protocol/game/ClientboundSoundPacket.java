package net.minecraft.network.protocol.game;

import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;

public class ClientboundSoundPacket implements Packet<ClientGamePacketListener> {
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
	public void write(FriendlyByteBuf friendlyByteBuf) {
		friendlyByteBuf.writeId(
			BuiltInRegistries.SOUND_EVENT.asHolderIdMap(), this.sound, (friendlyByteBufx, soundEvent) -> soundEvent.writeToNetwork(friendlyByteBufx)
		);
		friendlyByteBuf.writeEnum(this.source);
		friendlyByteBuf.writeInt(this.x);
		friendlyByteBuf.writeInt(this.y);
		friendlyByteBuf.writeInt(this.z);
		friendlyByteBuf.writeFloat(this.volume);
		friendlyByteBuf.writeFloat(this.pitch);
		friendlyByteBuf.writeLong(this.seed);
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

	public void handle(ClientGamePacketListener clientGamePacketListener) {
		clientGamePacketListener.handleSoundEvent(this);
	}
}
