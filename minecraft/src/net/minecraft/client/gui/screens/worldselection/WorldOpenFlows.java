package net.minecraft.client.gui.screens.worldselection;

import com.mojang.datafixers.util.Pair;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.Lifecycle;
import it.unimi.dsi.fastutil.booleans.BooleanConsumer;
import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.toasts.SystemToast;
import net.minecraft.client.gui.screens.BackupConfirmScreen;
import net.minecraft.client.gui.screens.ConfirmScreen;
import net.minecraft.client.gui.screens.DatapackLoadFailureScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.commands.Commands;
import net.minecraft.core.RegistryAccess;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.RegistryOps;
import net.minecraft.server.ReloadableServerResources;
import net.minecraft.server.WorldLoader;
import net.minecraft.server.WorldStem;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.repository.FolderRepositorySource;
import net.minecraft.server.packs.repository.PackRepository;
import net.minecraft.server.packs.repository.PackSource;
import net.minecraft.server.packs.repository.ServerPacksSource;
import net.minecraft.server.packs.resources.CloseableResourceManager;
import net.minecraft.world.level.DataPackConfig;
import net.minecraft.world.level.LevelSettings;
import net.minecraft.world.level.levelgen.WorldGenSettings;
import net.minecraft.world.level.storage.LevelResource;
import net.minecraft.world.level.storage.LevelStorageSource;
import net.minecraft.world.level.storage.PrimaryLevelData;
import net.minecraft.world.level.storage.WorldData;
import org.slf4j.Logger;

@Environment(EnvType.CLIENT)
public class WorldOpenFlows {
	private static final Logger LOGGER = LogUtils.getLogger();
	private final Minecraft minecraft;
	private final LevelStorageSource levelSource;

	public WorldOpenFlows(Minecraft minecraft, LevelStorageSource levelStorageSource) {
		this.minecraft = minecraft;
		this.levelSource = levelStorageSource;
	}

	public void loadLevel(Screen screen, String string) {
		this.doLoadLevel(screen, string, false, true);
	}

	public void createFreshLevel(String string, LevelSettings levelSettings, RegistryAccess registryAccess, WorldGenSettings worldGenSettings) {
		LevelStorageSource.LevelStorageAccess levelStorageAccess = this.createWorldAccess(string);
		if (levelStorageAccess != null) {
			PackRepository packRepository = createPackRepository(levelStorageAccess);
			DataPackConfig dataPackConfig = levelSettings.getDataPackConfig();

			try {
				WorldLoader.PackConfig packConfig = new WorldLoader.PackConfig(packRepository, dataPackConfig, false);
				WorldStem worldStem = this.loadWorldStem(
					packConfig,
					(resourceManager, dataPackConfigx) -> Pair.of(new PrimaryLevelData(levelSettings, worldGenSettings, Lifecycle.stable()), registryAccess.freeze())
				);
				this.minecraft.doWorldLoad(string, levelStorageAccess, packRepository, worldStem);
			} catch (Exception var10) {
				LOGGER.warn("Failed to load datapacks, can't proceed with server load", (Throwable)var10);
				safeCloseAccess(levelStorageAccess, string);
			}
		}
	}

	@Nullable
	private LevelStorageSource.LevelStorageAccess createWorldAccess(String string) {
		try {
			return this.levelSource.createAccess(string);
		} catch (IOException var3) {
			LOGGER.warn("Failed to read level {} data", string, var3);
			SystemToast.onWorldAccessFailure(this.minecraft, string);
			this.minecraft.setScreen(null);
			return null;
		}
	}

	public void createLevelFromExistingSettings(
		LevelStorageSource.LevelStorageAccess levelStorageAccess,
		ReloadableServerResources reloadableServerResources,
		RegistryAccess.Frozen frozen,
		WorldData worldData
	) {
		PackRepository packRepository = createPackRepository(levelStorageAccess);
		CloseableResourceManager closeableResourceManager = new WorldLoader.PackConfig(packRepository, worldData.getDataPackConfig(), false)
			.createResourceManager()
			.getSecond();
		this.minecraft
			.doWorldLoad(
				levelStorageAccess.getLevelId(), levelStorageAccess, packRepository, new WorldStem(closeableResourceManager, reloadableServerResources, frozen, worldData)
			);
	}

	private static PackRepository createPackRepository(LevelStorageSource.LevelStorageAccess levelStorageAccess) {
		return new PackRepository(
			PackType.SERVER_DATA,
			new ServerPacksSource(),
			new FolderRepositorySource(levelStorageAccess.getLevelPath(LevelResource.DATAPACK_DIR).toFile(), PackSource.WORLD)
		);
	}

