package net.minecraft.network.protocol.game;

import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.ChatType;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FilterMask;
import net.minecraft.network.chat.MessageSignature;
import net.minecraft.network.chat.SignedMessageBody;
import net.minecraft.network.protocol.Packet;

public record ClientboundPlayerChatPacket(
	UUID sender,
	int index,
	@Nullable MessageSignature signature,
	SignedMessageBody.Packed body,
	@Nullable Component unsignedContent,
	FilterMask filterMask,
	ChatType.BoundNetwork chatType
) implements Packet<ClientGamePacketListener> {
	public ClientboundPlayerChatPacket(FriendlyByteBuf friendlyByteBuf) {
		this(
			friendlyByteBuf.readUUID(),
			friendlyByteBuf.readVarInt(),
			friendlyByteBuf.readNullable(MessageSignature::read),
			new SignedMessageBody.Packed(friendlyByteBuf),
			friendlyByteBuf.readNullable(FriendlyByteBuf::readComponent),
			FilterMask.read(friendlyByteBuf),
			new ChatType.BoundNetwork(friendlyByteBuf)
		);
	}

	@Override
	public void write(FriendlyByteBuf friendlyByteBuf) {
		friendlyByteBuf.writeUUID(this.sender);
		friendlyByteBuf.writeVarInt(this.index);
		friendlyByteBuf.writeNullable(this.signature, MessageSignature::write);
		this.body.write(friendlyByteBuf);
		friendlyByteBuf.writeNullable(this.unsignedContent, FriendlyByteBuf::writeComponent);
		FilterMask.write(friendlyByteBuf, this.filterMask);
		this.chatType.write(friendlyByteBuf);
	}

	public void handle(ClientGamePacketListener clientGamePacketListener) {
		clientGamePacketListener.handlePlayerChat(this);
	}

	@Override
	public boolean isSkippable() {
		return true;
	}
}
