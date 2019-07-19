package net.minecraft.network.protocol.game;

import java.io.IOException;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;

public class ClientboundChunkBlocksUpdatePacket implements Packet<ClientGamePacketListener> {
	private ChunkPos chunkPos;
	private ClientboundChunkBlocksUpdatePacket.BlockUpdate[] updates;

	public ClientboundChunkBlocksUpdatePacket() {
	}

	public ClientboundChunkBlocksUpdatePacket(int i, short[] ss, LevelChunk levelChunk) {
		this.chunkPos = levelChunk.getPos();
		this.updates = new ClientboundChunkBlocksUpdatePacket.BlockUpdate[i];

		for (int j = 0; j < this.updates.length; j++) {
			this.updates[j] = new ClientboundChunkBlocksUpdatePacket.BlockUpdate(ss[j], levelChunk);
		}
	}

	@Override
	public void read(FriendlyByteBuf friendlyByteBuf) throws IOException {
		this.chunkPos = new ChunkPos(friendlyByteBuf.readInt(), friendlyByteBuf.readInt());
		this.updates = new ClientboundChunkBlocksUpdatePacket.BlockUpdate[friendlyByteBuf.readVarInt()];

		for (int i = 0; i < this.updates.length; i++) {
			this.updates[i] = new ClientboundChunkBlocksUpdatePacket.BlockUpdate(
				friendlyByteBuf.readShort(), Block.BLOCK_STATE_REGISTRY.byId(friendlyByteBuf.readVarInt())
			);
		}
	}

	@Override
	public void write(FriendlyByteBuf friendlyByteBuf) throws IOException {
		friendlyByteBuf.writeInt(this.chunkPos.x);
		friendlyByteBuf.writeInt(this.chunkPos.z);
		friendlyByteBuf.writeVarInt(this.updates.length);

		for (ClientboundChunkBlocksUpdatePacket.BlockUpdate blockUpdate : this.updates) {
			friendlyByteBuf.writeShort(blockUpdate.getOffset());
			friendlyByteBuf.writeVarInt(Block.getId(blockUpdate.getBlock()));
		}
	}

	public void handle(ClientGamePacketListener clientGamePacketListener) {
		clientGamePacketListener.handleChunkBlocksUpdate(this);
	}

	@Environment(EnvType.CLIENT)
	public ClientboundChunkBlocksUpdatePacket.BlockUpdate[] getUpdates() {
		return this.updates;
	}

	public class BlockUpdate {
		private final short offset;
		private final BlockState block;

		public BlockUpdate(short s, BlockState blockState) {
			this.offset = s;
			this.block = blockState;
		}

		public BlockUpdate(short s, LevelChunk levelChunk) {
			this.offset = s;
			this.block = levelChunk.getBlockState(this.getPos());
		}

		public BlockPos getPos() {
			return new BlockPos(ClientboundChunkBlocksUpdatePacket.this.chunkPos.getBlockAt(this.offset >> 12 & 15, this.offset & 255, this.offset >> 8 & 15));
		}

		public short getOffset() {
			return this.offset;
		}

		public BlockState getBlock() {
			return this.block;
		}
	}
}