	private WorldStem loadWorldStem(LevelStorageSource.LevelStorageAccess levelStorageAccess, boolean bl, PackRepository packRepository) throws Exception {
		DataPackConfig dataPackConfig = levelStorageAccess.getDataPacks();
		if (dataPackConfig == null) {
			throw new IllegalStateException("Failed to load data pack config");
		} else {
			WorldLoader.PackConfig packConfig = new WorldLoader.PackConfig(packRepository, dataPackConfig, bl);
			return this.loadWorldStem(packConfig, (resourceManager, dataPackConfigx) -> {
				RegistryAccess.Writable writable = RegistryAccess.builtinCopy();
				DynamicOps<Tag> dynamicOps = RegistryOps.createAndLoad(NbtOps.INSTANCE, writable, resourceManager);
				WorldData worldData = levelStorageAccess.getDataTag(dynamicOps, dataPackConfigx, writable.allElementsLifecycle());
				if (worldData == null) {
					throw new IllegalStateException("Failed to load world");
				} else {
					return Pair.of(worldData, writable.freeze());
				}
			});
		}
	}

	public WorldStem loadWorldStem(LevelStorageSource.LevelStorageAccess levelStorageAccess, boolean bl) throws Exception {
		PackRepository packRepository = createPackRepository(levelStorageAccess);
		return this.loadWorldStem(levelStorageAccess, bl, packRepository);
	}

	private WorldStem loadWorldStem(WorldLoader.PackConfig packConfig, WorldLoader.WorldDataSupplier<WorldData> worldDataSupplier) throws Exception {
		WorldLoader.InitConfig initConfig = new WorldLoader.InitConfig(packConfig, Commands.CommandSelection.INTEGRATED, 2);
		CompletableFuture<WorldStem> completableFuture = WorldStem.load(initConfig, worldDataSupplier, Util.backgroundExecutor(), this.minecraft);
		this.minecraft.managedBlock(completableFuture::isDone);
		return (WorldStem)completableFuture.get();
	}

	private void doLoadLevel(Screen screen, String string, boolean bl, boolean bl2) {
		LevelStorageSource.LevelStorageAccess levelStorageAccess = this.createWorldAccess(string);
		if (levelStorageAccess != null) {
			PackRepository packRepository = createPackRepository(levelStorageAccess);

			WorldStem worldStem;
			try {
				worldStem = this.loadWorldStem(levelStorageAccess, bl, packRepository);
			} catch (Exception var11) {
				LOGGER.warn("Failed to load datapacks, can't proceed with server load", (Throwable)var11);
				this.minecraft.setScreen(new DatapackLoadFailureScreen(() -> this.doLoadLevel(screen, string, true, bl2)));
				safeCloseAccess(levelStorageAccess, string);
				return;
			}

			WorldData worldData = worldStem.worldData();
			boolean bl3 = worldData.worldGenSettings().isOldCustomizedWorld();
			boolean bl4 = worldData.worldGenSettingsLifecycle() != Lifecycle.stable();
			if (!bl2 || !bl3 && !bl4) {
				this.minecraft.doWorldLoad(string, levelStorageAccess, packRepository, worldStem);
			} else {
				this.askForBackup(screen, string, bl3, () -> this.doLoadLevel(screen, string, bl, false));
				worldStem.close();
				safeCloseAccess(levelStorageAccess, string);
			}
		}
	}

	private static void safeCloseAccess(LevelStorageSource.LevelStorageAccess levelStorageAccess, String string) {
		try {
			levelStorageAccess.close();
		} catch (IOException var3) {
			LOGGER.warn("Failed to unlock access to level {}", string, var3);
		}
	}

	private void askForBackup(Screen screen, String string, boolean bl, Runnable runnable) {
		Component component;
		Component component2;
		if (bl) {
			component = new TranslatableComponent("selectWorld.backupQuestion.customized");
			component2 = new TranslatableComponent("selectWorld.backupWarning.customized");
		} else {
			component = new TranslatableComponent("selectWorld.backupQuestion.experimental");
			component2 = new TranslatableComponent("selectWorld.backupWarning.experimental");
		}

		this.minecraft.setScreen(new BackupConfirmScreen(screen, (blx, bl2) -> {
			if (blx) {
				EditWorldScreen.makeBackupAndShowToast(this.levelSource, string);
			}

			runnable.run();
		}, component, component2, false));
	}

	public static void confirmWorldCreation(Minecraft minecraft, CreateWorldScreen createWorldScreen, Lifecycle lifecycle, Runnable runnable) {
		BooleanConsumer booleanConsumer = bl -> {
			if (bl) {
				runnable.run();
			} else {
				minecraft.setScreen(createWorldScreen);
			}
		};
		if (lifecycle == Lifecycle.stable()) {
			runnable.run();
		} else if (lifecycle == Lifecycle.experimental()) {
			minecraft.setScreen(
				new ConfirmScreen(
					booleanConsumer,
					new TranslatableComponent("selectWorld.import_worldgen_settings.experimental.title"),
					new TranslatableComponent("selectWorld.import_worldgen_settings.experimental.question")
				)
			);
		} else {
			minecraft.setScreen(
				new ConfirmScreen(
					booleanConsumer,
					new TranslatableComponent("selectWorld.import_worldgen_settings.deprecated.title"),
					new TranslatableComponent("selectWorld.import_worldgen_settings.deprecated.question")
				)
			);
		}
	}
}
