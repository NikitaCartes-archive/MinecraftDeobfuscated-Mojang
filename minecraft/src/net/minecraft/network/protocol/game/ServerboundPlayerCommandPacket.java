package net.minecraft.network.protocol.game;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;
import net.minecraft.world.entity.Entity;

public class ServerboundPlayerCommandPacket implements Packet<ServerGamePacketListener> {
	public static final StreamCodec<FriendlyByteBuf, ServerboundPlayerCommandPacket> STREAM_CODEC = Packet.codec(
		ServerboundPlayerCommandPacket::write, ServerboundPlayerCommandPacket::new
	);
	private final int id;
	private final ServerboundPlayerCommandPacket.Action action;
	private final int data;

	public ServerboundPlayerCommandPacket(Entity entity, ServerboundPlayerCommandPacket.Action action) {
		this(entity, action, 0);
	}

	public ServerboundPlayerCommandPacket(Entity entity, ServerboundPlayerCommandPacket.Action action, int i) {
		this.id = entity.getId();
		this.action = action;
		this.data = i;
	}

	private ServerboundPlayerCommandPacket(FriendlyByteBuf friendlyByteBuf) {
		this.id = friendlyByteBuf.readVarInt();
		this.action = friendlyByteBuf.readEnum(ServerboundPlayerCommandPacket.Action.class);
		this.data = friendlyByteBuf.readVarInt();
	}

	private void write(FriendlyByteBuf friendlyByteBuf) {
		friendlyByteBuf.writeVarInt(this.id);
		friendlyByteBuf.writeEnum(this.action);
		friendlyByteBuf.writeVarInt(this.data);
	}

	@Override
	public PacketType<ServerboundPlayerCommandPacket> type() {
		return GamePacketTypes.SERVERBOUND_PLAYER_COMMAND;
	}

	public void handle(ServerGamePacketListener serverGamePacketListener) {
		serverGamePacketListener.handlePlayerCommand(this);
	}

	public int getId() {
		return this.id;
	}

	public ServerboundPlayerCommandPacket.Action getAction() {
		return this.action;
	}

	public int getData() {
		return this.data;
	}

	public static enum Action {
		PRESS_SHIFT_KEY,
		RELEASE_SHIFT_KEY,
		STOP_SLEEPING,
		START_SPRINTING,
		STOP_SPRINTING,
		START_RIDING_JUMP,
		STOP_RIDING_JUMP,
		OPEN_INVENTORY,
		START_FALL_FLYING;
	}
}
