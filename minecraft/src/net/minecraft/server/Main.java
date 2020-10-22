package net.minecraft.server;

import com.google.common.collect.ImmutableSet;
import com.mojang.authlib.GameProfileRepository;
import com.mojang.authlib.minecraft.MinecraftSessionService;
import com.mojang.authlib.yggdrasil.YggdrasilAuthenticationService;
import com.mojang.datafixers.DataFixer;
import com.mojang.serialization.Lifecycle;
import java.awt.GraphicsEnvironment;
import java.io.File;
import java.net.Proxy;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.BooleanSupplier;
import joptsimple.OptionParser;
import joptsimple.OptionSet;
import joptsimple.OptionSpec;
import net.minecraft.CrashReport;
import net.minecraft.DefaultUncaughtExceptionHandler;
import net.minecraft.Util;
import net.minecraft.commands.Commands;
import net.minecraft.core.RegistryAccess;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.RegistryReadOps;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.dedicated.DedicatedServer;
import net.minecraft.server.dedicated.DedicatedServerProperties;
import net.minecraft.server.dedicated.DedicatedServerSettings;
import net.minecraft.server.level.progress.LoggerChunkProgressListener;
import net.minecraft.server.packs.repository.FolderRepositorySource;
import net.minecraft.server.packs.repository.PackRepository;
import net.minecraft.server.packs.repository.PackSource;
import net.minecraft.server.packs.repository.ServerPacksSource;
import net.minecraft.server.players.GameProfileCache;
import net.minecraft.util.Mth;
import net.minecraft.util.datafix.DataFixers;
import net.minecraft.util.worldupdate.WorldUpgrader;
import net.minecraft.world.level.DataPackConfig;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelSettings;
import net.minecraft.world.level.levelgen.WorldGenSettings;
import net.minecraft.world.level.storage.LevelResource;
import net.minecraft.world.level.storage.LevelStorageSource;
import net.minecraft.world.level.storage.PrimaryLevelData;
import net.minecraft.world.level.storage.WorldData;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Main {
	private static final Logger LOGGER = LogManager.getLogger();

	public static void main(String[] strings) {
		OptionParser optionParser = new OptionParser();
		OptionSpec<Void> optionSpec = optionParser.accepts("nogui");
		OptionSpec<Void> optionSpec2 = optionParser.accepts("initSettings", "Initializes 'server.properties' and 'eula.txt', then quits");
		OptionSpec<Void> optionSpec3 = optionParser.accepts("demo");
		OptionSpec<Void> optionSpec4 = optionParser.accepts("bonusChest");
		OptionSpec<Void> optionSpec5 = optionParser.accepts("forceUpgrade");
		OptionSpec<Void> optionSpec6 = optionParser.accepts("eraseCache");
		OptionSpec<Void> optionSpec7 = optionParser.accepts("safeMode", "Loads level with vanilla datapack only");
		OptionSpec<Void> optionSpec8 = optionParser.accepts("help").forHelp();
		OptionSpec<String> optionSpec9 = optionParser.accepts("singleplayer").withRequiredArg();
		OptionSpec<String> optionSpec10 = optionParser.accepts("universe").withRequiredArg().defaultsTo(".");
		OptionSpec<String> optionSpec11 = optionParser.accepts("world").withRequiredArg();
		OptionSpec<Integer> optionSpec12 = optionParser.accepts("port").withRequiredArg().<Integer>ofType(Integer.class).defaultsTo(-1);
		OptionSpec<String> optionSpec13 = optionParser.accepts("serverId").withRequiredArg();
		OptionSpec<String> optionSpec14 = optionParser.nonOptions();

		try {
			OptionSet optionSet = optionParser.parse(strings);
			if (optionSet.has(optionSpec8)) {
				optionParser.printHelpOn(System.err);
				return;
			}

			CrashReport.preload();
			Bootstrap.bootStrap();
			Bootstrap.validate();
			Util.startTimerHackThread();
			RegistryAccess.RegistryHolder registryHolder = RegistryAccess.builtin();
			Path path = Paths.get("server.properties");
			DedicatedServerSettings dedicatedServerSettings = new DedicatedServerSettings(registryHolder, path);
			dedicatedServerSettings.forceSave();
			Path path2 = Paths.get("eula.txt");
			Eula eula = new Eula(path2);
			if (optionSet.has(optionSpec2)) {
				LOGGER.info("Initialized '{}' and '{}'", path.toAbsolutePath(), path2.toAbsolutePath());
				return;
			}

			if (!eula.hasAgreedToEULA()) {
				LOGGER.info("You need to agree to the EULA in order to run the server. Go to eula.txt for more info.");
				return;
			}

			File file = new File(optionSet.valueOf(optionSpec10));
			YggdrasilAuthenticationService yggdrasilAuthenticationService = new YggdrasilAuthenticationService(Proxy.NO_PROXY);
			MinecraftSessionService minecraftSessionService = yggdrasilAuthenticationService.createMinecraftSessionService();
			GameProfileRepository gameProfileRepository = yggdrasilAuthenticationService.createProfileRepository();
			GameProfileCache gameProfileCache = new GameProfileCache(gameProfileRepository, new File(file, MinecraftServer.USERID_CACHE_FILE.getName()));
			String string = (String)Optional.ofNullable(optionSet.valueOf(optionSpec11)).orElse(dedicatedServerSettings.getProperties().levelName);
			LevelStorageSource levelStorageSource = LevelStorageSource.createDefault(file.toPath());
			LevelStorageSource.LevelStorageAccess levelStorageAccess = levelStorageSource.createAccess(string);
			MinecraftServer.convertFromRegionFormatIfNeeded(levelStorageAccess);
			DataPackConfig dataPackConfig = levelStorageAccess.getDataPacks();
			boolean bl = optionSet.has(optionSpec7);
			if (bl) {
				LOGGER.warn("Safe mode active, only vanilla datapack will be loaded");
			}

			PackRepository packRepository = new PackRepository(
				new ServerPacksSource(), new FolderRepositorySource(levelStorageAccess.getLevelPath(LevelResource.DATAPACK_DIR).toFile(), PackSource.WORLD)
			);
			DataPackConfig dataPackConfig2 = MinecraftServer.configurePackRepository(
				packRepository, dataPackConfig == null ? DataPackConfig.DEFAULT : dataPackConfig, bl
			);
			CompletableFuture<ServerResources> completableFuture = ServerResources.loadResources(
				packRepository.openAllSelected(),
				Commands.CommandSelection.DEDICATED,
				dedicatedServerSettings.getProperties().functionPermissionLevel,
				Util.backgroundExecutor(),
				Runnable::run
			);

			ServerResources serverResources;
			try {
				serverResources = (ServerResources)completableFuture.get();
			} catch (Exception var41) {
				LOGGER.warn(
					"Failed to load datapacks, can't proceed with server load. You can either fix your datapacks or reset to vanilla with --safeMode", (Throwable)var41
				);
				packRepository.close();
				return;
			}

			serverResources.updateGlobals();
			RegistryReadOps<Tag> registryReadOps = RegistryReadOps.create(NbtOps.INSTANCE, serverResources.getResourceManager(), registryHolder);
			WorldData worldData = levelStorageAccess.getDataTag(registryReadOps, dataPackConfig2);
			if (worldData == null) {
				LevelSettings levelSettings;
				WorldGenSettings worldGenSettings;
				if (optionSet.has(optionSpec3)) {
					levelSettings = MinecraftServer.DEMO_SETTINGS;
					worldGenSettings = WorldGenSettings.demoSettings(registryHolder);
				} else {
					DedicatedServerProperties dedicatedServerProperties = dedicatedServerSettings.getProperties();
					levelSettings = new LevelSettings(
						dedicatedServerProperties.levelName,
						dedicatedServerProperties.gamemode,
						dedicatedServerProperties.hardcore,
						dedicatedServerProperties.difficulty,
						false,
						new GameRules(),
						dataPackConfig2
					);
					worldGenSettings = optionSet.has(optionSpec4) ? dedicatedServerProperties.worldGenSettings.withBonusChest() : dedicatedServerProperties.worldGenSettings;
				}

				worldData = new PrimaryLevelData(levelSettings, worldGenSettings, Lifecycle.stable());
			}

			if (optionSet.has(optionSpec5)) {
				forceUpgrade(levelStorageAccess, DataFixers.getDataFixer(), optionSet.has(optionSpec6), () -> true, worldData.worldGenSettings().levels());
			}

			levelStorageAccess.saveDataTag(registryHolder, worldData);
			WorldData worldData2 = worldData;
			final DedicatedServer dedicatedServer = MinecraftServer.spin(
				threadx -> {
					DedicatedServer dedicatedServerx = new DedicatedServer(
						threadx,
						registryHolder,
						levelStorageAccess,
						packRepository,
						serverResources,
						worldData2,
						dedicatedServerSettings,
						DataFixers.getDataFixer(),
						minecraftSessionService,
						gameProfileRepository,
						gameProfileCache,
						LoggerChunkProgressListener::new
					);
					dedicatedServerx.setSingleplayerName(optionSet.valueOf(optionSpec9));
					dedicatedServerx.setPort(optionSet.valueOf(optionSpec12));
					dedicatedServerx.setDemo(optionSet.has(optionSpec3));
					dedicatedServerx.setId(optionSet.valueOf(optionSpec13));
					boolean blx = !optionSet.has(optionSpec) && !optionSet.valuesOf(optionSpec14).contains("nogui");
					if (blx && !GraphicsEnvironment.isHeadless()) {
						dedicatedServerx.showGui();
					}

					return dedicatedServerx;
				}
			);
			Thread thread = new Thread("Server Shutdown Thread") {
				public void run() {
					dedicatedServer.halt(true);
				}
			};
			thread.setUncaughtExceptionHandler(new DefaultUncaughtExceptionHandler(LOGGER));
			Runtime.getRuntime().addShutdownHook(thread);
		} catch (Exception var42) {
			LOGGER.fatal("Failed to start the minecraft server", (Throwable)var42);
		}
	}

	private static void forceUpgrade(
		LevelStorageSource.LevelStorageAccess levelStorageAccess,
		DataFixer dataFixer,
		boolean bl,
		BooleanSupplier booleanSupplier,
		ImmutableSet<ResourceKey<Level>> immutableSet
	) {
		LOGGER.info("Forcing world upgrade!");
		WorldUpgrader worldUpgrader = new WorldUpgrader(levelStorageAccess, dataFixer, immutableSet, bl);
		Component component = null;

		while (!worldUpgrader.isFinished()) {
			Component component2 = worldUpgrader.getStatus();
			if (component != component2) {
				component = component2;
				LOGGER.info(worldUpgrader.getStatus().getString());
			}

			int i = worldUpgrader.getTotalChunks();
			if (i > 0) {
				int j = worldUpgrader.getConverted() + worldUpgrader.getSkipped();
				LOGGER.info("{}% completed ({} / {} chunks)...", Mth.floor((float)j / (float)i * 100.0F), j, i);
			}

			if (!booleanSupplier.getAsBoolean()) {
				worldUpgrader.cancel();
			} else {
				try {
					Thread.sleep(1000L);
				} catch (InterruptedException var10) {
				}
			}
		}
	}
}
