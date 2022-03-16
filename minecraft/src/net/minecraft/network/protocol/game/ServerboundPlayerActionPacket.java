package net.minecraft.network.protocol.game;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;

public class ServerboundPlayerActionPacket implements Packet<ServerGamePacketListener> {
	private final BlockPos pos;
	private final Direction direction;
	private final ServerboundPlayerActionPacket.Action action;
	private final int sequence;

	public ServerboundPlayerActionPacket(ServerboundPlayerActionPacket.Action action, BlockPos blockPos, Direction direction, int i) {
		this.action = action;
		this.pos = blockPos.immutable();
		this.direction = direction;
		this.sequence = i;
	}

	public ServerboundPlayerActionPacket(ServerboundPlayerActionPacket.Action action, BlockPos blockPos, Direction direction) {
		this(action, blockPos, direction, 0);
	}

	public ServerboundPlayerActionPacket(FriendlyByteBuf friendlyByteBuf) {
		this.action = friendlyByteBuf.readEnum(ServerboundPlayerActionPacket.Action.class);
		this.pos = friendlyByteBuf.readBlockPos();
		this.direction = Direction.from3DDataValue(friendlyByteBuf.readUnsignedByte());
		this.sequence = friendlyByteBuf.readVarInt();
	}

	@Override
	public void write(FriendlyByteBuf friendlyByteBuf) {
		friendlyByteBuf.writeEnum(this.action);
		friendlyByteBuf.writeBlockPos(this.pos);
		friendlyByteBuf.writeByte(this.direction.get3DDataValue());
		friendlyByteBuf.writeVarInt(this.sequence);
	}

	public void handle(ServerGamePacketListener serverGamePacketListener) {
		serverGamePacketListener.handlePlayerAction(this);
	}

	public BlockPos getPos() {
		return this.pos;
	}

	public Direction getDirection() {
		return this.direction;
	}

	public ServerboundPlayerActionPacket.Action getAction() {
		return this.action;
	}

	public int getSequence() {
		return this.sequence;
	}

	public static enum Action {
		START_DESTROY_BLOCK,
		ABORT_DESTROY_BLOCK,
		STOP_DESTROY_BLOCK,
		DROP_ALL_ITEMS,
		DROP_ITEM,
		RELEASE_USE_ITEM,
		SWAP_ITEM_WITH_OFFHAND;
	}
}
