package net.minecraft.client.server;

import com.google.common.collect.Lists;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.GameProfileRepository;
import com.mojang.authlib.minecraft.MinecraftSessionService;
import com.mojang.authlib.yggdrasil.YggdrasilAuthenticationService;
import java.io.File;
import java.io.IOException;
import java.util.Optional;
import java.util.UUID;
import java.util.function.BooleanSupplier;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.CrashReport;
import net.minecraft.CrashReportDetail;
import net.minecraft.SharedConstants;
import net.minecraft.client.ClientBrandRetriever;
import net.minecraft.client.Minecraft;
import net.minecraft.commands.Commands;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.progress.ChunkProgressListener;
import net.minecraft.server.level.progress.ChunkProgressListenerFactory;
import net.minecraft.server.players.GameProfileCache;
import net.minecraft.util.Crypt;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.Difficulty;
import net.minecraft.world.Snooper;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.LevelSettings;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.levelgen.ChunkGeneratorProvider;
import net.minecraft.world.level.storage.LevelData;
import net.minecraft.world.level.storage.LevelStorage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Environment(EnvType.CLIENT)
public class IntegratedServer extends MinecraftServer {
	private static final Logger LOGGER = LogManager.getLogger();
	private final Minecraft minecraft;
	private final LevelSettings settings;
	private boolean paused;
	private int publishedPort = -1;
	private LanServerPinger lanPinger;
	private UUID uuid;

	public IntegratedServer(
		Minecraft minecraft,
		String string,
		String string2,
		LevelSettings levelSettings,
		YggdrasilAuthenticationService yggdrasilAuthenticationService,
		MinecraftSessionService minecraftSessionService,
		GameProfileRepository gameProfileRepository,
		GameProfileCache gameProfileCache,
		ChunkProgressListenerFactory chunkProgressListenerFactory
	) {
		super(
			new File(minecraft.gameDirectory, "saves"),
			minecraft.getProxy(),
			minecraft.getFixerUpper(),
			new Commands(false),
			yggdrasilAuthenticationService,
			minecraftSessionService,
			gameProfileRepository,
			gameProfileCache,
			chunkProgressListenerFactory,
			string
		);
		this.setSingleplayerName(minecraft.getUser().getName());
		this.setLevelName(string2);
		this.setDemo(minecraft.isDemo());
		this.setBonusChest(levelSettings.hasStartingBonusItems());
		this.setMaxBuildHeight(256);
		this.setPlayerList(new IntegratedPlayerList(this));
		this.minecraft = minecraft;
		this.settings = this.isDemo() ? MinecraftServer.DEMO_SETTINGS : levelSettings;
	}

	@Override
	public void loadLevel(String string, String string2, long l, ChunkGeneratorProvider chunkGeneratorProvider) {
		this.ensureLevelConversion(string);
		LevelStorage levelStorage = this.getStorageSource().selectLevel(string, this);
		this.detectBundledResources(this.getLevelIdName(), levelStorage);
		LevelData levelData = levelStorage.prepareLevel();
		if (levelData == null) {
			levelData = new LevelData(this.settings, string2);
		} else {
			levelData.setLevelName(string2);
		}

		levelData.setModdedInfo(this.getServerModName(), this.getModdedStatus().isPresent());
		this.loadDataPacks(levelStorage.getFolder(), levelData);
		ChunkProgressListener chunkProgressListener = this.progressListenerFactory.create(11);
		this.createLevels(levelStorage, levelData, this.settings, chunkProgressListener);
		if (this.getLevel(DimensionType.OVERWORLD).getLevelData().getDifficulty() == null) {
			this.setDifficulty(this.minecraft.options.difficulty, true);
		}

		this.prepareLevels(chunkProgressListener);
	}

