package net.minecraft.network.protocol.configuration;

import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.protocol.PacketType;
import net.minecraft.resources.ResourceLocation;

public class ConfigurationPacketTypes {
	public static final PacketType<ClientboundFinishConfigurationPacket> CLIENTBOUND_FINISH_CONFIGURATION = createClientbound("finish_configuration");
	public static final PacketType<ClientboundRegistryDataPacket> CLIENTBOUND_REGISTRY_DATA = createClientbound("registry_data");
	public static final PacketType<ClientboundUpdateEnabledFeaturesPacket> CLIENTBOUND_UPDATE_ENABLED_FEATURES = createClientbound("update_enabled_features");
	public static final PacketType<ClientboundSelectKnownPacks> CLIENTBOUND_SELECT_KNOWN_PACKS = createClientbound("select_known_packs");
	public static final PacketType<ClientboundResetChatPacket> CLIENTBOUND_RESET_CHAT = createClientbound("reset_chat");
	public static final PacketType<ServerboundFinishConfigurationPacket> SERVERBOUND_FINISH_CONFIGURATION = createServerbound("finish_configuration");
	public static final PacketType<ServerboundSelectKnownPacks> SERVERBOUND_SELECT_KNOWN_PACKS = createServerbound("select_known_packs");

	private static <T extends Packet<ClientConfigurationPacketListener>> PacketType<T> createClientbound(String string) {
		return new PacketType<>(PacketFlow.CLIENTBOUND, ResourceLocation.withDefaultNamespace(string));
	}

	private static <T extends Packet<ServerConfigurationPacketListener>> PacketType<T> createServerbound(String string) {
		return new PacketType<>(PacketFlow.SERVERBOUND, ResourceLocation.withDefaultNamespace(string));
	}
}
