package net.minecraft.network.protocol.game;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import java.util.List;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.LevelChunkSection;

public record ClientboundChunksBiomesPacket(List<ClientboundChunksBiomesPacket.ChunkBiomeData> chunkBiomeData) implements Packet<ClientGamePacketListener> {
	private static final int TWO_MEGABYTES = 2097152;

	public ClientboundChunksBiomesPacket(FriendlyByteBuf friendlyByteBuf) {
		this(friendlyByteBuf.readList(ClientboundChunksBiomesPacket.ChunkBiomeData::new));
	}

	public static ClientboundChunksBiomesPacket forChunks(List<LevelChunk> list) {
		return new ClientboundChunksBiomesPacket(list.stream().map(ClientboundChunksBiomesPacket.ChunkBiomeData::new).toList());
	}

	@Override
	public void write(FriendlyByteBuf friendlyByteBuf) {
		friendlyByteBuf.writeCollection(this.chunkBiomeData, (friendlyByteBufx, chunkBiomeData) -> chunkBiomeData.write(friendlyByteBufx));
	}

	public void handle(ClientGamePacketListener clientGamePacketListener) {
		clientGamePacketListener.handleChunksBiomes(this);
	}

	public static record ChunkBiomeData(ChunkPos pos, byte[] buffer) {
		public ChunkBiomeData(LevelChunk levelChunk) {
			this(levelChunk.getPos(), new byte[calculateChunkSize(levelChunk)]);
			extractChunkData(new FriendlyByteBuf(this.getWriteBuffer()), levelChunk);
		}

		public ChunkBiomeData(FriendlyByteBuf friendlyByteBuf) {
			this(friendlyByteBuf.readChunkPos(), friendlyByteBuf.readByteArray(2097152));
		}

		private static int calculateChunkSize(LevelChunk levelChunk) {
			int i = 0;

			for (LevelChunkSection levelChunkSection : levelChunk.getSections()) {
				i += levelChunkSection.getBiomes().getSerializedSize();
			}

			return i;
		}

		public FriendlyByteBuf getReadBuffer() {
			return new FriendlyByteBuf(Unpooled.wrappedBuffer(this.buffer));
		}

		private ByteBuf getWriteBuffer() {
			ByteBuf byteBuf = Unpooled.wrappedBuffer(this.buffer);
			byteBuf.writerIndex(0);
			return byteBuf;
		}

		public static void extractChunkData(FriendlyByteBuf friendlyByteBuf, LevelChunk levelChunk) {
			for (LevelChunkSection levelChunkSection : levelChunk.getSections()) {
				levelChunkSection.getBiomes().write(friendlyByteBuf);
			}
		}

		public void write(FriendlyByteBuf friendlyByteBuf) {
			friendlyByteBuf.writeChunkPos(this.pos);
			friendlyByteBuf.writeByteArray(this.buffer);
		}
	}
}
