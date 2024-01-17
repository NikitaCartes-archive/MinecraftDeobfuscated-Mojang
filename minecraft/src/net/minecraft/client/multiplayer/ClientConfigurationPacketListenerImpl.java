package net.minecraft.client.multiplayer;

import com.mojang.authlib.GameProfile;
import com.mojang.logging.LogUtils;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.Connection;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.TickablePacketListener;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.PacketUtils;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.protocol.configuration.ClientConfigurationPacketListener;
import net.minecraft.network.protocol.configuration.ClientboundFinishConfigurationPacket;
import net.minecraft.network.protocol.configuration.ClientboundRegistryDataPacket;
import net.minecraft.network.protocol.configuration.ClientboundUpdateEnabledFeaturesPacket;
import net.minecraft.network.protocol.configuration.ServerboundFinishConfigurationPacket;
import net.minecraft.network.protocol.game.GameProtocols;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.flag.FeatureFlags;
import org.slf4j.Logger;

@Environment(EnvType.CLIENT)
public class ClientConfigurationPacketListenerImpl extends ClientCommonPacketListenerImpl implements TickablePacketListener, ClientConfigurationPacketListener {
	private static final Logger LOGGER = LogUtils.getLogger();
	private final GameProfile localGameProfile;
	private RegistryAccess.Frozen receivedRegistries;
	private FeatureFlagSet enabledFeatures;

	public ClientConfigurationPacketListenerImpl(Minecraft minecraft, Connection connection, CommonListenerCookie commonListenerCookie) {
		super(minecraft, connection, commonListenerCookie);
		this.localGameProfile = commonListenerCookie.localGameProfile();
		this.receivedRegistries = commonListenerCookie.receivedRegistries();
		this.enabledFeatures = commonListenerCookie.enabledFeatures();
	}

	@Override
	public boolean isAcceptingMessages() {
		return this.connection.isConnected();
	}

	@Override
	protected RegistryAccess.Frozen registryAccess() {
		return this.receivedRegistries;
	}

	@Override
	protected void handleCustomPayload(CustomPacketPayload customPacketPayload) {
		this.handleUnknownCustomPayload(customPacketPayload);
	}

	private void handleUnknownCustomPayload(CustomPacketPayload customPacketPayload) {
		LOGGER.warn("Unknown custom packet payload: {}", customPacketPayload.type().id());
	}

	@Override
	public void handleRegistryData(ClientboundRegistryDataPacket clientboundRegistryDataPacket) {
		PacketUtils.ensureRunningOnSameThread(clientboundRegistryDataPacket, this, this.minecraft);
		RegistryAccess.Frozen frozen = ClientRegistryLayer.createRegistryAccess()
			.replaceFrom(ClientRegistryLayer.REMOTE, clientboundRegistryDataPacket.registryHolder())
			.compositeAccess();
		if (!this.connection.isMemoryConnection()) {
			frozen.registries().forEach(registryEntry -> registryEntry.value().resetTags());
		}

		this.receivedRegistries = frozen;
	}

	@Override
	public void handleEnabledFeatures(ClientboundUpdateEnabledFeaturesPacket clientboundUpdateEnabledFeaturesPacket) {
		this.enabledFeatures = FeatureFlags.REGISTRY.fromNames(clientboundUpdateEnabledFeaturesPacket.features());
	}

	@Override
	public void handleConfigurationFinished(ClientboundFinishConfigurationPacket clientboundFinishConfigurationPacket) {
		PacketUtils.ensureRunningOnSameThread(clientboundFinishConfigurationPacket, this, this.minecraft);
		this.connection
			.setupInboundProtocol(
				GameProtocols.CLIENTBOUND.bind(RegistryFriendlyByteBuf.decorator(this.receivedRegistries)),
				new ClientPacketListener(
					this.minecraft,
					this.connection,
					new CommonListenerCookie(
						this.localGameProfile,
						this.telemetryManager,
						this.receivedRegistries,
						this.enabledFeatures,
						this.serverBrand,
						this.serverData,
						this.postDisconnectScreen,
						this.serverCookies
					)
				)
			);
		this.connection.send(ServerboundFinishConfigurationPacket.INSTANCE);
		this.connection.setupOutboundProtocol(GameProtocols.SERVERBOUND.bind(RegistryFriendlyByteBuf.decorator(this.receivedRegistries)));
	}

	@Override
	public void tick() {
		this.sendDeferredPackets();
	}

	@Override
	public void onDisconnect(Component component) {
		super.onDisconnect(component);
		this.minecraft.clearDownloadedResourcePacks();
	}
}
