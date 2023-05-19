package net.minecraft.network.protocol.game;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.world.damagesource.CombatTracker;

public class ClientboundPlayerCombatEndPacket implements Packet<ClientGamePacketListener> {
	private final int duration;

	public ClientboundPlayerCombatEndPacket(CombatTracker combatTracker) {
		this(combatTracker.getCombatDuration());
	}

	public ClientboundPlayerCombatEndPacket(int i) {
		this.duration = i;
	}

	public ClientboundPlayerCombatEndPacket(FriendlyByteBuf friendlyByteBuf) {
		this.duration = friendlyByteBuf.readVarInt();
	}

	@Override
	public void write(FriendlyByteBuf friendlyByteBuf) {
		friendlyByteBuf.writeVarInt(this.duration);
	}

	public void handle(ClientGamePacketListener clientGamePacketListener) {
		clientGamePacketListener.handlePlayerCombatEnd(this);
	}
}
