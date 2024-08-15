package net.minecraft.client.gui.screens.worldselection;

import com.mojang.datafixers.util.Pair;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.Lifecycle;
import it.unimi.dsi.fastutil.booleans.BooleanConsumer;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.ChatFormatting;
import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.ReportedException;
import net.minecraft.SharedConstants;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.toasts.SystemToast;
import net.minecraft.client.gui.screens.AlertScreen;
import net.minecraft.client.gui.screens.BackupConfirmScreen;
import net.minecraft.client.gui.screens.ConfirmScreen;
import net.minecraft.client.gui.screens.DatapackLoadFailureScreen;
import net.minecraft.client.gui.screens.GenericMessageScreen;
import net.minecraft.client.gui.screens.NoticeWithLinkScreen;
import net.minecraft.client.gui.screens.RecoverWorldDataScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.server.DownloadedPackSource;
import net.minecraft.commands.Commands;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.LayeredRegistryAccess;
import net.minecraft.core.MappedRegistry;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.NbtException;
import net.minecraft.nbt.ReportedNbtException;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.RegistryLayer;
import net.minecraft.server.ReloadableServerResources;
import net.minecraft.server.WorldLoader;
import net.minecraft.server.WorldStem;
import net.minecraft.server.packs.repository.PackRepository;
import net.minecraft.server.packs.repository.ServerPacksSource;
import net.minecraft.server.packs.resources.CloseableResourceManager;
import net.minecraft.util.MemoryReserve;
import net.minecraft.world.level.LevelSettings;
import net.minecraft.world.level.WorldDataConfiguration;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.level.levelgen.WorldDimensions;
import net.minecraft.world.level.levelgen.WorldOptions;
import net.minecraft.world.level.storage.LevelDataAndDimensions;
import net.minecraft.world.level.storage.LevelResource;
import net.minecraft.world.level.storage.LevelStorageSource;
import net.minecraft.world.level.storage.LevelSummary;
import net.minecraft.world.level.storage.PrimaryLevelData;
import net.minecraft.world.level.storage.WorldData;
import net.minecraft.world.level.validation.ContentValidationException;
import org.slf4j.Logger;

@Environment(EnvType.CLIENT)
public class WorldOpenFlows {
	private static final Logger LOGGER = LogUtils.getLogger();
	private static final UUID WORLD_PACK_ID = UUID.fromString("640a6a92-b6cb-48a0-b391-831586500359");
	private final Minecraft minecraft;
	private final LevelStorageSource levelSource;

	public WorldOpenFlows(Minecraft minecraft, LevelStorageSource levelStorageSource) {
		this.minecraft = minecraft;
		this.levelSource = levelStorageSource;
	}

	public void createFreshLevel(
		String string, LevelSettings levelSettings, WorldOptions worldOptions, Function<HolderLookup.Provider, WorldDimensions> function, Screen screen
	) {
		this.minecraft.forceSetScreen(new GenericMessageScreen(Component.translatable("selectWorld.data_read")));
		LevelStorageSource.LevelStorageAccess levelStorageAccess = this.createWorldAccess(string);
		if (levelStorageAccess != null) {
			PackRepository packRepository = ServerPacksSource.createPackRepository(levelStorageAccess);
			WorldDataConfiguration worldDataConfiguration = levelSettings.getDataConfiguration();

			try {
				WorldLoader.PackConfig packConfig = new WorldLoader.PackConfig(packRepository, worldDataConfiguration, false, false);
				WorldStem worldStem = this.loadWorldDataBlocking(
					packConfig,
					dataLoadContext -> {
						WorldDimensions.Complete complete = ((WorldDimensions)function.apply(dataLoadContext.datapackWorldgen()))
							.bake(dataLoadContext.datapackDimensions().registryOrThrow(Registries.LEVEL_STEM));
						return new WorldLoader.DataLoadOutput<>(
							new PrimaryLevelData(levelSettings, worldOptions, complete.specialWorldProperty(), complete.lifecycle()), complete.dimensionsRegistryAccess()
						);
					},
					WorldStem::new
				);
				this.minecraft.doWorldLoad(levelStorageAccess, packRepository, worldStem, true);
			} catch (Exception var11) {
				LOGGER.warn("Failed to load datapacks, can't proceed with server load", (Throwable)var11);
				levelStorageAccess.safeClose();
				this.minecraft.setScreen(screen);
			}
		}
	}

