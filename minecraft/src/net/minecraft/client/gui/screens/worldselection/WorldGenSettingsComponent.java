package net.minecraft.client.gui.screens.worldselection;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.Lifecycle;
import com.mojang.serialization.DataResult.PartialResult;
import it.unimi.dsi.fastutil.booleans.BooleanConsumer;
import java.io.BufferedReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;
import java.util.OptionalLong;
import java.util.stream.Collectors;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.MultiLineLabel;
import net.minecraft.client.gui.components.Widget;
import net.minecraft.client.gui.components.toasts.SystemToast;
import net.minecraft.client.gui.screens.ConfirmScreen;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.RegistryOps;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.WorldStem;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.repository.FolderRepositorySource;
import net.minecraft.server.packs.repository.PackRepository;
import net.minecraft.server.packs.repository.PackSource;
import net.minecraft.server.packs.repository.ServerPacksSource;
import net.minecraft.server.packs.resources.CloseableResourceManager;
import net.minecraft.server.packs.resources.MultiPackResourceManager;
import net.minecraft.world.level.levelgen.WorldGenSettings;
import org.lwjgl.util.tinyfd.TinyFileDialogs;
import org.slf4j.Logger;

@Environment(EnvType.CLIENT)
public class WorldGenSettingsComponent implements Widget {
	private static final Logger LOGGER = LogUtils.getLogger();
	private static final Component CUSTOM_WORLD_DESCRIPTION = new TranslatableComponent("generator.custom");
	private static final Component AMPLIFIED_HELP_TEXT = new TranslatableComponent("generator.amplified.info");
	private static final Component MAP_FEATURES_INFO = new TranslatableComponent("selectWorld.mapFeatures.info");
	private static final Component SELECT_FILE_PROMPT = new TranslatableComponent("selectWorld.import_worldgen_settings.select_file");
	private MultiLineLabel amplifiedWorldInfo = MultiLineLabel.EMPTY;
	private Font font;
	private int width;
	private EditBox seedEdit;
	private CycleButton<Boolean> featuresButton;
	private CycleButton<Boolean> bonusItemsButton;
	private CycleButton<WorldPreset> typeButton;
	private Button customWorldDummyButton;
	private Button customizeTypeButton;
	private Button importSettingsButton;
	private RegistryAccess.Frozen registryHolder;
	private WorldGenSettings settings;
	private Optional<WorldPreset> preset;
	private OptionalLong seed;

	public WorldGenSettingsComponent(RegistryAccess.Frozen frozen, WorldGenSettings worldGenSettings, Optional<WorldPreset> optional, OptionalLong optionalLong) {
		this.registryHolder = frozen;
		this.settings = worldGenSettings;
		this.preset = optional;
		this.seed = optionalLong;
	}

