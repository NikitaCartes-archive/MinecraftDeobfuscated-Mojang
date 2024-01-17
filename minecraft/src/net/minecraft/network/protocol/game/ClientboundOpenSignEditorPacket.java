package net.minecraft.network.protocol.game;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;

public class ClientboundOpenSignEditorPacket implements Packet<ClientGamePacketListener> {
	public static final StreamCodec<FriendlyByteBuf, ClientboundOpenSignEditorPacket> STREAM_CODEC = Packet.codec(
		ClientboundOpenSignEditorPacket::write, ClientboundOpenSignEditorPacket::new
	);
	private final BlockPos pos;
	private final boolean isFrontText;

	public ClientboundOpenSignEditorPacket(BlockPos blockPos, boolean bl) {
		this.pos = blockPos;
		this.isFrontText = bl;
	}

	private ClientboundOpenSignEditorPacket(FriendlyByteBuf friendlyByteBuf) {
		this.pos = friendlyByteBuf.readBlockPos();
		this.isFrontText = friendlyByteBuf.readBoolean();
	}

	private void write(FriendlyByteBuf friendlyByteBuf) {
		friendlyByteBuf.writeBlockPos(this.pos);
		friendlyByteBuf.writeBoolean(this.isFrontText);
	}

	@Override
	public PacketType<ClientboundOpenSignEditorPacket> type() {
		return GamePacketTypes.CLIENTBOUND_OPEN_SIGN_EDITOR;
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
