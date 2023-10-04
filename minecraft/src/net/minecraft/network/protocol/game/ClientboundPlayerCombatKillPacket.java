package net.minecraft.network.protocol.game;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;

public class ClientboundPlayerCombatKillPacket implements Packet<ClientGamePacketListener> {
	private final int playerId;
	private final Component message;

	public ClientboundPlayerCombatKillPacket(int i, Component component) {
		this.playerId = i;
		this.message = component;
	}

	public ClientboundPlayerCombatKillPacket(FriendlyByteBuf friendlyByteBuf) {
		this.playerId = friendlyByteBuf.readVarInt();
		this.message = friendlyByteBuf.readComponentTrusted();
	}

	@Override
	public void write(FriendlyByteBuf friendlyByteBuf) {
		friendlyByteBuf.writeVarInt(this.playerId);
		friendlyByteBuf.writeComponent(this.message);
	}

	public void handle(ClientGamePacketListener clientGamePacketListener) {
		clientGamePacketListener.handlePlayerCombatKill(this);
	}

	@Override
	public boolean isSkippable() {
		return true;
	}

	public int getPlayerId() {
		return this.playerId;
	}

	public Component getMessage() {
		return this.message;
	}
}
