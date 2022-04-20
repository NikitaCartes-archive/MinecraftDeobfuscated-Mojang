package net.minecraft.client.gui.screens.worldselection;

import com.google.common.collect.ImmutableList;
import com.google.gson.JsonElement;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.datafixers.util.Pair;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.Lifecycle;
import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.OptionalLong;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;
import javax.annotation.Nullable;
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
import net.minecraft.client.resources.language.I18n;
import net.minecraft.commands.Commands;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.RegistryOps;
import net.minecraft.server.WorldLoader;
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
import net.minecraft.world.level.levelgen.presets.WorldPresets;
import net.minecraft.world.level.storage.LevelResource;
import net.minecraft.world.level.storage.LevelStorageSource;
import net.minecraft.world.level.storage.PrimaryLevelData;
import net.minecraft.world.level.storage.WorldData;
import org.apache.commons.lang3.mutable.MutableObject;
import org.slf4j.Logger;

@Environment(EnvType.CLIENT)
public class CreateWorldScreen extends Screen {
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
	private CreateWorldScreen.SelectedGameMode gameMode = CreateWorldScreen.SelectedGameMode.SURVIVAL;
	@Nullable
	private CreateWorldScreen.SelectedGameMode oldGameMode;
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
	private CycleButton<CreateWorldScreen.SelectedGameMode> modeButton;
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
		queueLoadScreen(minecraft, PREPARING_WORLD_DATA);
		PackRepository packRepository = new PackRepository(PackType.SERVER_DATA, new ServerPacksSource());
		WorldLoader.InitConfig initConfig = createDefaultLoadConfig(packRepository, DataPackConfig.DEFAULT);
		CompletableFuture<WorldCreationContext> completableFuture = WorldLoader.load(initConfig, (resourceManager, dataPackConfig) -> {
			RegistryAccess.Frozen frozen = (RegistryAccess.Frozen)RegistryAccess.BUILTIN.get();
			WorldGenSettings worldGenSettings = WorldPresets.createNormalWorldFromPreset(frozen);
			return Pair.of(worldGenSettings, frozen);
		}, (closeableResourceManager, reloadableServerResources, frozen, worldGenSettings) -> {
			closeableResourceManager.close();
			return new WorldCreationContext(worldGenSettings, Lifecycle.stable(), frozen, reloadableServerResources);
		}, Util.backgroundExecutor(), minecraft);
		minecraft.managedBlock(completableFuture::isDone);
		minecraft.setScreen(
			new CreateWorldScreen(
				screen,
				DataPackConfig.DEFAULT,
				new WorldGenSettingsComponent((WorldCreationContext)completableFuture.join(), Optional.of(WorldPresets.NORMAL), OptionalLong.empty())
			)
		);
	}

	public static CreateWorldScreen createFromExisting(@Nullable Screen screen, WorldStem worldStem, @Nullable Path path) {
		WorldData worldData = worldStem.worldData();
		LevelSettings levelSettings = worldData.getLevelSettings();
		WorldGenSettings worldGenSettings = worldData.worldGenSettings();
		RegistryAccess.Frozen frozen = worldStem.registryAccess();
		WorldCreationContext worldCreationContext = new WorldCreationContext(
			worldGenSettings, worldData.worldGenSettingsLifecycle(), frozen, worldStem.dataPackResources()
		);
		DataPackConfig dataPackConfig = levelSettings.getDataPackConfig();
		CreateWorldScreen createWorldScreen = new CreateWorldScreen(
			screen,
			dataPackConfig,
			new WorldGenSettingsComponent(worldCreationContext, WorldPresets.fromSettings(worldGenSettings), OptionalLong.of(worldGenSettings.seed()))
		);
		createWorldScreen.initName = levelSettings.levelName();
		createWorldScreen.commands = levelSettings.allowCommands();
		createWorldScreen.commandsChanged = true;
		createWorldScreen.difficulty = levelSettings.difficulty();
		createWorldScreen.gameRules.assignFrom(levelSettings.gameRules(), null);
		if (levelSettings.hardcore()) {
			createWorldScreen.gameMode = CreateWorldScreen.SelectedGameMode.HARDCORE;
		} else if (levelSettings.gameType().isSurvival()) {
			createWorldScreen.gameMode = CreateWorldScreen.SelectedGameMode.SURVIVAL;
		} else if (levelSettings.gameType().isCreative()) {
			createWorldScreen.gameMode = CreateWorldScreen.SelectedGameMode.CREATIVE;
		}

		createWorldScreen.tempDataPackDir = path;
		return createWorldScreen;
	}

	private CreateWorldScreen(@Nullable Screen screen, DataPackConfig dataPackConfig, WorldGenSettingsComponent worldGenSettingsComponent) {
		super(Component.translatable("selectWorld.create"));
		this.lastScreen = screen;
		this.initName = I18n.get("selectWorld.newWorld");
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
		this.nameEdit = new EditBox(this.font, this.width / 2 - 100, 60, 200, 20, Component.translatable("selectWorld.enterName")) {
			@Override
			protected MutableComponent createNarrationMessage() {
				return CommonComponents.joinForNarration(super.createNarrationMessage(), Component.translatable("selectWorld.resultFolder"))
					.append(" ")
					.append(CreateWorldScreen.this.resultFolder);
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
		this.modeButton = this.addRenderableWidget(
			CycleButton.<CreateWorldScreen.SelectedGameMode>builder(CreateWorldScreen.SelectedGameMode::getDisplayName)
				.withValues(CreateWorldScreen.SelectedGameMode.SURVIVAL, CreateWorldScreen.SelectedGameMode.HARDCORE, CreateWorldScreen.SelectedGameMode.CREATIVE)
				.withInitialValue(this.gameMode)
				.withCustomNarration(
					cycleButton -> AbstractWidget.wrapDefaultNarrationMessage(cycleButton.getMessage())
							.append(CommonComponents.NARRATION_SEPARATOR)
							.append(this.gameModeHelp1)
							.append(" ")
							.append(this.gameModeHelp2)
				)
				.create(i, 100, 150, 20, GAME_MODEL_LABEL, (cycleButton, selectedGameMode) -> this.setGameMode(selectedGameMode))
		);
		this.difficultyButton = this.addRenderableWidget(
			CycleButton.<Difficulty>builder(Difficulty::getDisplayName)
				.withValues(Difficulty.values())
				.withInitialValue(this.getEffectiveDifficulty())
				.create(j, 100, 150, 20, Component.translatable("options.difficulty"), (cycleButton, difficulty) -> this.difficulty = difficulty)
		);
		this.commandsButton = this.addRenderableWidget(
			CycleButton.onOffBuilder(this.commands && !this.hardCore)
				.withCustomNarration(
					cycleButton -> CommonComponents.joinForNarration(cycleButton.createDefaultNarrationMessage(), Component.translatable("selectWorld.allowCommands.info"))
				)
				.create(i, 151, 150, 20, Component.translatable("selectWorld.allowCommands"), (cycleButton, boolean_) -> {
					this.commandsChanged = true;
					this.commands = boolean_;
				})
		);
		this.dataPacksButton = this.addRenderableWidget(
			new Button(j, 151, 150, 20, Component.translatable("selectWorld.dataPacks"), button -> this.openDataPackSelectionScreen())
		);
		this.gameRulesButton = this.addRenderableWidget(
			new Button(
				i,
				185,
				150,
				20,
				Component.translatable("selectWorld.gameRules"),
				button -> this.minecraft.setScreen(new EditGameRulesScreen(this.gameRules.copy(), optional -> {
						this.minecraft.setScreen(this);
						optional.ifPresent(gameRules -> this.gameRules = gameRules);
					}))
			)
		);
		this.worldGenSettingsComponent.init(this, this.minecraft, this.font);
		this.moreOptionsButton = this.addRenderableWidget(
			new Button(j, 185, 150, 20, Component.translatable("selectWorld.moreWorldOptions"), button -> this.toggleWorldGenSettingsVisibility())
		);
		this.createButton = this.addRenderableWidget(
			new Button(i, this.height - 28, 150, 20, Component.translatable("selectWorld.create"), button -> this.onCreate())
		);
		this.createButton.active = !this.initName.isEmpty();
		this.addRenderableWidget(new Button(j, this.height - 28, 150, 20, CommonComponents.GUI_CANCEL, button -> this.popScreen()));
		this.refreshWorldGenSettingsVisibility();
		this.setInitialFocus(this.nameEdit);
		this.setGameMode(this.gameMode);
		this.updateResultFolder();
	}

	private Difficulty getEffectiveDifficulty() {
		return this.gameMode == CreateWorldScreen.SelectedGameMode.HARDCORE ? Difficulty.HARD : this.difficulty;
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
		} catch (Exception var4) {
			this.resultFolder = "World";

			try {
				this.resultFolder = FileUtil.findAvailableName(this.minecraft.getLevelSource().getBaseDir(), this.resultFolder, "");
			} catch (Exception var3) {
				throw new RuntimeException("Could not create save folder", var3);
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
		WorldOpenFlows.confirmWorldCreation(this.minecraft, this, this.worldGenSettingsComponent.settings().worldSettingsStability(), this::createNewWorld);
	}

	private void createNewWorld() {
		queueLoadScreen(this.minecraft, PREPARING_WORLD_DATA);
		Optional<LevelStorageSource.LevelStorageAccess> optional = this.createNewWorldDirectory();
		if (!optional.isEmpty()) {
			this.removeTempDataPackDir();
			WorldCreationContext worldCreationContext = this.worldGenSettingsComponent.createFinalSettings(this.hardCore);
			LevelSettings levelSettings = this.createLevelSettings(worldCreationContext.worldGenSettings().isDebug());
			WorldData worldData = new PrimaryLevelData(levelSettings, worldCreationContext.worldGenSettings(), worldCreationContext.worldSettingsStability());
			this.minecraft
				.createWorldOpenFlows()
				.createLevelFromExistingSettings(
					(LevelStorageSource.LevelStorageAccess)optional.get(), worldCreationContext.dataPackResources(), worldCreationContext.registryAccess(), worldData
				);
		}
	}

	private LevelSettings createLevelSettings(boolean bl) {
		String string = this.nameEdit.getValue().trim();
		if (bl) {
			GameRules gameRules = new GameRules();
			gameRules.getRule(GameRules.RULE_DAYLIGHT).set(false, null);
			return new LevelSettings(string, GameType.SPECTATOR, false, Difficulty.PEACEFUL, true, gameRules, DataPackConfig.DEFAULT);
		} else {
			return new LevelSettings(
				string, this.gameMode.gameType, this.hardCore, this.getEffectiveDifficulty(), this.commands && !this.hardCore, this.gameRules, this.dataPacks
			);
		}
	}

	private void toggleWorldGenSettingsVisibility() {
		this.setWorldGenSettingsVisible(!this.worldGenSettingsVisible);
	}

	private void setGameMode(CreateWorldScreen.SelectedGameMode selectedGameMode) {
		if (!this.commandsChanged) {
			this.commands = selectedGameMode == CreateWorldScreen.SelectedGameMode.CREATIVE;
			this.commandsButton.setValue(this.commands);
		}

		if (selectedGameMode == CreateWorldScreen.SelectedGameMode.HARDCORE) {
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
		this.difficultyButton.visible = !bl;
		if (this.worldGenSettingsComponent.isDebug()) {
			this.dataPacksButton.visible = false;
			this.modeButton.active = false;
			if (this.oldGameMode == null) {
				this.oldGameMode = this.gameMode;
			}

			this.setGameMode(CreateWorldScreen.SelectedGameMode.DEBUG);
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
		} else if (i != 257 && i != 335) {
			return false;
		} else {
			this.onCreate();
			return true;
		}
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
		drawCenteredString(poseStack, this.font, this.title, this.width / 2, 20, -1);
		if (this.worldGenSettingsVisible) {
			drawString(poseStack, this.font, SEED_LABEL, this.width / 2 - 100, 47, -6250336);
			drawString(poseStack, this.font, SEED_INFO, this.width / 2 - 100, 85, -6250336);
			this.worldGenSettingsComponent.render(poseStack, i, j, f);
		} else {
			drawString(poseStack, this.font, NAME_LABEL, this.width / 2 - 100, 47, -6250336);
			drawString(poseStack, this.font, Component.empty().append(OUTPUT_DIR_INFO).append(" ").append(this.resultFolder), this.width / 2 - 100, 85, -6250336);
			this.nameEdit.render(poseStack, i, j, f);
			drawString(poseStack, this.font, this.gameModeHelp1, this.width / 2 - 150, 122, -6250336);
			drawString(poseStack, this.font, this.gameModeHelp2, this.width / 2 - 150, 134, -6250336);
			if (this.commandsButton.visible) {
				drawString(poseStack, this.font, COMMANDS_INFO, this.width / 2 - 150, 172, -6250336);
			}
		}

		super.render(poseStack, i, j, f);
	}

	@Override
	protected <T extends GuiEventListener & NarratableEntry> T addWidget(T guiEventListener) {
		return super.addWidget(guiEventListener);
	}

	@Override
	protected <T extends GuiEventListener & Widget & NarratableEntry> T addRenderableWidget(T guiEventListener) {
		return super.addRenderableWidget(guiEventListener);
	}

	@Nullable
	private Path getTempDataPackDir() {
		if (this.tempDataPackDir == null) {
			try {
				this.tempDataPackDir = Files.createTempDirectory("mcworld-");
			} catch (IOException var2) {
				LOGGER.warn("Failed to create temporary dir", (Throwable)var2);
				SystemToast.onPackCopyFailure(this.minecraft, this.resultFolder);
				this.popScreen();
			}
		}

		return this.tempDataPackDir;
	}

	private void openDataPackSelectionScreen() {
		Pair<File, PackRepository> pair = this.getDataPackSelectionSettings();
		if (pair != null) {
			this.minecraft
				.setScreen(new PackSelectionScreen(this, pair.getSecond(), this::tryApplyNewDataPacks, pair.getFirst(), Component.translatable("dataPack.title")));
		}
	}

	private void tryApplyNewDataPacks(PackRepository packRepository) {
		List<String> list = ImmutableList.copyOf(packRepository.getSelectedIds());
		List<String> list2 = (List<String>)packRepository.getAvailableIds()
			.stream()
			.filter(string -> !list.contains(string))
			.collect(ImmutableList.toImmutableList());
		DataPackConfig dataPackConfig = new DataPackConfig(list, list2);
		if (list.equals(this.dataPacks.getEnabled())) {
			this.dataPacks = dataPackConfig;
		} else {
			this.minecraft.tell(() -> this.minecraft.setScreen(new GenericDirtMessageScreen(Component.translatable("dataPack.validation.working"))));
			WorldLoader.InitConfig initConfig = createDefaultLoadConfig(packRepository, dataPackConfig);
			WorldLoader.<Pair, WorldCreationContext>load(
					initConfig,
					(resourceManager, dataPackConfigx) -> {
						WorldCreationContext worldCreationContext = this.worldGenSettingsComponent.settings();
						RegistryAccess registryAccess = worldCreationContext.registryAccess();
						RegistryAccess.Writable writable = RegistryAccess.builtinCopy();
						DynamicOps<JsonElement> dynamicOps = RegistryOps.create(JsonOps.INSTANCE, registryAccess);
						DynamicOps<JsonElement> dynamicOps2 = RegistryOps.createAndLoad(JsonOps.INSTANCE, writable, resourceManager);
						DataResult<JsonElement> dataResult = WorldGenSettings.CODEC
							.encodeStart(dynamicOps, worldCreationContext.worldGenSettings())
							.setLifecycle(Lifecycle.stable());
						DataResult<WorldGenSettings> dataResult2 = dataResult.flatMap(jsonElement -> WorldGenSettings.CODEC.parse(dynamicOps2, jsonElement));
						RegistryAccess.Frozen frozen = writable.freeze();
						Lifecycle lifecycle = dataResult2.lifecycle().add(frozen.allElementsLifecycle());
						WorldGenSettings worldGenSettings = dataResult2.getOrThrow(
							false, Util.prefix("Error parsing worldgen settings after loading data packs: ", LOGGER::error)
						);
						if (frozen.registryOrThrow(Registry.WORLD_PRESET_REGISTRY).size() == 0) {
							throw new IllegalStateException("Needs at least one world preset to continue");
						} else if (frozen.registryOrThrow(Registry.BIOME_REGISTRY).size() == 0) {
							throw new IllegalStateException("Needs at least one biome continue");
						} else {
							return Pair.of(Pair.of(worldGenSettings, lifecycle), frozen);
						}
					},
					(closeableResourceManager, reloadableServerResources, frozen, pair) -> {
						closeableResourceManager.close();
						return new WorldCreationContext((WorldGenSettings)pair.getFirst(), (Lifecycle)pair.getSecond(), frozen, reloadableServerResources);
					},
					Util.backgroundExecutor(),
					this.minecraft
				)
				.thenAcceptAsync(worldCreationContext -> {
					this.dataPacks = dataPackConfig;
					this.worldGenSettingsComponent.updateSettings(worldCreationContext);
					this.rebuildWidgets();
				}, this.minecraft)
				.handle(
					(void_, throwable) -> {
						if (throwable != null) {
							LOGGER.warn("Failed to validate datapack", throwable);
							this.minecraft
								.tell(
									() -> this.minecraft
											.setScreen(
												new ConfirmScreen(
													bl -> {
														if (bl) {
															this.openDataPackSelectionScreen();
														} else {
															this.dataPacks = DataPackConfig.DEFAULT;
															this.minecraft.setScreen(this);
														}
													},
													Component.translatable("dataPack.validation.failed"),
													CommonComponents.EMPTY,
													Component.translatable("dataPack.validation.back"),
													Component.translatable("dataPack.validation.reset")
												)
											)
								);
						} else {
							this.minecraft.tell(() -> this.minecraft.setScreen(this));
						}

						return null;
					}
				);
		}
	}

	private static WorldLoader.InitConfig createDefaultLoadConfig(PackRepository packRepository, DataPackConfig dataPackConfig) {
		WorldLoader.PackConfig packConfig = new WorldLoader.PackConfig(packRepository, dataPackConfig, false);
		return new WorldLoader.InitConfig(packConfig, Commands.CommandSelection.INTEGRATED, 2);
	}

	private void removeTempDataPackDir() {
		if (this.tempDataPackDir != null) {
			try {
				Stream<Path> stream = Files.walk(this.tempDataPackDir);

				try {
					stream.sorted(Comparator.reverseOrder()).forEach(path -> {
						try {
							Files.delete(path);
						} catch (IOException var2) {
							LOGGER.warn("Failed to remove temporary file {}", path, var2);
						}
					});
				} catch (Throwable var5) {
					if (stream != null) {
						try {
							stream.close();
						} catch (Throwable var4) {
							var5.addSuppressed(var4);
						}
					}

					throw var5;
				}

				if (stream != null) {
					stream.close();
				}
			} catch (IOException var6) {
				LOGGER.warn("Failed to list temporary dir {}", this.tempDataPackDir);
			}

			this.tempDataPackDir = null;
		}
	}

	private static void copyBetweenDirs(Path path, Path path2, Path path3) {
		try {
			Util.copyBetweenDirs(path, path2, path3);
		} catch (IOException var4) {
			LOGGER.warn("Failed to copy datapack file from {} to {}", path3, path2);
			throw new UncheckedIOException(var4);
		}
	}

	private Optional<LevelStorageSource.LevelStorageAccess> createNewWorldDirectory() {
		try {
			LevelStorageSource.LevelStorageAccess levelStorageAccess = this.minecraft.getLevelSource().createAccess(this.resultFolder);
			if (this.tempDataPackDir == null) {
				return Optional.of(levelStorageAccess);
			}

			try {
				Stream<Path> stream = Files.walk(this.tempDataPackDir);

				Optional var4;
				try {
					Path path = levelStorageAccess.getLevelPath(LevelResource.DATAPACK_DIR);
					Files.createDirectories(path);
					stream.filter(pathx -> !pathx.equals(this.tempDataPackDir)).forEach(path2 -> copyBetweenDirs(this.tempDataPackDir, path, path2));
					var4 = Optional.of(levelStorageAccess);
				} catch (Throwable var6) {
					if (stream != null) {
						try {
							stream.close();
						} catch (Throwable var5) {
							var6.addSuppressed(var5);
						}
					}

					throw var6;
				}

				if (stream != null) {
					stream.close();
				}

				return var4;
			} catch (UncheckedIOException | IOException var7) {
				LOGGER.warn("Failed to copy datapacks to world {}", this.resultFolder, var7);
				levelStorageAccess.close();
			}
		} catch (UncheckedIOException | IOException var8) {
			LOGGER.warn("Failed to create access for {}", this.resultFolder, var8);
		}

		SystemToast.onPackCopyFailure(this.minecraft, this.resultFolder);
		this.popScreen();
		return Optional.empty();
	}

	@Nullable
	public static Path createTempDataPackDirFromExistingWorld(Path path, Minecraft minecraft) {
		MutableObject<Path> mutableObject = new MutableObject<>();

		try {
			Stream<Path> stream = Files.walk(path);

			try {
				stream.filter(path2 -> !path2.equals(path)).forEach(path2 -> {
					Path path3 = mutableObject.getValue();
					if (path3 == null) {
						try {
							path3 = Files.createTempDirectory("mcworld-");
						} catch (IOException var5) {
							LOGGER.warn("Failed to create temporary dir");
							throw new UncheckedIOException(var5);
						}

						mutableObject.setValue(path3);
					}

					copyBetweenDirs(path, path3, path2);
				});
			} catch (Throwable var7) {
				if (stream != null) {
					try {
						stream.close();
					} catch (Throwable var6) {
						var7.addSuppressed(var6);
					}
				}

				throw var7;
			}

			if (stream != null) {
				stream.close();
			}
		} catch (UncheckedIOException | IOException var8) {
			LOGGER.warn("Failed to copy datapacks from world {}", path, var8);
			SystemToast.onPackCopyFailure(minecraft, path.toString());
			return null;
		}

		return mutableObject.getValue();
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
		} else {
			return null;
		}
	}

	@Environment(EnvType.CLIENT)
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
}
