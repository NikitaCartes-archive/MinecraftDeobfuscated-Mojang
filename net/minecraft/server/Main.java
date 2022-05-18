/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.server;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.yggdrasil.YggdrasilAuthenticationService;
import com.mojang.datafixers.DataFixer;
import com.mojang.datafixers.util.Pair;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Lifecycle;
import java.awt.GraphicsEnvironment;
import java.io.File;
import java.net.Proxy;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;
import java.util.Optional;
import java.util.function.BooleanSupplier;
import joptsimple.AbstractOptionSpec;
import joptsimple.ArgumentAcceptingOptionSpec;
import joptsimple.NonOptionArgumentSpec;
import joptsimple.OptionParser;
import joptsimple.OptionSet;
import joptsimple.OptionSpecBuilder;
import net.minecraft.CrashReport;
import net.minecraft.DefaultUncaughtExceptionHandler;
import net.minecraft.SharedConstants;
import net.minecraft.Util;
import net.minecraft.commands.Commands;
import net.minecraft.core.RegistryAccess;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.obfuscate.DontObfuscate;
import net.minecraft.resources.RegistryOps;
import net.minecraft.server.Bootstrap;
import net.minecraft.server.Eula;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.Services;
import net.minecraft.server.WorldLoader;
import net.minecraft.server.WorldStem;
import net.minecraft.server.dedicated.DedicatedServer;
import net.minecraft.server.dedicated.DedicatedServerProperties;
import net.minecraft.server.dedicated.DedicatedServerSettings;
import net.minecraft.server.level.progress.LoggerChunkProgressListener;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.repository.FolderRepositorySource;
import net.minecraft.server.packs.repository.PackRepository;
import net.minecraft.server.packs.repository.PackSource;
import net.minecraft.server.packs.repository.ServerPacksSource;
import net.minecraft.util.Mth;
import net.minecraft.util.datafix.DataFixers;
import net.minecraft.util.profiling.jfr.Environment;
import net.minecraft.util.profiling.jfr.JvmProfiler;
import net.minecraft.util.worldupdate.WorldUpgrader;
import net.minecraft.world.level.DataPackConfig;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.LevelSettings;
import net.minecraft.world.level.levelgen.WorldGenSettings;
import net.minecraft.world.level.levelgen.presets.WorldPresets;
import net.minecraft.world.level.storage.LevelResource;
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
        OptionSpecBuilder optionSpec = optionParser.accepts("nogui");
        OptionSpecBuilder optionSpec2 = optionParser.accepts("initSettings", "Initializes 'server.properties' and 'eula.txt', then quits");
        OptionSpecBuilder optionSpec3 = optionParser.accepts("demo");
        OptionSpecBuilder optionSpec4 = optionParser.accepts("bonusChest");
        OptionSpecBuilder optionSpec5 = optionParser.accepts("forceUpgrade");
        OptionSpecBuilder optionSpec6 = optionParser.accepts("eraseCache");
        OptionSpecBuilder optionSpec7 = optionParser.accepts("safeMode", "Loads level with vanilla datapack only");
        AbstractOptionSpec optionSpec8 = optionParser.accepts("help").forHelp();
        ArgumentAcceptingOptionSpec<String> optionSpec9 = optionParser.accepts("singleplayer").withRequiredArg();
        ArgumentAcceptingOptionSpec<String> optionSpec10 = optionParser.accepts("universe").withRequiredArg().defaultsTo(".", (String[])new String[0]);
        ArgumentAcceptingOptionSpec<String> optionSpec11 = optionParser.accepts("world").withRequiredArg();
        ArgumentAcceptingOptionSpec<Integer> optionSpec12 = optionParser.accepts("port").withRequiredArg().ofType(Integer.class).defaultsTo(-1, (Integer[])new Integer[0]);
        ArgumentAcceptingOptionSpec<String> optionSpec13 = optionParser.accepts("serverId").withRequiredArg();
        OptionSpecBuilder optionSpec14 = optionParser.accepts("jfrProfile");
        NonOptionArgumentSpec<String> optionSpec15 = optionParser.nonOptions();
        try {
            WorldStem worldStem;
            boolean bl;
            OptionSet optionSet = optionParser.parse(strings);
            if (optionSet.has(optionSpec8)) {
                optionParser.printHelpOn(System.err);
                return;
            }
            CrashReport.preload();
            if (optionSet.has(optionSpec14)) {
                JvmProfiler.INSTANCE.start(Environment.SERVER);
            }
            Bootstrap.bootStrap();
            Bootstrap.validate();
            Util.startTimerHackThread();
            Path path = Paths.get("server.properties", new String[0]);
            DedicatedServerSettings dedicatedServerSettings = new DedicatedServerSettings(path);
            dedicatedServerSettings.forceSave();
            Path path2 = Paths.get("eula.txt", new String[0]);
            Eula eula = new Eula(path2);
            if (optionSet.has(optionSpec2)) {
                LOGGER.info("Initialized '{}' and '{}'", (Object)path.toAbsolutePath(), (Object)path2.toAbsolutePath());
                return;
            }
            if (!eula.hasAgreedToEULA()) {
                LOGGER.info("You need to agree to the EULA in order to run the server. Go to eula.txt for more info.");
                return;
            }
            File file = new File(optionSet.valueOf(optionSpec10));
            Services services = Services.create(new YggdrasilAuthenticationService(Proxy.NO_PROXY), file);
            String string = Optional.ofNullable(optionSet.valueOf(optionSpec11)).orElse(dedicatedServerSettings.getProperties().levelName);
            LevelStorageSource levelStorageSource = LevelStorageSource.createDefault(file.toPath());
            LevelStorageSource.LevelStorageAccess levelStorageAccess = levelStorageSource.createAccess(string);
            LevelSummary levelSummary = levelStorageAccess.getSummary();
            if (levelSummary != null) {
                if (levelSummary.requiresManualConversion()) {
                    LOGGER.info("This world must be opened in an older version (like 1.6.4) to be safely converted");
                    return;
                }
                if (!levelSummary.isCompatible()) {
                    LOGGER.info("This world was created by an incompatible version.");
                    return;
                }
            }
            if (bl = optionSet.has(optionSpec7)) {
                LOGGER.warn("Safe mode active, only vanilla datapack will be loaded");
            }
            PackRepository packRepository = new PackRepository(PackType.SERVER_DATA, new ServerPacksSource(), new FolderRepositorySource(levelStorageAccess.getLevelPath(LevelResource.DATAPACK_DIR).toFile(), PackSource.WORLD));
            try {
                DataPackConfig dataPackConfig = Objects.requireNonNullElse(levelStorageAccess.getDataPacks(), DataPackConfig.DEFAULT);
                WorldLoader.PackConfig packConfig = new WorldLoader.PackConfig(packRepository, dataPackConfig, bl);
                WorldLoader.InitConfig initConfig = new WorldLoader.InitConfig(packConfig, Commands.CommandSelection.DEDICATED, dedicatedServerSettings.getProperties().functionPermissionLevel);
                worldStem = (WorldStem)Util.blockUntilDone(executor -> WorldStem.load(initConfig, (resourceManager, dataPackConfig) -> {
                    WorldGenSettings worldGenSettings;
                    LevelSettings levelSettings;
                    RegistryAccess.Writable writable = RegistryAccess.builtinCopy();
                    RegistryOps<Tag> dynamicOps = RegistryOps.createAndLoad(NbtOps.INSTANCE, writable, resourceManager);
                    WorldData worldData = levelStorageAccess.getDataTag(dynamicOps, dataPackConfig, writable.allElementsLifecycle());
                    if (worldData != null) {
                        return Pair.of(worldData, writable.freeze());
                    }
                    if (optionSet.has(optionSpec3)) {
                        levelSettings = MinecraftServer.DEMO_SETTINGS;
                        worldGenSettings = WorldPresets.demoSettings(writable);
                    } else {
                        DedicatedServerProperties dedicatedServerProperties = dedicatedServerSettings.getProperties();
                        levelSettings = new LevelSettings(dedicatedServerProperties.levelName, dedicatedServerProperties.gamemode, dedicatedServerProperties.hardcore, dedicatedServerProperties.difficulty, false, new GameRules(), dataPackConfig);
                        worldGenSettings = optionSet.has(optionSpec4) ? dedicatedServerProperties.getWorldGenSettings(writable).withBonusChest() : dedicatedServerProperties.getWorldGenSettings(writable);
                    }
                    PrimaryLevelData primaryLevelData = new PrimaryLevelData(levelSettings, worldGenSettings, Lifecycle.stable());
                    return Pair.of(primaryLevelData, writable.freeze());
                }, Util.backgroundExecutor(), executor)).get();
            } catch (Exception exception) {
                LOGGER.warn("Failed to load datapacks, can't proceed with server load. You can either fix your datapacks or reset to vanilla with --safeMode", exception);
                return;
            }
            RegistryAccess.Frozen frozen = worldStem.registryAccess();
            dedicatedServerSettings.getProperties().getWorldGenSettings(frozen);
            WorldData worldData = worldStem.worldData();
            if (optionSet.has(optionSpec5)) {
                Main.forceUpgrade(levelStorageAccess, DataFixers.getDataFixer(), optionSet.has(optionSpec6), () -> true, worldData.worldGenSettings());
            }
            levelStorageAccess.saveDataTag(frozen, worldData);
            final DedicatedServer dedicatedServer = MinecraftServer.spin(thread -> {
                boolean bl;
                DedicatedServer dedicatedServer = new DedicatedServer((Thread)thread, levelStorageAccess, packRepository, worldStem, dedicatedServerSettings, DataFixers.getDataFixer(), services, LoggerChunkProgressListener::new);
                dedicatedServer.setSingleplayerProfile(optionSet.has(optionSpec9) ? new GameProfile(null, (String)optionSet.valueOf(optionSpec9)) : null);
                dedicatedServer.setPort((Integer)optionSet.valueOf(optionSpec12));
                dedicatedServer.setDemo(optionSet.has(optionSpec3));
                dedicatedServer.setId((String)optionSet.valueOf(optionSpec13));
                boolean bl2 = bl = !optionSet.has(optionSpec) && !optionSet.valuesOf(optionSpec15).contains("nogui");
                if (bl && !GraphicsEnvironment.isHeadless()) {
                    dedicatedServer.showGui();
                }
                return dedicatedServer;
            });
            Thread thread2 = new Thread("Server Shutdown Thread"){

                @Override
                public void run() {
                    dedicatedServer.halt(true);
                }
            };
            thread2.setUncaughtExceptionHandler(new DefaultUncaughtExceptionHandler(LOGGER));
            Runtime.getRuntime().addShutdownHook(thread2);
        } catch (Exception exception2) {
            LOGGER.error(LogUtils.FATAL_MARKER, "Failed to start the minecraft server", exception2);
        }
    }

    private static void forceUpgrade(LevelStorageSource.LevelStorageAccess levelStorageAccess, DataFixer dataFixer, boolean bl, BooleanSupplier booleanSupplier, WorldGenSettings worldGenSettings) {
        LOGGER.info("Forcing world upgrade!");
        WorldUpgrader worldUpgrader = new WorldUpgrader(levelStorageAccess, dataFixer, worldGenSettings, bl);
        Component component = null;
        while (!worldUpgrader.isFinished()) {
            int i;
            Component component2 = worldUpgrader.getStatus();
            if (component != component2) {
                component = component2;
                LOGGER.info(worldUpgrader.getStatus().getString());
            }
            if ((i = worldUpgrader.getTotalChunks()) > 0) {
                int j = worldUpgrader.getConverted() + worldUpgrader.getSkipped();
                LOGGER.info("{}% completed ({} / {} chunks)...", Mth.floor((float)j / (float)i * 100.0f), j, i);
            }
            if (!booleanSupplier.getAsBoolean()) {
                worldUpgrader.cancel();
                continue;
            }
            try {
                Thread.sleep(1000L);
            } catch (InterruptedException interruptedException) {}
        }
    }
}

