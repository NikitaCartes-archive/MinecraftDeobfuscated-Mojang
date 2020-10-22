package net.minecraft.client.server;

import com.google.common.collect.Lists;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.GameProfileRepository;
import com.mojang.authlib.minecraft.MinecraftSessionService;
import java.io.File;
import java.io.IOException;
import java.util.Optional;
import java.util.UUID;
import java.util.function.BooleanSupplier;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.CrashReport;
import net.minecraft.CrashReportDetail;
import net.minecraft.SharedConstants;
import net.minecraft.client.ClientBrandRetriever;
import net.minecraft.client.Minecraft;
import net.minecraft.core.RegistryAccess;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.ServerResources;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.progress.ChunkProgressListenerFactory;
import net.minecraft.server.packs.repository.PackRepository;
import net.minecraft.server.players.GameProfileCache;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.Snooper;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.storage.LevelStorageSource;
import net.minecraft.world.level.storage.WorldData;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Environment(EnvType.CLIENT)
public class IntegratedServer extends MinecraftServer {
	private static final Logger LOGGER = LogManager.getLogger();
	private final Minecraft minecraft;
	private boolean paused;
	private int publishedPort = -1;
	private LanServerPinger lanPinger;
	private UUID uuid;

	public IntegratedServer(
		Thread thread,
		Minecraft minecraft,
		RegistryAccess.RegistryHolder registryHolder,
		LevelStorageSource.LevelStorageAccess levelStorageAccess,
		PackRepository packRepository,
		ServerResources serverResources,
		WorldData worldData,
		MinecraftSessionService minecraftSessionService,
		GameProfileRepository gameProfileRepository,
		GameProfileCache gameProfileCache,
		ChunkProgressListenerFactory chunkProgressListenerFactory
	) {
		super(
			thread,
			registryHolder,
			levelStorageAccess,
			worldData,
			packRepository,
			minecraft.getProxy(),
			minecraft.getFixerUpper(),
			serverResources,
			minecraftSessionService,
			gameProfileRepository,
			gameProfileCache,
			chunkProgressListenerFactory
		);
		this.setSingleplayerName(minecraft.getUser().getName());
		this.setDemo(minecraft.isDemo());
		this.setMaxBuildHeight(256);
		this.setPlayerList(new IntegratedPlayerList(this, this.registryHolder, this.playerDataStorage));
		this.minecraft = minecraft;
	}

	@Override
	public boolean initServer() {
		LOGGER.info("Starting integrated minecraft server version " + SharedConstants.getCurrentVersion().getName());
		this.setUsesAuthentication(true);
		this.setPvpAllowed(true);
		this.setFlightAllowed(true);
		this.initializeKeyPair();
		this.loadLevel();
		this.setMotd(this.getSingleplayerName() + " - " + this.getWorldData().getLevelName());
		return true;
	}

	@Override
	public void tickServer(BooleanSupplier booleanSupplier) {
		boolean bl = this.paused;
		this.paused = Minecraft.getInstance().getConnection() != null && Minecraft.getInstance().isPaused();
		ProfilerFiller profilerFiller = this.getProfiler();
		if (!bl && this.paused) {
			profilerFiller.push("autoSave");
			LOGGER.info("Saving and pausing game...");
			this.getPlayerList().saveAll();
			this.saveAllChunks(false, false, false);
			profilerFiller.pop();
		}

		if (!this.paused) {
			super.tickServer(booleanSupplier);
			int i = Math.max(2, this.minecraft.options.renderDistance + -1);
			if (i != this.getPlayerList().getViewDistance()) {
				LOGGER.info("Changing view distance to {}, from {}", i, this.getPlayerList().getViewDistance());
				this.getPlayerList().setViewDistance(i);
			}
		}
	}

	@Override
	public boolean shouldRconBroadcast() {
		return true;
	}

	@Override
	public boolean shouldInformAdmins() {
		return true;
	}

	@Override
	public File getServerDirectory() {
		return this.minecraft.gameDirectory;
	}

	@Override
	public boolean isDedicatedServer() {
		return false;
	}

