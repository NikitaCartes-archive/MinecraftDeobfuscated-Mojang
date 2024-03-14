package net.minecraft.client.gui.screens.worldselection;

import com.google.common.collect.ImmutableList;
import com.google.gson.JsonElement;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.datafixers.util.Pair;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.Lifecycle;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.OptionalLong;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.ChatFormatting;
import net.minecraft.FileUtil;
import net.minecraft.SharedConstants;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.components.tabs.GridLayoutTab;
import net.minecraft.client.gui.components.tabs.TabManager;
import net.minecraft.client.gui.components.tabs.TabNavigationBar;
import net.minecraft.client.gui.components.toasts.SystemToast;
import net.minecraft.client.gui.layouts.CommonLayouts;
import net.minecraft.client.gui.layouts.GridLayout;
import net.minecraft.client.gui.layouts.HeaderAndFooterLayout;
import net.minecraft.client.gui.layouts.LayoutSettings;
import net.minecraft.client.gui.layouts.LinearLayout;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.gui.screens.ConfirmScreen;
import net.minecraft.client.gui.screens.GenericMessageScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.packs.PackSelectionScreen;
import net.minecraft.commands.Commands;
import net.minecraft.core.LayeredRegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
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
import net.minecraft.world.level.levelgen.presets.WorldPreset;
import net.minecraft.world.level.levelgen.presets.WorldPresets;
import net.minecraft.world.level.storage.LevelResource;
import net.minecraft.world.level.storage.LevelStorageSource;
import net.minecraft.world.level.storage.PrimaryLevelData;
import net.minecraft.world.level.storage.WorldData;
import net.minecraft.world.level.validation.DirectoryValidator;
import org.apache.commons.lang3.mutable.MutableObject;
import org.slf4j.Logger;

@Environment(EnvType.CLIENT)
public class CreateWorldScreen extends Screen {
	private static final int GROUP_BOTTOM = 1;
	private static final int TAB_COLUMN_WIDTH = 210;
	private static final Logger LOGGER = LogUtils.getLogger();
	private static final String TEMP_WORLD_PREFIX = "mcworld-";
	static final Component GAME_MODEL_LABEL = Component.translatable("selectWorld.gameMode");
	static final Component NAME_LABEL = Component.translatable("selectWorld.enterName");
	static final Component EXPERIMENTS_LABEL = Component.translatable("selectWorld.experiments");
	static final Component ALLOW_COMMANDS_INFO = Component.translatable("selectWorld.allowCommands.info");
	private static final Component PREPARING_WORLD_DATA = Component.translatable("createWorld.preparing");
	private static final int HORIZONTAL_BUTTON_SPACING = 10;
	private static final int VERTICAL_BUTTON_SPACING = 8;
	public static final ResourceLocation TAB_HEADER_BACKGROUND = new ResourceLocation("textures/gui/tab_header_background.png");
	private final HeaderAndFooterLayout layout = new HeaderAndFooterLayout(this);
	final WorldCreationUiState uiState;
	private final TabManager tabManager = new TabManager(this::addRenderableWidget, guiEventListener -> this.removeWidget(guiEventListener));
	private boolean recreated;
	private final DirectoryValidator packValidator;
	@Nullable
	private final Screen lastScreen;
	@Nullable
	private Path tempDataPackDir;
	@Nullable
	private PackRepository tempDataPackRepository;
	@Nullable
	private TabNavigationBar tabNavigationBar;

	public static void openFresh(Minecraft minecraft, @Nullable Screen screen) {
		queueLoadScreen(minecraft, PREPARING_WORLD_DATA);
		PackRepository packRepository = new PackRepository(new ServerPacksSource(minecraft.directoryValidator()));
		WorldLoader.InitConfig initConfig = createDefaultLoadConfig(packRepository, WorldDataConfiguration.DEFAULT);
		CompletableFuture<WorldCreationContext> completableFuture = WorldLoader.load(
			initConfig,
			dataLoadContext -> new WorldLoader.DataLoadOutput<>(
					new CreateWorldScreen.DataPackReloadCookie(
						new WorldGenSettings(WorldOptions.defaultWithRandomSeed(), WorldPresets.createNormalWorldDimensions(dataLoadContext.datapackWorldgen())),
						dataLoadContext.dataConfiguration()
					),
					dataLoadContext.datapackDimensions()
				),
			(closeableResourceManager, reloadableServerResources, layeredRegistryAccess, dataPackReloadCookie) -> {
				closeableResourceManager.close();
				return new WorldCreationContext(
					dataPackReloadCookie.worldGenSettings(), layeredRegistryAccess, reloadableServerResources, dataPackReloadCookie.dataConfiguration()
				);
			},
			Util.backgroundExecutor(),
			minecraft
		);
		minecraft.managedBlock(completableFuture::isDone);
		minecraft.setScreen(
			new CreateWorldScreen(minecraft, screen, (WorldCreationContext)completableFuture.join(), Optional.of(WorldPresets.NORMAL), OptionalLong.empty())
		);
	}

