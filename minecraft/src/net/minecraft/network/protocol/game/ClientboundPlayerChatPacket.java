package net.minecraft.network.protocol.game;

import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.ChatType;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FilterMask;
import net.minecraft.network.chat.MessageSignature;
import net.minecraft.network.chat.SignedMessageBody;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;

public record ClientboundPlayerChatPacket(
	UUID sender,
	int index,
	@Nullable MessageSignature signature,
	SignedMessageBody.Packed body,
	@Nullable Component unsignedContent,
	FilterMask filterMask,
	ChatType.BoundNetwork chatType
) implements Packet<ClientGamePacketListener> {
	public static final StreamCodec<FriendlyByteBuf, ClientboundPlayerChatPacket> STREAM_CODEC = Packet.codec(
		ClientboundPlayerChatPacket::write, ClientboundPlayerChatPacket::new
	);

	private ClientboundPlayerChatPacket(FriendlyByteBuf friendlyByteBuf) {
		this(
			friendlyByteBuf.readUUID(),
			friendlyByteBuf.readVarInt(),
			friendlyByteBuf.readNullable(MessageSignature::read),
			new SignedMessageBody.Packed(friendlyByteBuf),
			friendlyByteBuf.readNullable(FriendlyByteBuf::readComponentTrusted),
			FilterMask.read(friendlyByteBuf),
			new ChatType.BoundNetwork(friendlyByteBuf)
		);
	}

	private void write(FriendlyByteBuf friendlyByteBuf) {
		friendlyByteBuf.writeUUID(this.sender);
		friendlyByteBuf.writeVarInt(this.index);
		friendlyByteBuf.writeNullable(this.signature, MessageSignature::write);
		this.body.write(friendlyByteBuf);
		friendlyByteBuf.writeNullable(this.unsignedContent, FriendlyByteBuf::writeComponent);
		FilterMask.write(friendlyByteBuf, this.filterMask);
		this.chatType.write(friendlyByteBuf);
	}

	@Override
	public PacketType<ClientboundPlayerChatPacket> type() {
		return GamePacketTypes.CLIENTBOUND_PLAYER_CHAT;
	}

	public void handle(ClientGamePacketListener clientGamePacketListener) {
		clientGamePacketListener.handlePlayerChat(this);
	}

	@Override
	public boolean isSkippable() {
		return true;
	}
}
