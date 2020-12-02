package net.minecraft.network.protocol.game;

import com.google.common.collect.Lists;
import java.io.IOException;
import java.util.BitSet;
import java.util.List;
import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.core.SectionPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.chunk.DataLayer;
import net.minecraft.world.level.lighting.LevelLightEngine;

public class ClientboundLightUpdatePacket implements Packet<ClientGamePacketListener> {
	private int x;
	private int z;
	private BitSet skyYMask = new BitSet();
	private BitSet blockYMask = new BitSet();
	private BitSet emptySkyYMask = new BitSet();
	private BitSet emptyBlockYMask = new BitSet();
	private final List<byte[]> skyUpdates = Lists.<byte[]>newArrayList();
	private final List<byte[]> blockUpdates = Lists.<byte[]>newArrayList();
	private boolean trustEdges;

	public ClientboundLightUpdatePacket() {
	}

	public ClientboundLightUpdatePacket(ChunkPos chunkPos, LevelLightEngine levelLightEngine, @Nullable BitSet bitSet, @Nullable BitSet bitSet2, boolean bl) {
		this.x = chunkPos.x;
		this.z = chunkPos.z;
		this.trustEdges = bl;

		for (int i = 0; i < levelLightEngine.getLightSectionCount(); i++) {
			if (bitSet == null || bitSet.get(i)) {
				prepareSectionData(chunkPos, levelLightEngine, LightLayer.SKY, i, this.skyYMask, this.emptySkyYMask, this.skyUpdates);
			}

			if (bitSet2 == null || bitSet2.get(i)) {
				prepareSectionData(chunkPos, levelLightEngine, LightLayer.BLOCK, i, this.blockYMask, this.emptyBlockYMask, this.blockUpdates);
			}
		}
	}

	private static void prepareSectionData(
		ChunkPos chunkPos, LevelLightEngine levelLightEngine, LightLayer lightLayer, int i, BitSet bitSet, BitSet bitSet2, List<byte[]> list
	) {
		DataLayer dataLayer = levelLightEngine.getLayerListener(lightLayer).getDataLayerData(SectionPos.of(chunkPos, levelLightEngine.getMinLightSection() + i));
		if (dataLayer != null) {
			if (dataLayer.isEmpty()) {
				bitSet2.set(i);
			} else {
				bitSet.set(i);
				list.add(dataLayer.getData().clone());
			}
		}
	}

	@Override
	public void read(FriendlyByteBuf friendlyByteBuf) throws IOException {
		this.x = friendlyByteBuf.readVarInt();
		this.z = friendlyByteBuf.readVarInt();
		this.trustEdges = friendlyByteBuf.readBoolean();
		this.skyYMask = BitSet.valueOf(friendlyByteBuf.readLongArray());
		this.blockYMask = BitSet.valueOf(friendlyByteBuf.readLongArray());
		this.emptySkyYMask = BitSet.valueOf(friendlyByteBuf.readLongArray());
		this.emptyBlockYMask = BitSet.valueOf(friendlyByteBuf.readLongArray());
		int i = friendlyByteBuf.readVarInt();

		for (int j = 0; j < i; j++) {
			this.skyUpdates.add(friendlyByteBuf.readByteArray(2048));
		}

		int j = friendlyByteBuf.readVarInt();

		for (int k = 0; k < j; k++) {
			this.blockUpdates.add(friendlyByteBuf.readByteArray(2048));
		}
	}

	@Override
	public void write(FriendlyByteBuf friendlyByteBuf) throws IOException {
		friendlyByteBuf.writeVarInt(this.x);
		friendlyByteBuf.writeVarInt(this.z);
		friendlyByteBuf.writeBoolean(this.trustEdges);
		friendlyByteBuf.writeLongArray(this.skyYMask.toLongArray());
		friendlyByteBuf.writeLongArray(this.blockYMask.toLongArray());
		friendlyByteBuf.writeLongArray(this.emptySkyYMask.toLongArray());
		friendlyByteBuf.writeLongArray(this.emptyBlockYMask.toLongArray());
		friendlyByteBuf.writeVarInt(this.skyUpdates.size());

		for (byte[] bs : this.skyUpdates) {
			friendlyByteBuf.writeByteArray(bs);
		}

		friendlyByteBuf.writeVarInt(this.blockUpdates.size());

		for (byte[] bs : this.blockUpdates) {
			friendlyByteBuf.writeByteArray(bs);
		}
	}

	public void handle(ClientGamePacketListener clientGamePacketListener) {
		clientGamePacketListener.handleLightUpdatePacked(this);
	}

	@Environment(EnvType.CLIENT)
	public int getX() {
		return this.x;
	}

	@Environment(EnvType.CLIENT)
	public int getZ() {
		return this.z;
	}

	@Environment(EnvType.CLIENT)
	public BitSet getSkyYMask() {
		return this.skyYMask;
	}

	@Environment(EnvType.CLIENT)
	public BitSet getEmptySkyYMask() {
		return this.emptySkyYMask;
	}

	@Environment(EnvType.CLIENT)
	public List<byte[]> getSkyUpdates() {
		return this.skyUpdates;
	}

	@Environment(EnvType.CLIENT)
	public BitSet getBlockYMask() {
		return this.blockYMask;
	}

	@Environment(EnvType.CLIENT)
	public BitSet getEmptyBlockYMask() {
		return this.emptyBlockYMask;
	}

	@Environment(EnvType.CLIENT)
	public List<byte[]> getBlockUpdates() {
		return this.blockUpdates;
	}

	@Environment(EnvType.CLIENT)
	public boolean getTrustEdges() {
		return this.trustEdges;
	}
}