	public static CreateWorldScreen createFromExisting(
		Minecraft minecraft, @Nullable Screen screen, LevelSettings levelSettings, WorldCreationContext worldCreationContext, @Nullable Path path
	) {
		CreateWorldScreen createWorldScreen = new CreateWorldScreen(
			minecraft,
			screen,
			worldCreationContext,
			WorldPresets.fromSettings(worldCreationContext.selectedDimensions()),
			OptionalLong.of(worldCreationContext.options().seed())
		);
		createWorldScreen.recreated = true;
		createWorldScreen.uiState.setName(levelSettings.levelName());
		createWorldScreen.uiState.setAllowCommands(levelSettings.allowCommands());
		createWorldScreen.uiState.setDifficulty(levelSettings.difficulty());
		createWorldScreen.uiState.getGameRules().assignFrom(levelSettings.gameRules(), null);
		if (levelSettings.hardcore()) {
			createWorldScreen.uiState.setGameMode(WorldCreationUiState.SelectedGameMode.HARDCORE);
		} else if (levelSettings.gameType().isSurvival()) {
			createWorldScreen.uiState.setGameMode(WorldCreationUiState.SelectedGameMode.SURVIVAL);
		} else if (levelSettings.gameType().isCreative()) {
			createWorldScreen.uiState.setGameMode(WorldCreationUiState.SelectedGameMode.CREATIVE);
		}

		createWorldScreen.tempDataPackDir = path;
		return createWorldScreen;
	}

	private CreateWorldScreen(
		Minecraft minecraft,
		@Nullable Screen screen,
		WorldCreationContext worldCreationContext,
		Optional<ResourceKey<WorldPreset>> optional,
		OptionalLong optionalLong
	) {
		super(Component.translatable("selectWorld.create"));
		this.lastScreen = screen;
		this.packValidator = minecraft.directoryValidator();
		this.uiState = new WorldCreationUiState(minecraft.getLevelSource().getBaseDir(), worldCreationContext, optional, optionalLong);
	}

	public WorldCreationUiState getUiState() {
		return this.uiState;
	}

	@Override
	protected void init() {
		this.tabNavigationBar = TabNavigationBar.builder(this.tabManager, this.width)
			.addTabs(new CreateWorldScreen.GameTab(), new CreateWorldScreen.WorldTab(), new CreateWorldScreen.MoreTab())
			.build();
		this.addRenderableWidget(this.tabNavigationBar);
		LinearLayout linearLayout = this.layout.addToFooter(LinearLayout.horizontal().spacing(8));
		linearLayout.addChild(Button.builder(Component.translatable("selectWorld.create"), button -> this.onCreate()).build());
		linearLayout.addChild(Button.builder(CommonComponents.GUI_CANCEL, button -> this.popScreen()).build());
		this.layout.visitWidgets(abstractWidget -> {
			abstractWidget.setTabOrderGroup(1);
			this.addRenderableWidget(abstractWidget);
		});
		this.tabNavigationBar.selectTab(0, false);
		this.uiState.onChanged();
		this.repositionElements();
	}

	@Override
	protected void setInitialFocus() {
	}

	@Override
	public void repositionElements() {
		if (this.tabNavigationBar != null) {
			this.tabNavigationBar.setWidth(this.width);
			this.tabNavigationBar.arrangeElements();
			int i = this.tabNavigationBar.getRectangle().bottom();
			ScreenRectangle screenRectangle = new ScreenRectangle(0, i, this.width, this.height - this.layout.getFooterHeight() - i);
			this.tabManager.setTabArea(screenRectangle);
			this.layout.setHeaderHeight(i);
			this.layout.arrangeElements();
		}
	}

