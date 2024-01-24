package net.minecraft.server.dedicated;

import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.JsonOps;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;
import javax.annotation.Nullable;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.Mth;
import net.minecraft.world.Difficulty;
import net.minecraft.world.level.DataPackConfig;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.WorldDataConfiguration;
import net.minecraft.world.level.levelgen.FlatLevelSource;
import net.minecraft.world.level.levelgen.WorldDimensions;
import net.minecraft.world.level.levelgen.WorldOptions;
import net.minecraft.world.level.levelgen.flat.FlatLevelGeneratorSettings;
import net.minecraft.world.level.levelgen.presets.WorldPreset;
import net.minecraft.world.level.levelgen.presets.WorldPresets;
import org.slf4j.Logger;

public class DedicatedServerProperties extends Settings<DedicatedServerProperties> {
	static final Logger LOGGER = LogUtils.getLogger();
	private static final Pattern SHA1 = Pattern.compile("^[a-fA-F0-9]{40}$");
	private static final Splitter COMMA_SPLITTER = Splitter.on(',').trimResults();
	public final boolean onlineMode = this.get("online-mode", true);
	public final boolean preventProxyConnections = this.get("prevent-proxy-connections", false);
	public final String serverIp = this.get("server-ip", "");
	public final boolean spawnAnimals = this.get("spawn-animals", true);
	public final boolean spawnNpcs = this.get("spawn-npcs", true);
	public final boolean pvp = this.get("pvp", true);
	public final boolean allowFlight = this.get("allow-flight", false);
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
	public final boolean hardcore = this.get("hardcore", false);
	public final boolean allowNether = this.get("allow-nether", true);
	public final boolean spawnMonsters = this.get("spawn-monsters", true);
	public final boolean useNativeTransport = this.get("use-native-transport", true);
	public final boolean enableCommandBlock = this.get("enable-command-block", false);
	public final int spawnProtection = this.get("spawn-protection", 16);
	public final int opPermissionLevel = this.get("op-permission-level", 4);
	public final int functionPermissionLevel = this.get("function-permission-level", 2);
	public final long maxTickTime = this.get("max-tick-time", TimeUnit.MINUTES.toMillis(1L));
	public final int maxChainedNeighborUpdates = this.get("max-chained-neighbor-updates", 1000000);
	public final int rateLimitPacketsPerSecond = this.get("rate-limit", 0);
	public final int viewDistance = this.get("view-distance", 10);
	public final int simulationDistance = this.get("simulation-distance", 10);
	public final int maxPlayers = this.get("max-players", 20);
	public final int networkCompressionThreshold = this.get("network-compression-threshold", 256);
	public final boolean broadcastRconToOps = this.get("broadcast-rcon-to-ops", true);
	public final boolean broadcastConsoleToOps = this.get("broadcast-console-to-ops", true);
	public final int maxWorldSize = this.get("max-world-size", integer -> Mth.clamp(integer, 1, 29999984), 29999984);
	public final boolean syncChunkWrites = this.get("sync-chunk-writes", true);
	public final String regionFileComression = this.get("region-file-compression", "deflate");
	public final boolean enableJmxMonitoring = this.get("enable-jmx-monitoring", false);
	public final boolean enableStatus = this.get("enable-status", true);
	public final boolean hideOnlinePlayers = this.get("hide-online-players", false);
	public final int entityBroadcastRangePercentage = this.get("entity-broadcast-range-percentage", integer -> Mth.clamp(integer, 10, 1000), 100);
	public final String textFilteringConfig = this.get("text-filtering-config", "");
	public final Optional<MinecraftServer.ServerResourcePackInfo> serverResourcePackInfo;
	public final DataPackConfig initialDataPackConfiguration;
	public final Settings<DedicatedServerProperties>.MutableValue<Integer> playerIdleTimeout = this.getMutable("player-idle-timeout", 0);
	public final Settings<DedicatedServerProperties>.MutableValue<Boolean> whiteList = this.getMutable("white-list", false);
	public final boolean enforceSecureProfile = this.get("enforce-secure-profile", true);
	public final boolean logIPs = this.get("log-ips", true);
	private final DedicatedServerProperties.WorldDimensionData worldDimensionData;
	public final WorldOptions worldOptions;
	public boolean acceptsTransfers = this.get("accepts-transfers", false);

	public DedicatedServerProperties(Properties properties) {
		super(properties);
		String string = this.get("level-seed", "");
		boolean bl = this.get("generate-structures", true);
		long l = WorldOptions.parseSeed(string).orElse(WorldOptions.randomSeed());
		this.worldOptions = new WorldOptions(l, bl, false);
		this.worldDimensionData = new DedicatedServerProperties.WorldDimensionData(
			this.get("generator-settings", stringx -> GsonHelper.parse(!stringx.isEmpty() ? stringx : "{}"), new JsonObject()),
			this.get("level-type", stringx -> stringx.toLowerCase(Locale.ROOT), WorldPresets.NORMAL.location().toString())
		);
		this.serverResourcePackInfo = getServerPackInfo(
			this.get("resource-pack-id", ""),
			this.get("resource-pack", ""),
			this.get("resource-pack-sha1", ""),
			this.getLegacyString("resource-pack-hash"),
			this.get("require-resource-pack", false),
			this.get("resource-pack-prompt", "")
		);
		this.initialDataPackConfiguration = getDatapackConfig(
			this.get("initial-enabled-packs", String.join(",", WorldDataConfiguration.DEFAULT.dataPacks().getEnabled())),
			this.get("initial-disabled-packs", String.join(",", WorldDataConfiguration.DEFAULT.dataPacks().getDisabled()))
		);
	}

