package net.minecraft.server.dedicated;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.GameProfileRepository;
import com.mojang.authlib.minecraft.MinecraftSessionService;
import com.mojang.datafixers.DataFixer;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.Proxy;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.function.BooleanSupplier;
import java.util.regex.Pattern;
import javax.annotation.Nullable;
import net.minecraft.CrashReport;
import net.minecraft.CrashReportDetail;
import net.minecraft.DefaultUncaughtExceptionHandler;
import net.minecraft.DefaultUncaughtExceptionHandlerWithName;
import net.minecraft.SharedConstants;
import net.minecraft.Util;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.core.RegistryAccess;
import net.minecraft.server.ConsoleInput;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.ServerInterface;
import net.minecraft.server.ServerResources;
import net.minecraft.server.gui.MinecraftServerGui;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.progress.ChunkProgressListenerFactory;
import net.minecraft.server.network.TextFilter;
import net.minecraft.server.network.TextFilterClient;
import net.minecraft.server.packs.repository.PackRepository;
import net.minecraft.server.players.GameProfileCache;
import net.minecraft.server.players.OldUsersConverter;
import net.minecraft.server.rcon.RconConsoleSource;
import net.minecraft.server.rcon.thread.QueryThreadGs4;
import net.minecraft.server.rcon.thread.RconThread;
import net.minecraft.util.Mth;
import net.minecraft.util.monitoring.jmx.MinecraftServerStatistics;
import net.minecraft.world.Snooper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.SkullBlockEntity;
import net.minecraft.world.level.storage.LevelStorageSource;
import net.minecraft.world.level.storage.WorldData;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class DedicatedServer extends MinecraftServer implements ServerInterface {
	private static final Logger LOGGER = LogManager.getLogger();
	private static final Pattern SHA1 = Pattern.compile("^[a-fA-F0-9]{40}$");
	private final List<ConsoleInput> consoleInput = Collections.synchronizedList(Lists.newArrayList());
	private QueryThreadGs4 queryThreadGs4;
	private final RconConsoleSource rconConsoleSource;
	private RconThread rconThread;
	private final DedicatedServerSettings settings;
	@Nullable
	private MinecraftServerGui gui;
	@Nullable
	private final TextFilterClient textFilterClient;

	public DedicatedServer(
		Thread thread,
		RegistryAccess.RegistryHolder registryHolder,
		LevelStorageSource.LevelStorageAccess levelStorageAccess,
		PackRepository packRepository,
		ServerResources serverResources,
		WorldData worldData,
		DedicatedServerSettings dedicatedServerSettings,
		DataFixer dataFixer,
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
			Proxy.NO_PROXY,
			dataFixer,
			serverResources,
			minecraftSessionService,
			gameProfileRepository,
			gameProfileCache,
			chunkProgressListenerFactory
		);
		this.settings = dedicatedServerSettings;
		this.rconConsoleSource = new RconConsoleSource(this);
		this.textFilterClient = TextFilterClient.createFromConfig(dedicatedServerSettings.getProperties().textFilteringConfig);
	}

	@Override
	public boolean initServer() throws IOException {
		Thread thread = new Thread("Server console handler") {
			public void run() {
				BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(System.in, StandardCharsets.UTF_8));

				String string;
				try {
					while (!DedicatedServer.this.isStopped() && DedicatedServer.this.isRunning() && (string = bufferedReader.readLine()) != null) {
						DedicatedServer.this.handleConsoleInput(string, DedicatedServer.this.createCommandSourceStack());
					}
				} catch (IOException var4) {
					DedicatedServer.LOGGER.error("Exception handling console input", (Throwable)var4);
				}
			}
		};
		thread.setDaemon(true);
		thread.setUncaughtExceptionHandler(new DefaultUncaughtExceptionHandler(LOGGER));
		thread.start();
		LOGGER.info("Starting minecraft server version {}", SharedConstants.getCurrentVersion().getName());
		if (Runtime.getRuntime().maxMemory() / 1024L / 1024L < 512L) {
			LOGGER.warn("To start the server with more ram, launch it as \"java -Xmx1024M -Xms1024M -jar minecraft_server.jar\"");
		}

		LOGGER.info("Loading properties");
		DedicatedServerProperties dedicatedServerProperties = this.settings.getProperties();
		if (this.isSingleplayer()) {
			this.setLocalIp("127.0.0.1");
		} else {
			this.setUsesAuthentication(dedicatedServerProperties.onlineMode);
			this.setPreventProxyConnections(dedicatedServerProperties.preventProxyConnections);
			this.setLocalIp(dedicatedServerProperties.serverIp);
		}

		this.setPvpAllowed(dedicatedServerProperties.pvp);
		this.setFlightAllowed(dedicatedServerProperties.allowFlight);
		this.setResourcePack(dedicatedServerProperties.resourcePack, this.getPackHash());
		this.setMotd(dedicatedServerProperties.motd);
		super.setPlayerIdleTimeout(dedicatedServerProperties.playerIdleTimeout.get());
		this.setEnforceWhitelist(dedicatedServerProperties.enforceWhitelist);
		this.worldData.setGameType(dedicatedServerProperties.gamemode);
		LOGGER.info("Default game type: {}", dedicatedServerProperties.gamemode);
		InetAddress inetAddress = null;
		if (!this.getLocalIp().isEmpty()) {
			inetAddress = InetAddress.getByName(this.getLocalIp());
		}

		if (this.getPort() < 0) {
			this.setPort(dedicatedServerProperties.serverPort);
		}

		this.initializeKeyPair();
		LOGGER.info("Starting Minecraft server on {}:{}", this.getLocalIp().isEmpty() ? "*" : this.getLocalIp(), this.getPort());

		try {
			this.getConnection().startTcpServerListener(inetAddress, this.getPort());
		} catch (IOException var10) {
			LOGGER.warn("**** FAILED TO BIND TO PORT!");
			LOGGER.warn("The exception was: {}", var10.toString());
			LOGGER.warn("Perhaps a server is already running on that port?");
			return false;
		}

		if (!this.usesAuthentication()) {
			LOGGER.warn("**** SERVER IS RUNNING IN OFFLINE/INSECURE MODE!");
			LOGGER.warn("The server will make no attempt to authenticate usernames. Beware.");
			LOGGER.warn(
				"While this makes the game possible to play without internet access, it also opens up the ability for hackers to connect with any username they choose."
			);
			LOGGER.warn("To change this, set \"online-mode\" to \"true\" in the server.properties file.");
		}

		if (this.convertOldUsers()) {
			this.getProfileCache().save();
		}

		if (!OldUsersConverter.serverReadyAfterUserconversion(this)) {
			return false;
		} else {
			this.setPlayerList(new DedicatedPlayerList(this, this.registryHolder, this.playerDataStorage));
			long l = Util.getNanos();
			SkullBlockEntity.setProfileCache(this.getProfileCache());
			SkullBlockEntity.setSessionService(this.getSessionService());
			GameProfileCache.setUsesAuthentication(this.usesAuthentication());
			LOGGER.info("Preparing level \"{}\"", this.getLevelIdName());
			this.loadLevel();
			long m = Util.getNanos() - l;
			String string = String.format(Locale.ROOT, "%.3fs", (double)m / 1.0E9);
			LOGGER.info("Done ({})! For help, type \"help\"", string);
			if (dedicatedServerProperties.announcePlayerAchievements != null) {
				this.getGameRules().getRule(GameRules.RULE_ANNOUNCE_ADVANCEMENTS).set(dedicatedServerProperties.announcePlayerAchievements, this);
			}

			if (dedicatedServerProperties.enableQuery) {
				LOGGER.info("Starting GS4 status listener");
				this.queryThreadGs4 = QueryThreadGs4.create(this);
			}

			if (dedicatedServerProperties.enableRcon) {
				LOGGER.info("Starting remote control listener");
				this.rconThread = RconThread.create(this);
			}

			if (this.getMaxTickLength() > 0L) {
				Thread thread2 = new Thread(new ServerWatchdog(this));
				thread2.setUncaughtExceptionHandler(new DefaultUncaughtExceptionHandlerWithName(LOGGER));
				thread2.setName("Server Watchdog");
				thread2.setDaemon(true);
				thread2.start();
			}

			Items.AIR.fillItemCategory(CreativeModeTab.TAB_SEARCH, NonNullList.create());
			if (dedicatedServerProperties.enableJmxMonitoring) {
				MinecraftServerStatistics.registerJmxMonitoring(this);
				LOGGER.info("JMX monitoring enabled");
			}

			return true;
		}
	}

	@Override
	public boolean isSpawningAnimals() {
		return this.getProperties().spawnAnimals && super.isSpawningAnimals();
	}

	@Override
	public boolean isSpawningMonsters() {
		return this.settings.getProperties().spawnMonsters && super.isSpawningMonsters();
	}

	@Override
	public boolean areNpcsEnabled() {
		return this.settings.getProperties().spawnNpcs && super.areNpcsEnabled();
	}

	public String getPackHash() {
		DedicatedServerProperties dedicatedServerProperties = this.settings.getProperties();
		String string;
		if (!dedicatedServerProperties.resourcePackSha1.isEmpty()) {
			string = dedicatedServerProperties.resourcePackSha1;
			if (!Strings.isNullOrEmpty(dedicatedServerProperties.resourcePackHash)) {
				LOGGER.warn("resource-pack-hash is deprecated and found along side resource-pack-sha1. resource-pack-hash will be ignored.");
			}
		} else if (!Strings.isNullOrEmpty(dedicatedServerProperties.resourcePackHash)) {
			LOGGER.warn("resource-pack-hash is deprecated. Please use resource-pack-sha1 instead.");
			string = dedicatedServerProperties.resourcePackHash;
		} else {
			string = "";
		}

		if (!string.isEmpty() && !SHA1.matcher(string).matches()) {
			LOGGER.warn("Invalid sha1 for ressource-pack-sha1");
		}

		if (!dedicatedServerProperties.resourcePack.isEmpty() && string.isEmpty()) {
			LOGGER.warn("You specified a resource pack without providing a sha1 hash. Pack will be updated on the client only if you change the name of the pack.");
		}

		return string;
	}

	@Override
	public DedicatedServerProperties getProperties() {
		return this.settings.getProperties();
	}

	@Override
	public void forceDifficulty() {
		this.setDifficulty(this.getProperties().difficulty, true);
	}

	@Override
	public boolean isHardcore() {
		return this.getProperties().hardcore;
	}

	@Override
	public CrashReport fillReport(CrashReport crashReport) {
		crashReport = super.fillReport(crashReport);
		crashReport.getSystemDetails().setDetail("Is Modded", (CrashReportDetail<String>)(() -> (String)this.getModdedStatus().orElse("Unknown (can't tell)")));
		crashReport.getSystemDetails().setDetail("Type", (CrashReportDetail<String>)(() -> "Dedicated Server (map_server.txt)"));
		return crashReport;
	}

	@Override
	public Optional<String> getModdedStatus() {
		String string = this.getServerModName();
		return !"vanilla".equals(string) ? Optional.of("Definitely; Server brand changed to '" + string + "'") : Optional.empty();
	}

	@Override
	public void onServerExit() {
		if (this.textFilterClient != null) {
			this.textFilterClient.close();
		}

		if (this.gui != null) {
			this.gui.close();
		}

		if (this.rconThread != null) {
			this.rconThread.stop();
		}

		if (this.queryThreadGs4 != null) {
			this.queryThreadGs4.stop();
		}
	}

	@Override
	public void tickChildren(BooleanSupplier booleanSupplier) {
		super.tickChildren(booleanSupplier);
		this.handleConsoleInputs();
	}

	@Override
	public boolean isNetherEnabled() {
		return this.getProperties().allowNether;
	}

	@Override
	public void populateSnooper(Snooper snooper) {
		snooper.setDynamicData("whitelist_enabled", this.getPlayerList().isUsingWhitelist());
		snooper.setDynamicData("whitelist_count", this.getPlayerList().getWhiteListNames().length);
		super.populateSnooper(snooper);
	}

	public void handleConsoleInput(String string, CommandSourceStack commandSourceStack) {
		this.consoleInput.add(new ConsoleInput(string, commandSourceStack));
	}

	public void handleConsoleInputs() {
		while (!this.consoleInput.isEmpty()) {
			ConsoleInput consoleInput = (ConsoleInput)this.consoleInput.remove(0);
			this.getCommands().performCommand(consoleInput.source, consoleInput.msg);
		}
	}

	@Override
	public boolean isDedicatedServer() {
		return true;
	}

	@Override
	public int getRateLimitPacketsPerSecond() {
		return this.getProperties().rateLimitPacketsPerSecond;
	}

	@Override
	public boolean isEpollEnabled() {
		return this.getProperties().useNativeTransport;
	}

	public DedicatedPlayerList getPlayerList() {
		return (DedicatedPlayerList)super.getPlayerList();
	}

	@Override
	public boolean isPublished() {
		return true;
	}

	@Override
	public String getServerIp() {
		return this.getLocalIp();
	}

	@Override
	public int getServerPort() {
		return this.getPort();
	}

	@Override
	public String getServerName() {
		return this.getMotd();
	}

	public void showGui() {
		if (this.gui == null) {
			this.gui = MinecraftServerGui.showFrameFor(this);
		}
	}

	@Override
	public boolean hasGui() {
		return this.gui != null;
	}

	@Override
	public boolean isCommandBlockEnabled() {
		return this.getProperties().enableCommandBlock;
	}

	@Override
	public int getSpawnProtectionRadius() {
		return this.getProperties().spawnProtection;
	}

	@Override
	public boolean isUnderSpawnProtection(ServerLevel serverLevel, BlockPos blockPos, Player player) {
		if (serverLevel.dimension() != Level.OVERWORLD) {
			return false;
		} else if (this.getPlayerList().getOps().isEmpty()) {
			return false;
		} else if (this.getPlayerList().isOp(player.getGameProfile())) {
			return false;
		} else if (this.getSpawnProtectionRadius() <= 0) {
			return false;
		} else {
			BlockPos blockPos2 = serverLevel.getSharedSpawnPos();
			int i = Mth.abs(blockPos.getX() - blockPos2.getX());
			int j = Mth.abs(blockPos.getZ() - blockPos2.getZ());
			int k = Math.max(i, j);
			return k <= this.getSpawnProtectionRadius();
		}
	}

	@Override
	public boolean repliesToStatus() {
		return this.getProperties().enableStatus;
	}

	@Override
	public int getOperatorUserPermissionLevel() {
		return this.getProperties().opPermissionLevel;
	}

	@Override
	public int getFunctionCompilationLevel() {
		return this.getProperties().functionPermissionLevel;
	}

	@Override
	public void setPlayerIdleTimeout(int i) {
		super.setPlayerIdleTimeout(i);
		this.settings.update(dedicatedServerProperties -> dedicatedServerProperties.playerIdleTimeout.update(this.registryAccess(), i));
	}

	@Override
	public boolean shouldRconBroadcast() {
		return this.getProperties().broadcastRconToOps;
	}

	@Override
	public boolean shouldInformAdmins() {
		return this.getProperties().broadcastConsoleToOps;
	}

	@Override
	public int getAbsoluteMaxWorldSize() {
		return this.getProperties().maxWorldSize;
	}

	@Override
	public int getCompressionThreshold() {
		return this.getProperties().networkCompressionThreshold;
	}

	protected boolean convertOldUsers() {
		boolean bl = false;

		for (int i = 0; !bl && i <= 2; i++) {
			if (i > 0) {
				LOGGER.warn("Encountered a problem while converting the user banlist, retrying in a few seconds");
				this.waitForRetry();
			}

			bl = OldUsersConverter.convertUserBanlist(this);
		}

		boolean bl2 = false;

		for (int var7 = 0; !bl2 && var7 <= 2; var7++) {
			if (var7 > 0) {
				LOGGER.warn("Encountered a problem while converting the ip banlist, retrying in a few seconds");
				this.waitForRetry();
			}

			bl2 = OldUsersConverter.convertIpBanlist(this);
		}

		boolean bl3 = false;

		for (int var8 = 0; !bl3 && var8 <= 2; var8++) {
			if (var8 > 0) {
				LOGGER.warn("Encountered a problem while converting the op list, retrying in a few seconds");
				this.waitForRetry();
			}

			bl3 = OldUsersConverter.convertOpsList(this);
		}

		boolean bl4 = false;

		for (int var9 = 0; !bl4 && var9 <= 2; var9++) {
			if (var9 > 0) {
				LOGGER.warn("Encountered a problem while converting the whitelist, retrying in a few seconds");
				this.waitForRetry();
			}

			bl4 = OldUsersConverter.convertWhiteList(this);
		}

		boolean bl5 = false;

		for (int var10 = 0; !bl5 && var10 <= 2; var10++) {
			if (var10 > 0) {
				LOGGER.warn("Encountered a problem while converting the player save files, retrying in a few seconds");
				this.waitForRetry();
			}

			bl5 = OldUsersConverter.convertPlayers(this);
		}

		return bl || bl2 || bl3 || bl4 || bl5;
	}

	private void waitForRetry() {
		try {
			Thread.sleep(5000L);
		} catch (InterruptedException var2) {
		}
	}

	public long getMaxTickLength() {
		return this.getProperties().maxTickTime;
	}

	@Override
	public String getPluginNames() {
		return "";
	}

	@Override
	public String runCommand(String string) {
		this.rconConsoleSource.prepareForCommand();
		this.executeBlocking(() -> this.getCommands().performCommand(this.rconConsoleSource.createCommandSourceStack(), string));
		return this.rconConsoleSource.getCommandResponse();
	}

	public void storeUsingWhiteList(boolean bl) {
		this.settings.update(dedicatedServerProperties -> dedicatedServerProperties.whiteList.update(this.registryAccess(), bl));
	}

	@Override
	public void stopServer() {
		super.stopServer();
		Util.shutdownExecutors();
	}

	@Override
	public boolean isSingleplayerOwner(GameProfile gameProfile) {
		return false;
	}

	@Override
	public int getScaledTrackingDistance(int i) {
		return this.getProperties().entityBroadcastRangePercentage * i / 100;
	}

	@Override
	public String getLevelIdName() {
		return this.storageSource.getLevelId();
	}

	@Override
	public boolean forceSynchronousWrites() {
		return this.settings.getProperties().syncChunkWrites;
	}

	@Override
	public TextFilter createTextFilterForPlayer(ServerPlayer serverPlayer) {
		return this.textFilterClient != null ? this.textFilterClient.createContext(serverPlayer.getGameProfile()) : TextFilter.DUMMY;
	}

	@Override
	public boolean isResourcePackRequired() {
		return this.settings.getProperties().requireResourcePack;
	}

	@Nullable
	@Override
	public GameType getForcedGameType() {
		return this.settings.getProperties().forceGameMode ? this.worldData.getGameType() : null;
	}
}
