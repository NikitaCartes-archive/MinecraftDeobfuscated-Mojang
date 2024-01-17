package net.minecraft.network.protocol.game;

import java.util.BitSet;
import javax.annotation.Nullable;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.lighting.LevelLightEngine;

public class ClientboundLightUpdatePacket implements Packet<ClientGamePacketListener> {
	public static final StreamCodec<FriendlyByteBuf, ClientboundLightUpdatePacket> STREAM_CODEC = Packet.codec(
		ClientboundLightUpdatePacket::write, ClientboundLightUpdatePacket::new
	);
	private final int x;
	private final int z;
	private final ClientboundLightUpdatePacketData lightData;

	public ClientboundLightUpdatePacket(ChunkPos chunkPos, LevelLightEngine levelLightEngine, @Nullable BitSet bitSet, @Nullable BitSet bitSet2) {
		this.x = chunkPos.x;
		this.z = chunkPos.z;
		this.lightData = new ClientboundLightUpdatePacketData(chunkPos, levelLightEngine, bitSet, bitSet2);
	}

	private ClientboundLightUpdatePacket(FriendlyByteBuf friendlyByteBuf) {
		this.x = friendlyByteBuf.readVarInt();
		this.z = friendlyByteBuf.readVarInt();
		this.lightData = new ClientboundLightUpdatePacketData(friendlyByteBuf, this.x, this.z);
	}

	private void write(FriendlyByteBuf friendlyByteBuf) {
		friendlyByteBuf.writeVarInt(this.x);
		friendlyByteBuf.writeVarInt(this.z);
		this.lightData.write(friendlyByteBuf);
	}

	@Override
	public PacketType<ClientboundLightUpdatePacket> type() {
		return GamePacketTypes.CLIENTBOUND_LIGHT_UPDATE;
	}

	public void handle(ClientGamePacketListener clientGamePacketListener) {
		clientGamePacketListener.handleLightUpdatePacket(this);
	}

	public int getX() {
		return this.x;
	}

	public int getZ() {
		return this.z;
	}

	public ClientboundLightUpdatePacketData getLightData() {
		return this.lightData;
	}
}
