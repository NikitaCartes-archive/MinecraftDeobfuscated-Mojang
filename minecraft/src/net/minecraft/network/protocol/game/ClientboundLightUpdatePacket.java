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
	private int skyYMask;
	private int blockYMask;
	private int emptySkyYMask;
	private int emptyBlockYMask;
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

		for (int i = 0; i < 18; i++) {
			DataLayer dataLayer = levelLightEngine.getLayerListener(LightLayer.SKY).getDataLayerData(SectionPos.of(chunkPos, -1 + i));
			DataLayer dataLayer2 = levelLightEngine.getLayerListener(LightLayer.BLOCK).getDataLayerData(SectionPos.of(chunkPos, -1 + i));
			if (dataLayer != null) {
				if (dataLayer.isEmpty()) {
					this.emptySkyYMask |= 1 << i;
				} else {
					this.skyYMask |= 1 << i;
					this.skyUpdates.add(dataLayer.getData().clone());
				}
			}

			if (dataLayer2 != null) {
				if (dataLayer2.isEmpty()) {
					this.emptyBlockYMask |= 1 << i;
				} else {
					this.blockYMask |= 1 << i;
					this.blockUpdates.add(dataLayer2.getData().clone());
				}
			}
		}
	}

	public ClientboundLightUpdatePacket(ChunkPos chunkPos, LevelLightEngine levelLightEngine, int i, int j, boolean bl) {
		this.x = chunkPos.x;
		this.z = chunkPos.z;
		this.trustEdges = bl;
		this.skyYMask = i;
		this.blockYMask = j;
		this.skyUpdates = Lists.<byte[]>newArrayList();
		this.blockUpdates = Lists.<byte[]>newArrayList();

		for (int k = 0; k < 18; k++) {
			if ((this.skyYMask & 1 << k) != 0) {
				DataLayer dataLayer = levelLightEngine.getLayerListener(LightLayer.SKY).getDataLayerData(SectionPos.of(chunkPos, -1 + k));
				if (dataLayer != null && !dataLayer.isEmpty()) {
					this.skyUpdates.add(dataLayer.getData().clone());
				} else {
					this.skyYMask &= ~(1 << k);
					if (dataLayer != null) {
						this.emptySkyYMask |= 1 << k;
					}
				}
			}

			if ((this.blockYMask & 1 << k) != 0) {
				DataLayer dataLayer = levelLightEngine.getLayerListener(LightLayer.BLOCK).getDataLayerData(SectionPos.of(chunkPos, -1 + k));
				if (dataLayer != null && !dataLayer.isEmpty()) {
					this.blockUpdates.add(dataLayer.getData().clone());
				} else {
					this.blockYMask &= ~(1 << k);
					if (dataLayer != null) {
						this.emptyBlockYMask |= 1 << k;
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
		this.skyYMask = friendlyByteBuf.readVarInt();
		this.blockYMask = friendlyByteBuf.readVarInt();
		this.emptySkyYMask = friendlyByteBuf.readVarInt();
		this.emptyBlockYMask = friendlyByteBuf.readVarInt();
		this.skyUpdates = Lists.<byte[]>newArrayList();

		for (int i = 0; i < 18; i++) {
			if ((this.skyYMask & 1 << i) != 0) {
				this.skyUpdates.add(friendlyByteBuf.readByteArray(2048));
			}
		}

		this.blockUpdates = Lists.<byte[]>newArrayList();

		for (int ix = 0; ix < 18; ix++) {
			if ((this.blockYMask & 1 << ix) != 0) {
				this.blockUpdates.add(friendlyByteBuf.readByteArray(2048));
			}
		}
	}

	@Override
	public void write(FriendlyByteBuf friendlyByteBuf) throws IOException {
		friendlyByteBuf.writeVarInt(this.x);
		friendlyByteBuf.writeVarInt(this.z);
		friendlyByteBuf.writeBoolean(this.trustEdges);
		friendlyByteBuf.writeVarInt(this.skyYMask);
		friendlyByteBuf.writeVarInt(this.blockYMask);
		friendlyByteBuf.writeVarInt(this.emptySkyYMask);
		friendlyByteBuf.writeVarInt(this.emptyBlockYMask);

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
	public int getSkyYMask() {
		return this.skyYMask;
	}

	@Environment(EnvType.CLIENT)
	public int getEmptySkyYMask() {
		return this.emptySkyYMask;
	}

	@Environment(EnvType.CLIENT)
	public List<byte[]> getSkyUpdates() {
		return this.skyUpdates;
	}

	@Environment(EnvType.CLIENT)
	public int getBlockYMask() {
		return this.blockYMask;
	}

	@Environment(EnvType.CLIENT)
	public int getEmptyBlockYMask() {
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
