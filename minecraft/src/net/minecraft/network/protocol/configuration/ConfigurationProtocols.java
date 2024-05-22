package net.minecraft.network.protocol.configuration;

import net.minecraft.network.ConnectionProtocol;
import net.minecraft.network.ProtocolInfo;
import net.minecraft.network.protocol.ProtocolInfoBuilder;
import net.minecraft.network.protocol.common.ClientboundCustomPayloadPacket;
import net.minecraft.network.protocol.common.ClientboundCustomReportDetailsPacket;
import net.minecraft.network.protocol.common.ClientboundDisconnectPacket;
import net.minecraft.network.protocol.common.ClientboundKeepAlivePacket;
import net.minecraft.network.protocol.common.ClientboundPingPacket;
import net.minecraft.network.protocol.common.ClientboundResourcePackPopPacket;
import net.minecraft.network.protocol.common.ClientboundResourcePackPushPacket;
import net.minecraft.network.protocol.common.ClientboundServerLinksPacket;
import net.minecraft.network.protocol.common.ClientboundStoreCookiePacket;
import net.minecraft.network.protocol.common.ClientboundTransferPacket;
import net.minecraft.network.protocol.common.ClientboundUpdateTagsPacket;
import net.minecraft.network.protocol.common.CommonPacketTypes;
import net.minecraft.network.protocol.common.ServerboundClientInformationPacket;
import net.minecraft.network.protocol.common.ServerboundCustomPayloadPacket;
import net.minecraft.network.protocol.common.ServerboundKeepAlivePacket;
import net.minecraft.network.protocol.common.ServerboundPongPacket;
import net.minecraft.network.protocol.common.ServerboundResourcePackPacket;
import net.minecraft.network.protocol.cookie.ClientboundCookieRequestPacket;
import net.minecraft.network.protocol.cookie.CookiePacketTypes;
import net.minecraft.network.protocol.cookie.ServerboundCookieResponsePacket;

public class ConfigurationProtocols {
	public static final ProtocolInfo<ServerConfigurationPacketListener> SERVERBOUND = ProtocolInfoBuilder.serverboundProtocol(
		ConnectionProtocol.CONFIGURATION,
		protocolInfoBuilder -> protocolInfoBuilder.addPacket(CommonPacketTypes.SERVERBOUND_CLIENT_INFORMATION, ServerboundClientInformationPacket.STREAM_CODEC)
				.addPacket(CookiePacketTypes.SERVERBOUND_COOKIE_RESPONSE, ServerboundCookieResponsePacket.STREAM_CODEC)
				.addPacket(CommonPacketTypes.SERVERBOUND_CUSTOM_PAYLOAD, ServerboundCustomPayloadPacket.STREAM_CODEC)
				.addPacket(ConfigurationPacketTypes.SERVERBOUND_FINISH_CONFIGURATION, ServerboundFinishConfigurationPacket.STREAM_CODEC)
				.addPacket(CommonPacketTypes.SERVERBOUND_KEEP_ALIVE, ServerboundKeepAlivePacket.STREAM_CODEC)
				.addPacket(CommonPacketTypes.SERVERBOUND_PONG, ServerboundPongPacket.STREAM_CODEC)
				.addPacket(CommonPacketTypes.SERVERBOUND_RESOURCE_PACK, ServerboundResourcePackPacket.STREAM_CODEC)
				.addPacket(ConfigurationPacketTypes.SERVERBOUND_SELECT_KNOWN_PACKS, ServerboundSelectKnownPacks.STREAM_CODEC)
	);
	public static final ProtocolInfo<ClientConfigurationPacketListener> CLIENTBOUND = ProtocolInfoBuilder.clientboundProtocol(
		ConnectionProtocol.CONFIGURATION,
		protocolInfoBuilder -> protocolInfoBuilder.addPacket(CookiePacketTypes.CLIENTBOUND_COOKIE_REQUEST, ClientboundCookieRequestPacket.STREAM_CODEC)
				.addPacket(CommonPacketTypes.CLIENTBOUND_CUSTOM_PAYLOAD, ClientboundCustomPayloadPacket.CONFIG_STREAM_CODEC)
				.addPacket(CommonPacketTypes.CLIENTBOUND_DISCONNECT, ClientboundDisconnectPacket.STREAM_CODEC)
				.addPacket(ConfigurationPacketTypes.CLIENTBOUND_FINISH_CONFIGURATION, ClientboundFinishConfigurationPacket.STREAM_CODEC)
				.addPacket(CommonPacketTypes.CLIENTBOUND_KEEP_ALIVE, ClientboundKeepAlivePacket.STREAM_CODEC)
				.addPacket(CommonPacketTypes.CLIENTBOUND_PING, ClientboundPingPacket.STREAM_CODEC)
				.addPacket(ConfigurationPacketTypes.CLIENTBOUND_RESET_CHAT, ClientboundResetChatPacket.STREAM_CODEC)
				.addPacket(ConfigurationPacketTypes.CLIENTBOUND_REGISTRY_DATA, ClientboundRegistryDataPacket.STREAM_CODEC)
				.addPacket(CommonPacketTypes.CLIENTBOUND_RESOURCE_PACK_POP, ClientboundResourcePackPopPacket.STREAM_CODEC)
				.addPacket(CommonPacketTypes.CLIENTBOUND_RESOURCE_PACK_PUSH, ClientboundResourcePackPushPacket.STREAM_CODEC)
				.addPacket(CommonPacketTypes.CLIENTBOUND_STORE_COOKIE, ClientboundStoreCookiePacket.STREAM_CODEC)
				.addPacket(CommonPacketTypes.CLIENTBOUND_TRANSFER, ClientboundTransferPacket.STREAM_CODEC)
				.addPacket(ConfigurationPacketTypes.CLIENTBOUND_UPDATE_ENABLED_FEATURES, ClientboundUpdateEnabledFeaturesPacket.STREAM_CODEC)
				.addPacket(CommonPacketTypes.CLIENTBOUND_UPDATE_TAGS, ClientboundUpdateTagsPacket.STREAM_CODEC)
				.addPacket(ConfigurationPacketTypes.CLIENTBOUND_SELECT_KNOWN_PACKS, ClientboundSelectKnownPacks.STREAM_CODEC)
				.addPacket(CommonPacketTypes.CLIENTBOUND_CUSTOM_REPORT_DETAILS, ClientboundCustomReportDetailsPacket.STREAM_CODEC)
				.addPacket(CommonPacketTypes.CLIENTBOUND_SERVER_LINKS, ClientboundServerLinksPacket.STREAM_CODEC)
	);
}
