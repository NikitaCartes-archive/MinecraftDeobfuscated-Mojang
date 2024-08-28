package net.minecraft.server;

import com.mojang.authlib.yggdrasil.YggdrasilAuthenticationService;
import com.mojang.datafixers.DataFixer;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.Lifecycle;
import java.awt.GraphicsEnvironment;
import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.Proxy;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.function.BooleanSupplier;
import javax.annotation.Nullable;
import joptsimple.OptionParser;
import joptsimple.OptionSet;
import joptsimple.OptionSpec;
import joptsimple.util.PathConverter;
import net.minecraft.CrashReport;
import net.minecraft.DefaultUncaughtExceptionHandler;
import net.minecraft.SharedConstants;
import net.minecraft.Util;
import net.minecraft.commands.Commands;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.NbtException;
import net.minecraft.nbt.ReportedNbtException;
import net.minecraft.network.chat.Component;
import net.minecraft.obfuscate.DontObfuscate;
import net.minecraft.server.dedicated.DedicatedServer;
import net.minecraft.server.dedicated.DedicatedServerProperties;
import net.minecraft.server.dedicated.DedicatedServerSettings;
import net.minecraft.server.level.progress.LoggerChunkProgressListener;
import net.minecraft.server.packs.repository.PackRepository;
import net.minecraft.server.packs.repository.ServerPacksSource;
import net.minecraft.util.Mth;
import net.minecraft.util.datafix.DataFixers;
import net.minecraft.util.profiling.jfr.Environment;
import net.minecraft.util.profiling.jfr.JvmProfiler;
import net.minecraft.util.worldupdate.WorldUpgrader;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.LevelSettings;
import net.minecraft.world.level.WorldDataConfiguration;
import net.minecraft.world.level.chunk.storage.RegionFileVersion;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.level.levelgen.WorldDimensions;
import net.minecraft.world.level.levelgen.WorldOptions;
import net.minecraft.world.level.levelgen.presets.WorldPresets;
import net.minecraft.world.level.storage.LevelDataAndDimensions;
import net.minecraft.world.level.storage.LevelStorageSource;
import net.minecraft.world.level.storage.LevelSummary;
import net.minecraft.world.level.storage.PrimaryLevelData;
import net.minecraft.world.level.storage.WorldData;
import org.slf4j.Logger;

public class Main {
	private static final Logger LOGGER = LogUtils.getLogger();

