package net.minecraft.network.protocol.game;

import javax.annotation.Nullable;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.numbers.NumberFormat;
import net.minecraft.network.chat.numbers.NumberFormatTypes;
import net.minecraft.network.protocol.Packet;

public record ClientboundSetScorePacket(String owner, String objectiveName, int score, @Nullable Component display, @Nullable NumberFormat numberFormat)
	implements Packet<ClientGamePacketListener> {
	public ClientboundSetScorePacket(FriendlyByteBuf friendlyByteBuf) {
		this(
			friendlyByteBuf.readUtf(),
			friendlyByteBuf.readUtf(),
			friendlyByteBuf.readVarInt(),
			friendlyByteBuf.readNullable(FriendlyByteBuf::readComponentTrusted),
			friendlyByteBuf.readNullable(NumberFormatTypes::readFromStream)
		);
	}

	@Override
	public void write(FriendlyByteBuf friendlyByteBuf) {
		friendlyByteBuf.writeUtf(this.owner);
		friendlyByteBuf.writeUtf(this.objectiveName);
		friendlyByteBuf.writeVarInt(this.score);
		friendlyByteBuf.writeNullable(this.display, FriendlyByteBuf::writeComponent);
		friendlyByteBuf.writeNullable(this.numberFormat, NumberFormatTypes::writeToStream);
	}

	public void handle(ClientGamePacketListener clientGamePacketListener) {
		clientGamePacketListener.handleSetScore(this);
	}
}
