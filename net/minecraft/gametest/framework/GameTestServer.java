/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.gametest.framework;

import com.google.common.base.Stopwatch;
import com.google.common.collect.Lists;
import com.mojang.authlib.GameProfile;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Lifecycle;
import java.net.Proxy;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.BooleanSupplier;
import net.minecraft.CrashReport;
import net.minecraft.SystemReport;
import net.minecraft.Util;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.core.MappedRegistry;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTestBatch;
import net.minecraft.gametest.framework.GameTestInfo;
import net.minecraft.gametest.framework.GameTestRunner;
import net.minecraft.gametest.framework.GameTestTicker;
import net.minecraft.gametest.framework.GlobalTestReporter;
import net.minecraft.gametest.framework.MultipleTestTracker;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.Services;
import net.minecraft.server.WorldLoader;
import net.minecraft.server.WorldStem;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.progress.LoggerChunkProgressListener;
import net.minecraft.server.packs.repository.PackRepository;
import net.minecraft.server.players.PlayerList;
import net.minecraft.util.SignatureValidator;
import net.minecraft.util.datafix.DataFixers;
import net.minecraft.world.Difficulty;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.level.DataPackConfig;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.LevelSettings;
import net.minecraft.world.level.WorldDataConfiguration;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.level.levelgen.WorldDimensions;
import net.minecraft.world.level.levelgen.WorldOptions;
import net.minecraft.world.level.levelgen.presets.WorldPresets;
import net.minecraft.world.level.storage.LevelStorageSource;
import net.minecraft.world.level.storage.PrimaryLevelData;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