	public void init(CreateWorldScreen createWorldScreen, Minecraft minecraft, Font font) {
		this.font = font;
		this.width = createWorldScreen.width;
		this.seedEdit = new EditBox(this.font, this.width / 2 - 100, 60, 200, 20, new TranslatableComponent("selectWorld.enterSeed"));
		this.seedEdit.setValue(toString(this.seed));
		this.seedEdit.setResponder(string -> this.seed = WorldGenSettings.parseSeed(this.seedEdit.getValue()));
		createWorldScreen.addWidget(this.seedEdit);
		int i = this.width / 2 - 155;
		int j = this.width / 2 + 5;
		this.featuresButton = createWorldScreen.addRenderableWidget(
			CycleButton.onOffBuilder(this.settings.generateFeatures())
				.withCustomNarration(
					cycleButton -> CommonComponents.joinForNarration(cycleButton.createDefaultNarrationMessage(), new TranslatableComponent("selectWorld.mapFeatures.info"))
				)
				.create(
					i, 100, 150, 20, new TranslatableComponent("selectWorld.mapFeatures"), (cycleButton, boolean_) -> this.settings = this.settings.withFeaturesToggled()
				)
		);
		this.featuresButton.visible = false;
		this.typeButton = createWorldScreen.addRenderableWidget(
			CycleButton.<WorldPreset>builder(WorldPreset::description)
				.withValues((List<WorldPreset>)WorldPreset.PRESETS.stream().filter(WorldPreset::isVisibleByDefault).collect(Collectors.toList()), WorldPreset.PRESETS)
				.withCustomNarration(
					cycleButton -> cycleButton.getValue() == WorldPreset.AMPLIFIED
							? CommonComponents.joinForNarration(cycleButton.createDefaultNarrationMessage(), AMPLIFIED_HELP_TEXT)
							: cycleButton.createDefaultNarrationMessage()
				)
				.create(j, 100, 150, 20, new TranslatableComponent("selectWorld.mapType"), (cycleButton, worldPreset) -> {
					this.preset = Optional.of(worldPreset);
					this.settings = worldPreset.create(this.registryHolder, this.settings.seed(), this.settings.generateFeatures(), this.settings.generateBonusChest());
					createWorldScreen.refreshWorldGenSettingsVisibility();
				})
		);
		this.preset.ifPresent(this.typeButton::setValue);
		this.typeButton.visible = false;
		this.customWorldDummyButton = createWorldScreen.addRenderableWidget(
			new Button(j, 100, 150, 20, CommonComponents.optionNameValue(new TranslatableComponent("selectWorld.mapType"), CUSTOM_WORLD_DESCRIPTION), button -> {
			})
		);
		this.customWorldDummyButton.active = false;
		this.customWorldDummyButton.visible = false;
		this.customizeTypeButton = createWorldScreen.addRenderableWidget(
			new Button(j, 120, 150, 20, new TranslatableComponent("selectWorld.customizeType"), button -> {
				WorldPreset.PresetEditor presetEditor = (WorldPreset.PresetEditor)WorldPreset.EDITORS.get(this.preset);
				if (presetEditor != null) {
					minecraft.setScreen(presetEditor.createEditScreen(createWorldScreen, this.settings));
				}
			})
		);
		this.customizeTypeButton.visible = false;
		this.bonusItemsButton = createWorldScreen.addRenderableWidget(
			CycleButton.onOffBuilder(this.settings.generateBonusChest() && !createWorldScreen.hardCore)
				.create(
					i, 151, 150, 20, new TranslatableComponent("selectWorld.bonusItems"), (cycleButton, boolean_) -> this.settings = this.settings.withBonusChestToggled()
				)
		);
		this.bonusItemsButton.visible = false;
		this.importSettingsButton = createWorldScreen.addRenderableWidget(
			new Button(
				i,
				185,
				150,
				20,
				new TranslatableComponent("selectWorld.import_worldgen_settings"),
				button -> {
					String string = TinyFileDialogs.tinyfd_openFileDialog(SELECT_FILE_PROMPT.getString(), null, null, null, false);
					if (string != null) {
						RegistryAccess.Writable writable = RegistryAccess.builtinCopy();

						DataResult<WorldGenSettings> dataResult;
						try (PackRepository packRepository = new PackRepository(
								PackType.SERVER_DATA, new ServerPacksSource(), new FolderRepositorySource(createWorldScreen.getTempDataPackDir().toFile(), PackSource.WORLD)
							)) {
							MinecraftServer.configurePackRepository(packRepository, createWorldScreen.dataPacks, false);

							try (CloseableResourceManager closeableResourceManager = new MultiPackResourceManager(PackType.SERVER_DATA, packRepository.openAllSelected())) {
								DynamicOps<JsonElement> dynamicOps = RegistryOps.createAndLoad(JsonOps.INSTANCE, writable, closeableResourceManager);

								try {
									BufferedReader bufferedReader = Files.newBufferedReader(Paths.get(string));

									try {
										JsonElement jsonElement = JsonParser.parseReader(bufferedReader);
										dataResult = WorldGenSettings.CODEC.parse(dynamicOps, jsonElement);
									} catch (Throwable var16) {
										if (bufferedReader != null) {
											try {
												bufferedReader.close();
											} catch (Throwable var15) {
												var16.addSuppressed(var15);
											}
										}

										throw var16;
									}

									if (bufferedReader != null) {
										bufferedReader.close();
									}
								} catch (Exception var17) {
									dataResult = DataResult.error("Failed to parse file: " + var17.getMessage());
								}

								if (dataResult.error().isPresent()) {
									Component component = new TranslatableComponent("selectWorld.import_worldgen_settings.failure");
									String string2 = ((PartialResult)dataResult.error().get()).message();
									LOGGER.error("Error parsing world settings: {}", string2);
									Component component2 = new TextComponent(string2);
									minecraft.getToasts().addToast(SystemToast.multiline(minecraft, SystemToast.SystemToastIds.WORLD_GEN_SETTINGS_TRANSFER, component, component2));
									return;
								}
							}
						}

						Lifecycle var20 = dataResult.lifecycle();
						dataResult.resultOrPartial(LOGGER::error)
							.ifPresent(
								worldGenSettings -> {
									BooleanConsumer booleanConsumer = bl -> {
										minecraft.setScreen(createWorldScreen);
										if (bl) {
											this.importSettings(writable.freeze(), worldGenSettings);
										}
									};
									if (var20 == Lifecycle.stable()) {
										this.importSettings(writable.freeze(), worldGenSettings);
									} else if (var20 == Lifecycle.experimental()) {
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
							);
					}
				}
			)
		);
		this.importSettingsButton.visible = false;
		this.amplifiedWorldInfo = MultiLineLabel.create(font, AMPLIFIED_HELP_TEXT, this.typeButton.getWidth());
	}

	private void importSettings(RegistryAccess.Frozen frozen, WorldGenSettings worldGenSettings) {
		this.registryHolder = frozen;
		this.settings = worldGenSettings;
		this.preset = WorldPreset.of(worldGenSettings);
		this.selectWorldTypeButton(true);
		this.seed = OptionalLong.of(worldGenSettings.seed());
		this.seedEdit.setValue(toString(this.seed));
	}

	public void tick() {
		this.seedEdit.tick();
	}

	@Override
	public void render(PoseStack poseStack, int i, int j, float f) {
		if (this.featuresButton.visible) {
			this.font.drawShadow(poseStack, MAP_FEATURES_INFO, (float)(this.width / 2 - 150), 122.0F, -6250336);
		}

		this.seedEdit.render(poseStack, i, j, f);
		if (this.preset.equals(Optional.of(WorldPreset.AMPLIFIED))) {
			this.amplifiedWorldInfo.renderLeftAligned(poseStack, this.typeButton.x + 2, this.typeButton.y + 22, 9, 10526880);
		}
	}

	protected void updateSettings(WorldGenSettings worldGenSettings) {
		this.settings = worldGenSettings;
	}

	private static String toString(OptionalLong optionalLong) {
		return optionalLong.isPresent() ? Long.toString(optionalLong.getAsLong()) : "";
	}

	public WorldGenSettings makeSettings(boolean bl) {
		OptionalLong optionalLong = WorldGenSettings.parseSeed(this.seedEdit.getValue());
		return this.settings.withSeed(bl, optionalLong);
	}

	public boolean isDebug() {
		return this.settings.isDebug();
	}

	public void setVisibility(boolean bl) {
		this.selectWorldTypeButton(bl);
		if (this.settings.isDebug()) {
			this.featuresButton.visible = false;
			this.bonusItemsButton.visible = false;
			this.customizeTypeButton.visible = false;
			this.importSettingsButton.visible = false;
		} else {
			this.featuresButton.visible = bl;
			this.bonusItemsButton.visible = bl;
			this.customizeTypeButton.visible = bl && WorldPreset.EDITORS.containsKey(this.preset);
			this.importSettingsButton.visible = bl;
		}

		this.seedEdit.setVisible(bl);
	}

	private void selectWorldTypeButton(boolean bl) {
		if (this.preset.isPresent()) {
			this.typeButton.visible = bl;
			this.customWorldDummyButton.visible = false;
		} else {
			this.typeButton.visible = false;
			this.customWorldDummyButton.visible = bl;
		}
	}

	public RegistryAccess registryHolder() {
		return this.registryHolder;
	}

	void updateDataPacks(WorldStem worldStem) {
		this.settings = worldStem.worldData().worldGenSettings();
		this.registryHolder = worldStem.registryAccess();
	}

	public void switchToHardcore() {
		this.bonusItemsButton.active = false;
		this.bonusItemsButton.setValue(false);
	}

	public void switchOutOfHardcode() {
		this.bonusItemsButton.active = true;
		this.bonusItemsButton.setValue(this.settings.generateBonusChest());
	}
}
