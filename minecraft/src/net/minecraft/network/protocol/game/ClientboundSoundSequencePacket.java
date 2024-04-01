package net.minecraft.network.protocol.game;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;
import java.util.List;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;

public class ClientboundSoundSequencePacket implements Packet<ClientGamePacketListener> {
	public static final StreamCodec<RegistryFriendlyByteBuf, ClientboundSoundSequencePacket> STREAM_CODEC = Packet.codec(
		ClientboundSoundSequencePacket::write, ClientboundSoundSequencePacket::new
	);
	private final List<ClientboundSoundSequencePacket.DelayedSound> sounds;

	public ClientboundSoundSequencePacket(List<ClientboundSoundSequencePacket.DelayedSound> list) {
		this.sounds = list;
	}

	private ClientboundSoundSequencePacket(RegistryFriendlyByteBuf registryFriendlyByteBuf) {
		int i = registryFriendlyByteBuf.readInt();
		Builder<ClientboundSoundSequencePacket.DelayedSound> builder = ImmutableList.builder();

		for (int j = 0; j < i; j++) {
			builder.add(
				new ClientboundSoundSequencePacket.DelayedSound(registryFriendlyByteBuf.readInt(), ClientboundSoundPacket.STREAM_CODEC.decode(registryFriendlyByteBuf))
			);
		}

		this.sounds = builder.build();
	}

	private void write(RegistryFriendlyByteBuf registryFriendlyByteBuf) {
		registryFriendlyByteBuf.writeInt(this.sounds.size());

		for (ClientboundSoundSequencePacket.DelayedSound delayedSound : this.sounds) {
			registryFriendlyByteBuf.writeInt(delayedSound.ticks);
			ClientboundSoundPacket.STREAM_CODEC.encode(registryFriendlyByteBuf, delayedSound.packet);
		}
	}

	@Override
	public PacketType<ClientboundSoundSequencePacket> type() {
		return GamePacketTypes.CLIENTBOUND_SOUND_SEQUENCE;
	}

	public void handle(ClientGamePacketListener clientGamePacketListener) {
		clientGamePacketListener.handleSoundSequenceEvent(this);
	}

	public List<ClientboundSoundSequencePacket.DelayedSound> getSounds() {
		return this.sounds;
	}

	public static record DelayedSound(int ticks, ClientboundSoundPacket packet) {
	}
}
