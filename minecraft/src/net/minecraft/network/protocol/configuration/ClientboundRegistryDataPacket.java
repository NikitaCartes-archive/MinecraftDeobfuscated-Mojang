package net.minecraft.network.protocol.configuration;

import net.minecraft.core.RegistryAccess;
import net.minecraft.core.RegistrySynchronization;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;
import net.minecraft.resources.RegistryOps;

public record ClientboundRegistryDataPacket(RegistryAccess.Frozen registryHolder) implements Packet<ClientConfigurationPacketListener> {
	public static final StreamCodec<FriendlyByteBuf, ClientboundRegistryDataPacket> STREAM_CODEC = Packet.codec(
		ClientboundRegistryDataPacket::write, ClientboundRegistryDataPacket::new
	);
	private static final RegistryOps<Tag> BUILTIN_CONTEXT_OPS = RegistryOps.create(
		NbtOps.INSTANCE, RegistryAccess.fromRegistryOfRegistries(BuiltInRegistries.REGISTRY)
	);

	private ClientboundRegistryDataPacket(FriendlyByteBuf friendlyByteBuf) {
		this(friendlyByteBuf.readWithCodecTrusted(BUILTIN_CONTEXT_OPS, RegistrySynchronization.NETWORK_CODEC).freeze());
	}

	private void write(FriendlyByteBuf friendlyByteBuf) {
		friendlyByteBuf.writeWithCodec(BUILTIN_CONTEXT_OPS, RegistrySynchronization.NETWORK_CODEC, this.registryHolder);
	}

	@Override
	public PacketType<ClientboundRegistryDataPacket> type() {
		return ConfigurationPacketTypes.CLIENTBOUND_REGISTRY_DATA;
	}

	public void handle(ClientConfigurationPacketListener clientConfigurationPacketListener) {
		clientConfigurationPacketListener.handleRegistryData(this);
	}
}
