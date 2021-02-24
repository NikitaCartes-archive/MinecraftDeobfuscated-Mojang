package net.minecraft.network.protocol.game;

import com.google.common.collect.Lists;
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
	private final int x;
	private final int z;
	private final BitSet skyYMask;
	private final BitSet blockYMask;
	private final BitSet emptySkyYMask;
	private final BitSet emptyBlockYMask;
	private final List<byte[]> skyUpdates;
	private final List<byte[]> blockUpdates;
	private final boolean trustEdges;

	public ClientboundLightUpdatePacket(ChunkPos chunkPos, LevelLightEngine levelLightEngine, @Nullable BitSet bitSet, @Nullable BitSet bitSet2, boolean bl) {
		this.x = chunkPos.x;
		this.z = chunkPos.z;
		this.trustEdges = bl;
		this.skyYMask = new BitSet();
		this.blockYMask = new BitSet();
		this.emptySkyYMask = new BitSet();
		this.emptyBlockYMask = new BitSet();
		this.skyUpdates = Lists.<byte[]>newArrayList();
		this.blockUpdates = Lists.<byte[]>newArrayList();

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

	public ClientboundLightUpdatePacket(FriendlyByteBuf friendlyByteBuf) {
		this.x = friendlyByteBuf.readVarInt();
		this.z = friendlyByteBuf.readVarInt();
		this.trustEdges = friendlyByteBuf.readBoolean();
		this.skyYMask = friendlyByteBuf.readBitSet();
		this.blockYMask = friendlyByteBuf.readBitSet();
		this.emptySkyYMask = friendlyByteBuf.readBitSet();
		this.emptyBlockYMask = friendlyByteBuf.readBitSet();
		this.skyUpdates = friendlyByteBuf.readList(friendlyByteBufx -> friendlyByteBufx.readByteArray(2048));
		this.blockUpdates = friendlyByteBuf.readList(friendlyByteBufx -> friendlyByteBufx.readByteArray(2048));
	}

	@Override
	public void write(FriendlyByteBuf friendlyByteBuf) {
		friendlyByteBuf.writeVarInt(this.x);
		friendlyByteBuf.writeVarInt(this.z);
		friendlyByteBuf.writeBoolean(this.trustEdges);
		friendlyByteBuf.writeBitSet(this.skyYMask);
		friendlyByteBuf.writeBitSet(this.blockYMask);
		friendlyByteBuf.writeBitSet(this.emptySkyYMask);
		friendlyByteBuf.writeBitSet(this.emptyBlockYMask);
		friendlyByteBuf.writeCollection(this.skyUpdates, FriendlyByteBuf::writeByteArray);
		friendlyByteBuf.writeCollection(this.blockUpdates, FriendlyByteBuf::writeByteArray);
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
