package net.minecraft.network.protocol.game;

import java.time.Instant;
import java.util.Objects;
import java.util.Optional;
import net.minecraft.core.Registry;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.ChatSender;
import net.minecraft.network.chat.ChatType;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MessageSignature;
import net.minecraft.network.chat.PlayerChatMessage;
import net.minecraft.network.protocol.Packet;
import net.minecraft.util.Crypt;

public record ClientboundPlayerChatPacket(
	Component signedContent, Optional<Component> unsignedContent, int typeId, ChatSender sender, Instant timeStamp, Crypt.SaltSignaturePair saltSignature
) implements Packet<ClientGamePacketListener> {
	public ClientboundPlayerChatPacket(FriendlyByteBuf friendlyByteBuf) {
		this(
			friendlyByteBuf.readComponent(),
			friendlyByteBuf.readOptional(FriendlyByteBuf::readComponent),
			friendlyByteBuf.readVarInt(),
			new ChatSender(friendlyByteBuf),
			friendlyByteBuf.readInstant(),
			new Crypt.SaltSignaturePair(friendlyByteBuf)
		);
	}

	public static ClientboundPlayerChatPacket system(Component component, int i, ChatSender chatSender, Instant instant) {
		return new ClientboundPlayerChatPacket(component, Optional.empty(), i, chatSender, instant, Crypt.SaltSignaturePair.EMPTY);
	}

	@Override
	public void write(FriendlyByteBuf friendlyByteBuf) {
		friendlyByteBuf.writeComponent(this.signedContent);
		friendlyByteBuf.writeOptional(this.unsignedContent, FriendlyByteBuf::writeComponent);
		friendlyByteBuf.writeVarInt(this.typeId);
		this.sender.write(friendlyByteBuf);
		friendlyByteBuf.writeInstant(this.timeStamp);
		Crypt.SaltSignaturePair.write(friendlyByteBuf, this.saltSignature);
	}

	public void handle(ClientGamePacketListener clientGamePacketListener) {
		clientGamePacketListener.handlePlayerChat(this);
	}

	@Override
	public boolean isSkippable() {
		return true;
	}

	public PlayerChatMessage getMessage() {
		MessageSignature messageSignature = new MessageSignature(this.sender.profileId(), this.timeStamp, this.saltSignature);
		return new PlayerChatMessage(this.signedContent, messageSignature, this.unsignedContent);
	}

	public ChatType resolveType(Registry<ChatType> registry) {
		return (ChatType)Objects.requireNonNull(registry.byId(this.typeId), "Invalid chat type");
	}
}
