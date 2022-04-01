package net.minecraft.network.protocol.game;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public class ClientboundBlockUpdatePacket implements Packet<ClientGamePacketListener> {
	private final BlockPos pos;
	private final BlockState blockState;

	public ClientboundBlockUpdatePacket(BlockPos blockPos, BlockState blockState) {
		this.pos = blockPos;
		this.blockState = blockState;
	}

	public ClientboundBlockUpdatePacket(BlockGetter blockGetter, BlockPos blockPos) {
		this(blockPos, blockGetter.getBlockState(blockPos));
	}

	public ClientboundBlockUpdatePacket(FriendlyByteBuf friendlyByteBuf) {
		this.pos = friendlyByteBuf.readBlockPos();
		this.blockState = Block.BLOCK_STATE_REGISTRY.byId(friendlyByteBuf.readVarInt());
	}

	@Override
	public void write(FriendlyByteBuf friendlyByteBuf) {
		friendlyByteBuf.writeBlockPos(this.pos);
		friendlyByteBuf.writeVarInt(Block.getId(this.blockState));
	}

	public void handle(ClientGamePacketListener clientGamePacketListener) {
		clientGamePacketListener.handleBlockUpdate(this);
	}

	public BlockState getBlockState() {
		return this.blockState;
	}

	public BlockPos getPos() {
		return this.pos;
	}
}
