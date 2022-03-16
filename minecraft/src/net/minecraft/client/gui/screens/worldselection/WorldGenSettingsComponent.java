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
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;
import net.minecraft.tags.WorldPresetTags;
import net.minecraft.world.level.levelgen.WorldGenSettings;
import net.minecraft.world.level.levelgen.presets.WorldPreset;
import net.minecraft.world.level.levelgen.presets.WorldPresets;
import org.lwjgl.util.tinyfd.TinyFileDialogs;
import org.slf4j.Logger;

@Environment(EnvType.CLIENT)
public class WorldGenSettingsComponent implements Widget {
	private static final Logger LOGGER = LogUtils.getLogger();
	private static final Component CUSTOM_WORLD_DESCRIPTION = new TranslatableComponent("generator.custom");
	private static final Component AMPLIFIED_HELP_TEXT = new TranslatableComponent("generator.minecraft.amplified.info");
	private static final Component MAP_FEATURES_INFO = new TranslatableComponent("selectWorld.mapFeatures.info");
	private static final Component SELECT_FILE_PROMPT = new TranslatableComponent("selectWorld.import_worldgen_settings.select_file");
	private MultiLineLabel amplifiedWorldInfo = MultiLineLabel.EMPTY;
	private Font font;
	private int width;
	private EditBox seedEdit;
	private CycleButton<Boolean> featuresButton;
	private CycleButton<Boolean> bonusItemsButton;
	private CycleButton<Holder<WorldPreset>> typeButton;
	private Button customWorldDummyButton;
	private Button customizeTypeButton;
	private Button importSettingsButton;
	private WorldCreationContext settings;
	private Optional<Holder<WorldPreset>> preset;
	private OptionalLong seed;

	public WorldGenSettingsComponent(WorldCreationContext worldCreationContext, Optional<ResourceKey<WorldPreset>> optional, OptionalLong optionalLong) {
		this.settings = worldCreationContext;
		this.preset = findPreset(worldCreationContext, optional);
		this.seed = optionalLong;
	}