	private static void queueLoadScreen(Minecraft minecraft, Component component) {
		minecraft.forceSetScreen(new GenericMessageScreen(component));
	}

	private void onCreate() {
		WorldCreationContext worldCreationContext = this.uiState.getSettings();
		WorldDimensions.Complete complete = worldCreationContext.selectedDimensions().bake(worldCreationContext.datapackDimensions());
		LayeredRegistryAccess<RegistryLayer> layeredRegistryAccess = worldCreationContext.worldgenRegistries()
			.replaceFrom(RegistryLayer.DIMENSIONS, complete.dimensionsRegistryAccess());
		Lifecycle lifecycle = FeatureFlags.isExperimental(worldCreationContext.dataConfiguration().enabledFeatures()) ? Lifecycle.experimental() : Lifecycle.stable();
		Lifecycle lifecycle2 = layeredRegistryAccess.compositeAccess().allRegistriesLifecycle();
		Lifecycle lifecycle3 = lifecycle2.add(lifecycle);
		boolean bl = !this.recreated && lifecycle2 == Lifecycle.stable();
		WorldOpenFlows.confirmWorldCreation(
			this.minecraft, this, lifecycle3, () -> this.createNewWorld(complete.specialWorldProperty(), layeredRegistryAccess, lifecycle3), bl
		);
	}

	private void createNewWorld(
		PrimaryLevelData.SpecialWorldProperty specialWorldProperty, LayeredRegistryAccess<RegistryLayer> layeredRegistryAccess, Lifecycle lifecycle
	) {
		queueLoadScreen(this.minecraft, PREPARING_WORLD_DATA);
		Optional<LevelStorageSource.LevelStorageAccess> optional = this.createNewWorldDirectory();
		if (!optional.isEmpty()) {
			this.removeTempDataPackDir();
			boolean bl = specialWorldProperty == PrimaryLevelData.SpecialWorldProperty.DEBUG;
			WorldCreationContext worldCreationContext = this.uiState.getSettings();
			LevelSettings levelSettings = this.createLevelSettings(bl);
			WorldData worldData = new PrimaryLevelData(levelSettings, worldCreationContext.options(), specialWorldProperty, lifecycle);
			this.minecraft
				.createWorldOpenFlows()
				.createLevelFromExistingSettings(
					(LevelStorageSource.LevelStorageAccess)optional.get(), worldCreationContext.dataPackResources(), layeredRegistryAccess, worldData
				);
		}
	}

	private LevelSettings createLevelSettings(boolean bl) {
		String string = this.uiState.getName().trim();
		if (bl) {
			GameRules gameRules = new GameRules();
			gameRules.getRule(GameRules.RULE_DAYLIGHT).set(false, null);
			return new LevelSettings(string, GameType.SPECTATOR, false, Difficulty.PEACEFUL, true, gameRules, WorldDataConfiguration.DEFAULT);
		} else {
			return new LevelSettings(
				string,
				this.uiState.getGameMode().gameType,
				this.uiState.isHardcore(),
				this.uiState.getDifficulty(),
				this.uiState.isAllowCommands(),
				this.uiState.getGameRules(),
				this.uiState.getSettings().dataConfiguration()
			);
		}
	}

