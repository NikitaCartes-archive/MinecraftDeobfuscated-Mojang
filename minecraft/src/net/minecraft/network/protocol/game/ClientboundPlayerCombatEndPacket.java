package net.minecraft.network.protocol.game;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;
import net.minecraft.world.damagesource.CombatTracker;

public class ClientboundPlayerCombatEndPacket implements Packet<ClientGamePacketListener> {
	public static final StreamCodec<FriendlyByteBuf, ClientboundPlayerCombatEndPacket> STREAM_CODEC = Packet.codec(
		ClientboundPlayerCombatEndPacket::write, ClientboundPlayerCombatEndPacket::new
	);
	private final int duration;

	public ClientboundPlayerCombatEndPacket(CombatTracker combatTracker) {
		this(combatTracker.getCombatDuration());
	}

	public ClientboundPlayerCombatEndPacket(int i) {
		this.duration = i;
	}

	private ClientboundPlayerCombatEndPacket(FriendlyByteBuf friendlyByteBuf) {
		this.duration = friendlyByteBuf.readVarInt();
	}

	private void write(FriendlyByteBuf friendlyByteBuf) {
		friendlyByteBuf.writeVarInt(this.duration);
	}

	@Override
	public PacketType<ClientboundPlayerCombatEndPacket> type() {
		return GamePacketTypes.CLIENTBOUND_PLAYER_COMBAT_END;
	}

	public void handle(ClientGamePacketListener clientGamePacketListener) {
		clientGamePacketListener.handlePlayerCombatEnd(this);
	}
}