	@Nullable
	private LevelStorageSource.LevelStorageAccess createWorldAccess(String string) {
		try {
			return this.levelSource.validateAndCreateAccess(string);
		} catch (IOException var3) {
			LOGGER.warn("Failed to read level {} data", string, var3);
			SystemToast.onWorldAccessFailure(this.minecraft, string);
			this.minecraft.setScreen(null);
			return null;
		} catch (ContentValidationException var4) {
			LOGGER.warn("{}", var4.getMessage());
			this.minecraft.setScreen(NoticeWithLinkScreen.createWorldSymlinkWarningScreen(() -> this.minecraft.setScreen(null)));
			return null;
		}
	}

	public void createLevelFromExistingSettings(
		LevelStorageSource.LevelStorageAccess levelStorageAccess,
		ReloadableServerResources reloadableServerResources,
		LayeredRegistryAccess<RegistryLayer> layeredRegistryAccess,
		WorldData worldData
	) {
		PackRepository packRepository = ServerPacksSource.createPackRepository(levelStorageAccess);
		CloseableResourceManager closeableResourceManager = new WorldLoader.PackConfig(packRepository, worldData.getDataConfiguration(), false, false)
			.createResourceManager()
			.getSecond();
		this.minecraft
			.doWorldLoad(levelStorageAccess, packRepository, new WorldStem(closeableResourceManager, reloadableServerResources, layeredRegistryAccess, worldData), true);
	}

	public WorldStem loadWorldStem(Dynamic<?> dynamic, boolean bl, PackRepository packRepository) throws Exception {
		WorldLoader.PackConfig packConfig = LevelStorageSource.getPackConfig(dynamic, packRepository, bl);
		return this.loadWorldDataBlocking(
			packConfig,
			dataLoadContext -> {
				Registry<LevelStem> registry = dataLoadContext.datapackDimensions().registryOrThrow(Registries.LEVEL_STEM);
				LevelDataAndDimensions levelDataAndDimensions = LevelStorageSource.getLevelDataAndDimensions(
					dynamic, dataLoadContext.dataConfiguration(), registry, dataLoadContext.datapackWorldgen()
				);
				return new WorldLoader.DataLoadOutput<>(levelDataAndDimensions.worldData(), levelDataAndDimensions.dimensions().dimensionsRegistryAccess());
			},
			WorldStem::new
		);
	}

	public Pair<LevelSettings, WorldCreationContext> recreateWorldData(LevelStorageSource.LevelStorageAccess levelStorageAccess) throws Exception {
		PackRepository packRepository = ServerPacksSource.createPackRepository(levelStorageAccess);
		Dynamic<?> dynamic = levelStorageAccess.getDataTag();
		WorldLoader.PackConfig packConfig = LevelStorageSource.getPackConfig(dynamic, packRepository, false);

		@Environment(EnvType.CLIENT)
		record Data(LevelSettings levelSettings, WorldOptions options, Registry<LevelStem> existingDimensions) {
		}

		return this.loadWorldDataBlocking(
			packConfig,
			dataLoadContext -> {
				Registry<LevelStem> registry = new MappedRegistry<>(Registries.LEVEL_STEM, Lifecycle.stable()).freeze();
				LevelDataAndDimensions levelDataAndDimensions = LevelStorageSource.getLevelDataAndDimensions(
					dynamic, dataLoadContext.dataConfiguration(), registry, dataLoadContext.datapackWorldgen()
				);
				return new WorldLoader.DataLoadOutput<>(
					new Data(
						levelDataAndDimensions.worldData().getLevelSettings(),
						levelDataAndDimensions.worldData().worldGenOptions(),
						levelDataAndDimensions.dimensions().dimensions()
					),
					dataLoadContext.datapackDimensions()
				);
			},
			(closeableResourceManager, reloadableServerResources, layeredRegistryAccess, arg) -> {
				closeableResourceManager.close();
				return Pair.of(
					arg.levelSettings,
					new WorldCreationContext(
						arg.options, new WorldDimensions(arg.existingDimensions), layeredRegistryAccess, reloadableServerResources, arg.levelSettings.getDataConfiguration()
					)
				);
			}
		);
	}

