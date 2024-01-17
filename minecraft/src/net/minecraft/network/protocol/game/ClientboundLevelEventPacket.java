package net.minecraft.network.protocol.game;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;

public class ClientboundLevelEventPacket implements Packet<ClientGamePacketListener> {
	public static final StreamCodec<FriendlyByteBuf, ClientboundLevelEventPacket> STREAM_CODEC = Packet.codec(
		ClientboundLevelEventPacket::write, ClientboundLevelEventPacket::new
	);
	private final int type;
	private final BlockPos pos;
	private final int data;
	private final boolean globalEvent;

	public ClientboundLevelEventPacket(int i, BlockPos blockPos, int j, boolean bl) {
		this.type = i;
		this.pos = blockPos.immutable();
		this.data = j;
		this.globalEvent = bl;
	}

	private ClientboundLevelEventPacket(FriendlyByteBuf friendlyByteBuf) {
		this.type = friendlyByteBuf.readInt();
		this.pos = friendlyByteBuf.readBlockPos();
		this.data = friendlyByteBuf.readInt();
		this.globalEvent = friendlyByteBuf.readBoolean();
	}

	private void write(FriendlyByteBuf friendlyByteBuf) {
		friendlyByteBuf.writeInt(this.type);
		friendlyByteBuf.writeBlockPos(this.pos);
		friendlyByteBuf.writeInt(this.data);
		friendlyByteBuf.writeBoolean(this.globalEvent);
	}

	@Override
	public PacketType<ClientboundLevelEventPacket> type() {
		return GamePacketTypes.CLIENTBOUND_LEVEL_EVENT;
	}

	public void handle(ClientGamePacketListener clientGamePacketListener) {
		clientGamePacketListener.handleLevelEvent(this);
	}

	public boolean isGlobalEvent() {
		return this.globalEvent;
	}

	public int getType() {
		return this.type;
	}

	public int getData() {
		return this.data;
	}

	public BlockPos getPos() {
		return this.pos;
	}
}