	@Override
	public boolean initServer() throws IOException {
		LOGGER.info("Starting integrated minecraft server version " + SharedConstants.getCurrentVersion().getName());
		this.setUsesAuthentication(true);
		this.setAnimals(true);
		this.setNpcsEnabled(true);
		this.setPvpAllowed(true);
		this.setFlightAllowed(true);
		LOGGER.info("Generating keypair");
		this.setKeyPair(Crypt.generateKeyPair());
		this.loadLevel(this.getLevelIdName(), this.getLevelName(), this.settings.getSeed(), this.settings.getGeneratorProvider());
		this.setMotd(this.getSingleplayerName() + " - " + this.getLevel(DimensionType.OVERWORLD).getLevelData().getLevelName());
		return true;
	}

	@Override
	public void tickServer(BooleanSupplier booleanSupplier) {
		boolean bl = this.paused;
		this.paused = Minecraft.getInstance().getConnection() != null && Minecraft.getInstance().isPaused();
		ProfilerFiller profilerFiller = this.getProfiler();
		if (!bl && this.paused) {
			profilerFiller.push("autoSave");
			LOGGER.info("Saving and pausing game...");
			this.getPlayerList().saveAll();
			this.saveAllChunks(false, false, false);
			profilerFiller.pop();
		}

		if (!this.paused) {
			super.tickServer(booleanSupplier);
			int i = Math.max(2, this.minecraft.options.renderDistance + -1);
			if (i != this.getPlayerList().getViewDistance()) {
				LOGGER.info("Changing view distance to {}, from {}", i, this.getPlayerList().getViewDistance());
				this.getPlayerList().setViewDistance(i);
			}
		}
	}

	@Override
	public boolean canGenerateStructures() {
		return false;
	}

	@Override
	public GameType getDefaultGameType() {
		return this.settings.getGameType();
	}

	@Override
	public Difficulty getDefaultDifficulty() {
		return this.minecraft.level.getLevelData().getDifficulty();
	}

	@Override
	public boolean isHardcore() {
		return this.settings.isHardcore();
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
	public boolean isEpollEnabled() {
		return false;
	}

	@Override
	public void onServerCrash(CrashReport crashReport) {
		this.minecraft.delayCrash(crashReport);
	}

	@Override
	public CrashReport fillReport(CrashReport crashReport) {
		crashReport = super.fillReport(crashReport);
		crashReport.getSystemDetails().setDetail("Type", "Integrated Server (map_client.txt)");
		crashReport.getSystemDetails()
			.setDetail(
				"Is Modded",
				(CrashReportDetail<String>)(() -> (String)this.getModdedStatus()
						.orElse("Probably not. Jar signature remains and both client + server brands are untouched."))
			);
		return crashReport;
	}

	@Override
	public Optional<String> getModdedStatus() {
		String string = ClientBrandRetriever.getClientModName();
		if (!string.equals("vanilla")) {
			return Optional.of("Definitely; Client brand changed to '" + string + "'");
		} else {
			string = this.getServerModName();
			if (!"vanilla".equals(string)) {
				return Optional.of("Definitely; Server brand changed to '" + string + "'");
			} else {
				return Minecraft.class.getSigners() == null ? Optional.of("Very likely; Jar signature invalidated") : Optional.empty();
			}
		}
	}

	@Override
	public void populateSnooper(Snooper snooper) {
		super.populateSnooper(snooper);
		snooper.setDynamicData("snooper_partner", this.minecraft.getSnooper().getToken());
	}

	@Override
	public boolean publishServer(GameType gameType, boolean bl, int i) {
		try {
			this.getConnection().startTcpServerListener(null, i);
			LOGGER.info("Started serving on {}", i);
			this.publishedPort = i;
			this.lanPinger = new LanServerPinger(this.getMotd(), i + "");
			this.lanPinger.start();
			this.getPlayerList().setOverrideGameMode(gameType);
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
	public void setDefaultGameMode(GameType gameType) {
		super.setDefaultGameMode(gameType);
		this.getPlayerList().setOverrideGameMode(gameType);
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
}
