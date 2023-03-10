/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.gui.screens.worldselection;

import com.mojang.datafixers.util.Pair;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Lifecycle;
import it.unimi.dsi.fastutil.booleans.BooleanConsumer;
import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Function;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.CrashReport;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.toasts.SystemToast;
import net.minecraft.client.gui.screens.AlertScreen;
import net.minecraft.client.gui.screens.BackupConfirmScreen;
import net.minecraft.client.gui.screens.ConfirmScreen;
import net.minecraft.client.gui.screens.DatapackLoadFailureScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.worldselection.CreateWorldScreen;
import net.minecraft.client.gui.screens.worldselection.EditWorldScreen;
import net.minecraft.client.gui.screens.worldselection.WorldCreationContext;
import net.minecraft.commands.Commands;
import net.minecraft.core.LayeredRegistryAccess;
import net.minecraft.core.MappedRegistry;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
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
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

@Environment(value=EnvType.CLIENT)
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
        if (levelStorageAccess == null) {
            return;
        }
        PackRepository packRepository = ServerPacksSource.createPackRepository(levelStorageAccess);
        WorldDataConfiguration worldDataConfiguration = levelSettings.getDataConfiguration();
        try {
            WorldLoader.PackConfig packConfig = new WorldLoader.PackConfig(packRepository, worldDataConfiguration, false, false);
            WorldStem worldStem = this.loadWorldDataBlocking(packConfig, dataLoadContext -> {
                WorldDimensions.Complete complete = ((WorldDimensions)function.apply(dataLoadContext.datapackWorldgen())).bake(dataLoadContext.datapackDimensions().registryOrThrow(Registries.LEVEL_STEM));
                return new WorldLoader.DataLoadOutput<PrimaryLevelData>(new PrimaryLevelData(levelSettings, worldOptions, complete.specialWorldProperty(), complete.lifecycle()), complete.dimensionsRegistryAccess());
            }, WorldStem::new);
            this.minecraft.doWorldLoad(string, levelStorageAccess, packRepository, worldStem, true);
        } catch (Exception exception) {
            LOGGER.warn("Failed to load datapacks, can't proceed with server load", exception);
            WorldOpenFlows.safeCloseAccess(levelStorageAccess, string);
        }
    }

    @Nullable
    private LevelStorageSource.LevelStorageAccess createWorldAccess(String string) {
        try {
            return this.levelSource.createAccess(string);
        } catch (IOException iOException) {
            LOGGER.warn("Failed to read level {} data", (Object)string, (Object)iOException);
            SystemToast.onWorldAccessFailure(this.minecraft, string);
            this.minecraft.setScreen(null);
            return null;
        }
    }

    public void createLevelFromExistingSettings(LevelStorageSource.LevelStorageAccess levelStorageAccess, ReloadableServerResources reloadableServerResources, LayeredRegistryAccess<RegistryLayer> layeredRegistryAccess, WorldData worldData) {
        PackRepository packRepository = ServerPacksSource.createPackRepository(levelStorageAccess);
        CloseableResourceManager closeableResourceManager = new WorldLoader.PackConfig(packRepository, worldData.getDataConfiguration(), false, false).createResourceManager().getSecond();
        this.minecraft.doWorldLoad(levelStorageAccess.getLevelId(), levelStorageAccess, packRepository, new WorldStem(closeableResourceManager, reloadableServerResources, layeredRegistryAccess, worldData), true);
    }

    private WorldStem loadWorldStem(LevelStorageSource.LevelStorageAccess levelStorageAccess, boolean bl, PackRepository packRepository) throws Exception {
        WorldLoader.PackConfig packConfig = this.getPackConfigFromLevelData(levelStorageAccess, bl, packRepository);
        return this.loadWorldDataBlocking(packConfig, dataLoadContext -> {
            RegistryOps<Tag> dynamicOps = RegistryOps.create(NbtOps.INSTANCE, dataLoadContext.datapackWorldgen());
            Registry<LevelStem> registry = dataLoadContext.datapackDimensions().registryOrThrow(Registries.LEVEL_STEM);
            Pair<WorldData, WorldDimensions.Complete> pair = levelStorageAccess.getDataTag(dynamicOps, dataLoadContext.dataConfiguration(), registry, dataLoadContext.datapackWorldgen().allRegistriesLifecycle());
            if (pair == null) {
                throw new IllegalStateException("Failed to load world");
            }
            return new WorldLoader.DataLoadOutput<WorldData>(pair.getFirst(), pair.getSecond().dimensionsRegistryAccess());
        }, WorldStem::new);
    }

    public Pair<LevelSettings, WorldCreationContext> recreateWorldData(LevelStorageSource.LevelStorageAccess levelStorageAccess) throws Exception {
        @Environment(value=EnvType.CLIENT)
        record Data(LevelSettings levelSettings, WorldOptions options, Registry<LevelStem> existingDimensions) {
        }
        PackRepository packRepository = ServerPacksSource.createPackRepository(levelStorageAccess);
        WorldLoader.PackConfig packConfig = this.getPackConfigFromLevelData(levelStorageAccess, false, packRepository);
        return this.loadWorldDataBlocking(packConfig, dataLoadContext -> {
            RegistryOps<Tag> dynamicOps = RegistryOps.create(NbtOps.INSTANCE, dataLoadContext.datapackWorldgen());
            Registry<LevelStem> registry = new MappedRegistry<LevelStem>(Registries.LEVEL_STEM, Lifecycle.stable()).freeze();
            Pair<WorldData, WorldDimensions.Complete> pair = levelStorageAccess.getDataTag(dynamicOps, dataLoadContext.dataConfiguration(), registry, dataLoadContext.datapackWorldgen().allRegistriesLifecycle());
            if (pair == null) {
                throw new IllegalStateException("Failed to load world");
            }
            return new WorldLoader.DataLoadOutput<Data>(new Data(pair.getFirst().getLevelSettings(), pair.getFirst().worldGenOptions(), pair.getSecond().dimensions()), dataLoadContext.datapackDimensions());
        }, (closeableResourceManager, reloadableServerResources, layeredRegistryAccess, arg) -> {
            closeableResourceManager.close();
            return Pair.of(arg.levelSettings, new WorldCreationContext(arg.options, new WorldDimensions(arg.existingDimensions), layeredRegistryAccess, reloadableServerResources, arg.levelSettings.getDataConfiguration()));
        });
    }

    private WorldLoader.PackConfig getPackConfigFromLevelData(LevelStorageSource.LevelStorageAccess levelStorageAccess, boolean bl, PackRepository packRepository) {
        WorldDataConfiguration worldDataConfiguration = levelStorageAccess.getDataConfiguration();
        if (worldDataConfiguration == null) {
            throw new IllegalStateException("Failed to load data pack config");
        }
        return new WorldLoader.PackConfig(packRepository, worldDataConfiguration, bl, false);
    }

    public WorldStem loadWorldStem(LevelStorageSource.LevelStorageAccess levelStorageAccess, boolean bl) throws Exception {
        PackRepository packRepository = ServerPacksSource.createPackRepository(levelStorageAccess);
        return this.loadWorldStem(levelStorageAccess, bl, packRepository);
    }

    private <D, R> R loadWorldDataBlocking(WorldLoader.PackConfig packConfig, WorldLoader.WorldDataSupplier<D> worldDataSupplier, WorldLoader.ResultFactory<D, R> resultFactory) throws Exception {
        WorldLoader.InitConfig initConfig = new WorldLoader.InitConfig(packConfig, Commands.CommandSelection.INTEGRATED, 2);
        CompletableFuture<R> completableFuture = WorldLoader.load(initConfig, worldDataSupplier, resultFactory, Util.backgroundExecutor(), this.minecraft);
        this.minecraft.managedBlock(completableFuture::isDone);
        return completableFuture.get();
    }

    private void doLoadLevel(Screen screen, String string, boolean bl, boolean bl2) {
        boolean bl4;
        WorldStem worldStem;
        LevelStorageSource.LevelStorageAccess levelStorageAccess = this.createWorldAccess(string);
        if (levelStorageAccess == null) {
            return;
        }
        PackRepository packRepository = ServerPacksSource.createPackRepository(levelStorageAccess);
        try {
            worldStem = this.loadWorldStem(levelStorageAccess, bl, packRepository);
        } catch (Exception exception) {
            LOGGER.warn("Failed to load level data or datapacks, can't proceed with server load", exception);
            if (!bl) {
                this.minecraft.setScreen(new DatapackLoadFailureScreen(() -> this.doLoadLevel(screen, string, true, bl2)));
            } else {
                this.minecraft.setScreen(new AlertScreen(() -> this.minecraft.setScreen(null), Component.translatable("datapackFailure.safeMode.failed.title"), Component.translatable("datapackFailure.safeMode.failed.description"), CommonComponents.GUI_TO_TITLE, true));
            }
            WorldOpenFlows.safeCloseAccess(levelStorageAccess, string);
            return;
        }
        WorldData worldData = worldStem.worldData();
        boolean bl3 = worldData.worldGenOptions().isOldCustomizedWorld();
        boolean bl5 = bl4 = worldData.worldGenSettingsLifecycle() != Lifecycle.stable();
        if (bl2 && (bl3 || bl4)) {
            this.askForBackup(screen, string, bl3, () -> this.doLoadLevel(screen, string, bl, false));
            worldStem.close();
            WorldOpenFlows.safeCloseAccess(levelStorageAccess, string);
            return;
        }
        ((CompletableFuture)((CompletableFuture)((CompletableFuture)this.minecraft.getDownloadedPackSource().loadBundledResourcePack(levelStorageAccess).thenApply(void_ -> true)).exceptionallyComposeAsync(throwable -> {
            LOGGER.warn("Failed to load pack: ", (Throwable)throwable);
            return this.promptBundledPackLoadFailure();
        }, (Executor)this.minecraft)).thenAcceptAsync(boolean_ -> {
            if (boolean_.booleanValue()) {
                this.minecraft.doWorldLoad(string, levelStorageAccess, packRepository, worldStem, false);
            } else {
                worldStem.close();
                WorldOpenFlows.safeCloseAccess(levelStorageAccess, string);
                this.minecraft.getDownloadedPackSource().clearServerPack().thenRunAsync(() -> this.minecraft.setScreen(screen), this.minecraft);
            }
        }, (Executor)this.minecraft)).exceptionally(throwable -> {
            this.minecraft.delayCrash(CrashReport.forThrowable(throwable, "Load world"));
            return null;
        });
    }

    private CompletableFuture<Boolean> promptBundledPackLoadFailure() {
        CompletableFuture<Boolean> completableFuture = new CompletableFuture<Boolean>();
        this.minecraft.setScreen(new ConfirmScreen(completableFuture::complete, Component.translatable("multiplayer.texturePrompt.failure.line1"), Component.translatable("multiplayer.texturePrompt.failure.line2"), CommonComponents.GUI_PROCEED, CommonComponents.GUI_CANCEL));
        return completableFuture;
    }

    private static void safeCloseAccess(LevelStorageSource.LevelStorageAccess levelStorageAccess, String string) {
        try {
            levelStorageAccess.close();
        } catch (IOException iOException) {
            LOGGER.warn("Failed to unlock access to level {}", (Object)string, (Object)iOException);
        }
    }

    private void askForBackup(Screen screen, String string, boolean bl3, Runnable runnable) {
        MutableComponent component2;
        MutableComponent component;
        if (bl3) {
            component = Component.translatable("selectWorld.backupQuestion.customized");
            component2 = Component.translatable("selectWorld.backupWarning.customized");
        } else {
            component = Component.translatable("selectWorld.backupQuestion.experimental");
            component2 = Component.translatable("selectWorld.backupWarning.experimental");
        }
        this.minecraft.setScreen(new BackupConfirmScreen(screen, (bl, bl2) -> {
            if (bl) {
                EditWorldScreen.makeBackupAndShowToast(this.levelSource, string);
            }
            runnable.run();
        }, component, component2, false));
    }

    public static void confirmWorldCreation(Minecraft minecraft, CreateWorldScreen createWorldScreen, Lifecycle lifecycle, Runnable runnable, boolean bl2) {
        BooleanConsumer booleanConsumer = bl -> {
            if (bl) {
                runnable.run();
            } else {
                minecraft.setScreen(createWorldScreen);
            }
        };
        if (bl2 || lifecycle == Lifecycle.stable()) {
            runnable.run();
        } else if (lifecycle == Lifecycle.experimental()) {
            minecraft.setScreen(new ConfirmScreen(booleanConsumer, Component.translatable("selectWorld.warning.experimental.title"), Component.translatable("selectWorld.warning.experimental.question")));
        } else {
            minecraft.setScreen(new ConfirmScreen(booleanConsumer, Component.translatable("selectWorld.warning.deprecated.title"), Component.translatable("selectWorld.warning.deprecated.question")));
        }
    }
}

