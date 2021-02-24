package net.minecraft.network.protocol.game;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;

public class ClientboundOpenSignEditorPacket implements Packet<ClientGamePacketListener> {
	private final BlockPos pos;

	public ClientboundOpenSignEditorPacket(BlockPos blockPos) {
		this.pos = blockPos;
	}

	public ClientboundOpenSignEditorPacket(FriendlyByteBuf friendlyByteBuf) {
		this.pos = friendlyByteBuf.readBlockPos();
	}

	@Override
	public void write(FriendlyByteBuf friendlyByteBuf) {
		friendlyByteBuf.writeBlockPos(this.pos);
	}

	public void handle(ClientGamePacketListener clientGamePacketListener) {
		clientGamePacketListener.handleOpenSignEditor(this);
	}

	@Environment(EnvType.CLIENT)
	public BlockPos getPos() {
		return this.pos;
	}
}
