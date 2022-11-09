/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.gui.screens.worldselection;

import com.google.common.collect.ImmutableList;
import com.google.gson.JsonElement;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.datafixers.util.Pair;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.Lifecycle;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.FileVisitOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileAttribute;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.stream.Stream;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.FileUtil;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.components.toasts.SystemToast;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.screens.ConfirmScreen;
import net.minecraft.client.gui.screens.GenericDirtMessageScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.packs.PackSelectionScreen;
import net.minecraft.client.gui.screens.worldselection.ConfirmExperimentalFeaturesScreen;
import net.minecraft.client.gui.screens.worldselection.EditGameRulesScreen;
import net.minecraft.client.gui.screens.worldselection.WorldCreationContext;
import net.minecraft.client.gui.screens.worldselection.WorldGenSettingsComponent;
import net.minecraft.client.gui.screens.worldselection.WorldOpenFlows;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.commands.Commands;
import net.minecraft.core.LayeredRegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.RegistryOps;
import net.minecraft.server.RegistryLayer;
import net.minecraft.server.WorldLoader;
import net.minecraft.server.packs.repository.PackRepository;
import net.minecraft.server.packs.repository.ServerPacksSource;
import net.minecraft.world.Difficulty;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.level.DataPackConfig;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.LevelSettings;
import net.minecraft.world.level.WorldDataConfiguration;
import net.minecraft.world.level.levelgen.WorldDimensions;
import net.minecraft.world.level.levelgen.WorldGenSettings;
import net.minecraft.world.level.levelgen.WorldOptions;
import net.minecraft.world.level.levelgen.presets.WorldPresets;
import net.minecraft.world.level.storage.LevelResource;
import net.minecraft.world.level.storage.LevelStorageSource;
import net.minecraft.world.level.storage.PrimaryLevelData;
import org.apache.commons.lang3.mutable.MutableObject;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

