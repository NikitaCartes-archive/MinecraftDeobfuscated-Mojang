package net.minecraft.server.dedicated;

import java.nio.file.Path;
import java.util.Properties;
import java.util.concurrent.TimeUnit;
import javax.annotation.Nullable;
import net.minecraft.core.RegistryAccess;
import net.minecraft.util.Mth;
import net.minecraft.world.Difficulty;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.levelgen.WorldGenSettings;

public class DedicatedServerProperties extends Settings<DedicatedServerProperties> {
	public final boolean onlineMode = this.get("online-mode", true);
	public final boolean preventProxyConnections = this.get("prevent-proxy-connections", false);
	public final String serverIp = this.get("server-ip", "");
	public final boolean spawnAnimals = this.get("spawn-animals", true);
	public final boolean spawnNpcs = this.get("spawn-npcs", true);
	public final boolean pvp = this.get("pvp", true);
	public final boolean allowFlight = this.get("allow-flight", false);
	public final String resourcePack = this.get("resource-pack", "");
	public final boolean requireResourcePack = this.get("require-resource-pack", false);
	public final String resourcePackPrompt = this.get("resource-pack-prompt", "");
	public final String motd = this.get("motd", "A Minecraft Server");
	public final boolean forceGameMode = this.get("force-gamemode", false);
	public final boolean enforceWhitelist = this.get("enforce-whitelist", false);
	public final Difficulty difficulty = this.get("difficulty", dispatchNumberOrString(Difficulty::byId, Difficulty::byName), Difficulty::getKey, Difficulty.EASY);
	public final GameType gamemode = this.get("gamemode", dispatchNumberOrString(GameType::byId, GameType::byName), GameType::getName, GameType.SURVIVAL);
	public final String levelName = this.get("level-name", "world");
	public final int serverPort = this.get("server-port", 25565);
	@Nullable
	public final Boolean announcePlayerAchievements = this.getLegacyBoolean("announce-player-achievements");
	public final boolean enableQuery = this.get("enable-query", false);
	public final int queryPort = this.get("query.port", 25565);
	public final boolean enableRcon = this.get("enable-rcon", false);
	public final int rconPort = this.get("rcon.port", 25575);
	public final String rconPassword = this.get("rcon.password", "");
	@Nullable
	public final String resourcePackHash = this.getLegacyString("resource-pack-hash");
	public final String resourcePackSha1 = this.get("resource-pack-sha1", "");
	public final boolean hardcore = this.get("hardcore", false);
	public final boolean allowNether = this.get("allow-nether", true);
	public final boolean spawnMonsters = this.get("spawn-monsters", true);
	public final boolean snooperEnabled;
	public final boolean useNativeTransport;
	public final boolean enableCommandBlock;
	public final int spawnProtection;
	public final int opPermissionLevel;
	public final int functionPermissionLevel;
	public final long maxTickTime;
	public final int rateLimitPacketsPerSecond;
	public final int viewDistance;
	public final int simulationDistance;
	public final int maxPlayers;
	public final int networkCompressionThreshold;
	public final boolean broadcastRconToOps;
	public final boolean broadcastConsoleToOps;
	public final int maxWorldSize;
	public final boolean syncChunkWrites;
	public final boolean enableJmxMonitoring;
	public final boolean enableStatus;
	public final int entityBroadcastRangePercentage;
	public final String textFilteringConfig;
	public final Settings<DedicatedServerProperties>.MutableValue<Integer> playerIdleTimeout;
	public final Settings<DedicatedServerProperties>.MutableValue<Boolean> whiteList;
	@Nullable
	private WorldGenSettings worldGenSettings;

	public DedicatedServerProperties(Properties properties) {
		super(properties);
		if (this.get("snooper-enabled", true)) {
		}

		this.snooperEnabled = false;
		this.useNativeTransport = this.get("use-native-transport", true);
		this.enableCommandBlock = this.get("enable-command-block", false);
		this.spawnProtection = this.get("spawn-protection", 16);
		this.opPermissionLevel = this.get("op-permission-level", 4);
		this.functionPermissionLevel = this.get("function-permission-level", 2);
		this.maxTickTime = this.get("max-tick-time", TimeUnit.MINUTES.toMillis(1L));
		this.rateLimitPacketsPerSecond = this.get("rate-limit", 0);
		this.viewDistance = this.get("view-distance", 10);
		this.simulationDistance = this.get("simulation-distance", 10);
		this.maxPlayers = this.get("max-players", 20);
		this.networkCompressionThreshold = this.get("network-compression-threshold", 256);
		this.broadcastRconToOps = this.get("broadcast-rcon-to-ops", true);
		this.broadcastConsoleToOps = this.get("broadcast-console-to-ops", true);
		this.maxWorldSize = this.get("max-world-size", integer -> Mth.clamp(integer, 1, 29999984), 29999984);
		this.syncChunkWrites = this.get("sync-chunk-writes", true);
		this.enableJmxMonitoring = this.get("enable-jmx-monitoring", false);
		this.enableStatus = this.get("enable-status", true);
		this.entityBroadcastRangePercentage = this.get("entity-broadcast-range-percentage", integer -> Mth.clamp(integer, 10, 1000), 100);
		this.textFilteringConfig = this.get("text-filtering-config", "");
		this.playerIdleTimeout = this.getMutable("player-idle-timeout", 0);
		this.whiteList = this.getMutable("white-list", false);
	}

	public static DedicatedServerProperties fromFile(Path path) {
		return new DedicatedServerProperties(loadFromFile(path));
	}

	protected DedicatedServerProperties reload(RegistryAccess registryAccess, Properties properties) {
		DedicatedServerProperties dedicatedServerProperties = new DedicatedServerProperties(properties);
		dedicatedServerProperties.getWorldGenSettings(registryAccess);
		return dedicatedServerProperties;
	}

	public WorldGenSettings getWorldGenSettings(RegistryAccess registryAccess) {
		if (this.worldGenSettings == null) {
			this.worldGenSettings = WorldGenSettings.create(registryAccess, this.properties);
		}

		return this.worldGenSettings;
	}
}