	private <D, R> R loadWorldDataBlocking(
		WorldLoader.PackConfig packConfig, WorldLoader.WorldDataSupplier<D> worldDataSupplier, WorldLoader.ResultFactory<D, R> resultFactory
	) throws Exception {
		WorldLoader.InitConfig initConfig = new WorldLoader.InitConfig(packConfig, Commands.CommandSelection.INTEGRATED, 2);
		CompletableFuture<R> completableFuture = WorldLoader.load(initConfig, worldDataSupplier, resultFactory, Util.backgroundExecutor(), this.minecraft);
		this.minecraft.managedBlock(completableFuture::isDone);
		return (R)completableFuture.get();
	}

	private void askForBackup(LevelStorageSource.LevelStorageAccess levelStorageAccess, boolean bl, Runnable runnable, Runnable runnable2) {
		Component component;
		Component component2;
		if (bl) {
			component = Component.translatable("selectWorld.backupQuestion.customized");
			component2 = Component.translatable("selectWorld.backupWarning.customized");
		} else {
			component = Component.translatable("selectWorld.backupQuestion.experimental");
			component2 = Component.translatable("selectWorld.backupWarning.experimental");
		}

		this.minecraft.setScreen(new BackupConfirmScreen(runnable2, (blx, bl2) -> {
			if (blx) {
				EditWorldScreen.makeBackupAndShowToast(levelStorageAccess);
			}

			runnable.run();
		}, component, component2, false));
	}

	public static void confirmWorldCreation(Minecraft minecraft, CreateWorldScreen createWorldScreen, Lifecycle lifecycle, Runnable runnable, boolean bl) {
		BooleanConsumer booleanConsumer = blx -> {
			if (blx) {
				runnable.run();
			} else {
				minecraft.setScreen(createWorldScreen);
			}
		};
		if (bl || lifecycle == Lifecycle.stable()) {
			runnable.run();
		} else if (lifecycle == Lifecycle.experimental()) {
			minecraft.setScreen(
				new ConfirmScreen(
					booleanConsumer, Component.translatable("selectWorld.warning.experimental.title"), Component.translatable("selectWorld.warning.experimental.question")
				)
			);
		} else {
			minecraft.setScreen(
				new ConfirmScreen(
					booleanConsumer, Component.translatable("selectWorld.warning.deprecated.title"), Component.translatable("selectWorld.warning.deprecated.question")
				)
			);
		}
	}

	public void openWorld(String string, Runnable runnable) {
		this.minecraft.forceSetScreen(new GenericMessageScreen(Component.translatable("selectWorld.data_read")));
		LevelStorageSource.LevelStorageAccess levelStorageAccess = this.createWorldAccess(string);
		if (levelStorageAccess != null) {
			this.openWorldLoadLevelData(levelStorageAccess, runnable);
		}
	}

	private void openWorldLoadLevelData(LevelStorageSource.LevelStorageAccess levelStorageAccess, Runnable runnable) {
		this.minecraft.forceSetScreen(new GenericMessageScreen(Component.translatable("selectWorld.data_read")));

		Dynamic<?> dynamic;
		LevelSummary levelSummary;
		try {
			dynamic = levelStorageAccess.getDataTag();
			levelSummary = levelStorageAccess.getSummary(dynamic);
		} catch (NbtException | ReportedNbtException | IOException var10) {
			this.minecraft.setScreen(new RecoverWorldDataScreen(this.minecraft, bl -> {
				if (bl) {
					this.openWorldLoadLevelData(levelStorageAccess, runnable);
				} else {
					levelStorageAccess.safeClose();
					runnable.run();
				}
			}, levelStorageAccess));
			return;
		} catch (OutOfMemoryError var11) {
			MemoryReserve.release();
			System.gc();
			String string = "Ran out of memory trying to read level data of world folder \"" + levelStorageAccess.getLevelId() + "\"";
			LOGGER.error(LogUtils.FATAL_MARKER, string);
			OutOfMemoryError outOfMemoryError2 = new OutOfMemoryError("Ran out of memory reading level data");
			outOfMemoryError2.initCause(var11);
			CrashReport crashReport = CrashReport.forThrowable(outOfMemoryError2, string);
			CrashReportCategory crashReportCategory = crashReport.addCategory("World details");
			crashReportCategory.setDetail("World folder", levelStorageAccess.getLevelId());
			throw new ReportedException(crashReport);
		}

		this.openWorldCheckVersionCompatibility(levelStorageAccess, levelSummary, dynamic, runnable);
	}

