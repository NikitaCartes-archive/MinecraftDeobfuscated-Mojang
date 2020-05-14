/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.server;

import com.mojang.authlib.GameProfileRepository;
import com.mojang.authlib.minecraft.MinecraftSessionService;
import com.mojang.authlib.yggdrasil.YggdrasilAuthenticationService;
import java.awt.GraphicsEnvironment;
import java.io.File;
import java.net.Proxy;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.UUID;
import joptsimple.AbstractOptionSpec;
import joptsimple.ArgumentAcceptingOptionSpec;
import joptsimple.NonOptionArgumentSpec;
import joptsimple.OptionParser;
import joptsimple.OptionSet;
import joptsimple.OptionSpecBuilder;
import net.minecraft.CrashReport;
import net.minecraft.DefaultUncaughtExceptionHandler;
import net.minecraft.server.Bootstrap;
import net.minecraft.server.Eula;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.dedicated.DedicatedServer;
import net.minecraft.server.dedicated.DedicatedServerProperties;
import net.minecraft.server.dedicated.DedicatedServerSettings;
import net.minecraft.server.level.progress.LoggerChunkProgressListener;
import net.minecraft.server.players.GameProfileCache;
import net.minecraft.util.datafix.DataFixers;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.LevelSettings;
import net.minecraft.world.level.storage.LevelStorageSource;
import net.minecraft.world.level.storage.PrimaryLevelData;
import net.minecraft.world.level.storage.WorldData;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Main {
    private static final Logger LOGGER = LogManager.getLogger();

    public static void main(String[] strings) {
        OptionParser optionParser = new OptionParser();
        OptionSpecBuilder optionSpec = optionParser.accepts("nogui");
        OptionSpecBuilder optionSpec2 = optionParser.accepts("initSettings", "Initializes 'server.properties' and 'eula.txt', then quits");
        OptionSpecBuilder optionSpec3 = optionParser.accepts("demo");
        OptionSpecBuilder optionSpec4 = optionParser.accepts("bonusChest");
        OptionSpecBuilder optionSpec5 = optionParser.accepts("forceUpgrade");
        OptionSpecBuilder optionSpec6 = optionParser.accepts("eraseCache");
        AbstractOptionSpec optionSpec7 = optionParser.accepts("help").forHelp();
        ArgumentAcceptingOptionSpec<String> optionSpec8 = optionParser.accepts("singleplayer").withRequiredArg();
        ArgumentAcceptingOptionSpec<String> optionSpec9 = optionParser.accepts("universe").withRequiredArg().defaultsTo(".", (String[])new String[0]);
        ArgumentAcceptingOptionSpec<String> optionSpec10 = optionParser.accepts("world").withRequiredArg();
        ArgumentAcceptingOptionSpec<Integer> optionSpec11 = optionParser.accepts("port").withRequiredArg().ofType(Integer.class).defaultsTo(-1, (Integer[])new Integer[0]);
        ArgumentAcceptingOptionSpec<String> optionSpec12 = optionParser.accepts("serverId").withRequiredArg();
        NonOptionArgumentSpec<String> optionSpec13 = optionParser.nonOptions();
        try {
            boolean bl;
            OptionSet optionSet = optionParser.parse(strings);
            if (optionSet.has(optionSpec7)) {
                optionParser.printHelpOn(System.err);
                return;
            }
            CrashReport.preload();
            Bootstrap.bootStrap();
            Bootstrap.validate();
            Path path = Paths.get("server.properties", new String[0]);
            DedicatedServerSettings dedicatedServerSettings = new DedicatedServerSettings(path);
            dedicatedServerSettings.forceSave();
            Path path2 = Paths.get("eula.txt", new String[0]);
            Eula eula = new Eula(path2);
            if (optionSet.has(optionSpec2)) {
                LOGGER.info("Initialized '" + path.toAbsolutePath().toString() + "' and '" + path2.toAbsolutePath().toString() + "'");
                return;
            }
            if (!eula.hasAgreedToEULA()) {
                LOGGER.info("You need to agree to the EULA in order to run the server. Go to eula.txt for more info.");
                return;
            }
            File file = new File(optionSet.valueOf(optionSpec9));
            YggdrasilAuthenticationService yggdrasilAuthenticationService = new YggdrasilAuthenticationService(Proxy.NO_PROXY, UUID.randomUUID().toString());
            MinecraftSessionService minecraftSessionService = yggdrasilAuthenticationService.createMinecraftSessionService();
            GameProfileRepository gameProfileRepository = yggdrasilAuthenticationService.createProfileRepository();
            GameProfileCache gameProfileCache = new GameProfileCache(gameProfileRepository, new File(file, MinecraftServer.USERID_CACHE_FILE.getName()));
            String string = Optional.ofNullable(optionSet.valueOf(optionSpec10)).orElse(dedicatedServerSettings.getProperties().levelName);
            LevelStorageSource levelStorageSource = LevelStorageSource.createDefault(file.toPath());
            LevelStorageSource.LevelStorageAccess levelStorageAccess = levelStorageSource.createAccess(string);
            MinecraftServer.ensureLevelConversion(levelStorageAccess, DataFixers.getDataFixer(), optionSet.has(optionSpec5), optionSet.has(optionSpec6), () -> true);
            WorldData worldData = levelStorageAccess.getDataTag();
            if (worldData == null) {
                LevelSettings levelSettings;
                if (optionSet.has(optionSpec3)) {
                    levelSettings = MinecraftServer.DEMO_SETTINGS;
                } else {
                    DedicatedServerProperties dedicatedServerProperties = dedicatedServerSettings.getProperties();
                    levelSettings = new LevelSettings(dedicatedServerProperties.levelName, dedicatedServerProperties.gamemode, dedicatedServerProperties.hardcore, dedicatedServerProperties.difficulty, false, new GameRules(), optionSet.has(optionSpec4) ? dedicatedServerProperties.worldGenSettings : dedicatedServerProperties.worldGenSettings.withBonusChest());
                }
                worldData = new PrimaryLevelData(levelSettings);
            }
            final DedicatedServer dedicatedServer = new DedicatedServer(levelStorageAccess, worldData, dedicatedServerSettings, DataFixers.getDataFixer(), minecraftSessionService, gameProfileRepository, gameProfileCache, LoggerChunkProgressListener::new);
            dedicatedServer.setSingleplayerName(optionSet.valueOf(optionSpec8));
            dedicatedServer.setPort(optionSet.valueOf(optionSpec11));
            dedicatedServer.setDemo(optionSet.has(optionSpec3));
            dedicatedServer.setId(optionSet.valueOf(optionSpec12));
            boolean bl2 = bl = !optionSet.has(optionSpec) && !optionSet.valuesOf(optionSpec13).contains("nogui");
            if (bl && !GraphicsEnvironment.isHeadless()) {
                dedicatedServer.showGui();
            }
            dedicatedServer.forkAndRun();
            Thread thread = new Thread("Server Shutdown Thread"){

                @Override
                public void run() {
                    dedicatedServer.halt(true);
                }
            };
            thread.setUncaughtExceptionHandler(new DefaultUncaughtExceptionHandler(LOGGER));
            Runtime.getRuntime().addShutdownHook(thread);
        } catch (Exception exception) {
            LOGGER.fatal("Failed to start the minecraft server", (Throwable)exception);
        }
    }
}