	@Override
	public boolean keyPressed(int i, int j, int k) {
		if (this.tabNavigationBar.keyPressed(i)) {
			return true;
		} else if (super.keyPressed(i, j, k)) {
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
		this.popScreen();
	}

	public void popScreen() {
		this.minecraft.setScreen(this.lastScreen);
		this.removeTempDataPackDir();
	}

	@Override
	public void render(GuiGraphics guiGraphics, int i, int j, float f) {
		super.render(guiGraphics, i, j, f);
		RenderSystem.enableBlend();
		guiGraphics.blit(Screen.FOOTER_SEPARATOR, 0, this.height - this.layout.getFooterHeight() - 2, 0.0F, 0.0F, this.width, 2, 32, 2);
		RenderSystem.disableBlend();
	}

	@Override
	protected void renderMenuBackground(GuiGraphics guiGraphics) {
		guiGraphics.blit(TAB_HEADER_BACKGROUND, 0, 0, 0.0F, 0.0F, this.width, this.layout.getHeaderHeight(), 16, 16);
		this.renderMenuBackground(guiGraphics, 0, this.layout.getHeaderHeight(), this.width, this.height);
	}

	@Override
	protected <T extends GuiEventListener & NarratableEntry> T addWidget(T guiEventListener) {
		return super.addWidget(guiEventListener);
	}

	@Override
	protected <T extends GuiEventListener & Renderable & NarratableEntry> T addRenderableWidget(T guiEventListener) {
		return super.addRenderableWidget(guiEventListener);
	}

	@Nullable
	private Path getTempDataPackDir() {
		if (this.tempDataPackDir == null) {
			try {
				this.tempDataPackDir = Files.createTempDirectory("mcworld-");
			} catch (IOException var2) {
				LOGGER.warn("Failed to create temporary dir", (Throwable)var2);
				SystemToast.onPackCopyFailure(this.minecraft, this.uiState.getTargetFolder());
				this.popScreen();
			}
		}

		return this.tempDataPackDir;
	}

	void openExperimentsScreen(WorldDataConfiguration worldDataConfiguration) {
		Pair<Path, PackRepository> pair = this.getDataPackSelectionSettings(worldDataConfiguration);
		if (pair != null) {
			this.minecraft
				.setScreen(new ExperimentsScreen(this, pair.getSecond(), packRepository -> this.tryApplyNewDataPacks(packRepository, false, this::openExperimentsScreen)));
		}
	}

	void openDataPackSelectionScreen(WorldDataConfiguration worldDataConfiguration) {
		Pair<Path, PackRepository> pair = this.getDataPackSelectionSettings(worldDataConfiguration);
		if (pair != null) {
			this.minecraft
				.setScreen(
					new PackSelectionScreen(
						pair.getSecond(),
						packRepository -> this.tryApplyNewDataPacks(packRepository, true, this::openDataPackSelectionScreen),
						pair.getFirst(),
						Component.translatable("dataPack.title")
					)
				);
		}
	}

	private void tryApplyNewDataPacks(PackRepository packRepository, boolean bl, Consumer<WorldDataConfiguration> consumer) {
		List<String> list = ImmutableList.copyOf(packRepository.getSelectedIds());
		List<String> list2 = (List<String>)packRepository.getAvailableIds()
			.stream()
			.filter(string -> !list.contains(string))
			.collect(ImmutableList.toImmutableList());
		WorldDataConfiguration worldDataConfiguration = new WorldDataConfiguration(
			new DataPackConfig(list, list2), this.uiState.getSettings().dataConfiguration().enabledFeatures()
		);
		if (this.uiState.tryUpdateDataConfiguration(worldDataConfiguration)) {
			this.minecraft.setScreen(this);
		} else {
			FeatureFlagSet featureFlagSet = packRepository.getRequestedFeatureFlags();
			if (FeatureFlags.isExperimental(featureFlagSet) && bl) {
				this.minecraft.setScreen(new ConfirmExperimentalFeaturesScreen(packRepository.getSelectedPacks(), blx -> {
					if (blx) {
						this.applyNewPackConfig(packRepository, worldDataConfiguration, consumer);
					} else {
						consumer.accept(this.uiState.getSettings().dataConfiguration());
					}
				}));
			} else {
				this.applyNewPackConfig(packRepository, worldDataConfiguration, consumer);
			}
		}
	}

	private void applyNewPackConfig(PackRepository packRepository, WorldDataConfiguration worldDataConfiguration, Consumer<WorldDataConfiguration> consumer) {
		this.minecraft.forceSetScreen(new GenericMessageScreen(Component.translatable("dataPack.validation.working")));
		WorldLoader.InitConfig initConfig = createDefaultLoadConfig(packRepository, worldDataConfiguration);
		WorldLoader.<CreateWorldScreen.DataPackReloadCookie, WorldCreationContext>load(
				initConfig,
				dataLoadContext -> {
					if (dataLoadContext.datapackWorldgen().registryOrThrow(Registries.WORLD_PRESET).size() == 0) {
						throw new IllegalStateException("Needs at least one world preset to continue");
					} else if (dataLoadContext.datapackWorldgen().registryOrThrow(Registries.BIOME).size() == 0) {
						throw new IllegalStateException("Needs at least one biome continue");
					} else {
						WorldCreationContext worldCreationContext = this.uiState.getSettings();
						DynamicOps<JsonElement> dynamicOps = worldCreationContext.worldgenLoadContext().createSerializationContext(JsonOps.INSTANCE);
						DataResult<JsonElement> dataResult = WorldGenSettings.encode(dynamicOps, worldCreationContext.options(), worldCreationContext.selectedDimensions())
							.setLifecycle(Lifecycle.stable());
						DynamicOps<JsonElement> dynamicOps2 = dataLoadContext.datapackWorldgen().createSerializationContext(JsonOps.INSTANCE);
						WorldGenSettings worldGenSettings = dataResult.<WorldGenSettings>flatMap(jsonElement -> WorldGenSettings.CODEC.parse(dynamicOps2, jsonElement))
							.getOrThrow(false, Util.prefix("Error parsing worldgen settings after loading data packs: ", LOGGER::error));
						return new WorldLoader.DataLoadOutput<>(
							new CreateWorldScreen.DataPackReloadCookie(worldGenSettings, dataLoadContext.dataConfiguration()), dataLoadContext.datapackDimensions()
						);
					}
				},
				(closeableResourceManager, reloadableServerResources, layeredRegistryAccess, dataPackReloadCookie) -> {
					closeableResourceManager.close();
					return new WorldCreationContext(
						dataPackReloadCookie.worldGenSettings(), layeredRegistryAccess, reloadableServerResources, dataPackReloadCookie.dataConfiguration()
					);
				},
				Util.backgroundExecutor(),
				this.minecraft
			)
			.thenAcceptAsync(this.uiState::setSettings, this.minecraft)
			.handle(
				(void_, throwable) -> {
					if (throwable != null) {
						LOGGER.warn("Failed to validate datapack", throwable);
						this.minecraft
							.setScreen(
								new ConfirmScreen(
									bl -> {
										if (bl) {
											consumer.accept(this.uiState.getSettings().dataConfiguration());
										} else {
											consumer.accept(WorldDataConfiguration.DEFAULT);
										}
									},
									Component.translatable("dataPack.validation.failed"),
									CommonComponents.EMPTY,
									Component.translatable("dataPack.validation.back"),
									Component.translatable("dataPack.validation.reset")
								)
							);
					} else {
						this.minecraft.setScreen(this);
					}

					return null;
				}
			);
	}

	private static WorldLoader.InitConfig createDefaultLoadConfig(PackRepository packRepository, WorldDataConfiguration worldDataConfiguration) {
		WorldLoader.PackConfig packConfig = new WorldLoader.PackConfig(packRepository, worldDataConfiguration, false, true);
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
		String string = this.uiState.getTargetFolder();

		try {
			LevelStorageSource.LevelStorageAccess levelStorageAccess = this.minecraft.getLevelSource().createAccess(string);
			if (this.tempDataPackDir == null) {
				return Optional.of(levelStorageAccess);
			}

			try {
				Stream<Path> stream = Files.walk(this.tempDataPackDir);

				Optional var5;
				try {
					Path path = levelStorageAccess.getLevelPath(LevelResource.DATAPACK_DIR);
					FileUtil.createDirectoriesSafe(path);
					stream.filter(pathx -> !pathx.equals(this.tempDataPackDir)).forEach(path2 -> copyBetweenDirs(this.tempDataPackDir, path, path2));
					var5 = Optional.of(levelStorageAccess);
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

				return var5;
			} catch (UncheckedIOException | IOException var8) {
				LOGGER.warn("Failed to copy datapacks to world {}", string, var8);
				levelStorageAccess.close();
			}
		} catch (UncheckedIOException | IOException var9) {
			LOGGER.warn("Failed to create access for {}", string, var9);
		}

		SystemToast.onPackCopyFailure(this.minecraft, string);
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
	private Pair<Path, PackRepository> getDataPackSelectionSettings(WorldDataConfiguration worldDataConfiguration) {
		Path path = this.getTempDataPackDir();
		if (path != null) {
			if (this.tempDataPackRepository == null) {
				this.tempDataPackRepository = ServerPacksSource.createPackRepository(path, this.packValidator);
				this.tempDataPackRepository.reload();
			}

			this.tempDataPackRepository.setSelected(worldDataConfiguration.dataPacks().getEnabled());
			return Pair.of(path, this.tempDataPackRepository);
		} else {
			return null;
		}
	}

	@Environment(EnvType.CLIENT)
	static record DataPackReloadCookie(WorldGenSettings worldGenSettings, WorldDataConfiguration dataConfiguration) {
	}

	@Environment(EnvType.CLIENT)
	class GameTab extends GridLayoutTab {
		private static final Component TITLE = Component.translatable("createWorld.tab.game.title");
		private static final Component ALLOW_COMMANDS = Component.translatable("selectWorld.allowCommands.new");
		private final EditBox nameEdit;

		GameTab() {
			super(TITLE);
			GridLayout.RowHelper rowHelper = this.layout.rowSpacing(8).createRowHelper(1);
			LayoutSettings layoutSettings = rowHelper.newCellSettings();
			this.nameEdit = new EditBox(CreateWorldScreen.this.font, 208, 20, Component.translatable("selectWorld.enterName"));
			this.nameEdit.setValue(CreateWorldScreen.this.uiState.getName());
			this.nameEdit.setResponder(CreateWorldScreen.this.uiState::setName);
			CreateWorldScreen.this.uiState
				.addListener(
					worldCreationUiState -> this.nameEdit
							.setTooltip(
								Tooltip.create(
									Component.translatable("selectWorld.targetFolder", Component.literal(worldCreationUiState.getTargetFolder()).withStyle(ChatFormatting.ITALIC))
								)
							)
				);
			CreateWorldScreen.this.setInitialFocus(this.nameEdit);
			rowHelper.addChild(
				CommonLayouts.labeledElement(CreateWorldScreen.this.font, this.nameEdit, CreateWorldScreen.NAME_LABEL),
				rowHelper.newCellSettings().alignHorizontallyCenter()
			);
			CycleButton<WorldCreationUiState.SelectedGameMode> cycleButton = rowHelper.addChild(
				CycleButton.<WorldCreationUiState.SelectedGameMode>builder(selectedGameMode -> selectedGameMode.displayName)
					.withValues(WorldCreationUiState.SelectedGameMode.SURVIVAL, WorldCreationUiState.SelectedGameMode.HARDCORE, WorldCreationUiState.SelectedGameMode.CREATIVE)
					.create(
						0, 0, 210, 20, CreateWorldScreen.GAME_MODEL_LABEL, (cycleButtonx, selectedGameMode) -> CreateWorldScreen.this.uiState.setGameMode(selectedGameMode)
					),
				layoutSettings
			);
			CreateWorldScreen.this.uiState.addListener(worldCreationUiState -> {
				cycleButton.setValue(worldCreationUiState.getGameMode());
				cycleButton.active = !worldCreationUiState.isDebug();
				cycleButton.setTooltip(Tooltip.create(worldCreationUiState.getGameMode().getInfo()));
			});
			CycleButton<Difficulty> cycleButton2 = rowHelper.addChild(
				CycleButton.<Difficulty>builder(Difficulty::getDisplayName)
					.withValues(Difficulty.values())
					.create(
						0, 0, 210, 20, Component.translatable("options.difficulty"), (cycleButtonx, difficulty) -> CreateWorldScreen.this.uiState.setDifficulty(difficulty)
					),
				layoutSettings
			);
			CreateWorldScreen.this.uiState.addListener(worldCreationUiState -> {
				cycleButton2.setValue(CreateWorldScreen.this.uiState.getDifficulty());
				cycleButton2.active = !CreateWorldScreen.this.uiState.isHardcore();
				cycleButton2.setTooltip(Tooltip.create(CreateWorldScreen.this.uiState.getDifficulty().getInfo()));
			});
			CycleButton<Boolean> cycleButton3 = rowHelper.addChild(
				CycleButton.onOffBuilder()
					.withTooltip(boolean_ -> Tooltip.create(CreateWorldScreen.ALLOW_COMMANDS_INFO))
					.create(0, 0, 210, 20, ALLOW_COMMANDS, (cycleButtonx, boolean_) -> CreateWorldScreen.this.uiState.setAllowCommands(boolean_))
			);
			CreateWorldScreen.this.uiState.addListener(worldCreationUiState -> {
				cycleButton3.setValue(CreateWorldScreen.this.uiState.isAllowCommands());
				cycleButton3.active = !CreateWorldScreen.this.uiState.isDebug() && !CreateWorldScreen.this.uiState.isHardcore();
			});
			if (!SharedConstants.getCurrentVersion().isStable()) {
				rowHelper.addChild(
					Button.builder(
							CreateWorldScreen.EXPERIMENTS_LABEL,
							button -> CreateWorldScreen.this.openExperimentsScreen(CreateWorldScreen.this.uiState.getSettings().dataConfiguration())
						)
						.width(210)
						.build()
				);
			}
		}
	}

	@Environment(EnvType.CLIENT)
	class MoreTab extends GridLayoutTab {
		private static final Component TITLE = Component.translatable("createWorld.tab.more.title");
		private static final Component GAME_RULES_LABEL = Component.translatable("selectWorld.gameRules");
		private static final Component DATA_PACKS_LABEL = Component.translatable("selectWorld.dataPacks");

		MoreTab() {
			super(TITLE);
			GridLayout.RowHelper rowHelper = this.layout.rowSpacing(8).createRowHelper(1);
			rowHelper.addChild(Button.builder(GAME_RULES_LABEL, button -> this.openGameRulesScreen()).width(210).build());
			rowHelper.addChild(
				Button.builder(
						CreateWorldScreen.EXPERIMENTS_LABEL,
						button -> CreateWorldScreen.this.openExperimentsScreen(CreateWorldScreen.this.uiState.getSettings().dataConfiguration())
					)
					.width(210)
					.build()
			);
			rowHelper.addChild(
				Button.builder(
						DATA_PACKS_LABEL, button -> CreateWorldScreen.this.openDataPackSelectionScreen(CreateWorldScreen.this.uiState.getSettings().dataConfiguration())
					)
					.width(210)
					.build()
			);
		}

		private void openGameRulesScreen() {
			CreateWorldScreen.this.minecraft.setScreen(new EditGameRulesScreen(CreateWorldScreen.this.uiState.getGameRules().copy(), optional -> {
				CreateWorldScreen.this.minecraft.setScreen(CreateWorldScreen.this);
				optional.ifPresent(CreateWorldScreen.this.uiState::setGameRules);
			}));
		}
	}

	@Environment(EnvType.CLIENT)
	class WorldTab extends GridLayoutTab {
		private static final Component TITLE = Component.translatable("createWorld.tab.world.title");
		private static final Component AMPLIFIED_HELP_TEXT = Component.translatable("generator.minecraft.amplified.info");
		private static final Component GENERATE_STRUCTURES = Component.translatable("selectWorld.mapFeatures");
		private static final Component GENERATE_STRUCTURES_INFO = Component.translatable("selectWorld.mapFeatures.info");
		private static final Component BONUS_CHEST = Component.translatable("selectWorld.bonusItems");
		private static final Component SEED_LABEL = Component.translatable("selectWorld.enterSeed");
		static final Component SEED_EMPTY_HINT = Component.translatable("selectWorld.seedInfo").withStyle(ChatFormatting.DARK_GRAY);
		private static final int WORLD_TAB_WIDTH = 310;
		private final EditBox seedEdit;
		private final Button customizeTypeButton;

		WorldTab() {
			super(TITLE);
			GridLayout.RowHelper rowHelper = this.layout.columnSpacing(10).rowSpacing(8).createRowHelper(2);
			CycleButton<WorldCreationUiState.WorldTypeEntry> cycleButton = rowHelper.addChild(
				CycleButton.<WorldCreationUiState.WorldTypeEntry>builder(WorldCreationUiState.WorldTypeEntry::describePreset)
					.withValues(this.createWorldTypeValueSupplier())
					.withCustomNarration(CreateWorldScreen.WorldTab::createTypeButtonNarration)
					.create(
						0,
						0,
						150,
						20,
						Component.translatable("selectWorld.mapType"),
						(cycleButtonx, worldTypeEntry) -> CreateWorldScreen.this.uiState.setWorldType(worldTypeEntry)
					)
			);
			cycleButton.setValue(CreateWorldScreen.this.uiState.getWorldType());
			CreateWorldScreen.this.uiState.addListener(worldCreationUiState -> {
				WorldCreationUiState.WorldTypeEntry worldTypeEntry = worldCreationUiState.getWorldType();
				cycleButton.setValue(worldTypeEntry);
				if (worldTypeEntry.isAmplified()) {
					cycleButton.setTooltip(Tooltip.create(AMPLIFIED_HELP_TEXT));
				} else {
					cycleButton.setTooltip(null);
				}

				cycleButton.active = CreateWorldScreen.this.uiState.getWorldType().preset() != null;
			});
			this.customizeTypeButton = rowHelper.addChild(Button.builder(Component.translatable("selectWorld.customizeType"), button -> this.openPresetEditor()).build());
			CreateWorldScreen.this.uiState
				.addListener(worldCreationUiState -> this.customizeTypeButton.active = !worldCreationUiState.isDebug() && worldCreationUiState.getPresetEditor() != null);
			this.seedEdit = new EditBox(CreateWorldScreen.this.font, 308, 20, Component.translatable("selectWorld.enterSeed")) {
				@Override
				protected MutableComponent createNarrationMessage() {
					return super.createNarrationMessage().append(CommonComponents.NARRATION_SEPARATOR).append(CreateWorldScreen.WorldTab.SEED_EMPTY_HINT);
				}
			};
			this.seedEdit.setHint(SEED_EMPTY_HINT);
			this.seedEdit.setValue(CreateWorldScreen.this.uiState.getSeed());
			this.seedEdit.setResponder(string -> CreateWorldScreen.this.uiState.setSeed(this.seedEdit.getValue()));
			rowHelper.addChild(CommonLayouts.labeledElement(CreateWorldScreen.this.font, this.seedEdit, SEED_LABEL), 2);
			SwitchGrid.Builder builder = SwitchGrid.builder(310);
			builder.addSwitch(GENERATE_STRUCTURES, CreateWorldScreen.this.uiState::isGenerateStructures, CreateWorldScreen.this.uiState::setGenerateStructures)
				.withIsActiveCondition(() -> !CreateWorldScreen.this.uiState.isDebug())
				.withInfo(GENERATE_STRUCTURES_INFO);
			builder.addSwitch(BONUS_CHEST, CreateWorldScreen.this.uiState::isBonusChest, CreateWorldScreen.this.uiState::setBonusChest)
				.withIsActiveCondition(() -> !CreateWorldScreen.this.uiState.isHardcore() && !CreateWorldScreen.this.uiState.isDebug());
			SwitchGrid switchGrid = builder.build(layoutElement -> rowHelper.addChild(layoutElement, 2));
			CreateWorldScreen.this.uiState.addListener(worldCreationUiState -> switchGrid.refreshStates());
		}

		private void openPresetEditor() {
			PresetEditor presetEditor = CreateWorldScreen.this.uiState.getPresetEditor();
			if (presetEditor != null) {
				CreateWorldScreen.this.minecraft.setScreen(presetEditor.createEditScreen(CreateWorldScreen.this, CreateWorldScreen.this.uiState.getSettings()));
			}
		}

		private CycleButton.ValueListSupplier<WorldCreationUiState.WorldTypeEntry> createWorldTypeValueSupplier() {
			return new CycleButton.ValueListSupplier<WorldCreationUiState.WorldTypeEntry>() {
				@Override
				public List<WorldCreationUiState.WorldTypeEntry> getSelectedList() {
					return CycleButton.DEFAULT_ALT_LIST_SELECTOR.getAsBoolean()
						? CreateWorldScreen.this.uiState.getAltPresetList()
						: CreateWorldScreen.this.uiState.getNormalPresetList();
				}

				@Override
				public List<WorldCreationUiState.WorldTypeEntry> getDefaultList() {
					return CreateWorldScreen.this.uiState.getNormalPresetList();
				}
			};
		}

		private static MutableComponent createTypeButtonNarration(CycleButton<WorldCreationUiState.WorldTypeEntry> cycleButton) {
			return cycleButton.getValue().isAmplified()
				? CommonComponents.joinForNarration(cycleButton.createDefaultNarrationMessage(), AMPLIFIED_HELP_TEXT)
				: cycleButton.createDefaultNarrationMessage();
		}
	}
}