	@DontObfuscate
	public static void main(String[] strings) {
		SharedConstants.tryDetectVersion();
		OptionParser optionParser = new OptionParser();
		OptionSpec<Void> optionSpec = optionParser.accepts("nogui");
		OptionSpec<Void> optionSpec2 = optionParser.accepts("initSettings", "Initializes 'server.properties' and 'eula.txt', then quits");
		OptionSpec<Void> optionSpec3 = optionParser.accepts("demo");
		OptionSpec<Void> optionSpec4 = optionParser.accepts("bonusChest");
		OptionSpec<Void> optionSpec5 = optionParser.accepts("forceUpgrade");
		OptionSpec<Void> optionSpec6 = optionParser.accepts("eraseCache");
		OptionSpec<Void> optionSpec7 = optionParser.accepts("recreateRegionFiles");
		OptionSpec<Void> optionSpec8 = optionParser.accepts("safeMode", "Loads level with vanilla datapack only");
		OptionSpec<Void> optionSpec9 = optionParser.accepts("help").forHelp();
		OptionSpec<String> optionSpec10 = optionParser.accepts("universe").withRequiredArg().defaultsTo(".");
		OptionSpec<String> optionSpec11 = optionParser.accepts("world").withRequiredArg();
		OptionSpec<Integer> optionSpec12 = optionParser.accepts("port").withRequiredArg().<Integer>ofType(Integer.class).defaultsTo(-1);
		OptionSpec<String> optionSpec13 = optionParser.accepts("serverId").withRequiredArg();
		OptionSpec<Void> optionSpec14 = optionParser.accepts("jfrProfile");
		OptionSpec<Path> optionSpec15 = optionParser.accepts("pidFile").withRequiredArg().withValuesConvertedBy(new PathConverter());
		OptionSpec<String> optionSpec16 = optionParser.nonOptions();

		try {
			OptionSet optionSet = optionParser.parse(strings);
			if (optionSet.has(optionSpec9)) {
				optionParser.printHelpOn(System.err);
				return;
			}

			Path path = optionSet.valueOf(optionSpec15);
			if (path != null) {
				writePidFile(path);
			}

			CrashReport.preload();
			if (optionSet.has(optionSpec14)) {
				JvmProfiler.INSTANCE.start(Environment.SERVER);
			}

			Bootstrap.bootStrap();
			Bootstrap.validate();
			Util.startTimerHackThread();
			Path path2 = Paths.get("server.properties");
			DedicatedServerSettings dedicatedServerSettings = new DedicatedServerSettings(path2);
			dedicatedServerSettings.forceSave();
			RegionFileVersion.configure(dedicatedServerSettings.getProperties().regionFileComression);
			Path path3 = Paths.get("eula.txt");
			Eula eula = new Eula(path3);
			if (optionSet.has(optionSpec2)) {
				LOGGER.info("Initialized '{}' and '{}'", path2.toAbsolutePath(), path3.toAbsolutePath());
				return;
			}

			if (!eula.hasAgreedToEULA()) {
				LOGGER.info("You need to agree to the EULA in order to run the server. Go to eula.txt for more info.");
				return;
			}

			File file = new File(optionSet.valueOf(optionSpec10));
			Services services = Services.create(new YggdrasilAuthenticationService(Proxy.NO_PROXY), file);
			String string = (String)Optional.ofNullable(optionSet.valueOf(optionSpec11)).orElse(dedicatedServerSettings.getProperties().levelName);
			LevelStorageSource levelStorageSource = LevelStorageSource.createDefault(file.toPath());
			LevelStorageSource.LevelStorageAccess levelStorageAccess = levelStorageSource.validateAndCreateAccess(string);
			Dynamic<?> dynamic;
			if (levelStorageAccess.hasWorldData()) {
				LevelSummary levelSummary;
				try {
					dynamic = levelStorageAccess.getDataTag();
					levelSummary = levelStorageAccess.getSummary(dynamic);
				} catch (NbtException | ReportedNbtException | IOException var41) {
					LevelStorageSource.LevelDirectory levelDirectory = levelStorageAccess.getLevelDirectory();
					LOGGER.warn("Failed to load world data from {}", levelDirectory.dataFile(), var41);
					LOGGER.info("Attempting to use fallback");

					try {
						dynamic = levelStorageAccess.getDataTagFallback();
						levelSummary = levelStorageAccess.getSummary(dynamic);
					} catch (NbtException | ReportedNbtException | IOException var40) {
						LOGGER.error("Failed to load world data from {}", levelDirectory.oldDataFile(), var40);
						LOGGER.error(
							"Failed to load world data from {} and {}. World files may be corrupted. Shutting down.", levelDirectory.dataFile(), levelDirectory.oldDataFile()
						);
						return;
					}

					levelStorageAccess.restoreLevelDataFromOld();
				}

				if (levelSummary.requiresManualConversion()) {
					LOGGER.info("This world must be opened in an older version (like 1.6.4) to be safely converted");
					return;
				}

				if (!levelSummary.isCompatible()) {
					LOGGER.info("This world was created by an incompatible version.");
					return;
				}
			} else {
				dynamic = null;
			}

			Dynamic<?> dynamic2 = dynamic;
			boolean bl = optionSet.has(optionSpec8);
			if (bl) {
				LOGGER.warn("Safe mode active, only vanilla datapack will be loaded");
			}

			PackRepository packRepository = ServerPacksSource.createPackRepository(levelStorageAccess);

			WorldStem worldStem;
			try {
				WorldLoader.InitConfig initConfig = loadOrCreateConfig(dedicatedServerSettings.getProperties(), dynamic2, bl, packRepository);
				worldStem = (WorldStem)Util.blockUntilDone(
						executor -> WorldLoader.load(
								initConfig,
								dataLoadContext -> {
									Registry<LevelStem> registry = dataLoadContext.datapackDimensions().lookupOrThrow(Registries.LEVEL_STEM);
									if (dynamic2 != null) {
										LevelDataAndDimensions levelDataAndDimensions = LevelStorageSource.getLevelDataAndDimensions(
											dynamic2, dataLoadContext.dataConfiguration(), registry, dataLoadContext.datapackWorldgen()
										);
										return new WorldLoader.DataLoadOutput<>(levelDataAndDimensions.worldData(), levelDataAndDimensions.dimensions().dimensionsRegistryAccess());
									} else {
										LOGGER.info("No existing world data, creating new world");
										LevelSettings levelSettings;
										WorldOptions worldOptions;
										WorldDimensions worldDimensions;
										if (optionSet.has(optionSpec3)) {
											levelSettings = MinecraftServer.DEMO_SETTINGS;
											worldOptions = WorldOptions.DEMO_OPTIONS;
											worldDimensions = WorldPresets.createNormalWorldDimensions(dataLoadContext.datapackWorldgen());
										} else {
											DedicatedServerProperties dedicatedServerProperties = dedicatedServerSettings.getProperties();
											levelSettings = new LevelSettings(
												dedicatedServerProperties.levelName,
												dedicatedServerProperties.gamemode,
												dedicatedServerProperties.hardcore,
												dedicatedServerProperties.difficulty,
												false,
												new GameRules(dataLoadContext.dataConfiguration().enabledFeatures()),
												dataLoadContext.dataConfiguration()
											);
											worldOptions = optionSet.has(optionSpec4) ? dedicatedServerProperties.worldOptions.withBonusChest(true) : dedicatedServerProperties.worldOptions;
											worldDimensions = dedicatedServerProperties.createDimensions(dataLoadContext.datapackWorldgen());
										}

										WorldDimensions.Complete complete = worldDimensions.bake(registry);
										Lifecycle lifecycle = complete.lifecycle().add(dataLoadContext.datapackWorldgen().allRegistriesLifecycle());
										return new WorldLoader.DataLoadOutput<>(
											new PrimaryLevelData(levelSettings, worldOptions, complete.specialWorldProperty(), lifecycle), complete.dimensionsRegistryAccess()
										);
									}
								},
								WorldStem::new,
								Util.backgroundExecutor(),
								executor
							)
					)
					.get();
			} catch (Exception var39) {
				LOGGER.warn(
					"Failed to load datapacks, can't proceed with server load. You can either fix your datapacks or reset to vanilla with --safeMode", (Throwable)var39
				);
				return;
			}

			RegistryAccess.Frozen frozen = worldStem.registries().compositeAccess();
			boolean bl2 = optionSet.has(optionSpec7);
			if (optionSet.has(optionSpec5) || bl2) {
				forceUpgrade(levelStorageAccess, DataFixers.getDataFixer(), optionSet.has(optionSpec6), () -> true, frozen, bl2);
			}

			WorldData worldData = worldStem.worldData();
			levelStorageAccess.saveDataTag(frozen, worldData);
			final DedicatedServer dedicatedServer = MinecraftServer.spin(
				threadx -> {
					DedicatedServer dedicatedServerx = new DedicatedServer(
						threadx,
						levelStorageAccess,
						packRepository,
						worldStem,
						dedicatedServerSettings,
						DataFixers.getDataFixer(),
						services,
						LoggerChunkProgressListener::createFromGameruleRadius
					);
					dedicatedServerx.setPort(optionSet.valueOf(optionSpec12));
					dedicatedServerx.setDemo(optionSet.has(optionSpec3));
					dedicatedServerx.setId(optionSet.valueOf(optionSpec13));
					boolean blx = !optionSet.has(optionSpec) && !optionSet.valuesOf(optionSpec16).contains("nogui");
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
			LOGGER.error(LogUtils.FATAL_MARKER, "Failed to start the minecraft server", (Throwable)var42);
		}
	}

	private static void writePidFile(Path path) {
		try {
			long l = ProcessHandle.current().pid();
			Files.writeString(path, Long.toString(l));
		} catch (IOException var3) {
			throw new UncheckedIOException(var3);
		}
	}

	private static WorldLoader.InitConfig loadOrCreateConfig(
		DedicatedServerProperties dedicatedServerProperties, @Nullable Dynamic<?> dynamic, boolean bl, PackRepository packRepository
	) {
		boolean bl2;
		WorldDataConfiguration worldDataConfiguration2;
		if (dynamic != null) {
			WorldDataConfiguration worldDataConfiguration = LevelStorageSource.readDataConfig(dynamic);
			bl2 = false;
			worldDataConfiguration2 = worldDataConfiguration;
		} else {
			bl2 = true;
			worldDataConfiguration2 = new WorldDataConfiguration(dedicatedServerProperties.initialDataPackConfiguration, FeatureFlags.DEFAULT_FLAGS);
		}

		WorldLoader.PackConfig packConfig = new WorldLoader.PackConfig(packRepository, worldDataConfiguration2, bl, bl2);
		return new WorldLoader.InitConfig(packConfig, Commands.CommandSelection.DEDICATED, dedicatedServerProperties.functionPermissionLevel);
	}

	private static void forceUpgrade(
		LevelStorageSource.LevelStorageAccess levelStorageAccess,
		DataFixer dataFixer,
		boolean bl,
		BooleanSupplier booleanSupplier,
		RegistryAccess registryAccess,
		boolean bl2
	) {
		LOGGER.info("Forcing world upgrade!");

		try (WorldUpgrader worldUpgrader = new WorldUpgrader(levelStorageAccess, dataFixer, registryAccess, bl, bl2)) {
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
					} catch (InterruptedException var12) {
					}
				}
			}
		}
	}
}
