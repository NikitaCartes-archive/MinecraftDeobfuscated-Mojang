package net.minecraft.network.protocol.common;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.server.level.ClientInformation;

public record ServerboundClientInformationPacket(ClientInformation information) implements Packet<ServerCommonPacketListener> {
	public ServerboundClientInformationPacket(FriendlyByteBuf friendlyByteBuf) {
		this(new ClientInformation(friendlyByteBuf));
	}

	@Override
	public void write(FriendlyByteBuf friendlyByteBuf) {
		this.information.write(friendlyByteBuf);
	}

	public void handle(ServerCommonPacketListener serverCommonPacketListener) {
		serverCommonPacketListener.handleClientInformation(this);
	}
}
