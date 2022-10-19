package net.minecraft.client.gui.screens.worldselection;

import com.mojang.datafixers.util.Pair;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.Lifecycle;
import it.unimi.dsi.fastutil.booleans.BooleanConsumer;
import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.CrashReport;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.toasts.SystemToast;
import net.minecraft.client.gui.screens.BackupConfirmScreen;
import net.minecraft.client.gui.screens.ConfirmScreen;
import net.minecraft.client.gui.screens.DatapackLoadFailureScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.commands.Commands;
import net.minecraft.core.LayeredRegistryAccess;
import net.minecraft.core.MappedRegistry;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.RegistryOps;
import net.minecraft.server.RegistryLayer;
import net.minecraft.server.ReloadableServerResources;
import net.minecraft.server.WorldLoader;
import net.minecraft.server.WorldStem;
import net.minecraft.server.packs.repository.PackRepository;
import net.minecraft.server.packs.repository.ServerPacksSource;
import net.minecraft.server.packs.resources.CloseableResourceManager;
import net.minecraft.world.level.LevelSettings;
import net.minecraft.world.level.WorldDataConfiguration;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.level.levelgen.WorldDimensions;
import net.minecraft.world.level.levelgen.WorldOptions;
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

	public void createFreshLevel(String string, LevelSettings levelSettings, WorldOptions worldOptions, Function<RegistryAccess, WorldDimensions> function) {
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
							.bake(dataLoadContext.datapackDimensions().registryOrThrow(Registry.LEVEL_STEM_REGISTRY));
						return new WorldLoader.DataLoadOutput<>(
							new PrimaryLevelData(levelSettings, worldOptions, complete.specialWorldProperty(), complete.lifecycle()), complete.dimensionsRegistryAccess()
						);
					},
					WorldStem::new
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
		LayeredRegistryAccess<RegistryLayer> layeredRegistryAccess,
		WorldData worldData
	) {
		PackRepository packRepository = ServerPacksSource.createPackRepository(levelStorageAccess);
		CloseableResourceManager closeableResourceManager = new WorldLoader.PackConfig(packRepository, worldData.getDataConfiguration(), false, false)
			.createResourceManager()
			.getSecond();
		this.minecraft
			.doWorldLoad(
				levelStorageAccess.getLevelId(),
				levelStorageAccess,
				packRepository,
				new WorldStem(closeableResourceManager, reloadableServerResources, layeredRegistryAccess, worldData)
			);
	}

	private WorldStem loadWorldStem(LevelStorageSource.LevelStorageAccess levelStorageAccess, boolean bl, PackRepository packRepository) throws Exception {
		WorldLoader.PackConfig packConfig = this.getPackConfigFromLevelData(levelStorageAccess, bl, packRepository);
		return this.loadWorldDataBlocking(
			packConfig,
			dataLoadContext -> {
				DynamicOps<Tag> dynamicOps = RegistryOps.create(NbtOps.INSTANCE, dataLoadContext.datapackWorldgen());
				Registry<LevelStem> registry = dataLoadContext.datapackDimensions().registryOrThrow(Registry.LEVEL_STEM_REGISTRY);
				Pair<WorldData, WorldDimensions.Complete> pair = levelStorageAccess.getDataTag(
					dynamicOps, dataLoadContext.dataConfiguration(), registry, dataLoadContext.datapackWorldgen().allElementsLifecycle()
				);
				if (pair == null) {
					throw new IllegalStateException("Failed to load world");
				} else {
					return new WorldLoader.DataLoadOutput<>(pair.getFirst(), pair.getSecond().dimensionsRegistryAccess());
				}
			},
			WorldStem::new
		);
	}

	public Pair<LevelSettings, WorldCreationContext> recreateWorldData(LevelStorageSource.LevelStorageAccess levelStorageAccess) throws Exception {
		PackRepository packRepository = ServerPacksSource.createPackRepository(levelStorageAccess);
		WorldLoader.PackConfig packConfig = this.getPackConfigFromLevelData(levelStorageAccess, false, packRepository);

		@Environment(EnvType.CLIENT)
		record Data(LevelSettings levelSettings, WorldOptions options, Registry<LevelStem> existingDimensions) {
		}

		return this.loadWorldDataBlocking(
			packConfig,
			dataLoadContext -> {
				DynamicOps<Tag> dynamicOps = RegistryOps.create(NbtOps.INSTANCE, dataLoadContext.datapackWorldgen());
				Registry<LevelStem> registry = new MappedRegistry<>(Registry.LEVEL_STEM_REGISTRY, Lifecycle.stable()).freeze();
				Pair<WorldData, WorldDimensions.Complete> pair = levelStorageAccess.getDataTag(
					dynamicOps, dataLoadContext.dataConfiguration(), registry, dataLoadContext.datapackWorldgen().allElementsLifecycle()
				);
				if (pair == null) {
					throw new IllegalStateException("Failed to load world");
				} else {
					return new WorldLoader.DataLoadOutput<>(
						new Data(pair.getFirst().getLevelSettings(), pair.getFirst().worldGenOptions(), pair.getSecond().dimensions()), dataLoadContext.datapackDimensions()
					);
				}
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

	private WorldLoader.PackConfig getPackConfigFromLevelData(LevelStorageSource.LevelStorageAccess levelStorageAccess, boolean bl, PackRepository packRepository) {
		WorldDataConfiguration worldDataConfiguration = levelStorageAccess.getDataConfiguration();
		if (worldDataConfiguration == null) {
			throw new IllegalStateException("Failed to load data pack config");
		} else {
			return new WorldLoader.PackConfig(packRepository, worldDataConfiguration, bl, false);
		}
	}

	public WorldStem loadWorldStem(LevelStorageSource.LevelStorageAccess levelStorageAccess, boolean bl) throws Exception {
		PackRepository packRepository = ServerPacksSource.createPackRepository(levelStorageAccess);
		return this.loadWorldStem(levelStorageAccess, bl, packRepository);
	}

	private <D, R> R loadWorldDataBlocking(
		WorldLoader.PackConfig packConfig, WorldLoader.WorldDataSupplier<D> worldDataSupplier, WorldLoader.ResultFactory<D, R> resultFactory
	) throws Exception {
		WorldLoader.InitConfig initConfig = new WorldLoader.InitConfig(packConfig, Commands.CommandSelection.INTEGRATED, 2);
		CompletableFuture<R> completableFuture = WorldLoader.load(initConfig, worldDataSupplier, resultFactory, Util.backgroundExecutor(), this.minecraft);
		this.minecraft.managedBlock(completableFuture::isDone);
		return (R)completableFuture.get();
	}

	private void doLoadLevel(Screen screen, String string, boolean bl, boolean bl2) {
		LevelStorageSource.LevelStorageAccess levelStorageAccess = this.createWorldAccess(string);
		if (levelStorageAccess != null) {
			PackRepository packRepository = ServerPacksSource.createPackRepository(levelStorageAccess);

			WorldStem worldStem;
			try {
				worldStem = this.loadWorldStem(levelStorageAccess, bl, packRepository);
			} catch (Exception var11) {
				LOGGER.warn("Failed to load level data or datapacks, can't proceed with server load", (Throwable)var11);
				this.minecraft.setScreen(new DatapackLoadFailureScreen(() -> this.doLoadLevel(screen, string, true, bl2)));
				safeCloseAccess(levelStorageAccess, string);
				return;
			}

			WorldData worldData = worldStem.worldData();
			boolean bl3 = worldData.worldGenOptions().isOldCustomizedWorld();
			boolean bl4 = worldData.worldGenSettingsLifecycle() != Lifecycle.stable();
			if (!bl2 || !bl3 && !bl4) {
				this.minecraft.getDownloadedPackSource().loadBundledResourcePack(levelStorageAccess).thenApply(void_ -> true).exceptionallyComposeAsync(throwable -> {
					LOGGER.warn("Failed to load pack: ", throwable);
					return this.promptBundledPackLoadFailure();
				}, this.minecraft).thenAcceptAsync(boolean_ -> {
					if (boolean_) {
						this.minecraft.doWorldLoad(string, levelStorageAccess, packRepository, worldStem);
					} else {
						worldStem.close();
						safeCloseAccess(levelStorageAccess, string);
						this.minecraft.getDownloadedPackSource().clearServerPack().thenRunAsync(() -> this.minecraft.setScreen(screen), this.minecraft);
					}
				}, this.minecraft).exceptionally(throwable -> {
					this.minecraft.delayCrash(CrashReport.forThrowable(throwable, "Load world"));
					return null;
				});
			} else {
				this.askForBackup(screen, string, bl3, () -> this.doLoadLevel(screen, string, bl, false));
				worldStem.close();
				safeCloseAccess(levelStorageAccess, string);
			}
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
			component = Component.translatable("selectWorld.backupQuestion.customized");
			component2 = Component.translatable("selectWorld.backupWarning.customized");
		} else {
			component = Component.translatable("selectWorld.backupQuestion.experimental");
			component2 = Component.translatable("selectWorld.backupWarning.experimental");
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
}