	private void openWorldCheckVersionCompatibility(
		LevelStorageSource.LevelStorageAccess levelStorageAccess, LevelSummary levelSummary, Dynamic<?> dynamic, Runnable runnable
	) {
		if (!levelSummary.isCompatible()) {
			levelStorageAccess.safeClose();
			this.minecraft
				.setScreen(
					new AlertScreen(
						runnable,
						Component.translatable("selectWorld.incompatible.title").withColor(-65536),
						Component.translatable("selectWorld.incompatible.description", levelSummary.getWorldVersionName())
					)
				);
		} else {
			LevelSummary.BackupStatus backupStatus = levelSummary.backupStatus();
			if (backupStatus.shouldBackup()) {
				String string = "selectWorld.backupQuestion." + backupStatus.getTranslationKey();
				String string2 = "selectWorld.backupWarning." + backupStatus.getTranslationKey();
				MutableComponent mutableComponent = Component.translatable(string);
				if (backupStatus.isSevere()) {
					mutableComponent.withColor(-2142128);
				}

				Component component = Component.translatable(string2, levelSummary.getWorldVersionName(), SharedConstants.getCurrentVersion().getName());
				this.minecraft.setScreen(new BackupConfirmScreen(() -> {
					levelStorageAccess.safeClose();
					runnable.run();
				}, (bl, bl2) -> {
					if (bl) {
						EditWorldScreen.makeBackupAndShowToast(levelStorageAccess);
					}

					this.openWorldLoadLevelStem(levelStorageAccess, dynamic, false, runnable);
				}, mutableComponent, component, false));
			} else {
				this.openWorldLoadLevelStem(levelStorageAccess, dynamic, false, runnable);
			}
		}
	}

	private void openWorldLoadLevelStem(LevelStorageSource.LevelStorageAccess levelStorageAccess, Dynamic<?> dynamic, boolean bl, Runnable runnable) {
		this.minecraft.forceSetScreen(new GenericMessageScreen(Component.translatable("selectWorld.resource_load")));
		PackRepository packRepository = ServerPacksSource.createPackRepository(levelStorageAccess);

		WorldStem worldStem;
		try {
			worldStem = this.loadWorldStem(dynamic, bl, packRepository);

			for (LevelStem levelStem : worldStem.registries().compositeAccess().registryOrThrow(Registries.LEVEL_STEM)) {
				levelStem.generator().validate();
			}
		} catch (Exception var9) {
			LOGGER.warn("Failed to load level data or datapacks, can't proceed with server load", (Throwable)var9);
			if (!bl) {
				this.minecraft.setScreen(new DatapackLoadFailureScreen(() -> {
					levelStorageAccess.safeClose();
					runnable.run();
				}, () -> this.openWorldLoadLevelStem(levelStorageAccess, dynamic, true, runnable)));
			} else {
				levelStorageAccess.safeClose();
				this.minecraft
					.setScreen(
						new AlertScreen(
							runnable,
							Component.translatable("datapackFailure.safeMode.failed.title"),
							Component.translatable("datapackFailure.safeMode.failed.description"),
							CommonComponents.GUI_BACK,
							true
						)
					);
			}

			return;
		}

		this.openWorldCheckWorldStemCompatibility(levelStorageAccess, worldStem, packRepository, runnable);
	}

