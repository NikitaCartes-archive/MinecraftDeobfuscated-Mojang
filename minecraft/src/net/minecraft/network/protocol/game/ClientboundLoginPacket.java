package net.minecraft.network.protocol.game;

import com.google.common.collect.Sets;
import java.util.Set;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

public record ClientboundLoginPacket(
	int playerId,
	boolean hardcore,
	Set<ResourceKey<Level>> levels,
	int maxPlayers,
	int chunkRadius,
	int simulationDistance,
	boolean reducedDebugInfo,
	boolean showDeathScreen,
	boolean doLimitedCrafting,
	CommonPlayerSpawnInfo commonPlayerSpawnInfo,
	boolean enforcesSecureChat
) implements Packet<ClientGamePacketListener> {
	public static final StreamCodec<FriendlyByteBuf, ClientboundLoginPacket> STREAM_CODEC = Packet.codec(
		ClientboundLoginPacket::write, ClientboundLoginPacket::new
	);

	private ClientboundLoginPacket(FriendlyByteBuf friendlyByteBuf) {
		this(
			friendlyByteBuf.readInt(),
			friendlyByteBuf.readBoolean(),
			friendlyByteBuf.readCollection(Sets::newHashSetWithExpectedSize, friendlyByteBufx -> friendlyByteBufx.readResourceKey(Registries.DIMENSION)),
			friendlyByteBuf.readVarInt(),
			friendlyByteBuf.readVarInt(),
			friendlyByteBuf.readVarInt(),
			friendlyByteBuf.readBoolean(),
			friendlyByteBuf.readBoolean(),
			friendlyByteBuf.readBoolean(),
			new CommonPlayerSpawnInfo(friendlyByteBuf),
			friendlyByteBuf.readBoolean()
		);
	}

	private void write(FriendlyByteBuf friendlyByteBuf) {
		friendlyByteBuf.writeInt(this.playerId);
		friendlyByteBuf.writeBoolean(this.hardcore);
		friendlyByteBuf.writeCollection(this.levels, FriendlyByteBuf::writeResourceKey);
		friendlyByteBuf.writeVarInt(this.maxPlayers);
		friendlyByteBuf.writeVarInt(this.chunkRadius);
		friendlyByteBuf.writeVarInt(this.simulationDistance);
		friendlyByteBuf.writeBoolean(this.reducedDebugInfo);
		friendlyByteBuf.writeBoolean(this.showDeathScreen);
		friendlyByteBuf.writeBoolean(this.doLimitedCrafting);
		this.commonPlayerSpawnInfo.write(friendlyByteBuf);
		friendlyByteBuf.writeBoolean(this.enforcesSecureChat);
	}

	@Override
	public PacketType<ClientboundLoginPacket> type() {
		return GamePacketTypes.CLIENTBOUND_LOGIN;
	}

	public void handle(ClientGamePacketListener clientGamePacketListener) {
		clientGamePacketListener.handleLogin(this);
	}
}
