package net.minecraft.client.gui.screens.worldselection;

import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.vertex.PoseStack;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.FileUtil;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.components.toasts.SystemToast;
import net.minecraft.client.gui.screens.ConfirmScreen;
import net.minecraft.client.gui.screens.DataPackSelectScreen;
import net.minecraft.client.gui.screens.GenericDirtMessageScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.commands.Commands;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.server.ServerResources;
import net.minecraft.server.packs.repository.FolderRepositorySource;
import net.minecraft.server.packs.repository.Pack;
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
import org.apache.commons.lang3.mutable.MutableObject;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Environment(EnvType.CLIENT)
public class CreateWorldScreen extends Screen {
	private static final Logger LOGGER = LogManager.getLogger();
	private final Screen lastScreen;
	private EditBox nameEdit;
	private String resultFolder;
	private CreateWorldScreen.SelectedGameMode gameMode = CreateWorldScreen.SelectedGameMode.SURVIVAL;
	@Nullable
	private CreateWorldScreen.SelectedGameMode oldGameMode;
	private Difficulty selectedDifficulty = Difficulty.NORMAL;
	private Difficulty effectiveDifficulty = Difficulty.NORMAL;
	private boolean commands;
	private boolean commandsChanged;
	public boolean hardCore;
	protected DataPackConfig dataPacks = DataPackConfig.DEFAULT;
	@Nullable
	private Path tempDataPackDir;
	private boolean displayOptions;
	private Button createButton;
	private Button modeButton;
	private Button difficultyButton;
	private Button moreOptionsButton;
	private Button gameRulesButton;
	private Button dataPacksButton;
	private Button commandsButton;
	private Component gameModeHelp1;
	private Component gameModeHelp2;
	private String initName;
	private GameRules gameRules = new GameRules();
	public final WorldGenSettingsComponent worldGenSettingsComponent;

	public CreateWorldScreen(
		@Nullable Screen screen, LevelSettings levelSettings, WorldGenSettings worldGenSettings, @Nullable Path path, RegistryAccess.RegistryHolder registryHolder
	) {
		this(screen, new WorldGenSettingsComponent(registryHolder, worldGenSettings));
		this.initName = levelSettings.levelName();
		this.commands = levelSettings.allowCommands();
		this.commandsChanged = true;
		this.selectedDifficulty = levelSettings.difficulty();
		this.effectiveDifficulty = this.selectedDifficulty;
		this.gameRules.assignFrom(levelSettings.gameRules(), null);
		this.dataPacks = levelSettings.getDataPackConfig();
		if (levelSettings.hardcore()) {
			this.gameMode = CreateWorldScreen.SelectedGameMode.HARDCORE;
		} else if (levelSettings.gameType().isSurvival()) {
			this.gameMode = CreateWorldScreen.SelectedGameMode.SURVIVAL;
		} else if (levelSettings.gameType().isCreative()) {
			this.gameMode = CreateWorldScreen.SelectedGameMode.CREATIVE;
		}

		this.tempDataPackDir = path;
	}

	public CreateWorldScreen(@Nullable Screen screen) {
		this(screen, new WorldGenSettingsComponent());
	}