	private static Optional<Holder<WorldPreset>> findPreset(WorldCreationContext worldCreationContext, Optional<ResourceKey<WorldPreset>> optional) {
		return optional.flatMap(resourceKey -> worldCreationContext.registryAccess().registryOrThrow(Registry.WORLD_PRESET_REGISTRY).getHolder(resourceKey));
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
			CycleButton.onOffBuilder(this.settings.worldGenSettings().generateStructures())
				.withCustomNarration(
					cycleButton -> CommonComponents.joinForNarration(cycleButton.createDefaultNarrationMessage(), new TranslatableComponent("selectWorld.mapFeatures.info"))
				)
				.create(
					i,
					100,
					150,
					20,
					new TranslatableComponent("selectWorld.mapFeatures"),
					(cycleButton, boolean_) -> this.updateSettings(WorldGenSettings::withStructuresToggled)
				)
		);
		this.featuresButton.visible = false;
		Registry<WorldPreset> registry = this.settings.registryAccess().registryOrThrow(Registry.WORLD_PRESET_REGISTRY);
		List<Holder<WorldPreset>> list = (List<Holder<WorldPreset>>)getNonEmptyList(registry, WorldPresetTags.NORMAL)
			.orElseGet(() -> (List)registry.holders().collect(Collectors.toUnmodifiableList()));
		List<Holder<WorldPreset>> list2 = (List<Holder<WorldPreset>>)getNonEmptyList(registry, WorldPresetTags.EXTENDED).orElse(list);
		this.typeButton = createWorldScreen.addRenderableWidget(
			CycleButton.<Holder<WorldPreset>>builder(WorldGenSettingsComponent::describePreset)
				.withValues(list, list2)
				.withCustomNarration(
					cycleButton -> isAmplified((Holder<WorldPreset>)cycleButton.getValue())
							? CommonComponents.joinForNarration(cycleButton.createDefaultNarrationMessage(), AMPLIFIED_HELP_TEXT)
							: cycleButton.createDefaultNarrationMessage()
				)
				.create(j, 100, 150, 20, new TranslatableComponent("selectWorld.mapType"), (cycleButton, holder) -> {
					this.preset = Optional.of(holder);
					this.updateSettings(worldGenSettings -> ((WorldPreset)holder.value()).recreateWorldGenSettings(worldGenSettings));
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
				PresetEditor presetEditor = (PresetEditor)PresetEditor.EDITORS.get(this.preset.flatMap(Holder::unwrapKey));
				if (presetEditor != null) {
					minecraft.setScreen(presetEditor.createEditScreen(createWorldScreen, this.settings));
				}
			})
		);
		this.customizeTypeButton.visible = false;
		this.bonusItemsButton = createWorldScreen.addRenderableWidget(
			CycleButton.onOffBuilder(this.settings.worldGenSettings().generateBonusChest() && !createWorldScreen.hardCore)
				.create(
					i,
					151,
					150,
					20,
					new TranslatableComponent("selectWorld.bonusItems"),
					(cycleButton, boolean_) -> this.updateSettings(WorldGenSettings::withBonusChestToggled)
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
						DynamicOps<JsonElement> dynamicOps = RegistryOps.create(JsonOps.INSTANCE, this.settings.registryAccess());

						DataResult<WorldGenSettings> dataResult;
						try {
							BufferedReader bufferedReader = Files.newBufferedReader(Paths.get(string));

							try {
								JsonElement jsonElement = JsonParser.parseReader(bufferedReader);
								dataResult = WorldGenSettings.CODEC.parse(dynamicOps, jsonElement);
							} catch (Throwable var11) {
								if (bufferedReader != null) {
									try {
										bufferedReader.close();
									} catch (Throwable var10) {
										var11.addSuppressed(var10);
									}
								}

								throw var11;
							}

							if (bufferedReader != null) {
								bufferedReader.close();
							}
						} catch (Exception var12) {
							dataResult = DataResult.error("Failed to parse file: " + var12.getMessage());
						}

						if (dataResult.error().isPresent()) {
							Component component = new TranslatableComponent("selectWorld.import_worldgen_settings.failure");
							String string2 = ((PartialResult)dataResult.error().get()).message();
							LOGGER.error("Error parsing world settings: {}", string2);
							Component component2 = new TextComponent(string2);
							minecraft.getToasts().addToast(SystemToast.multiline(minecraft, SystemToast.SystemToastIds.WORLD_GEN_SETTINGS_TRANSFER, component, component2));
						} else {
							Lifecycle lifecycle = dataResult.lifecycle();
							dataResult.resultOrPartial(LOGGER::error)
								.ifPresent(
									worldGenSettings -> WorldOpenFlows.confirmWorldCreation(minecraft, createWorldScreen, lifecycle, () -> this.importSettings(worldGenSettings))
								);
						}
					}
				}
			)
		);
		this.importSettingsButton.visible = false;
		this.amplifiedWorldInfo = MultiLineLabel.create(font, AMPLIFIED_HELP_TEXT, this.typeButton.getWidth());
	}

	private static Optional<List<Holder<WorldPreset>>> getNonEmptyList(Registry<WorldPreset> registry, TagKey<WorldPreset> tagKey) {
		return registry.getTag(tagKey).map(named -> named.stream().toList()).filter(list -> !list.isEmpty());
	}

	private static boolean isAmplified(Holder<WorldPreset> holder) {
		return holder.unwrapKey().filter(resourceKey -> resourceKey.equals(WorldPresets.AMPLIFIED)).isPresent();
	}

	private static Component describePreset(Holder<WorldPreset> holder) {
		return (Component)holder.unwrapKey()
			.map(resourceKey -> new TranslatableComponent(resourceKey.location().toLanguageKey("generator")))
			.orElse(CUSTOM_WORLD_DESCRIPTION);
	}

	private void importSettings(WorldGenSettings worldGenSettings) {
		this.settings = this.settings.withSettings(worldGenSettings);
		this.preset = findPreset(this.settings, WorldPresets.fromSettings(worldGenSettings));
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
		if (this.preset.filter(WorldGenSettingsComponent::isAmplified).isPresent()) {
			this.amplifiedWorldInfo.renderLeftAligned(poseStack, this.typeButton.x + 2, this.typeButton.y + 22, 9, 10526880);
		}
	}

	void updateSettings(WorldCreationContext.SimpleUpdater simpleUpdater) {
		this.settings = this.settings.withSettings(simpleUpdater);
	}

	void updateSettings(WorldCreationContext.Updater updater) {
		this.settings = this.settings.withSettings(updater);
	}

	void updateSettings(WorldCreationContext worldCreationContext) {
		this.settings = worldCreationContext;
	}

	private static String toString(OptionalLong optionalLong) {
		return optionalLong.isPresent() ? Long.toString(optionalLong.getAsLong()) : "";
	}

	public WorldCreationContext createFinalSettings(boolean bl) {
		OptionalLong optionalLong = WorldGenSettings.parseSeed(this.seedEdit.getValue());
		return this.settings.withSettings(worldGenSettings -> worldGenSettings.withSeed(bl, optionalLong));
	}

	public boolean isDebug() {
		return this.settings.worldGenSettings().isDebug();
	}

	public void setVisibility(boolean bl) {
		this.selectWorldTypeButton(bl);
		if (this.isDebug()) {
			this.featuresButton.visible = false;
			this.bonusItemsButton.visible = false;
			this.customizeTypeButton.visible = false;
			this.importSettingsButton.visible = false;
		} else {
			this.featuresButton.visible = bl;
			this.bonusItemsButton.visible = bl;
			this.customizeTypeButton.visible = bl && PresetEditor.EDITORS.containsKey(this.preset.flatMap(Holder::unwrapKey));
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

	public WorldCreationContext settings() {
		return this.settings;
	}

	public RegistryAccess registryHolder() {
		return this.settings.registryAccess();
	}

	public void switchToHardcore() {
		this.bonusItemsButton.active = false;
		this.bonusItemsButton.setValue(false);
	}

	public void switchOutOfHardcode() {
		this.bonusItemsButton.active = true;
		this.bonusItemsButton.setValue(this.settings.worldGenSettings().generateBonusChest());
	}
}
