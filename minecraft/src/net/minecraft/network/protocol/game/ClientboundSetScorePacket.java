package net.minecraft.network.protocol.game;

import java.util.Optional;
import javax.annotation.Nullable;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.numbers.NumberFormat;
import net.minecraft.network.chat.numbers.NumberFormatTypes;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;

public record ClientboundSetScorePacket(String owner, String objectiveName, int score, @Nullable Component display, Optional<NumberFormat> numberFormat)
	implements Packet<ClientGamePacketListener> {
	public static final StreamCodec<RegistryFriendlyByteBuf, ClientboundSetScorePacket> STREAM_CODEC = Packet.codec(
		ClientboundSetScorePacket::write, ClientboundSetScorePacket::new
	);

	private ClientboundSetScorePacket(RegistryFriendlyByteBuf registryFriendlyByteBuf) {
		this(
			registryFriendlyByteBuf.readUtf(),
			registryFriendlyByteBuf.readUtf(),
			registryFriendlyByteBuf.readVarInt(),
			registryFriendlyByteBuf.readNullable(FriendlyByteBuf::readComponentTrusted),
			NumberFormatTypes.OPTIONAL_STREAM_CODEC.decode(registryFriendlyByteBuf)
		);
	}

	private void write(RegistryFriendlyByteBuf registryFriendlyByteBuf) {
		registryFriendlyByteBuf.writeUtf(this.owner);
		registryFriendlyByteBuf.writeUtf(this.objectiveName);
		registryFriendlyByteBuf.writeVarInt(this.score);
		registryFriendlyByteBuf.writeNullable(this.display, FriendlyByteBuf::writeComponent);
		NumberFormatTypes.OPTIONAL_STREAM_CODEC.encode(registryFriendlyByteBuf, this.numberFormat);
	}

	@Override
	public PacketType<ClientboundSetScorePacket> type() {
		return GamePacketTypes.CLIENTBOUND_SET_SCORE;
	}

	public void handle(ClientGamePacketListener clientGamePacketListener) {
		clientGamePacketListener.handleSetScore(this);
	}
}