	public static DedicatedServerProperties fromFile(Path path) {
		return new DedicatedServerProperties(loadFromFile(path));
	}

	protected DedicatedServerProperties reload(RegistryAccess registryAccess, Properties properties) {
		return new DedicatedServerProperties(properties);
	}

	@Nullable
	private static Component parseResourcePackPrompt(String string) {
		if (!Strings.isNullOrEmpty(string)) {
			try {
				return Component.Serializer.fromJson(string);
			} catch (Exception var2) {
				LOGGER.warn("Failed to parse resource pack prompt '{}'", string, var2);
			}
		}

		return null;
	}

	private static Optional<MinecraftServer.ServerResourcePackInfo> getServerPackInfo(
		String string, String string2, String string3, @Nullable String string4, boolean bl, String string5
	) {
		if (string2.isEmpty()) {
			return Optional.empty();
		} else {
			String string6;
			if (!string3.isEmpty()) {
				string6 = string3;
				if (!Strings.isNullOrEmpty(string4)) {
					LOGGER.warn("resource-pack-hash is deprecated and found along side resource-pack-sha1. resource-pack-hash will be ignored.");
				}
			} else if (!Strings.isNullOrEmpty(string4)) {
				LOGGER.warn("resource-pack-hash is deprecated. Please use resource-pack-sha1 instead.");
				string6 = string4;
			} else {
				string6 = "";
			}

			if (string6.isEmpty()) {
				LOGGER.warn("You specified a resource pack without providing a sha1 hash. Pack will be updated on the client only if you change the name of the pack.");
			} else if (!SHA1.matcher(string6).matches()) {
				LOGGER.warn("Invalid sha1 for resource-pack-sha1");
			}

			Component component = parseResourcePackPrompt(string5);
			UUID uUID;
			if (string.isEmpty()) {
				uUID = UUID.nameUUIDFromBytes(string2.getBytes(StandardCharsets.UTF_8));
				LOGGER.warn("resource-pack-id missing, using default of {}", uUID);
			} else {
				try {
					uUID = UUID.fromString(string);
				} catch (IllegalArgumentException var10) {
					LOGGER.warn("Failed to parse '{}' into UUID", string);
					return Optional.empty();
				}
			}

			return Optional.of(new MinecraftServer.ServerResourcePackInfo(uUID, string2, string6, bl, component));
		}
	}

	private static DataPackConfig getDatapackConfig(String string, String string2) {
		List<String> list = COMMA_SPLITTER.splitToList(string);
		List<String> list2 = COMMA_SPLITTER.splitToList(string2);
		return new DataPackConfig(list, list2);
	}

	public WorldDimensions createDimensions(RegistryAccess registryAccess) {
		return this.worldDimensionData.create(registryAccess);
	}

	static record WorldDimensionData(JsonObject generatorSettings, String levelType) {
		private static final Map<String, ResourceKey<WorldPreset>> LEGACY_PRESET_NAMES = Map.of(
			"default", WorldPresets.NORMAL, "largebiomes", WorldPresets.LARGE_BIOMES
		);

		public WorldDimensions create(RegistryAccess registryAccess) {
			Registry<WorldPreset> registry = registryAccess.registryOrThrow(Registries.WORLD_PRESET);
			Holder.Reference<WorldPreset> reference = (Holder.Reference<WorldPreset>)registry.getHolder(WorldPresets.NORMAL)
				.or(() -> registry.holders().findAny())
				.orElseThrow(() -> new IllegalStateException("Invalid datapack contents: can't find default preset"));
			Holder<WorldPreset> holder = (Holder<WorldPreset>)Optional.ofNullable(ResourceLocation.tryParse(this.levelType))
				.map(resourceLocation -> ResourceKey.create(Registries.WORLD_PRESET, resourceLocation))
				.or(() -> Optional.ofNullable((ResourceKey)LEGACY_PRESET_NAMES.get(this.levelType)))
				.flatMap(registry::getHolder)
				.orElseGet(() -> {
					DedicatedServerProperties.LOGGER.warn("Failed to parse level-type {}, defaulting to {}", this.levelType, reference.key().location());
					return reference;
				});
			WorldDimensions worldDimensions = holder.value().createWorldDimensions();
			if (holder.is(WorldPresets.FLAT)) {
				RegistryOps<JsonElement> registryOps = RegistryOps.create(JsonOps.INSTANCE, registryAccess);
				Optional<FlatLevelGeneratorSettings> optional = FlatLevelGeneratorSettings.CODEC
					.parse(new Dynamic<>(registryOps, this.generatorSettings()))
					.resultOrPartial(DedicatedServerProperties.LOGGER::error);
				if (optional.isPresent()) {
					return worldDimensions.replaceOverworldGenerator(registryAccess, new FlatLevelSource((FlatLevelGeneratorSettings)optional.get()));
				}
			}

			return worldDimensions;
		}
	}
}
