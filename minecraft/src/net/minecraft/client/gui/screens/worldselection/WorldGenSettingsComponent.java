package net.minecraft.client.gui.screens.worldselection;

import com.google.gson.JsonElement;
import com.google.gson.JsonIOException;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.Lifecycle;
import com.mojang.serialization.DataResult.PartialResult;
import it.unimi.dsi.fastutil.booleans.BooleanConsumer;
import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Objects;
import java.util.Optional;
import java.util.OptionalLong;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.TickableWidget;
import net.minecraft.client.gui.components.Widget;
import net.minecraft.client.gui.components.toasts.SystemToast;
import net.minecraft.client.gui.screens.ConfirmScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.level.levelgen.WorldGenSettings;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.util.tinyfd.TinyFileDialogs;

@Environment(EnvType.CLIENT)
public class WorldGenSettingsComponent implements TickableWidget, Widget {
	private static final Logger LOGGER = LogManager.getLogger();
	private static final Component CUSTOM_WORLD_DESCRIPTION = new TranslatableComponent("generator.custom");
	private static final Component AMPLIFIED_HELP_TEXT = new TranslatableComponent("generator.amplified.info");
	private Font font;
	private int width;
	private EditBox seedEdit;
	private Button featuresButton;
	public Button bonusItemsButton;
	private Button typeButton;
	private Button customizeTypeButton;
	private Button importSettingsButton;
	private WorldGenSettings settings;
	private Optional<WorldPreset> preset;
	private String initSeed;

	public WorldGenSettingsComponent() {
		this.settings = WorldGenSettings.makeDefault();
		this.preset = Optional.of(WorldPreset.NORMAL);
		this.initSeed = "";
	}

	public WorldGenSettingsComponent(WorldGenSettings worldGenSettings) {
		this.settings = worldGenSettings;
		this.preset = WorldPreset.of(worldGenSettings);
		this.initSeed = Long.toString(worldGenSettings.seed());
	}

