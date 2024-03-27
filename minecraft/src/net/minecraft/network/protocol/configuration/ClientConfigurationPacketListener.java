package net.minecraft.network.protocol.configuration;

import net.minecraft.network.ConnectionProtocol;
import net.minecraft.network.protocol.common.ClientCommonPacketListener;

public interface ClientConfigurationPacketListener extends ClientCommonPacketListener {
	@Override
	default ConnectionProtocol protocol() {
		return ConnectionProtocol.CONFIGURATION;
	}

	void handleConfigurationFinished(ClientboundFinishConfigurationPacket clientboundFinishConfigurationPacket);

	void handleRegistryData(ClientboundRegistryDataPacket clientboundRegistryDataPacket);

	void handleEnabledFeatures(ClientboundUpdateEnabledFeaturesPacket clientboundUpdateEnabledFeaturesPacket);

	void handleSelectKnownPacks(ClientboundSelectKnownPacks clientboundSelectKnownPacks);

	void handleResetChat(ClientboundResetChatPacket clientboundResetChatPacket);
}
