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
import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileAttribute;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.OptionalLong;
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
import net.minecraft.client.gui.components.Widget;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.components.toasts.SystemToast;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.screens.ConfirmScreen;
import net.minecraft.client.gui.screens.GenericDirtMessageScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.packs.PackSelectionScreen;
import net.minecraft.client.gui.screens.worldselection.EditGameRulesScreen;
import net.minecraft.client.gui.screens.worldselection.WorldGenSettingsComponent;
import net.minecraft.client.gui.screens.worldselection.WorldPreset;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.commands.Commands;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.RegistryOps;
import net.minecraft.server.WorldStem;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.repository.FolderRepositorySource;
import net.minecraft.server.packs.repository.PackRepository;
import net.minecraft.server.packs.repository.PackSource;
import net.minecraft.server.packs.repository.ServerPacksSource;
import net.minecraft.world.Difficulty;
import net.minecraft.world.level.DataPackConfig;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.LevelSettings;
import net.minecraft.world.level.levelgen.WorldGenSettings;
import net.minecraft.world.level.storage.LevelResource;
import net.minecraft.world.level.storage.LevelStorageSource;
import net.minecraft.world.level.storage.PrimaryLevelData;
import net.minecraft.world.level.storage.WorldData;
import org.apache.commons.lang3.mutable.MutableObject;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

