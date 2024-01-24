package net.minecraft.network.protocol.game;

import com.google.common.collect.Sets;
import java.util.Set;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
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
	public static final StreamCodec<RegistryFriendlyByteBuf, ClientboundLoginPacket> STREAM_CODEC = Packet.codec(
		ClientboundLoginPacket::write, ClientboundLoginPacket::new
	);

	private ClientboundLoginPacket(RegistryFriendlyByteBuf registryFriendlyByteBuf) {
		this(
			registryFriendlyByteBuf.readInt(),
			registryFriendlyByteBuf.readBoolean(),
			registryFriendlyByteBuf.readCollection(Sets::newHashSetWithExpectedSize, friendlyByteBuf -> friendlyByteBuf.readResourceKey(Registries.DIMENSION)),
			registryFriendlyByteBuf.readVarInt(),
			registryFriendlyByteBuf.readVarInt(),
			registryFriendlyByteBuf.readVarInt(),
			registryFriendlyByteBuf.readBoolean(),
			registryFriendlyByteBuf.readBoolean(),
			registryFriendlyByteBuf.readBoolean(),
			new CommonPlayerSpawnInfo(registryFriendlyByteBuf),
			registryFriendlyByteBuf.readBoolean()
		);
	}

	private void write(RegistryFriendlyByteBuf registryFriendlyByteBuf) {
		registryFriendlyByteBuf.writeInt(this.playerId);
		registryFriendlyByteBuf.writeBoolean(this.hardcore);
		registryFriendlyByteBuf.writeCollection(this.levels, FriendlyByteBuf::writeResourceKey);
		registryFriendlyByteBuf.writeVarInt(this.maxPlayers);
		registryFriendlyByteBuf.writeVarInt(this.chunkRadius);
		registryFriendlyByteBuf.writeVarInt(this.simulationDistance);
		registryFriendlyByteBuf.writeBoolean(this.reducedDebugInfo);
		registryFriendlyByteBuf.writeBoolean(this.showDeathScreen);
		registryFriendlyByteBuf.writeBoolean(this.doLimitedCrafting);
		this.commonPlayerSpawnInfo.write(registryFriendlyByteBuf);
		registryFriendlyByteBuf.writeBoolean(this.enforcesSecureChat);
	}

	@Override
	public PacketType<ClientboundLoginPacket> type() {
		return GamePacketTypes.CLIENTBOUND_LOGIN;
	}

	public void handle(ClientGamePacketListener clientGamePacketListener) {
		clientGamePacketListener.handleLogin(this);
	}
}