	private void openWorldCheckWorldStemCompatibility(
		LevelStorageSource.LevelStorageAccess levelStorageAccess, WorldStem worldStem, PackRepository packRepository, Runnable runnable
	) {
		WorldData worldData = worldStem.worldData();
		boolean bl = worldData.worldGenOptions().isOldCustomizedWorld();
		boolean bl2 = worldData.worldGenSettingsLifecycle() != Lifecycle.stable();
		if (!bl && !bl2) {
			this.openWorldLoadBundledResourcePack(levelStorageAccess, worldStem, packRepository, runnable);
		} else {
			this.askForBackup(levelStorageAccess, bl, () -> this.openWorldLoadBundledResourcePack(levelStorageAccess, worldStem, packRepository, runnable), () -> {
				worldStem.close();
				levelStorageAccess.safeClose();
				runnable.run();
			});
		}
	}

	private void openWorldLoadBundledResourcePack(
		LevelStorageSource.LevelStorageAccess levelStorageAccess, WorldStem worldStem, PackRepository packRepository, Runnable runnable
	) {
		DownloadedPackSource downloadedPackSource = this.minecraft.getDownloadedPackSource();
		this.loadBundledResourcePack(downloadedPackSource, levelStorageAccess).thenApply(void_ -> true).exceptionallyComposeAsync(throwable -> {
			LOGGER.warn("Failed to load pack: ", throwable);
			return this.promptBundledPackLoadFailure();
		}, this.minecraft).thenAcceptAsync(boolean_ -> {
			if (boolean_) {
				this.openWorldCheckDiskSpace(levelStorageAccess, worldStem, downloadedPackSource, packRepository, runnable);
			} else {
				downloadedPackSource.popAll();
				worldStem.close();
				levelStorageAccess.safeClose();
				runnable.run();
			}
		}, this.minecraft).exceptionally(throwable -> {
			this.minecraft.delayCrash(CrashReport.forThrowable(throwable, "Load world"));
			return null;
		});
	}

	private void openWorldCheckDiskSpace(
		LevelStorageSource.LevelStorageAccess levelStorageAccess,
		WorldStem worldStem,
		DownloadedPackSource downloadedPackSource,
		PackRepository packRepository,
		Runnable runnable
	) {
		if (levelStorageAccess.checkForLowDiskSpace()) {
			this.minecraft
				.setScreen(
					new ConfirmScreen(
						bl -> {
							if (bl) {
								this.openWorldDoLoad(levelStorageAccess, worldStem, packRepository);
							} else {
								downloadedPackSource.popAll();
								worldStem.close();
								levelStorageAccess.safeClose();
								runnable.run();
							}
						},
						Component.translatable("selectWorld.warning.lowDiskSpace.title").withStyle(ChatFormatting.RED),
						Component.translatable("selectWorld.warning.lowDiskSpace.description"),
						CommonComponents.GUI_CONTINUE,
						CommonComponents.GUI_BACK
					)
				);
		} else {
			this.openWorldDoLoad(levelStorageAccess, worldStem, packRepository);
		}
	}

	private void openWorldDoLoad(LevelStorageSource.LevelStorageAccess levelStorageAccess, WorldStem worldStem, PackRepository packRepository) {
		this.minecraft.doWorldLoad(levelStorageAccess, packRepository, worldStem, false);
	}

	private CompletableFuture<Void> loadBundledResourcePack(DownloadedPackSource downloadedPackSource, LevelStorageSource.LevelStorageAccess levelStorageAccess) {
		Path path = levelStorageAccess.getLevelPath(LevelResource.MAP_RESOURCE_FILE);
		if (Files.exists(path, new LinkOption[0]) && !Files.isDirectory(path, new LinkOption[0])) {
			downloadedPackSource.configureForLocalWorld();
			CompletableFuture<Void> completableFuture = downloadedPackSource.waitForPackFeedback(WORLD_PACK_ID);
			downloadedPackSource.pushLocalPack(WORLD_PACK_ID, path);
			return completableFuture;
		} else {
			return CompletableFuture.completedFuture(null);
		}
	}

	private CompletableFuture<Boolean> promptBundledPackLoadFailure() {
		CompletableFuture<Boolean> completableFuture = new CompletableFuture();
		this.minecraft
			.setScreen(
				new ConfirmScreen(
					completableFuture::complete,
					Component.translatable("multiplayer.texturePrompt.failure.line1"),
					Component.translatable("multiplayer.texturePrompt.failure.line2"),
					CommonComponents.GUI_PROCEED,
					CommonComponents.GUI_CANCEL
				)
			);
		return completableFuture;
	}
}
