package net.minecraft.client.server;

import com.google.common.base.MoreObjects;
import com.google.common.collect.Lists;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.GameProfileRepository;
import com.mojang.authlib.minecraft.MinecraftSessionService;
import java.io.File;
import java.io.IOException;
import java.util.UUID;
import java.util.function.BooleanSupplier;
import java.util.function.Supplier;
import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.CrashReport;
import net.minecraft.SharedConstants;
import net.minecraft.SystemReport;
import net.minecraft.client.Minecraft;
import net.minecraft.core.RegistryAccess;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.ServerResources;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.progress.ChunkProgressListenerFactory;
import net.minecraft.server.packs.repository.PackRepository;
import net.minecraft.server.players.GameProfileCache;
import net.minecraft.stats.Stats;
import net.minecraft.util.ModCheck;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.storage.LevelStorageSource;
import net.minecraft.world.level.storage.WorldData;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Environment(EnvType.CLIENT)
public class IntegratedServer extends MinecraftServer {
	private static final Logger LOGGER = LogManager.getLogger();
	private static final int MIN_SIM_DISTANCE = 2;
	private final Minecraft minecraft;
	private boolean paused = true;
	private int publishedPort = -1;
	@Nullable
	private GameType publishedGameType;
	@Nullable
	private LanServerPinger lanPinger;
	@Nullable
	private UUID uuid;
	private int previousSimulationDistance = 0;

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
		this.setPlayerList(new IntegratedPlayerList(this, this.registryHolder, this.playerDataStorage));
		this.minecraft = minecraft;
	}

	@Override
	public boolean initServer() {
		LOGGER.info("Starting integrated minecraft server version {}", SharedConstants.getCurrentVersion().getName());
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
		this.paused = Minecraft.getInstance().isPaused();
		ProfilerFiller profilerFiller = this.getProfiler();
		if (!bl && this.paused) {
			profilerFiller.push("autoSave");
			LOGGER.info("Saving and pausing game...");
			this.saveEverything(false, false, false);
			profilerFiller.pop();
		}

		boolean bl2 = Minecraft.getInstance().getConnection() != null;
		if (bl2 && this.paused) {
			this.tickPaused();
		} else {
			super.tickServer(booleanSupplier);
			int i = Math.max(2, this.minecraft.options.renderDistance);
			if (i != this.getPlayerList().getViewDistance()) {
				LOGGER.info("Changing view distance to {}, from {}", i, this.getPlayerList().getViewDistance());
				this.getPlayerList().setViewDistance(i);
			}

			int j = Math.max(2, this.minecraft.options.simulationDistance);
			if (j != this.previousSimulationDistance) {
				LOGGER.info("Changing simulation distance to {}, from {}", j, this.previousSimulationDistance);
				this.getPlayerList().setSimulationDistance(j);
				this.previousSimulationDistance = j;
			}
		}
	}

	private void tickPaused() {
		for (ServerPlayer serverPlayer : this.getPlayerList().getPlayers()) {
			serverPlayer.awardStat(Stats.TOTAL_WORLD_TIME);
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
	public SystemReport fillServerSystemReport(SystemReport systemReport) {
		systemReport.setDetail("Type", "Integrated Server (map_client.txt)");
		systemReport.setDetail("Is Modded", (Supplier<String>)(() -> this.getModdedStatus().fullDescription()));
		return systemReport;
	}

	@Override
	public ModCheck getModdedStatus() {
		return Minecraft.checkModStatus().merge(super.getModdedStatus());
	}

	@Override
	public boolean publishServer(@Nullable GameType gameType, boolean bl, int i) {
		try {
			this.minecraft.prepareForMultiplayer();
			this.getConnection().startTcpServerListener(null, i);
			LOGGER.info("Started serving on {}", i);
			this.publishedPort = i;
			this.lanPinger = new LanServerPinger(this.getMotd(), i + "");
			this.lanPinger.start();
			this.publishedGameType = gameType;
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
		this.publishedGameType = null;
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

	@Nullable
	@Override
	public GameType getForcedGameType() {
		return this.isPublished() ? MoreObjects.firstNonNull(this.publishedGameType, this.worldData.getGameType()) : null;
	}
}