@Environment(value=EnvType.CLIENT)
public class CreateWorldScreen
extends Screen {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final String TEMP_WORLD_PREFIX = "mcworld-";
    private static final Component GAME_MODEL_LABEL = new TranslatableComponent("selectWorld.gameMode");
    private static final Component SEED_LABEL = new TranslatableComponent("selectWorld.enterSeed");
    private static final Component SEED_INFO = new TranslatableComponent("selectWorld.seedInfo");
    private static final Component NAME_LABEL = new TranslatableComponent("selectWorld.enterName");
    private static final Component OUTPUT_DIR_INFO = new TranslatableComponent("selectWorld.resultFolder");
    private static final Component COMMANDS_INFO = new TranslatableComponent("selectWorld.allowCommands.info");
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
    protected DataPackConfig dataPacks;
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

    public static CreateWorldScreen createFresh(@Nullable Screen screen) {
        RegistryAccess.Frozen frozen = RegistryAccess.BUILTIN.get();
        return new CreateWorldScreen(screen, DataPackConfig.DEFAULT, new WorldGenSettingsComponent(frozen, WorldGenSettings.makeDefault(frozen), Optional.of(WorldPreset.NORMAL), OptionalLong.empty()));
    }

    public static CreateWorldScreen createFromExisting(@Nullable Screen screen, WorldStem worldStem, @Nullable Path path) {
        WorldData worldData = worldStem.worldData();
        LevelSettings levelSettings = worldData.getLevelSettings();
        WorldGenSettings worldGenSettings = worldData.worldGenSettings();
        RegistryAccess.Frozen frozen = worldStem.registryAccess();
        DataPackConfig dataPackConfig = levelSettings.getDataPackConfig();
        CreateWorldScreen createWorldScreen = new CreateWorldScreen(screen, dataPackConfig, new WorldGenSettingsComponent(frozen, worldGenSettings, WorldPreset.of(worldGenSettings), OptionalLong.of(worldGenSettings.seed())));
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

    private CreateWorldScreen(@Nullable Screen screen, DataPackConfig dataPackConfig, WorldGenSettingsComponent worldGenSettingsComponent) {
        super(new TranslatableComponent("selectWorld.create"));
        this.lastScreen = screen;
        this.initName = I18n.get("selectWorld.newWorld", new Object[0]);
        this.dataPacks = dataPackConfig;
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
        this.nameEdit = new EditBox(this.font, this.width / 2 - 100, 60, 200, 20, (Component)new TranslatableComponent("selectWorld.enterName")){

            @Override
            protected MutableComponent createNarrationMessage() {
                return CommonComponents.joinForNarration(super.createNarrationMessage(), new TranslatableComponent("selectWorld.resultFolder")).append(" ").append(CreateWorldScreen.this.resultFolder);
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
        this.modeButton = this.addRenderableWidget((GuiEventListener & Widget)CycleButton.builder(SelectedGameMode::getDisplayName).withValues((SelectedGameMode[])new SelectedGameMode[]{SelectedGameMode.SURVIVAL, SelectedGameMode.HARDCORE, SelectedGameMode.CREATIVE}).withInitialValue(this.gameMode).withCustomNarration(cycleButton -> AbstractWidget.wrapDefaultNarrationMessage(cycleButton.getMessage()).append(CommonComponents.NARRATION_SEPARATOR).append(this.gameModeHelp1).append(" ").append(this.gameModeHelp2)).create(i, 100, 150, 20, GAME_MODEL_LABEL, (cycleButton, selectedGameMode) -> this.setGameMode((SelectedGameMode)((Object)selectedGameMode))));
        this.difficultyButton = this.addRenderableWidget((GuiEventListener & Widget)CycleButton.builder(Difficulty::getDisplayName).withValues((Difficulty[])Difficulty.values()).withInitialValue(this.getEffectiveDifficulty()).create(j, 100, 150, 20, new TranslatableComponent("options.difficulty"), (cycleButton, difficulty) -> {
            this.difficulty = difficulty;
        }));
        this.commandsButton = this.addRenderableWidget((GuiEventListener & Widget)CycleButton.onOffBuilder(this.commands && !this.hardCore).withCustomNarration(cycleButton -> CommonComponents.joinForNarration(cycleButton.createDefaultNarrationMessage(), new TranslatableComponent("selectWorld.allowCommands.info"))).create(i, 151, 150, 20, new TranslatableComponent("selectWorld.allowCommands"), (cycleButton, boolean_) -> {
            this.commandsChanged = true;
            this.commands = boolean_;
        }));
        this.dataPacksButton = this.addRenderableWidget(new Button(j, 151, 150, 20, new TranslatableComponent("selectWorld.dataPacks"), button -> this.openDataPackSelectionScreen()));
        this.gameRulesButton = this.addRenderableWidget(new Button(i, 185, 150, 20, new TranslatableComponent("selectWorld.gameRules"), button -> this.minecraft.setScreen(new EditGameRulesScreen(this.gameRules.copy(), optional -> {
            this.minecraft.setScreen(this);
            optional.ifPresent(gameRules -> {
                this.gameRules = gameRules;
            });
        }))));
        this.worldGenSettingsComponent.init(this, this.minecraft, this.font);
        this.moreOptionsButton = this.addRenderableWidget(new Button(j, 185, 150, 20, new TranslatableComponent("selectWorld.moreWorldOptions"), button -> this.toggleWorldGenSettingsVisibility()));
        this.createButton = this.addRenderableWidget(new Button(i, this.height - 28, 150, 20, new TranslatableComponent("selectWorld.create"), button -> this.onCreate()));
        this.createButton.active = !this.initName.isEmpty();
        this.addRenderableWidget(new Button(j, this.height - 28, 150, 20, CommonComponents.GUI_CANCEL, button -> this.popScreen()));
        this.refreshWorldGenSettingsVisibility();
        this.setInitialFocus(this.nameEdit);
        this.setGameMode(this.gameMode);
        this.updateResultFolder();
    }

    private Difficulty getEffectiveDifficulty() {
        return this.gameMode == SelectedGameMode.HARDCORE ? Difficulty.HARD : this.difficulty;
    }

    private void updateGameModeHelp() {
        this.gameModeHelp1 = new TranslatableComponent("selectWorld.gameMode." + this.gameMode.name + ".line1");
        this.gameModeHelp2 = new TranslatableComponent("selectWorld.gameMode." + this.gameMode.name + ".line2");
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

    private void onCreate() {
        this.minecraft.forceSetScreen(new GenericDirtMessageScreen(new TranslatableComponent("createWorld.preparing")));
        if (!this.copyTempDataPackDirToNewWorld()) {
            return;
        }
        this.cleanupTempResources();
        WorldGenSettings worldGenSettings = this.worldGenSettingsComponent.makeSettings(this.hardCore);
        LevelSettings levelSettings = this.createLevelSettings(worldGenSettings.isDebug());
        this.minecraft.createLevel(this.resultFolder, levelSettings, this.worldGenSettingsComponent.registryHolder(), worldGenSettings);
    }

    private LevelSettings createLevelSettings(boolean bl) {
        String string = this.nameEdit.getValue().trim();
        if (bl) {
            GameRules gameRules = new GameRules();
            gameRules.getRule(GameRules.RULE_DAYLIGHT).set(false, null);
            return new LevelSettings(string, GameType.SPECTATOR, false, Difficulty.PEACEFUL, true, gameRules, DataPackConfig.DEFAULT);
        }
        return new LevelSettings(string, this.gameMode.gameType, this.hardCore, this.getEffectiveDifficulty(), this.commands && !this.hardCore, this.gameRules, this.dataPacks);
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
            this.moreOptionsButton.setMessage(new TranslatableComponent("selectWorld.moreWorldOptions"));
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
        this.cleanupTempResources();
    }

    private void cleanupTempResources() {
        if (this.tempDataPackRepository != null) {
            this.tempDataPackRepository.close();
        }
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
            CreateWorldScreen.drawString(poseStack, this.font, new TextComponent("").append(OUTPUT_DIR_INFO).append(" ").append(this.resultFolder), this.width / 2 - 100, 85, -6250336);
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
    protected <T extends GuiEventListener & Widget> T addRenderableWidget(T guiEventListener) {
        return super.addRenderableWidget(guiEventListener);
    }

    @Nullable
    protected Path getTempDataPackDir() {
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
        Pair<File, PackRepository> pair = this.getDataPackSelectionSettings();
        if (pair != null) {
            this.minecraft.setScreen(new PackSelectionScreen(this, pair.getSecond(), this::tryApplyNewDataPacks, pair.getFirst(), new TranslatableComponent("dataPack.title")));
        }
    }

    private void tryApplyNewDataPacks(PackRepository packRepository) {
        ImmutableList<String> list = ImmutableList.copyOf(packRepository.getSelectedIds());
        List list2 = packRepository.getAvailableIds().stream().filter(string -> !list.contains(string)).collect(ImmutableList.toImmutableList());
        DataPackConfig dataPackConfig2 = new DataPackConfig(list, list2);
        if (list.equals(this.dataPacks.getEnabled())) {
            this.dataPacks = dataPackConfig2;
            return;
        }
        this.minecraft.tell(() -> this.minecraft.setScreen(new GenericDirtMessageScreen(new TranslatableComponent("dataPack.validation.working"))));
        ((CompletableFuture)WorldStem.load(new WorldStem.InitConfig(packRepository, Commands.CommandSelection.INTEGRATED, 2, false), () -> dataPackConfig2, (resourceManager, dataPackConfig) -> {
            RegistryAccess registryAccess = this.worldGenSettingsComponent.registryHolder();
            RegistryAccess.Writable writable = RegistryAccess.builtinCopy();
            RegistryOps<JsonElement> dynamicOps = RegistryOps.create(JsonOps.INSTANCE, registryAccess);
            RegistryOps<JsonElement> dynamicOps2 = RegistryOps.createAndLoad(JsonOps.INSTANCE, writable, resourceManager);
            DataResult dataResult = WorldGenSettings.CODEC.encodeStart(dynamicOps, this.worldGenSettingsComponent.makeSettings(this.hardCore)).flatMap(jsonElement -> WorldGenSettings.CODEC.parse(dynamicOps2, jsonElement));
            WorldGenSettings worldGenSettings = (WorldGenSettings)dataResult.getOrThrow(false, Util.prefix("Error parsing worldgen settings after loading data packs: ", LOGGER::error));
            LevelSettings levelSettings = this.createLevelSettings(worldGenSettings.isDebug());
            return Pair.of(new PrimaryLevelData(levelSettings, worldGenSettings, dataResult.lifecycle()), writable.freeze());
        }, Util.backgroundExecutor(), this.minecraft).thenAcceptAsync(worldStem -> {
            this.dataPacks = dataPackConfig2;
            this.worldGenSettingsComponent.updateDataPacks((WorldStem)worldStem);
            worldStem.close();
        }, (Executor)this.minecraft)).handle((void_, throwable) -> {
            if (throwable != null) {
                LOGGER.warn("Failed to validate datapack", (Throwable)throwable);
                this.minecraft.tell(() -> this.minecraft.setScreen(new ConfirmScreen(bl -> {
                    if (bl) {
                        this.openDataPackSelectionScreen();
                    } else {
                        this.dataPacks = DataPackConfig.DEFAULT;
                        this.minecraft.setScreen(this);
                    }
                }, new TranslatableComponent("dataPack.validation.failed"), TextComponent.EMPTY, new TranslatableComponent("dataPack.validation.back"), new TranslatableComponent("dataPack.validation.reset"))));
            } else {
                this.minecraft.tell(() -> this.minecraft.setScreen(this));
            }
            return null;
        });
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
            throw new OperationFailedException(iOException);
        }
    }

    private boolean copyTempDataPackDirToNewWorld() {
        if (this.tempDataPackDir != null) {
            try (LevelStorageSource.LevelStorageAccess levelStorageAccess = this.minecraft.getLevelSource().createAccess(this.resultFolder);
                 Stream<Path> stream = Files.walk(this.tempDataPackDir, new FileVisitOption[0]);){
                Path path3 = levelStorageAccess.getLevelPath(LevelResource.DATAPACK_DIR);
                Files.createDirectories(path3, new FileAttribute[0]);
                stream.filter(path -> !path.equals(this.tempDataPackDir)).forEach(path2 -> CreateWorldScreen.copyBetweenDirs(this.tempDataPackDir, path3, path2));
            } catch (IOException | OperationFailedException exception) {
                LOGGER.warn("Failed to copy datapacks to world {}", (Object)this.resultFolder, (Object)exception);
                SystemToast.onPackCopyFailure(this.minecraft, this.resultFolder);
                this.popScreen();
                return false;
            }
        }
        return true;
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
                        throw new OperationFailedException(iOException);
                    }
                    mutableObject.setValue(path3);
                }
                CreateWorldScreen.copyBetweenDirs(path, path3, path2);
            });
        } catch (IOException | OperationFailedException exception) {
            LOGGER.warn("Failed to copy datapacks from world {}", (Object)path, (Object)exception);
            SystemToast.onPackCopyFailure(minecraft, path.toString());
            return null;
        }
        return (Path)mutableObject.getValue();
    }

    @Nullable
    private Pair<File, PackRepository> getDataPackSelectionSettings() {
        Path path = this.getTempDataPackDir();
        if (path != null) {
            File file = path.toFile();
            if (this.tempDataPackRepository == null) {
                this.tempDataPackRepository = new PackRepository(PackType.SERVER_DATA, new ServerPacksSource(), new FolderRepositorySource(file, PackSource.DEFAULT));
                this.tempDataPackRepository.reload();
            }
            this.tempDataPackRepository.setSelected(this.dataPacks.getEnabled());
            return Pair.of(file, this.tempDataPackRepository);
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
            this.displayName = new TranslatableComponent("selectWorld.gameMode." + string2);
        }

        public Component getDisplayName() {
            return this.displayName;
        }
    }

    @Environment(value=EnvType.CLIENT)
    static class OperationFailedException
    extends RuntimeException {
        public OperationFailedException(Throwable throwable) {
            super(throwable);
        }
    }
}

