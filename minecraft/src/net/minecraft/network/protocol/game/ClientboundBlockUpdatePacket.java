package net.minecraft.network.protocol.game;

import java.io.IOException;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public class ClientboundBlockUpdatePacket implements Packet<ClientGamePacketListener> {
	private BlockPos pos;
	private BlockState blockState;

	public ClientboundBlockUpdatePacket() {
	}

	public ClientboundBlockUpdatePacket(BlockGetter blockGetter, BlockPos blockPos) {
		this.pos = blockPos;
		this.blockState = blockGetter.getBlockState(blockPos);
	}

	@Override
	public void read(FriendlyByteBuf friendlyByteBuf) throws IOException {
		this.pos = friendlyByteBuf.readBlockPos();
		this.blockState = Block.BLOCK_STATE_REGISTRY.byId(friendlyByteBuf.readVarInt());
	}

	@Override
	public void write(FriendlyByteBuf friendlyByteBuf) throws IOException {
		friendlyByteBuf.writeBlockPos(this.pos);
		friendlyByteBuf.writeVarInt(Block.getId(this.blockState));
	}

	public void handle(ClientGamePacketListener clientGamePacketListener) {
		clientGamePacketListener.handleBlockUpdate(this);
	}

	@Environment(EnvType.CLIENT)
	public BlockState getBlockState() {
		return this.blockState;
	}

	@Environment(EnvType.CLIENT)
	public BlockPos getPos() {
		return this.pos;
	}
}
