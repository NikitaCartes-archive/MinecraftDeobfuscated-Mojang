package net.minecraft.network.protocol.game;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;

public class ClientboundPlayerCombatKillPacket implements Packet<ClientGamePacketListener> {
	public static final StreamCodec<FriendlyByteBuf, ClientboundPlayerCombatKillPacket> STREAM_CODEC = Packet.codec(
		ClientboundPlayerCombatKillPacket::write, ClientboundPlayerCombatKillPacket::new
	);
	private final int playerId;
	private final Component message;

	public ClientboundPlayerCombatKillPacket(int i, Component component) {
		this.playerId = i;
		this.message = component;
	}

	private ClientboundPlayerCombatKillPacket(FriendlyByteBuf friendlyByteBuf) {
		this.playerId = friendlyByteBuf.readVarInt();
		this.message = friendlyByteBuf.readComponentTrusted();
	}

	private void write(FriendlyByteBuf friendlyByteBuf) {
		friendlyByteBuf.writeVarInt(this.playerId);
		friendlyByteBuf.writeComponent(this.message);
	}

	@Override
	public PacketType<ClientboundPlayerCombatKillPacket> type() {
		return GamePacketTypes.CLIENTBOUND_PLAYER_COMBAT_KILL;
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