@Environment(value=EnvType.CLIENT)
public class CreateWorldScreen
extends Screen {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final String TEMP_WORLD_PREFIX = "mcworld-";
    private static final Component GAME_MODEL_LABEL = Component.translatable("selectWorld.gameMode");
    private static final Component SEED_LABEL = Component.translatable("selectWorld.enterSeed");
    private static final Component SEED_INFO = Component.translatable("selectWorld.seedInfo");
    private static final Component NAME_LABEL = Component.translatable("selectWorld.enterName");
    private static final Component OUTPUT_DIR_INFO = Component.translatable("selectWorld.resultFolder");
    private static final Component COMMANDS_INFO = Component.translatable("selectWorld.allowCommands.info");
    private static final Component PREPARING_WORLD_DATA = Component.translatable("createWorld.preparing");
    @Nullable
    private final Screen lastScreen;
    private EditBox nameEdit;
    String resultFolder;
    private SelectedGameMode gameMode = SelectedGameMode.SURVIVAL;
    @Nullable
    private SelectedGameMode oldGameMode;
    private Difficulty difficulty = Difficulty.NORMAL;
    private boolean commands;
    private boolean commandsChanged;
    public boolean hardCore;
    protected WorldDataConfiguration dataConfiguration;
    @Nullable
    private Path tempDataPackDir;
    @Nullable
    private PackRepository tempDataPackRepository;
    private boolean worldGenSettingsVisible;
    private Button createButton;
    private CycleButton<SelectedGameMode> modeButton;
    private CycleButton<Difficulty> difficultyButton;
    private Button moreOptionsButton;
    private Button gameRulesButton;
    private Button dataPacksButton;
    private CycleButton<Boolean> commandsButton;
    private Component gameModeHelp1;
    private Component gameModeHelp2;
    private String initName;
    private GameRules gameRules = new GameRules();
    public final WorldGenSettingsComponent worldGenSettingsComponent;

    public static void openFresh(Minecraft minecraft, @Nullable Screen screen) {
        CreateWorldScreen.queueLoadScreen(minecraft, PREPARING_WORLD_DATA);
        PackRepository packRepository = new PackRepository(new ServerPacksSource());
        WorldLoader.InitConfig initConfig = CreateWorldScreen.createDefaultLoadConfig(packRepository, WorldDataConfiguration.DEFAULT);
        CompletableFuture<WorldCreationContext> completableFuture = WorldLoader.load(initConfig, dataLoadContext -> new WorldLoader.DataLoadOutput<DataPackReloadCookie>(new DataPackReloadCookie(new WorldGenSettings(WorldOptions.defaultWithRandomSeed(), WorldPresets.createNormalWorldDimensions(dataLoadContext.datapackWorldgen())), dataLoadContext.dataConfiguration()), dataLoadContext.datapackDimensions()), (closeableResourceManager, reloadableServerResources, layeredRegistryAccess, dataPackReloadCookie) -> {
            closeableResourceManager.close();
            return new WorldCreationContext(dataPackReloadCookie.worldGenSettings(), layeredRegistryAccess, reloadableServerResources, dataPackReloadCookie.dataConfiguration());
        }, Util.backgroundExecutor(), minecraft);
        minecraft.managedBlock(completableFuture::isDone);
        minecraft.setScreen(new CreateWorldScreen(screen, WorldDataConfiguration.DEFAULT, new WorldGenSettingsComponent(completableFuture.join(), Optional.of(WorldPresets.NORMAL))));
    }

    public static CreateWorldScreen createFromExisting(@Nullable Screen screen, LevelSettings levelSettings, WorldCreationContext worldCreationContext, @Nullable Path path) {
        CreateWorldScreen createWorldScreen = new CreateWorldScreen(screen, worldCreationContext.dataConfiguration(), new WorldGenSettingsComponent(worldCreationContext, WorldPresets.fromSettings(worldCreationContext.selectedDimensions().dimensions()), worldCreationContext.options().seed()));
        createWorldScreen.initName = levelSettings.levelName();
        createWorldScreen.commands = levelSettings.allowCommands();
        createWorldScreen.commandsChanged = true;
        createWorldScreen.difficulty = levelSettings.difficulty();
        createWorldScreen.gameRules.assignFrom(levelSettings.gameRules(), null);
        if (levelSettings.hardcore()) {
            createWorldScreen.gameMode = SelectedGameMode.HARDCORE;
        } else if (levelSettings.gameType().isSurvival()) {
            createWorldScreen.gameMode = SelectedGameMode.SURVIVAL;
        } else if (levelSettings.gameType().isCreative()) {
            createWorldScreen.gameMode = SelectedGameMode.CREATIVE;
        }
        createWorldScreen.tempDataPackDir = path;
        return createWorldScreen;
    }

    private CreateWorldScreen(@Nullable Screen screen, WorldDataConfiguration worldDataConfiguration, WorldGenSettingsComponent worldGenSettingsComponent) {
        super(Component.translatable("selectWorld.create"));
        this.lastScreen = screen;
        this.initName = I18n.get("selectWorld.newWorld", new Object[0]);
        this.dataConfiguration = worldDataConfiguration;
        this.worldGenSettingsComponent = worldGenSettingsComponent;
    }

    @Override
    public void tick() {
        this.nameEdit.tick();
        this.worldGenSettingsComponent.tick();
    }

    @Override
    protected void init() {
        this.minecraft.keyboardHandler.setSendRepeatsToGui(true);
        this.nameEdit = new EditBox(this.font, this.width / 2 - 100, 60, 200, 20, (Component)Component.translatable("selectWorld.enterName")){

            @Override
            protected MutableComponent createNarrationMessage() {
                return CommonComponents.joinForNarration(super.createNarrationMessage(), Component.translatable("selectWorld.resultFolder")).append(" ").append(CreateWorldScreen.this.resultFolder);
            }
        };
        this.nameEdit.setValue(this.initName);
        this.nameEdit.setResponder(string -> {
            this.initName = string;
            this.createButton.active = !this.nameEdit.getValue().isEmpty();
            this.updateResultFolder();
        });
        this.addWidget(this.nameEdit);
        int i = this.width / 2 - 155;
        int j = this.width / 2 + 5;
        this.modeButton = this.addRenderableWidget((GuiEventListener & Renderable)CycleButton.builder(SelectedGameMode::getDisplayName).withValues((SelectedGameMode[])new SelectedGameMode[]{SelectedGameMode.SURVIVAL, SelectedGameMode.HARDCORE, SelectedGameMode.CREATIVE}).withInitialValue(this.gameMode).withCustomNarration(cycleButton -> AbstractWidget.wrapDefaultNarrationMessage(cycleButton.getMessage()).append(CommonComponents.NARRATION_SEPARATOR).append(this.gameModeHelp1).append(" ").append(this.gameModeHelp2)).create(i, 100, 150, 20, GAME_MODEL_LABEL, (cycleButton, selectedGameMode) -> this.setGameMode((SelectedGameMode)((Object)selectedGameMode))));
        this.difficultyButton = this.addRenderableWidget((GuiEventListener & Renderable)CycleButton.builder(Difficulty::getDisplayName).withValues((Difficulty[])Difficulty.values()).withInitialValue(this.getEffectiveDifficulty()).create(j, 100, 150, 20, Component.translatable("options.difficulty"), (cycleButton, difficulty) -> {
            this.difficulty = difficulty;
        }));
        this.commandsButton = this.addRenderableWidget((GuiEventListener & Renderable)CycleButton.onOffBuilder(this.commands && !this.hardCore).withCustomNarration(cycleButton -> CommonComponents.joinForNarration(cycleButton.createDefaultNarrationMessage(), Component.translatable("selectWorld.allowCommands.info"))).create(i, 151, 150, 20, Component.translatable("selectWorld.allowCommands"), (cycleButton, boolean_) -> {
            this.commandsChanged = true;
            this.commands = boolean_;
        }));
        this.dataPacksButton = this.addRenderableWidget(Button.builder(Component.translatable("selectWorld.dataPacks"), button -> this.openDataPackSelectionScreen()).bounds(j, 151, 150, 20).build());
        this.gameRulesButton = this.addRenderableWidget(Button.builder(Component.translatable("selectWorld.gameRules"), button -> this.minecraft.setScreen(new EditGameRulesScreen(this.gameRules.copy(), optional -> {
            this.minecraft.setScreen(this);
            optional.ifPresent(gameRules -> {
                this.gameRules = gameRules;
            });
        }))).bounds(i, 185, 150, 20).build());
        this.worldGenSettingsComponent.init(this, this.minecraft, this.font);
        this.moreOptionsButton = this.addRenderableWidget(Button.builder(Component.translatable("selectWorld.moreWorldOptions"), button -> this.toggleWorldGenSettingsVisibility()).bounds(j, 185, 150, 20).build());
        this.createButton = this.addRenderableWidget(Button.builder(Component.translatable("selectWorld.create"), button -> this.onCreate()).bounds(i, this.height - 28, 150, 20).build());
        this.createButton.active = !this.initName.isEmpty();
        this.addRenderableWidget(Button.builder(CommonComponents.GUI_CANCEL, button -> this.popScreen()).bounds(j, this.height - 28, 150, 20).build());
        this.refreshWorldGenSettingsVisibility();
        this.setInitialFocus(this.nameEdit);
        this.setGameMode(this.gameMode);
        this.updateResultFolder();
    }

    private Difficulty getEffectiveDifficulty() {
        return this.gameMode == SelectedGameMode.HARDCORE ? Difficulty.HARD : this.difficulty;
    }

    private void updateGameModeHelp() {
        this.gameModeHelp1 = Component.translatable("selectWorld.gameMode." + this.gameMode.name + ".line1");
        this.gameModeHelp2 = Component.translatable("selectWorld.gameMode." + this.gameMode.name + ".line2");
    }

    private void updateResultFolder() {
        this.resultFolder = this.nameEdit.getValue().trim();
        if (this.resultFolder.isEmpty()) {
            this.resultFolder = "World";
        }
        try {
            this.resultFolder = FileUtil.findAvailableName(this.minecraft.getLevelSource().getBaseDir(), this.resultFolder, "");
        } catch (Exception exception) {
            this.resultFolder = "World";
            try {
                this.resultFolder = FileUtil.findAvailableName(this.minecraft.getLevelSource().getBaseDir(), this.resultFolder, "");
            } catch (Exception exception2) {
                throw new RuntimeException("Could not create save folder", exception2);
            }
        }
    }

    @Override
    public void removed() {
        this.minecraft.keyboardHandler.setSendRepeatsToGui(false);
    }

    private static void queueLoadScreen(Minecraft minecraft, Component component) {
        minecraft.forceSetScreen(new GenericDirtMessageScreen(component));
    }

    private void onCreate() {
        WorldCreationContext worldCreationContext = this.worldGenSettingsComponent.settings();
        WorldDimensions.Complete complete = worldCreationContext.selectedDimensions().bake(worldCreationContext.datapackDimensions());
        LayeredRegistryAccess<RegistryLayer> layeredRegistryAccess = worldCreationContext.worldgenRegistries().replaceFrom(RegistryLayer.DIMENSIONS, complete.dimensionsRegistryAccess());
        Lifecycle lifecycle = FeatureFlags.isExperimental(worldCreationContext.dataConfiguration().enabledFeatures()) ? Lifecycle.experimental() : Lifecycle.stable();
        Lifecycle lifecycle2 = layeredRegistryAccess.compositeAccess().allRegistriesLifecycle();
        Lifecycle lifecycle3 = lifecycle2.add(lifecycle);
        WorldOpenFlows.confirmWorldCreation(this.minecraft, this, lifecycle3, () -> this.createNewWorld(complete.specialWorldProperty(), layeredRegistryAccess, lifecycle3));
    }

    private void createNewWorld(PrimaryLevelData.SpecialWorldProperty specialWorldProperty, LayeredRegistryAccess<RegistryLayer> layeredRegistryAccess, Lifecycle lifecycle) {
        CreateWorldScreen.queueLoadScreen(this.minecraft, PREPARING_WORLD_DATA);
        Optional<LevelStorageSource.LevelStorageAccess> optional = this.createNewWorldDirectory();
        if (optional.isEmpty()) {
            return;
        }
        this.removeTempDataPackDir();
        boolean bl = specialWorldProperty == PrimaryLevelData.SpecialWorldProperty.DEBUG;
        WorldCreationContext worldCreationContext = this.worldGenSettingsComponent.settings();
        WorldOptions worldOptions = this.worldGenSettingsComponent.createFinalOptions(bl, this.hardCore);
        LevelSettings levelSettings = this.createLevelSettings(bl);
        PrimaryLevelData worldData = new PrimaryLevelData(levelSettings, worldOptions, specialWorldProperty, lifecycle);
        this.minecraft.createWorldOpenFlows().createLevelFromExistingSettings(optional.get(), worldCreationContext.dataPackResources(), layeredRegistryAccess, worldData);
    }

    private LevelSettings createLevelSettings(boolean bl) {
        String string = this.nameEdit.getValue().trim();
        if (bl) {
            GameRules gameRules = new GameRules();
            gameRules.getRule(GameRules.RULE_DAYLIGHT).set(false, null);
            return new LevelSettings(string, GameType.SPECTATOR, false, Difficulty.PEACEFUL, true, gameRules, WorldDataConfiguration.DEFAULT);
        }
        return new LevelSettings(string, this.gameMode.gameType, this.hardCore, this.getEffectiveDifficulty(), this.commands && !this.hardCore, this.gameRules, this.dataConfiguration);
    }

    private void toggleWorldGenSettingsVisibility() {
        this.setWorldGenSettingsVisible(!this.worldGenSettingsVisible);
    }

    private void setGameMode(SelectedGameMode selectedGameMode) {
        if (!this.commandsChanged) {
            this.commands = selectedGameMode == SelectedGameMode.CREATIVE;
            this.commandsButton.setValue(this.commands);
        }
        if (selectedGameMode == SelectedGameMode.HARDCORE) {
            this.hardCore = true;
            this.commandsButton.active = false;
            this.commandsButton.setValue(false);
            this.worldGenSettingsComponent.switchToHardcore();
            this.difficultyButton.setValue(Difficulty.HARD);
            this.difficultyButton.active = false;
        } else {
            this.hardCore = false;
            this.commandsButton.active = true;
            this.commandsButton.setValue(this.commands);
            this.worldGenSettingsComponent.switchOutOfHardcode();
            this.difficultyButton.setValue(this.difficulty);
            this.difficultyButton.active = true;
        }
        this.gameMode = selectedGameMode;
        this.updateGameModeHelp();
    }

    public void refreshWorldGenSettingsVisibility() {
        this.setWorldGenSettingsVisible(this.worldGenSettingsVisible);
    }

    private void setWorldGenSettingsVisible(boolean bl) {
        this.worldGenSettingsVisible = bl;
        this.modeButton.visible = !bl;
        boolean bl2 = this.difficultyButton.visible = !bl;
        if (this.worldGenSettingsComponent.isDebug()) {
            this.dataPacksButton.visible = false;
            this.modeButton.active = false;
            if (this.oldGameMode == null) {
                this.oldGameMode = this.gameMode;
            }
            this.setGameMode(SelectedGameMode.DEBUG);
            this.commandsButton.visible = false;
        } else {
            this.modeButton.active = true;
            if (this.oldGameMode != null) {
                this.setGameMode(this.oldGameMode);
            }
            this.commandsButton.visible = !bl;
            this.dataPacksButton.visible = !bl;
        }
        this.worldGenSettingsComponent.setVisibility(bl);
        this.nameEdit.setVisible(!bl);
        if (bl) {
            this.moreOptionsButton.setMessage(CommonComponents.GUI_DONE);
        } else {
            this.moreOptionsButton.setMessage(Component.translatable("selectWorld.moreWorldOptions"));
        }
        this.gameRulesButton.visible = !bl;
    }

    @Override
    public boolean keyPressed(int i, int j, int k) {
        if (super.keyPressed(i, j, k)) {
            return true;
        }
        if (i == 257 || i == 335) {
            this.onCreate();
            return true;
        }
        return false;
    }

    @Override
    public void onClose() {
        if (this.worldGenSettingsVisible) {
            this.setWorldGenSettingsVisible(false);
        } else {
            this.popScreen();
        }
    }

    public void popScreen() {
        this.minecraft.setScreen(this.lastScreen);
        this.removeTempDataPackDir();
    }

    @Override
    public void render(PoseStack poseStack, int i, int j, float f) {
        this.renderBackground(poseStack);
        CreateWorldScreen.drawCenteredString(poseStack, this.font, this.title, this.width / 2, 20, -1);
        if (this.worldGenSettingsVisible) {
            CreateWorldScreen.drawString(poseStack, this.font, SEED_LABEL, this.width / 2 - 100, 47, -6250336);
            CreateWorldScreen.drawString(poseStack, this.font, SEED_INFO, this.width / 2 - 100, 85, -6250336);
            this.worldGenSettingsComponent.render(poseStack, i, j, f);
        } else {
            CreateWorldScreen.drawString(poseStack, this.font, NAME_LABEL, this.width / 2 - 100, 47, -6250336);
            CreateWorldScreen.drawString(poseStack, this.font, Component.empty().append(OUTPUT_DIR_INFO).append(" ").append(this.resultFolder), this.width / 2 - 100, 85, -6250336);
            this.nameEdit.render(poseStack, i, j, f);
            CreateWorldScreen.drawString(poseStack, this.font, this.gameModeHelp1, this.width / 2 - 150, 122, -6250336);
            CreateWorldScreen.drawString(poseStack, this.font, this.gameModeHelp2, this.width / 2 - 150, 134, -6250336);
            if (this.commandsButton.visible) {
                CreateWorldScreen.drawString(poseStack, this.font, COMMANDS_INFO, this.width / 2 - 150, 172, -6250336);
            }
        }
        super.render(poseStack, i, j, f);
    }

    @Override
    protected <T extends GuiEventListener & NarratableEntry> T addWidget(T guiEventListener) {
        return super.addWidget(guiEventListener);
    }

    @Override
    protected <T extends GuiEventListener & Renderable> T addRenderableWidget(T guiEventListener) {
        return super.addRenderableWidget(guiEventListener);
    }

    @Nullable
    private Path getTempDataPackDir() {
        if (this.tempDataPackDir == null) {
            try {
                this.tempDataPackDir = Files.createTempDirectory(TEMP_WORLD_PREFIX, new FileAttribute[0]);
            } catch (IOException iOException) {
                LOGGER.warn("Failed to create temporary dir", iOException);
                SystemToast.onPackCopyFailure(this.minecraft, this.resultFolder);
                this.popScreen();
            }
        }
        return this.tempDataPackDir;
    }

    private void openDataPackSelectionScreen() {
        Pair<Path, PackRepository> pair = this.getDataPackSelectionSettings();
        if (pair != null) {
            this.minecraft.setScreen(new PackSelectionScreen(this, pair.getSecond(), this::tryApplyNewDataPacks, pair.getFirst(), Component.translatable("dataPack.title")));
        }
    }

    private void tryApplyNewDataPacks(PackRepository packRepository) {
        ImmutableList<String> list = ImmutableList.copyOf(packRepository.getSelectedIds());
        List list2 = packRepository.getAvailableIds().stream().filter(string -> !list.contains(string)).collect(ImmutableList.toImmutableList());
        WorldDataConfiguration worldDataConfiguration = new WorldDataConfiguration(new DataPackConfig(list, list2), this.dataConfiguration.enabledFeatures());
        if (list.equals(this.dataConfiguration.dataPacks().getEnabled())) {
            this.dataConfiguration = worldDataConfiguration;
            return;
        }
        FeatureFlagSet featureFlagSet = packRepository.getRequestedFeatureFlags();
        if (FeatureFlags.isExperimental(featureFlagSet)) {
            this.minecraft.tell(() -> this.minecraft.setScreen(new ConfirmExperimentalFeaturesScreen(packRepository.getSelectedPacks(), bl -> {
                if (bl) {
                    this.applyNewPackConfig(packRepository, worldDataConfiguration);
                } else {
                    this.openDataPackSelectionScreen();
                }
            })));
        } else {
            this.applyNewPackConfig(packRepository, worldDataConfiguration);
        }
    }

    private void applyNewPackConfig(PackRepository packRepository, WorldDataConfiguration worldDataConfiguration) {
        this.minecraft.tell(() -> this.minecraft.setScreen(new GenericDirtMessageScreen(Component.translatable("dataPack.validation.working"))));
        WorldLoader.InitConfig initConfig = CreateWorldScreen.createDefaultLoadConfig(packRepository, worldDataConfiguration);
        ((CompletableFuture)WorldLoader.load(initConfig, dataLoadContext -> {
            if (dataLoadContext.datapackWorldgen().registryOrThrow(Registries.WORLD_PRESET).size() == 0) {
                throw new IllegalStateException("Needs at least one world preset to continue");
            }
            if (dataLoadContext.datapackWorldgen().registryOrThrow(Registries.BIOME).size() == 0) {
                throw new IllegalStateException("Needs at least one biome continue");
            }
            WorldCreationContext worldCreationContext = this.worldGenSettingsComponent.settings();
            RegistryOps<JsonElement> dynamicOps = RegistryOps.create(JsonOps.INSTANCE, worldCreationContext.worldgenLoadContext());
            DataResult<JsonElement> dataResult = WorldGenSettings.encode(dynamicOps, worldCreationContext.options(), worldCreationContext.selectedDimensions()).setLifecycle(Lifecycle.stable());
            RegistryOps<JsonElement> dynamicOps2 = RegistryOps.create(JsonOps.INSTANCE, dataLoadContext.datapackWorldgen());
            WorldGenSettings worldGenSettings = (WorldGenSettings)dataResult.flatMap(jsonElement -> WorldGenSettings.CODEC.parse(dynamicOps2, jsonElement)).getOrThrow(false, Util.prefix("Error parsing worldgen settings after loading data packs: ", LOGGER::error));
            return new WorldLoader.DataLoadOutput<DataPackReloadCookie>(new DataPackReloadCookie(worldGenSettings, dataLoadContext.dataConfiguration()), dataLoadContext.datapackDimensions());
        }, (closeableResourceManager, reloadableServerResources, layeredRegistryAccess, dataPackReloadCookie) -> {
            closeableResourceManager.close();
            return new WorldCreationContext(dataPackReloadCookie.worldGenSettings(), layeredRegistryAccess, reloadableServerResources, dataPackReloadCookie.dataConfiguration());
        }, Util.backgroundExecutor(), this.minecraft).thenAcceptAsync(worldCreationContext -> {
            this.dataConfiguration = worldCreationContext.dataConfiguration();
            this.worldGenSettingsComponent.updateSettings((WorldCreationContext)worldCreationContext);
            this.rebuildWidgets();
        }, (Executor)this.minecraft)).handle((void_, throwable) -> {
            if (throwable != null) {
                LOGGER.warn("Failed to validate datapack", (Throwable)throwable);
                this.minecraft.tell(() -> this.minecraft.setScreen(new ConfirmScreen(bl -> {
                    if (bl) {
                        this.openDataPackSelectionScreen();
                    } else {
                        this.dataConfiguration = WorldDataConfiguration.DEFAULT;
                        this.minecraft.setScreen(this);
                    }
                }, Component.translatable("dataPack.validation.failed"), CommonComponents.EMPTY, Component.translatable("dataPack.validation.back"), Component.translatable("dataPack.validation.reset"))));
            } else {
                this.minecraft.tell(() -> this.minecraft.setScreen(this));
            }
            return null;
        });
    }

    private static WorldLoader.InitConfig createDefaultLoadConfig(PackRepository packRepository, WorldDataConfiguration worldDataConfiguration) {
        WorldLoader.PackConfig packConfig = new WorldLoader.PackConfig(packRepository, worldDataConfiguration, false, true);
        return new WorldLoader.InitConfig(packConfig, Commands.CommandSelection.INTEGRATED, 2);
    }

    private void removeTempDataPackDir() {
        if (this.tempDataPackDir != null) {
            try (Stream<Path> stream = Files.walk(this.tempDataPackDir, new FileVisitOption[0]);){
                stream.sorted(Comparator.reverseOrder()).forEach(path -> {
                    try {
                        Files.delete(path);
                    } catch (IOException iOException) {
                        LOGGER.warn("Failed to remove temporary file {}", path, (Object)iOException);
                    }
                });
            } catch (IOException iOException) {
                LOGGER.warn("Failed to list temporary dir {}", (Object)this.tempDataPackDir);
            }
            this.tempDataPackDir = null;
        }
    }

    private static void copyBetweenDirs(Path path, Path path2, Path path3) {
        try {
            Util.copyBetweenDirs(path, path2, path3);
        } catch (IOException iOException) {
            LOGGER.warn("Failed to copy datapack file from {} to {}", (Object)path3, (Object)path2);
            throw new UncheckedIOException(iOException);
        }
    }

    private Optional<LevelStorageSource.LevelStorageAccess> createNewWorldDirectory() {
        Optional<LevelStorageSource.LevelStorageAccess> optional;
        block12: {
            LevelStorageSource.LevelStorageAccess levelStorageAccess;
            block11: {
                levelStorageAccess = this.minecraft.getLevelSource().createAccess(this.resultFolder);
                if (this.tempDataPackDir != null) break block11;
                return Optional.of(levelStorageAccess);
            }
            Stream<Path> stream = Files.walk(this.tempDataPackDir, new FileVisitOption[0]);
            try {
                Path path3 = levelStorageAccess.getLevelPath(LevelResource.DATAPACK_DIR);
                FileUtil.createDirectoriesSafe(path3);
                stream.filter(path -> !path.equals(this.tempDataPackDir)).forEach(path2 -> CreateWorldScreen.copyBetweenDirs(this.tempDataPackDir, path3, path2));
                optional = Optional.of(levelStorageAccess);
                if (stream == null) break block12;
            } catch (Throwable throwable) {
                try {
                    try {
                        if (stream != null) {
                            try {
                                stream.close();
                            } catch (Throwable throwable2) {
                                throwable.addSuppressed(throwable2);
                            }
                        }
                        throw throwable;
                    } catch (IOException | UncheckedIOException exception) {
                        LOGGER.warn("Failed to copy datapacks to world {}", (Object)this.resultFolder, (Object)exception);
                        levelStorageAccess.close();
                    }
                } catch (IOException | UncheckedIOException exception2) {
                    LOGGER.warn("Failed to create access for {}", (Object)this.resultFolder, (Object)exception2);
                }
            }
            stream.close();
        }
        return optional;
        SystemToast.onPackCopyFailure(this.minecraft, this.resultFolder);
        this.popScreen();
        return Optional.empty();
    }

    @Nullable
    public static Path createTempDataPackDirFromExistingWorld(Path path, Minecraft minecraft) {
        MutableObject mutableObject = new MutableObject();
        try (Stream<Path> stream = Files.walk(path, new FileVisitOption[0]);){
            stream.filter(path2 -> !path2.equals(path)).forEach(path2 -> {
                Path path3 = (Path)mutableObject.getValue();
                if (path3 == null) {
                    try {
                        path3 = Files.createTempDirectory(TEMP_WORLD_PREFIX, new FileAttribute[0]);
                    } catch (IOException iOException) {
                        LOGGER.warn("Failed to create temporary dir");
                        throw new UncheckedIOException(iOException);
                    }
                    mutableObject.setValue(path3);
                }
                CreateWorldScreen.copyBetweenDirs(path, path3, path2);
            });
        } catch (IOException | UncheckedIOException exception) {
            LOGGER.warn("Failed to copy datapacks from world {}", (Object)path, (Object)exception);
            SystemToast.onPackCopyFailure(minecraft, path.toString());
            return null;
        }
        return (Path)mutableObject.getValue();
    }

    @Nullable
    private Pair<Path, PackRepository> getDataPackSelectionSettings() {
        Path path = this.getTempDataPackDir();
        if (path != null) {
            if (this.tempDataPackRepository == null) {
                this.tempDataPackRepository = ServerPacksSource.createPackRepository(path);
                this.tempDataPackRepository.reload();
            }
            this.tempDataPackRepository.setSelected(this.dataConfiguration.dataPacks().getEnabled());
            return Pair.of(path, this.tempDataPackRepository);
        }
        return null;
    }

    @Environment(value=EnvType.CLIENT)
    static enum SelectedGameMode {
        SURVIVAL("survival", GameType.SURVIVAL),
        HARDCORE("hardcore", GameType.SURVIVAL),
        CREATIVE("creative", GameType.CREATIVE),
        DEBUG("spectator", GameType.SPECTATOR);

        final String name;
        final GameType gameType;
        private final Component displayName;

        private SelectedGameMode(String string2, GameType gameType) {
            this.name = string2;
            this.gameType = gameType;
            this.displayName = Component.translatable("selectWorld.gameMode." + string2);
        }

        public Component getDisplayName() {
            return this.displayName;
        }
    }

    @Environment(value=EnvType.CLIENT)
    record DataPackReloadCookie(WorldGenSettings worldGenSettings, WorldDataConfiguration dataConfiguration) {
    }
}

