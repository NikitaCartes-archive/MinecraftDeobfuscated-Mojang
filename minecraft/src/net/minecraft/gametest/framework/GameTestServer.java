package net.minecraft.gametest.framework;

import com.google.common.collect.Lists;
import com.mojang.authlib.GameProfile;
import com.mojang.serialization.Lifecycle;
import java.net.Proxy;
import java.util.Collection;
import java.util.List;
import java.util.function.BooleanSupplier;
import javax.annotation.Nullable;
import net.minecraft.CrashReport;
import net.minecraft.SystemReport;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.ServerResources;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.progress.LoggerChunkProgressListener;
import net.minecraft.server.packs.repository.PackRepository;
import net.minecraft.server.players.PlayerList;
import net.minecraft.util.datafix.DataFixers;
import net.minecraft.world.Difficulty;
import net.minecraft.world.level.DataPackConfig;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.LevelSettings;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.levelgen.FlatLevelSource;
import net.minecraft.world.level.levelgen.WorldGenSettings;
import net.minecraft.world.level.levelgen.flat.FlatLevelGeneratorSettings;
import net.minecraft.world.level.storage.LevelStorageSource;
import net.minecraft.world.level.storage.PrimaryLevelData;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class GameTestServer extends MinecraftServer {
	private static final Logger LOGGER = LogManager.getLogger();
	private static final int PROGRESS_REPORT_INTERVAL = 20;
	private final List<GameTestBatch> testBatches;
	private final BlockPos spawnPos;
	private static final GameRules TEST_GAME_RULES = Util.make(new GameRules(), gameRules -> {
		gameRules.getRule(GameRules.RULE_DOMOBSPAWNING).set(false, null);
		gameRules.getRule(GameRules.RULE_WEATHER_CYCLE).set(false, null);
	});
	private static final LevelSettings TEST_SETTINGS = new LevelSettings(
		"Test Level", GameType.CREATIVE, false, Difficulty.NORMAL, true, TEST_GAME_RULES, DataPackConfig.DEFAULT
	);
	@Nullable
	private MultipleTestTracker testTracker;

	public GameTestServer(
		Thread thread,
		LevelStorageSource.LevelStorageAccess levelStorageAccess,
		PackRepository packRepository,
		ServerResources serverResources,
		Collection<GameTestBatch> collection,
		BlockPos blockPos,
		RegistryAccess.RegistryHolder registryHolder
	) {
		this(
			thread,
			levelStorageAccess,
			packRepository,
			serverResources,
			collection,
			blockPos,
			registryHolder,
			registryHolder.registryOrThrow(Registry.BIOME_REGISTRY),
			registryHolder.registryOrThrow(Registry.DIMENSION_TYPE_REGISTRY)
		);
	}

	private GameTestServer(
		Thread thread,
		LevelStorageSource.LevelStorageAccess levelStorageAccess,
		PackRepository packRepository,
		ServerResources serverResources,
		Collection<GameTestBatch> collection,
		BlockPos blockPos,
		RegistryAccess.RegistryHolder registryHolder,
		Registry<Biome> registry,
		Registry<DimensionType> registry2
	) {
		super(
			thread,
			registryHolder,
			levelStorageAccess,
			new PrimaryLevelData(
				TEST_SETTINGS,
				new WorldGenSettings(
					0L,
					false,
					false,
					WorldGenSettings.withOverworld(
						registry2, DimensionType.defaultDimensions(registryHolder, 0L), new FlatLevelSource(FlatLevelGeneratorSettings.getDefault(registry))
					)
				),
				Lifecycle.stable()
			),
			packRepository,
			Proxy.NO_PROXY,
			DataFixers.getDataFixer(),
			serverResources,
			null,
			null,
			null,
			LoggerChunkProgressListener::new
		);
		this.testBatches = Lists.<GameTestBatch>newArrayList(collection);
		this.spawnPos = blockPos;
		if (collection.isEmpty()) {
			throw new IllegalArgumentException("No test batches were given!");
		}
	}

	@Override
	public boolean initServer() {
		this.setPlayerList(new PlayerList(this, this.registryHolder, this.playerDataStorage, 1) {
		});
		this.loadLevel();
		ServerLevel serverLevel = this.overworld();
		serverLevel.setDefaultSpawnPos(this.spawnPos, 0.0F);
		int i = 20000000;
		serverLevel.setWeatherParameters(20000000, 20000000, false, false);
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
			LOGGER.info("========= {} GAME TESTS COMPLETE ======================", this.testTracker.getTotalCount());
			if (this.testTracker.hasFailedRequired()) {
				LOGGER.info("{} required tests failed :(", this.testTracker.getFailedRequiredCount());
				this.testTracker.getFailedRequired().forEach(gameTestInfo -> LOGGER.info("   - {}", gameTestInfo.getTestName()));
			} else {
				LOGGER.info("All {} required tests passed :)", this.testTracker.getTotalCount());
			}

			if (this.testTracker.hasFailedOptional()) {
				LOGGER.info("{} optional tests failed", this.testTracker.getFailedOptionalCount());
				this.testTracker.getFailedOptional().forEach(gameTestInfo -> LOGGER.info("   - {}", gameTestInfo.getTestName()));
			}

			LOGGER.info("====================================================");
		}
	}

	@Override
	public SystemReport fillServerSystemReport(SystemReport systemReport) {
		systemReport.setDetail("Type", "Game test server");
		return systemReport;
	}

	@Override
	public void onServerExit() {
		super.onServerExit();
		System.exit(this.testTracker.getFailedRequiredCount());
	}

	@Override
	public void onServerCrash(CrashReport crashReport) {
		System.exit(1);
	}

	private void startTests(ServerLevel serverLevel) {
		Collection<GameTestInfo> collection = GameTestRunner.runTestBatches(
			this.testBatches, new BlockPos(0, -60, 0), Rotation.NONE, serverLevel, GameTestTicker.SINGLETON, 8
		);
		this.testTracker = new MultipleTestTracker(collection);
		LOGGER.info("{} tests are now running!", this.testTracker.getTotalCount());
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
