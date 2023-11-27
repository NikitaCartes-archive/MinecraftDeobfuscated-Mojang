package net.minecraft.server.network;

import com.mojang.authlib.GameProfile;
import com.mojang.logging.LogUtils;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import javax.annotation.Nullable;
import net.minecraft.core.LayeredRegistryAccess;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.RegistrySynchronization;
import net.minecraft.network.Connection;
import net.minecraft.network.TickablePacketListener;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.PacketUtils;
import net.minecraft.network.protocol.common.ClientboundCustomPayloadPacket;
import net.minecraft.network.protocol.common.ClientboundDisconnectPacket;
import net.minecraft.network.protocol.common.ClientboundUpdateTagsPacket;
import net.minecraft.network.protocol.common.ServerboundClientInformationPacket;
import net.minecraft.network.protocol.common.ServerboundResourcePackPacket;
import net.minecraft.network.protocol.common.custom.BrandPayload;
import net.minecraft.network.protocol.configuration.ClientboundRegistryDataPacket;
import net.minecraft.network.protocol.configuration.ClientboundUpdateEnabledFeaturesPacket;
import net.minecraft.network.protocol.configuration.ServerConfigurationPacketListener;
import net.minecraft.network.protocol.configuration.ServerboundFinishConfigurationPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.RegistryLayer;
import net.minecraft.server.level.ClientInformation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.config.JoinWorldTask;
import net.minecraft.server.network.config.ServerResourcePackConfigurationTask;
import net.minecraft.server.players.PlayerList;
import net.minecraft.tags.TagNetworkSerialization;
import net.minecraft.world.flag.FeatureFlags;
import org.slf4j.Logger;

public class ServerConfigurationPacketListenerImpl extends ServerCommonPacketListenerImpl implements TickablePacketListener, ServerConfigurationPacketListener {
	private static final Logger LOGGER = LogUtils.getLogger();
	private static final Component DISCONNECT_REASON_INVALID_DATA = Component.translatable("multiplayer.disconnect.invalid_player_data");
	private final GameProfile gameProfile;
	private final Queue<ConfigurationTask> configurationTasks = new ConcurrentLinkedQueue();
	@Nullable
	private ConfigurationTask currentTask;
	private ClientInformation clientInformation;

	public ServerConfigurationPacketListenerImpl(MinecraftServer minecraftServer, Connection connection, CommonListenerCookie commonListenerCookie) {
		super(minecraftServer, connection, commonListenerCookie);
		this.gameProfile = commonListenerCookie.gameProfile();
		this.clientInformation = commonListenerCookie.clientInformation();
	}

	@Override
	protected GameProfile playerProfile() {
		return this.gameProfile;
	}

	@Override
	public void onDisconnect(Component component) {
		LOGGER.info("{} lost connection: {}", this.gameProfile, component.getString());
		super.onDisconnect(component);
	}

	@Override
	public boolean isAcceptingMessages() {
		return this.connection.isConnected();
	}

	public void startConfiguration() {
		this.send(new ClientboundCustomPayloadPacket(new BrandPayload(this.server.getServerModName())));
		LayeredRegistryAccess<RegistryLayer> layeredRegistryAccess = this.server.registries();
		this.send(new ClientboundUpdateEnabledFeaturesPacket(FeatureFlags.REGISTRY.toNames(this.server.getWorldData().enabledFeatures())));
		this.send(
			new ClientboundRegistryDataPacket(new RegistryAccess.ImmutableRegistryAccess(RegistrySynchronization.networkedRegistries(layeredRegistryAccess)).freeze())
		);
		this.send(new ClientboundUpdateTagsPacket(TagNetworkSerialization.serializeTagsToNetwork(layeredRegistryAccess)));
		this.addOptionalTasks();
		this.configurationTasks.add(new JoinWorldTask());
		this.startNextTask();
	}

	public void returnToWorld() {
		this.configurationTasks.add(new JoinWorldTask());
		this.startNextTask();
	}

	private void addOptionalTasks() {
		this.server
			.getServerResourcePack()
			.ifPresent(serverResourcePackInfo -> this.configurationTasks.add(new ServerResourcePackConfigurationTask(serverResourcePackInfo)));
	}

	@Override
	public void handleClientInformation(ServerboundClientInformationPacket serverboundClientInformationPacket) {
		this.clientInformation = serverboundClientInformationPacket.information();
	}

	@Override
	public void handleResourcePackResponse(ServerboundResourcePackPacket serverboundResourcePackPacket) {
		super.handleResourcePackResponse(serverboundResourcePackPacket);
		if (serverboundResourcePackPacket.action().isTerminal()) {
			this.finishCurrentTask(ServerResourcePackConfigurationTask.TYPE);
		}
	}

	@Override
	public void handleConfigurationFinished(ServerboundFinishConfigurationPacket serverboundFinishConfigurationPacket) {
		this.connection.suspendInboundAfterProtocolChange();
		PacketUtils.ensureRunningOnSameThread(serverboundFinishConfigurationPacket, this, this.server);
		this.finishCurrentTask(JoinWorldTask.TYPE);

		try {
			PlayerList playerList = this.server.getPlayerList();
			if (playerList.getPlayer(this.gameProfile.getId()) != null) {
				this.disconnect(PlayerList.DUPLICATE_LOGIN_DISCONNECT_MESSAGE);
				return;
			}

			Component component = playerList.canPlayerLogin(this.connection.getRemoteAddress(), this.gameProfile);
			if (component != null) {
				this.disconnect(component);
				return;
			}

			ServerPlayer serverPlayer = playerList.getPlayerForLogin(this.gameProfile, this.clientInformation);
			playerList.placeNewPlayer(this.connection, serverPlayer, this.createCookie(this.clientInformation));
			this.connection.resumeInboundAfterProtocolChange();
		} catch (Exception var5) {
			LOGGER.error("Couldn't place player in world", (Throwable)var5);
			this.connection.send(new ClientboundDisconnectPacket(DISCONNECT_REASON_INVALID_DATA));
			this.connection.disconnect(DISCONNECT_REASON_INVALID_DATA);
		}
	}

	@Override
	public void tick() {
		this.keepConnectionAlive();
	}

	private void startNextTask() {
		if (this.currentTask != null) {
			throw new IllegalStateException("Task " + this.currentTask.type().id() + " has not finished yet");
		} else if (this.isAcceptingMessages()) {
			ConfigurationTask configurationTask = (ConfigurationTask)this.configurationTasks.poll();
			if (configurationTask != null) {
				this.currentTask = configurationTask;
				configurationTask.start(this::send);
			}
		}
	}

	private void finishCurrentTask(ConfigurationTask.Type type) {
		ConfigurationTask.Type type2 = this.currentTask != null ? this.currentTask.type() : null;
		if (!type.equals(type2)) {
			throw new IllegalStateException("Unexpected request for task finish, current task: " + type2 + ", requested: " + type);
		} else {
			this.currentTask = null;
			this.startNextTask();
		}
	}
}
