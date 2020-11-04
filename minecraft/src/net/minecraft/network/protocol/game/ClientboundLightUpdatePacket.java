package net.minecraft.network.protocol.game;

import com.google.common.collect.Lists;
import java.io.IOException;
import java.util.List;
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
	private long skyYMask;
	private long blockYMask;
	private long emptySkyYMask;
	private long emptyBlockYMask;
	private List<byte[]> skyUpdates;
	private List<byte[]> blockUpdates;
	private boolean trustEdges;

	public ClientboundLightUpdatePacket() {
	}

	public ClientboundLightUpdatePacket(ChunkPos chunkPos, LevelLightEngine levelLightEngine, boolean bl) {
		this.x = chunkPos.x;
		this.z = chunkPos.z;
		this.trustEdges = bl;
		this.skyUpdates = Lists.<byte[]>newArrayList();
		this.blockUpdates = Lists.<byte[]>newArrayList();

		for (int i = 0; i < levelLightEngine.getLightSectionCount(); i++) {
			DataLayer dataLayer = levelLightEngine.getLayerListener(LightLayer.SKY).getDataLayerData(SectionPos.of(chunkPos, levelLightEngine.getMinLightSection() + i));
			DataLayer dataLayer2 = levelLightEngine.getLayerListener(LightLayer.BLOCK)
				.getDataLayerData(SectionPos.of(chunkPos, levelLightEngine.getMinLightSection() + i));
			if (dataLayer != null) {
				if (dataLayer.isEmpty()) {
					this.emptySkyYMask |= 1L << i;
				} else {
					this.skyYMask |= 1L << i;
					this.skyUpdates.add(dataLayer.getData().clone());
				}
			}

			if (dataLayer2 != null) {
				if (dataLayer2.isEmpty()) {
					this.emptyBlockYMask |= 1L << i;
				} else {
					this.blockYMask |= 1L << i;
					this.blockUpdates.add(dataLayer2.getData().clone());
				}
			}
		}
	}

	public ClientboundLightUpdatePacket(ChunkPos chunkPos, LevelLightEngine levelLightEngine, int i, int j, boolean bl) {
		this.x = chunkPos.x;
		this.z = chunkPos.z;
		this.trustEdges = bl;
		this.skyYMask = (long)i;
		this.blockYMask = (long)j;
		this.skyUpdates = Lists.<byte[]>newArrayList();
		this.blockUpdates = Lists.<byte[]>newArrayList();

		for (int k = 0; k < levelLightEngine.getLightSectionCount(); k++) {
			if ((this.skyYMask & 1L << k) != 0L) {
				DataLayer dataLayer = levelLightEngine.getLayerListener(LightLayer.SKY)
					.getDataLayerData(SectionPos.of(chunkPos, levelLightEngine.getMinLightSection() + k));
				if (dataLayer != null && !dataLayer.isEmpty()) {
					this.skyUpdates.add(dataLayer.getData().clone());
				} else {
					this.skyYMask &= ~(1L << k);
					if (dataLayer != null) {
						this.emptySkyYMask |= 1L << k;
					}
				}
			}

			if ((this.blockYMask & 1L << k) != 0L) {
				DataLayer dataLayer = levelLightEngine.getLayerListener(LightLayer.BLOCK)
					.getDataLayerData(SectionPos.of(chunkPos, levelLightEngine.getMinLightSection() + k));
				if (dataLayer != null && !dataLayer.isEmpty()) {
					this.blockUpdates.add(dataLayer.getData().clone());
				} else {
					this.blockYMask &= ~(1L << k);
					if (dataLayer != null) {
						this.emptyBlockYMask |= 1L << k;
					}
				}
			}
		}
	}

	@Override
	public void read(FriendlyByteBuf friendlyByteBuf) throws IOException {
		this.x = friendlyByteBuf.readVarInt();
		this.z = friendlyByteBuf.readVarInt();
		this.trustEdges = friendlyByteBuf.readBoolean();
		this.skyYMask = friendlyByteBuf.readVarLong();
		this.blockYMask = friendlyByteBuf.readVarLong();
		this.emptySkyYMask = friendlyByteBuf.readVarLong();
		this.emptyBlockYMask = friendlyByteBuf.readVarLong();
		this.skyUpdates = Lists.<byte[]>newArrayList();

		for (int i = 0; i < 64; i++) {
			if ((this.skyYMask & 1L << i) != 0L) {
				this.skyUpdates.add(friendlyByteBuf.readByteArray(2048));
			}
		}

		this.blockUpdates = Lists.<byte[]>newArrayList();

		for (int ix = 0; ix < 64; ix++) {
			if ((this.blockYMask & 1L << ix) != 0L) {
				this.blockUpdates.add(friendlyByteBuf.readByteArray(2048));
			}
		}
	}

	@Override
	public void write(FriendlyByteBuf friendlyByteBuf) throws IOException {
		friendlyByteBuf.writeVarInt(this.x);
		friendlyByteBuf.writeVarInt(this.z);
		friendlyByteBuf.writeBoolean(this.trustEdges);
		friendlyByteBuf.writeVarLong(this.skyYMask);
		friendlyByteBuf.writeVarLong(this.blockYMask);
		friendlyByteBuf.writeVarLong(this.emptySkyYMask);
		friendlyByteBuf.writeVarLong(this.emptyBlockYMask);

		for (byte[] bs : this.skyUpdates) {
			friendlyByteBuf.writeByteArray(bs);
		}

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
	public long getSkyYMask() {
		return this.skyYMask;
	}

	@Environment(EnvType.CLIENT)
	public long getEmptySkyYMask() {
		return this.emptySkyYMask;
	}

	@Environment(EnvType.CLIENT)
	public List<byte[]> getSkyUpdates() {
		return this.skyUpdates;
	}

	@Environment(EnvType.CLIENT)
	public long getBlockYMask() {
		return this.blockYMask;
	}

	@Environment(EnvType.CLIENT)
	public long getEmptyBlockYMask() {
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