	@Override
	public int getRateLimitPacketsPerSecond() {
		return 0;
	}

	@Override
	public boolean isEpollEnabled() {
		return false;
	}

	@Override
	public void onServerCrash(CrashReport crashReport) {
		this.minecraft.delayCrash(crashReport);
	}

	@Override
	public CrashReport fillReport(CrashReport crashReport) {
		crashReport = super.fillReport(crashReport);
		crashReport.getSystemDetails().setDetail("Type", "Integrated Server (map_client.txt)");
		crashReport.getSystemDetails()
			.setDetail(
				"Is Modded",
				(CrashReportDetail<String>)(() -> (String)this.getModdedStatus()
						.orElse("Probably not. Jar signature remains and both client + server brands are untouched."))
			);
		return crashReport;
	}

	@Override
	public Optional<String> getModdedStatus() {
		String string = ClientBrandRetriever.getClientModName();
		if (!string.equals("vanilla")) {
			return Optional.of("Definitely; Client brand changed to '" + string + "'");
		} else {
			string = this.getServerModName();
			if (!"vanilla".equals(string)) {
				return Optional.of("Definitely; Server brand changed to '" + string + "'");
			} else {
				return Minecraft.class.getSigners() == null ? Optional.of("Very likely; Jar signature invalidated") : Optional.empty();
			}
		}
	}

	@Override
	public void populateSnooper(Snooper snooper) {
		super.populateSnooper(snooper);
		snooper.setDynamicData("snooper_partner", this.minecraft.getSnooper().getToken());
	}

	@Override
	public boolean publishServer(GameType gameType, boolean bl, int i) {
		try {
			this.getConnection().startTcpServerListener(null, i);
			LOGGER.info("Started serving on {}", i);
			this.publishedPort = i;
			this.lanPinger = new LanServerPinger(this.getMotd(), i + "");
			this.lanPinger.start();
			this.getPlayerList().setOverrideGameMode(gameType);
			this.getPlayerList().setAllowCheatsForAllPlayers(bl);
			int j = this.getProfilePermissions(this.minecraft.player.getGameProfile());
			this.minecraft.player.setPermissionLevel(j);

			for (ServerPlayer serverPlayer : this.getPlayerList().getPlayers()) {
				this.getCommands().sendCommands(serverPlayer);
			}

			return true;
		} catch (IOException var7) {
			return false;
		}
	}

	@Override
	public void stopServer() {
		super.stopServer();
		if (this.lanPinger != null) {
			this.lanPinger.interrupt();
			this.lanPinger = null;
		}
	}

	@Override
	public void halt(boolean bl) {
		this.executeBlocking(() -> {
			for (ServerPlayer serverPlayer : Lists.newArrayList(this.getPlayerList().getPlayers())) {
				if (!serverPlayer.getUUID().equals(this.uuid)) {
					this.getPlayerList().remove(serverPlayer);
				}
			}
		});
		super.halt(bl);
		if (this.lanPinger != null) {
			this.lanPinger.interrupt();
			this.lanPinger = null;
		}
	}

	@Override
	public boolean isPublished() {
		return this.publishedPort > -1;
	}

	@Override
	public int getPort() {
		return this.publishedPort;
	}

	@Override
	public void setDefaultGameType(GameType gameType) {
		super.setDefaultGameType(gameType);
		this.getPlayerList().setOverrideGameMode(gameType);
	}

	@Override
	public boolean isCommandBlockEnabled() {
		return true;
	}

	@Override
	public int getOperatorUserPermissionLevel() {
		return 2;
	}

	@Override
	public int getFunctionCompilationLevel() {
		return 2;
	}

	public void setUUID(UUID uUID) {
		this.uuid = uUID;
	}

	@Override
	public boolean isSingleplayerOwner(GameProfile gameProfile) {
		return gameProfile.getName().equalsIgnoreCase(this.getSingleplayerName());
	}

	@Override
	public int getScaledTrackingDistance(int i) {
		return (int)(this.minecraft.options.entityDistanceScaling * (float)i);
	}

	@Override
	public boolean forceSynchronousWrites() {
		return this.minecraft.options.syncWrites;
	}
}
