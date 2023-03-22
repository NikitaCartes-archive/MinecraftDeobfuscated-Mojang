package net.minecraft.network.protocol.game;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;

public class ClientboundOpenSignEditorPacket implements Packet<ClientGamePacketListener> {
	private final BlockPos pos;
	private final boolean isFrontText;

	public ClientboundOpenSignEditorPacket(BlockPos blockPos, boolean bl) {
		this.pos = blockPos;
		this.isFrontText = bl;
	}

	public ClientboundOpenSignEditorPacket(FriendlyByteBuf friendlyByteBuf) {
		this.pos = friendlyByteBuf.readBlockPos();
		this.isFrontText = friendlyByteBuf.readBoolean();
	}

	@Override
	public void write(FriendlyByteBuf friendlyByteBuf) {
		friendlyByteBuf.writeBlockPos(this.pos);
		friendlyByteBuf.writeBoolean(this.isFrontText);
	}

	public void handle(ClientGamePacketListener clientGamePacketListener) {
		clientGamePacketListener.handleOpenSignEditor(this);
	}

	public BlockPos getPos() {
		return this.pos;
	}

	public boolean isFrontText() {
		return this.isFrontText;
	}
}
