package net.minecraft.network.protocol.game;

import java.util.Optional;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.ChatType;
import net.minecraft.network.chat.PlayerChatMessage;
import net.minecraft.network.protocol.Packet;

public record ClientboundPlayerChatPacket(PlayerChatMessage message, ChatType.BoundNetwork chatType) implements Packet<ClientGamePacketListener> {
	public ClientboundPlayerChatPacket(FriendlyByteBuf friendlyByteBuf) {
		this(new PlayerChatMessage(friendlyByteBuf), new ChatType.BoundNetwork(friendlyByteBuf));
	}

	@Override
	public void write(FriendlyByteBuf friendlyByteBuf) {
		this.message.write(friendlyByteBuf);
		this.chatType.write(friendlyByteBuf);
	}

	public void handle(ClientGamePacketListener clientGamePacketListener) {
		clientGamePacketListener.handlePlayerChat(this);
	}

	@Override
	public boolean isSkippable() {
		return true;
	}

	public Optional<ChatType.Bound> resolveChatType(RegistryAccess registryAccess) {
		return this.chatType.resolve(registryAccess);
	}
}
