package net.minecraft.network.protocol.game;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;

public class ClientboundPlayerCombatEnterPacket implements Packet<ClientGamePacketListener> {
	public ClientboundPlayerCombatEnterPacket() {
	}

	public ClientboundPlayerCombatEnterPacket(FriendlyByteBuf friendlyByteBuf) {
	}

	@Override
	public void write(FriendlyByteBuf friendlyByteBuf) {
	}

	public void handle(ClientGamePacketListener clientGamePacketListener) {
		clientGamePacketListener.handlePlayerCombatEnter(this);
	}
}
