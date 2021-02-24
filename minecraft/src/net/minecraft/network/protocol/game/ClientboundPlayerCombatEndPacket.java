package net.minecraft.network.protocol.game;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.world.damagesource.CombatTracker;

public class ClientboundPlayerCombatEndPacket implements Packet<ClientGamePacketListener> {
	private final int killerId;
	private final int duration;

	public ClientboundPlayerCombatEndPacket(CombatTracker combatTracker) {
		this(combatTracker.getKillerId(), combatTracker.getCombatDuration());
	}

	public ClientboundPlayerCombatEndPacket(int i, int j) {
		this.killerId = i;
		this.duration = j;
	}

	public ClientboundPlayerCombatEndPacket(FriendlyByteBuf friendlyByteBuf) {
		this.duration = friendlyByteBuf.readVarInt();
		this.killerId = friendlyByteBuf.readInt();
	}

	@Override
	public void write(FriendlyByteBuf friendlyByteBuf) {
		friendlyByteBuf.writeVarInt(this.duration);
		friendlyByteBuf.writeInt(this.killerId);
	}

	public void handle(ClientGamePacketListener clientGamePacketListener) {
		clientGamePacketListener.handlePlayerCombatEnd(this);
	}
}