	public void init(CreateWorldScreen createWorldScreen, Minecraft minecraft, Font font) {
		this.font = font;
		this.width = createWorldScreen.width;
		this.seedEdit = new EditBox(this.font, this.width / 2 - 100, 60, 200, 20, new TranslatableComponent("selectWorld.enterSeed"));
		this.seedEdit.setValue(this.initSeed);
		this.seedEdit.setResponder(string -> this.initSeed = this.seedEdit.getValue());
		createWorldScreen.addWidget(this.seedEdit);
		this.featuresButton = createWorldScreen.addButton(
			new Button(this.width / 2 - 155, 100, 150, 20, new TranslatableComponent("selectWorld.mapFeatures"), button -> {
				this.settings = this.settings.withFeaturesToggled();
				button.queueNarration(250);
			}) {
				@Override
				public Component getMessage() {
					return super.getMessage().mutableCopy().append(" ").append(CommonComponents.optionStatus(WorldGenSettingsComponent.this.settings.generateFeatures()));
				}

				@Override
				protected MutableComponent createNarrationMessage() {
					return super.createNarrationMessage().append(". ").append(new TranslatableComponent("selectWorld.mapFeatures.info"));
				}
			}
		);
		this.featuresButton.visible = false;
		this.typeButton = createWorldScreen.addButton(
			new Button(this.width / 2 + 5, 100, 150, 20, new TranslatableComponent("selectWorld.mapType"), button -> {
				while (this.preset.isPresent()) {
					int i = WorldPreset.PRESETS.indexOf(this.preset.get()) + 1;
					if (i >= WorldPreset.PRESETS.size()) {
						i = 0;
					}

					WorldPreset worldPreset = (WorldPreset)WorldPreset.PRESETS.get(i);
					this.preset = Optional.of(worldPreset);
					this.settings = worldPreset.create(this.settings.seed(), this.settings.generateFeatures(), this.settings.generateBonusChest());
					if (!this.settings.isDebug() || Screen.hasShiftDown()) {
						break;
					}
				}

				createWorldScreen.updateDisplayOptions();
				button.queueNarration(250);
			}) {
				@Override
				public Component getMessage() {
					return super.getMessage()
						.mutableCopy()
						.append(" ")
						.append((Component)WorldGenSettingsComponent.this.preset.map(WorldPreset::description).orElse(WorldGenSettingsComponent.CUSTOM_WORLD_DESCRIPTION));
				}

				@Override
				protected MutableComponent createNarrationMessage() {
					return Objects.equals(WorldGenSettingsComponent.this.preset, Optional.of(WorldPreset.AMPLIFIED))
						? super.createNarrationMessage().append(". ").append(WorldGenSettingsComponent.AMPLIFIED_HELP_TEXT)
						: super.createNarrationMessage();
				}
			}
		);
		this.typeButton.visible = false;
		this.typeButton.active = this.preset.isPresent();
		this.customizeTypeButton = createWorldScreen.addButton(
			new Button(createWorldScreen.width / 2 + 5, 120, 150, 20, new TranslatableComponent("selectWorld.customizeType"), button -> {
				WorldPreset.PresetEditor presetEditor = (WorldPreset.PresetEditor)WorldPreset.EDITORS.get(this.preset);
				if (presetEditor != null) {
					minecraft.setScreen(presetEditor.createEditScreen(createWorldScreen, this.settings));
				}
			})
		);
		this.customizeTypeButton.visible = false;
		this.bonusItemsButton = createWorldScreen.addButton(
			new Button(createWorldScreen.width / 2 - 155, 151, 150, 20, new TranslatableComponent("selectWorld.bonusItems"), button -> {
				this.settings = this.settings.withBonusChestToggled();
				button.queueNarration(250);
			}) {
				@Override
				public Component getMessage() {
					return super.getMessage()
						.mutableCopy()
						.append(" ")
						.append(CommonComponents.optionStatus(WorldGenSettingsComponent.this.settings.generateBonusChest() && !createWorldScreen.hardCore));
				}
			}
		);
		this.bonusItemsButton.visible = false;
		this.importSettingsButton = createWorldScreen.addButton(
			new Button(
				this.width / 2 - 155,
				185,
				150,
				20,
				new TranslatableComponent("selectWorld.import_worldgen_settings"),
				button -> {
					TranslatableComponent translatableComponent = new TranslatableComponent("selectWorld.import_worldgen_settings.select_file");
					String string = TinyFileDialogs.tinyfd_openFileDialog(translatableComponent.getString(), null, null, null, false);
					if (string != null) {
						JsonParser jsonParser = new JsonParser();

						DataResult<WorldGenSettings> dataResult;
						try {
							BufferedReader bufferedReader = Files.newBufferedReader(Paths.get(string));
							Throwable string2 = null;

							try {
								JsonElement jsonElement = jsonParser.parse(bufferedReader);
								dataResult = WorldGenSettings.CODEC.parse(JsonOps.INSTANCE, jsonElement);
							} catch (Throwable var19) {
								string2 = var19;
								throw var19;
							} finally {
								if (bufferedReader != null) {
									if (string2 != null) {
										try {
											bufferedReader.close();
										} catch (Throwable var18) {
											string2.addSuppressed(var18);
										}
									} else {
										bufferedReader.close();
									}
								}
							}
						} catch (JsonIOException | JsonSyntaxException | IOException var21) {
							dataResult = DataResult.error("Failed to parse file: " + var21.getMessage());
						}

						if (dataResult.error().isPresent()) {
							Component component = new TranslatableComponent("selectWorld.import_worldgen_settings.failure");
							String string2 = ((PartialResult)dataResult.error().get()).message();
							LOGGER.error("Error parsing world settings: {}", string2);
							Component component2 = new TextComponent(string2);
							minecraft.getToasts().addToast(SystemToast.multiline(SystemToast.SystemToastIds.WORLD_GEN_SETTINGS_TRANSFER, component, component2));
						}

						Lifecycle lifecycle = dataResult.lifecycle();
						dataResult.resultOrPartial(LOGGER::error)
							.ifPresent(
								worldGenSettings -> {
									BooleanConsumer booleanConsumer = bl -> {
										minecraft.setScreen(createWorldScreen);
										if (bl) {
											this.importSettings(worldGenSettings);
										}
									};
									if (lifecycle == Lifecycle.stable()) {
										this.importSettings(worldGenSettings);
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
							);
					}
				}
			)
		);
		this.importSettingsButton.visible = false;
	}

	private void importSettings(WorldGenSettings worldGenSettings) {
		this.settings = worldGenSettings;
		this.preset = WorldPreset.of(worldGenSettings);
		this.initSeed = Long.toString(worldGenSettings.seed());
		this.seedEdit.setValue(this.initSeed);
		this.typeButton.active = this.preset.isPresent();
	}

	@Override
	public void tick() {
		this.seedEdit.tick();
	}

	@Override
	public void render(PoseStack poseStack, int i, int j, float f) {
		if (this.featuresButton.visible) {
			this.font.drawShadow(poseStack, I18n.get("selectWorld.mapFeatures.info"), (float)(this.width / 2 - 150), 122.0F, -6250336);
		}

		this.seedEdit.render(poseStack, i, j, f);
		if (this.preset.equals(Optional.of(WorldPreset.AMPLIFIED))) {
			this.font.drawWordWrap(AMPLIFIED_HELP_TEXT, this.typeButton.x + 2, this.typeButton.y + 22, this.typeButton.getWidth(), 10526880);
		}
	}

	protected void updateSettings(WorldGenSettings worldGenSettings) {
		this.settings = worldGenSettings;
	}

	private static OptionalLong parseLong(String string) {
		try {
			return OptionalLong.of(Long.parseLong(string));
		} catch (NumberFormatException var2) {
			return OptionalLong.empty();
		}
	}

	public WorldGenSettings makeSettings(boolean bl) {
		String string = this.seedEdit.getValue();
		OptionalLong optionalLong;
		if (StringUtils.isEmpty(string)) {
			optionalLong = OptionalLong.empty();
		} else {
			OptionalLong optionalLong2 = parseLong(string);
			if (optionalLong2.isPresent() && optionalLong2.getAsLong() != 0L) {
				optionalLong = optionalLong2;
			} else {
				optionalLong = OptionalLong.of((long)string.hashCode());
			}
		}

		return this.settings.withSeed(bl, optionalLong);
	}

	public boolean isDebug() {
		return this.settings.isDebug();
	}

	public void setDisplayOptions(boolean bl) {
		this.typeButton.visible = bl;
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
}