	private CreateWorldScreen(@Nullable Screen screen, WorldGenSettingsComponent worldGenSettingsComponent) {
		super(new TranslatableComponent("selectWorld.create"));
		this.lastScreen = screen;
		this.initName = I18n.get("selectWorld.newWorld");
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
		this.nameEdit = new EditBox(this.font, this.width / 2 - 100, 60, 200, 20, new TranslatableComponent("selectWorld.enterName")) {
			@Override
			protected MutableComponent createNarrationMessage() {
				return super.createNarrationMessage()
					.append(". ")
					.append(new TranslatableComponent("selectWorld.resultFolder"))
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
		this.children.add(this.nameEdit);
		int i = this.width / 2 - 155;
		int j = this.width / 2 + 5;
		this.modeButton = this.addButton(new Button(i, 100, 150, 20, new TranslatableComponent("selectWorld.gameMode"), button -> {
			switch (this.gameMode) {
				case SURVIVAL:
					this.setGameMode(CreateWorldScreen.SelectedGameMode.HARDCORE);
					break;
				case HARDCORE:
					this.setGameMode(CreateWorldScreen.SelectedGameMode.CREATIVE);
					break;
				case CREATIVE:
					this.setGameMode(CreateWorldScreen.SelectedGameMode.SURVIVAL);
			}

			button.queueNarration(250);
		}) {
			@Override
			public Component getMessage() {
				return super.getMessage().copy().append(": ").append(new TranslatableComponent("selectWorld.gameMode." + CreateWorldScreen.this.gameMode.name));
			}

			@Override
			protected MutableComponent createNarrationMessage() {
				return super.createNarrationMessage().append(". ").append(CreateWorldScreen.this.gameModeHelp1).append(" ").append(CreateWorldScreen.this.gameModeHelp2);
			}
		});
		this.difficultyButton = this.addButton(new Button(j, 100, 150, 20, new TranslatableComponent("options.difficulty"), button -> {
			this.selectedDifficulty = this.selectedDifficulty.nextById();
			this.effectiveDifficulty = this.selectedDifficulty;
			button.queueNarration(250);
		}) {
			@Override
			public Component getMessage() {
				return new TranslatableComponent("options.difficulty").append(": ").append(CreateWorldScreen.this.effectiveDifficulty.getDisplayName());
			}
		});
		this.commandsButton = this.addButton(new Button(i, 151, 150, 20, new TranslatableComponent("selectWorld.allowCommands"), button -> {
			this.commandsChanged = true;
			this.commands = !this.commands;
			button.queueNarration(250);
		}) {
			@Override
			public Component getMessage() {
				return super.getMessage().copy().append(" ").append(CommonComponents.optionStatus(CreateWorldScreen.this.commands && !CreateWorldScreen.this.hardCore));
			}

			@Override
			protected MutableComponent createNarrationMessage() {
				return super.createNarrationMessage().append(". ").append(new TranslatableComponent("selectWorld.allowCommands.info"));
			}
		});
		this.dataPacksButton = this.addButton(
			new Button(j, 151, 150, 20, new TranslatableComponent("selectWorld.dataPacks"), button -> this.openDataPackSelectionScreen())
		);
		this.gameRulesButton = this.addButton(
			new Button(
				i,
				185,
				150,
				20,
				new TranslatableComponent("selectWorld.gameRules"),
				button -> this.minecraft.setScreen(new EditGameRulesScreen(this.gameRules.copy(), optional -> {
						this.minecraft.setScreen(this);
						optional.ifPresent(gameRules -> this.gameRules = gameRules);
					}))
			)
		);
		this.worldGenSettingsComponent.init(this, this.minecraft, this.font);
		this.moreOptionsButton = this.addButton(
			new Button(j, 185, 150, 20, new TranslatableComponent("selectWorld.moreWorldOptions"), button -> this.toggleDisplayOptions())
		);
		this.createButton = this.addButton(new Button(i, this.height - 28, 150, 20, new TranslatableComponent("selectWorld.create"), button -> this.onCreate()));
		this.createButton.active = !this.initName.isEmpty();
		this.addButton(new Button(j, this.height - 28, 150, 20, CommonComponents.GUI_CANCEL, button -> {
			this.removeTempDataPackDir();
			this.minecraft.setScreen(this.lastScreen);
		}));
		this.updateDisplayOptions();
		this.setInitialFocus(this.nameEdit);
		this.setGameMode(this.gameMode);
		this.updateResultFolder();
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

	private void onCreate() {
		this.minecraft.forceSetScreen(new GenericDirtMessageScreen(new TranslatableComponent("createWorld.preparing")));
		if (this.copyTempDataPackDirToNewWorld()) {
			WorldGenSettings worldGenSettings = this.worldGenSettingsComponent.makeSettings(this.hardCore);
			LevelSettings levelSettings;
			if (worldGenSettings.isDebug()) {
				GameRules gameRules = new GameRules();
				gameRules.getRule(GameRules.RULE_DAYLIGHT).set(false, null);
				levelSettings = new LevelSettings(this.nameEdit.getValue().trim(), GameType.SPECTATOR, false, Difficulty.PEACEFUL, true, gameRules, DataPackConfig.DEFAULT);
			} else {
				levelSettings = new LevelSettings(
					this.nameEdit.getValue().trim(),
					this.gameMode.gameType,
					this.hardCore,
					this.effectiveDifficulty,
					this.commands && !this.hardCore,
					this.gameRules,
					this.dataPacks
				);
			}

			this.minecraft.createLevel(this.resultFolder, levelSettings, this.worldGenSettingsComponent.registryHolder(), worldGenSettings);
		}
	}

	private void toggleDisplayOptions() {
		this.setDisplayOptions(!this.displayOptions);
	}

	private void setGameMode(CreateWorldScreen.SelectedGameMode selectedGameMode) {
		if (!this.commandsChanged) {
			this.commands = selectedGameMode == CreateWorldScreen.SelectedGameMode.CREATIVE;
		}

		if (selectedGameMode == CreateWorldScreen.SelectedGameMode.HARDCORE) {
			this.hardCore = true;
			this.commandsButton.active = false;
			this.worldGenSettingsComponent.bonusItemsButton.active = false;
			this.effectiveDifficulty = Difficulty.HARD;
			this.difficultyButton.active = false;
		} else {
			this.hardCore = false;
			this.commandsButton.active = true;
			this.worldGenSettingsComponent.bonusItemsButton.active = true;
			this.effectiveDifficulty = this.selectedDifficulty;
			this.difficultyButton.active = true;
		}

		this.gameMode = selectedGameMode;
		this.updateGameModeHelp();
	}

	public void updateDisplayOptions() {
		this.setDisplayOptions(this.displayOptions);
	}

	private void setDisplayOptions(boolean bl) {
		this.displayOptions = bl;
		this.modeButton.visible = !this.displayOptions;
		this.difficultyButton.visible = !this.displayOptions;
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

			this.commandsButton.visible = !this.displayOptions;
			this.dataPacksButton.visible = !this.displayOptions;
		}

		this.worldGenSettingsComponent.setDisplayOptions(this.displayOptions);
		this.nameEdit.setVisible(!this.displayOptions);
		if (this.displayOptions) {
			this.moreOptionsButton.setMessage(CommonComponents.GUI_DONE);
		} else {
			this.moreOptionsButton.setMessage(new TranslatableComponent("selectWorld.moreWorldOptions"));
		}

		this.gameRulesButton.visible = !this.displayOptions;
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
		if (this.displayOptions) {
			this.setDisplayOptions(false);
		} else {
			this.minecraft.setScreen(this.lastScreen);
		}

		this.removeTempDataPackDir();
	}

	@Override
	public void render(PoseStack poseStack, int i, int j, float f) {
		this.renderBackground(poseStack);
		this.drawCenteredString(poseStack, this.font, this.title, this.width / 2, 20, -1);
		if (this.displayOptions) {
			this.drawString(poseStack, this.font, I18n.get("selectWorld.enterSeed"), this.width / 2 - 100, 47, -6250336);
			this.drawString(poseStack, this.font, I18n.get("selectWorld.seedInfo"), this.width / 2 - 100, 85, -6250336);
			this.worldGenSettingsComponent.render(poseStack, i, j, f);
		} else {
			this.drawString(poseStack, this.font, I18n.get("selectWorld.enterName"), this.width / 2 - 100, 47, -6250336);
			this.drawString(poseStack, this.font, I18n.get("selectWorld.resultFolder") + " " + this.resultFolder, this.width / 2 - 100, 85, -6250336);
			this.nameEdit.render(poseStack, i, j, f);
			this.drawString(poseStack, this.font, this.gameModeHelp1, this.width / 2 - 150, 122, -6250336);
			this.drawString(poseStack, this.font, this.gameModeHelp2, this.width / 2 - 150, 134, -6250336);
			if (this.commandsButton.visible) {
				this.drawString(poseStack, this.font, I18n.get("selectWorld.allowCommands.info"), this.width / 2 - 150, 172, -6250336);
			}
		}

		super.render(poseStack, i, j, f);
	}

	@Override
	protected <T extends GuiEventListener> T addWidget(T guiEventListener) {
		return super.addWidget(guiEventListener);
	}

	@Override
	protected <T extends AbstractWidget> T addButton(T abstractWidget) {
		return super.addButton(abstractWidget);
	}

	@Nullable
	protected Path getTempDataPackDir() {
		if (this.tempDataPackDir == null) {
			try {
				this.tempDataPackDir = Files.createTempDirectory("mcworld-");
			} catch (IOException var2) {
				LOGGER.warn("Failed to create temporary dir", (Throwable)var2);
				SystemToast.onPackCopyFailure(this.minecraft, this.resultFolder);
				this.minecraft.setScreen(this.lastScreen);
			}
		}

		return this.tempDataPackDir;
	}

	private void openDataPackSelectionScreen() {
		Path path = this.getTempDataPackDir();
		if (path != null) {
			File file = path.toFile();
			PackRepository<Pack> packRepository = new PackRepository<>(Pack::new, new ServerPacksSource(), new FolderRepositorySource(file, PackSource.DEFAULT));
			packRepository.reload();
			packRepository.setSelected(this.dataPacks.getEnabled());
			this.minecraft.setScreen(new DataPackSelectScreen(this, packRepository, this::tryApplyNewDataPacks, file));
		}
	}

	private void tryApplyNewDataPacks(PackRepository<Pack> packRepository) {
		List<String> list = ImmutableList.copyOf(packRepository.getSelectedIds());
		List<String> list2 = (List<String>)packRepository.getAvailableIds()
			.stream()
			.filter(string -> !list.contains(string))
			.collect(ImmutableList.toImmutableList());
		DataPackConfig dataPackConfig = new DataPackConfig(list, list2);
		if (list.equals(this.dataPacks.getEnabled())) {
			this.dataPacks = dataPackConfig;
		} else {
			this.minecraft.tell(() -> this.minecraft.setScreen(new GenericDirtMessageScreen(new TranslatableComponent("dataPack.validation.working"))));
			ServerResources.loadResources(packRepository.openAllSelected(), Commands.CommandSelection.INTEGRATED, 2, Util.backgroundExecutor(), this.minecraft)
				.handle(
					(serverResources, throwable) -> {
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
													new TranslatableComponent("dataPack.validation.failed"),
													TextComponent.EMPTY,
													new TranslatableComponent("dataPack.validation.back"),
													new TranslatableComponent("dataPack.validation.reset")
												)
											)
								);
						} else {
							this.minecraft.tell(() -> {
								this.dataPacks = dataPackConfig;
								this.minecraft.setScreen(this);
							});
						}

						return null;
					}
				);
		}
	}

	private void removeTempDataPackDir() {
		if (this.tempDataPackDir != null) {
			try {
				Stream<Path> stream = Files.walk(this.tempDataPackDir);
				Throwable var2 = null;

				try {
					stream.sorted(Comparator.reverseOrder()).forEach(path -> {
						try {
							Files.delete(path);
						} catch (IOException var2x) {
							LOGGER.warn("Failed to remove temporary file {}", path, var2x);
						}
					});
				} catch (Throwable var12) {
					var2 = var12;
					throw var12;
				} finally {
					if (stream != null) {
						if (var2 != null) {
							try {
								stream.close();
							} catch (Throwable var11) {
								var2.addSuppressed(var11);
							}
						} else {
							stream.close();
						}
					}
				}
			} catch (IOException var14) {
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
			throw new CreateWorldScreen.OperationFailedException(var4);
		}
	}

	private boolean copyTempDataPackDirToNewWorld() {
		if (this.tempDataPackDir != null) {
			try (LevelStorageSource.LevelStorageAccess levelStorageAccess = this.minecraft.getLevelSource().createAccess(this.resultFolder)) {
				Stream<Path> stream = Files.walk(this.tempDataPackDir);
				Throwable var4 = null;

				try {
					Path path = levelStorageAccess.getLevelPath(LevelResource.DATAPACK_DIR);
					Files.createDirectories(path);
					stream.filter(pathx -> !pathx.equals(this.tempDataPackDir)).forEach(path2 -> copyBetweenDirs(this.tempDataPackDir, path, path2));
				} catch (Throwable var29) {
					var4 = var29;
					throw var29;
				} finally {
					if (stream != null) {
						if (var4 != null) {
							try {
								stream.close();
							} catch (Throwable var28) {
								var4.addSuppressed(var28);
							}
						} else {
							stream.close();
						}
					}
				}
			} catch (CreateWorldScreen.OperationFailedException | IOException var33) {
				LOGGER.warn("Failed to copy datapacks to world {}", this.resultFolder, var33);
				SystemToast.onPackCopyFailure(this.minecraft, this.resultFolder);
				this.minecraft.setScreen(this.lastScreen);
				this.removeTempDataPackDir();
				return false;
			}

			this.removeTempDataPackDir();
		}

		return true;
	}

	@Nullable
	public static Path createTempDataPackDirFromExistingWorld(Path path, Minecraft minecraft) {
		MutableObject<Path> mutableObject = new MutableObject<>();

		try {
			Stream<Path> stream = Files.walk(path);
			Throwable var4 = null;

			try {
				stream.filter(path2 -> !path2.equals(path)).forEach(path2 -> {
					Path path3 = mutableObject.getValue();
					if (path3 == null) {
						try {
							path3 = Files.createTempDirectory("mcworld-");
						} catch (IOException var5) {
							LOGGER.warn("Failed to create temporary dir");
							throw new CreateWorldScreen.OperationFailedException(var5);
						}

						mutableObject.setValue(path3);
					}

					copyBetweenDirs(path, path3, path2);
				});
			} catch (Throwable var14) {
				var4 = var14;
				throw var14;
			} finally {
				if (stream != null) {
					if (var4 != null) {
						try {
							stream.close();
						} catch (Throwable var13) {
							var4.addSuppressed(var13);
						}
					} else {
						stream.close();
					}
				}
			}
		} catch (CreateWorldScreen.OperationFailedException | IOException var16) {
			LOGGER.warn("Failed to copy datapacks from world {}", path, var16);
			SystemToast.onPackCopyFailure(minecraft, path.toString());
			return null;
		}

		return mutableObject.getValue();
	}

	@Environment(EnvType.CLIENT)
	static class OperationFailedException extends RuntimeException {
		public OperationFailedException(Throwable throwable) {
			super(throwable);
		}
	}

	@Environment(EnvType.CLIENT)
	static enum SelectedGameMode {
		SURVIVAL("survival", GameType.SURVIVAL),
		HARDCORE("hardcore", GameType.SURVIVAL),
		CREATIVE("creative", GameType.CREATIVE),
		DEBUG("spectator", GameType.SPECTATOR);

		private final String name;
		private final GameType gameType;

		private SelectedGameMode(String string2, GameType gameType) {
			this.name = string2;
			this.gameType = gameType;
		}
	}
}
