package net.minecraft.network.protocol.game;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.world.damagesource.CombatTracker;

public class ClientboundPlayerCombatKillPacket implements Packet<ClientGamePacketListener> {
	private final int playerId;
	private final int killerId;
	private final Component message;

	public ClientboundPlayerCombatKillPacket(CombatTracker combatTracker, Component component) {
		this(combatTracker.getMob().getId(), combatTracker.getKillerId(), component);
	}

	public ClientboundPlayerCombatKillPacket(int i, int j, Component component) {
		this.playerId = i;
		this.killerId = j;
		this.message = component;
	}

	public ClientboundPlayerCombatKillPacket(FriendlyByteBuf friendlyByteBuf) {
		this.playerId = friendlyByteBuf.readVarInt();
		this.killerId = friendlyByteBuf.readInt();
		this.message = friendlyByteBuf.readComponent();
	}

	@Override
	public void write(FriendlyByteBuf friendlyByteBuf) {
		friendlyByteBuf.writeVarInt(this.playerId);
		friendlyByteBuf.writeInt(this.killerId);
		friendlyByteBuf.writeComponent(this.message);
	}

	public void handle(ClientGamePacketListener clientGamePacketListener) {
		clientGamePacketListener.handlePlayerCombatKill(this);
	}

	@Override
	public boolean isSkippable() {
		return true;
	}

	public int getKillerId() {
		return this.killerId;
	}

	public int getPlayerId() {
		return this.playerId;
	}

	public Component getMessage() {
		return this.message;
	}
}