public class GameTestServer
extends MinecraftServer {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final int PROGRESS_REPORT_INTERVAL = 20;
    private static final Services NO_SERVICES = new Services(null, SignatureValidator.NO_VALIDATION, null, null);
    private final List<GameTestBatch> testBatches;
    private final BlockPos spawnPos;
    private static final GameRules TEST_GAME_RULES = Util.make(new GameRules(), gameRules -> {
        gameRules.getRule(GameRules.RULE_DOMOBSPAWNING).set(false, null);
        gameRules.getRule(GameRules.RULE_WEATHER_CYCLE).set(false, null);
    });
    private static final WorldOptions WORLD_OPTIONS = new WorldOptions(0L, false, false);
    @Nullable
    private MultipleTestTracker testTracker;

    public static GameTestServer create(Thread thread, LevelStorageSource.LevelStorageAccess levelStorageAccess, PackRepository packRepository, Collection<GameTestBatch> collection, BlockPos blockPos) {
        if (collection.isEmpty()) {
            throw new IllegalArgumentException("No test batches were given!");
        }
        packRepository.reload();
        WorldDataConfiguration worldDataConfiguration = new WorldDataConfiguration(new DataPackConfig(new ArrayList<String>(packRepository.getAvailableIds()), List.of()), FeatureFlags.REGISTRY.allFlags());
        LevelSettings levelSettings = new LevelSettings("Test Level", GameType.CREATIVE, false, Difficulty.NORMAL, true, TEST_GAME_RULES, worldDataConfiguration);
        WorldLoader.PackConfig packConfig = new WorldLoader.PackConfig(packRepository, worldDataConfiguration, false, true);
        WorldLoader.InitConfig initConfig = new WorldLoader.InitConfig(packConfig, Commands.CommandSelection.DEDICATED, 4);
        try {
            LOGGER.debug("Starting resource loading");
            Stopwatch stopwatch = Stopwatch.createStarted();
            WorldStem worldStem = (WorldStem)Util.blockUntilDone(executor -> WorldLoader.load(initConfig, dataLoadContext -> {
                Registry<LevelStem> registry = new MappedRegistry<LevelStem>(Registries.LEVEL_STEM, Lifecycle.stable()).freeze();
                WorldDimensions.Complete complete = dataLoadContext.datapackWorldgen().registryOrThrow(Registries.WORLD_PRESET).getHolderOrThrow(WorldPresets.FLAT).value().createWorldDimensions().bake(registry);
                return new WorldLoader.DataLoadOutput<PrimaryLevelData>(new PrimaryLevelData(levelSettings, WORLD_OPTIONS, complete.specialWorldProperty(), complete.lifecycle()), complete.dimensionsRegistryAccess());
            }, WorldStem::new, Util.backgroundExecutor(), executor)).get();
            stopwatch.stop();
            LOGGER.debug("Finished resource loading after {} ms", (Object)stopwatch.elapsed(TimeUnit.MILLISECONDS));
            return new GameTestServer(thread, levelStorageAccess, packRepository, worldStem, collection, blockPos);
        } catch (Exception exception) {
            LOGGER.warn("Failed to load vanilla datapack, bit oops", exception);
            System.exit(-1);
            throw new IllegalStateException();
        }
    }

    private GameTestServer(Thread thread, LevelStorageSource.LevelStorageAccess levelStorageAccess, PackRepository packRepository, WorldStem worldStem, Collection<GameTestBatch> collection, BlockPos blockPos) {
        super(thread, levelStorageAccess, packRepository, worldStem, Proxy.NO_PROXY, DataFixers.getDataFixer(), NO_SERVICES, LoggerChunkProgressListener::new);
        this.testBatches = Lists.newArrayList(collection);
        this.spawnPos = blockPos;
    }

    @Override
    public boolean initServer() {
        this.setPlayerList(new PlayerList(this, this.registries(), this.playerDataStorage, 1){});
        this.loadLevel();
        ServerLevel serverLevel = this.overworld();
        serverLevel.setDefaultSpawnPos(this.spawnPos, 0.0f);
        int i = 20000000;
        serverLevel.setWeatherParameters(20000000, 20000000, false, false);
        LOGGER.info("Started game test server");
        return true;
    }

    @Override
    public void tickServer(BooleanSupplier booleanSupplier) {
        super.tickServer(booleanSupplier);
        ServerLevel serverLevel = this.overworld();
        if (!this.haveTestsStarted()) {
            this.startTests(serverLevel);
        }
        if (serverLevel.getGameTime() % 20L == 0L) {
            LOGGER.info(this.testTracker.getProgressBar());
        }
        if (this.testTracker.isDone()) {
            this.halt(false);
            LOGGER.info(this.testTracker.getProgressBar());
            GlobalTestReporter.finish();
            LOGGER.info("========= {} GAME TESTS COMPLETE ======================", (Object)this.testTracker.getTotalCount());
            if (this.testTracker.hasFailedRequired()) {
                LOGGER.info("{} required tests failed :(", (Object)this.testTracker.getFailedRequiredCount());
                this.testTracker.getFailedRequired().forEach(gameTestInfo -> LOGGER.info("   - {}", (Object)gameTestInfo.getTestName()));
            } else {
                LOGGER.info("All {} required tests passed :)", (Object)this.testTracker.getTotalCount());
            }
            if (this.testTracker.hasFailedOptional()) {
                LOGGER.info("{} optional tests failed", (Object)this.testTracker.getFailedOptionalCount());
                this.testTracker.getFailedOptional().forEach(gameTestInfo -> LOGGER.info("   - {}", (Object)gameTestInfo.getTestName()));
            }
            LOGGER.info("====================================================");
        }
    }

    @Override
    public void waitUntilNextTick() {
        this.runAllTasks();
    }

    @Override
    public SystemReport fillServerSystemReport(SystemReport systemReport) {
        systemReport.setDetail("Type", "Game test server");
        return systemReport;
    }

    @Override
    public void onServerExit() {
        super.onServerExit();
        LOGGER.info("Game test server shutting down");
        System.exit(this.testTracker.getFailedRequiredCount());
    }

    @Override
    public void onServerCrash(CrashReport crashReport) {
        super.onServerCrash(crashReport);
        LOGGER.error("Game test server crashed\n{}", (Object)crashReport.getFriendlyReport());
        System.exit(1);
    }

    private void startTests(ServerLevel serverLevel) {
        Collection<GameTestInfo> collection = GameTestRunner.runTestBatches(this.testBatches, new BlockPos(0, -60, 0), Rotation.NONE, serverLevel, GameTestTicker.SINGLETON, 8);
        this.testTracker = new MultipleTestTracker(collection);
        LOGGER.info("{} tests are now running!", (Object)this.testTracker.getTotalCount());
    }

    private boolean haveTestsStarted() {
        return this.testTracker != null;
    }

    @Override
    public boolean isHardcore() {
        return false;
    }

    @Override
    public int getOperatorUserPermissionLevel() {
        return 0;
    }

    @Override
    public int getFunctionCompilationLevel() {
        return 4;
    }

    @Override
    public boolean shouldRconBroadcast() {
        return false;
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
    public boolean isCommandBlockEnabled() {
        return true;
    }

    @Override
    public boolean isPublished() {
        return false;
    }

    @Override
    public boolean shouldInformAdmins() {
        return false;
    }

    @Override
    public boolean isSingleplayerOwner(GameProfile gameProfile) {
        return false;
    }
}

