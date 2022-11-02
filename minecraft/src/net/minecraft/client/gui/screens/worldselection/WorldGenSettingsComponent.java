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
import java.util.stream.Collectors;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.MultiLineLabel;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.components.toasts.SystemToast;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;
import net.minecraft.tags.WorldPresetTags;
import net.minecraft.world.level.levelgen.WorldDimensions;
import net.minecraft.world.level.levelgen.WorldGenSettings;
import net.minecraft.world.level.levelgen.WorldOptions;
import net.minecraft.world.level.levelgen.presets.WorldPreset;
import net.minecraft.world.level.levelgen.presets.WorldPresets;
import org.lwjgl.util.tinyfd.TinyFileDialogs;
import org.slf4j.Logger;

@Environment(EnvType.CLIENT)
public class WorldGenSettingsComponent implements Renderable {
	private static final Logger LOGGER = LogUtils.getLogger();
	private static final Component CUSTOM_WORLD_DESCRIPTION = Component.translatable("generator.custom");
	private static final Component AMPLIFIED_HELP_TEXT = Component.translatable("generator.minecraft.amplified.info");
	private static final Component MAP_FEATURES_INFO = Component.translatable("selectWorld.mapFeatures.info");
	private static final Component SELECT_FILE_PROMPT = Component.translatable("selectWorld.import_worldgen_settings.select_file");
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
	private long seed;

	public WorldGenSettingsComponent(WorldCreationContext worldCreationContext, Optional<ResourceKey<WorldPreset>> optional, long l) {
		this.settings = worldCreationContext;
		this.preset = findPreset(worldCreationContext, optional);
		this.seed = l;
	}

	public WorldGenSettingsComponent(WorldCreationContext worldCreationContext, Optional<ResourceKey<WorldPreset>> optional) {
		this.settings = worldCreationContext;
		this.preset = findPreset(worldCreationContext, optional);
		this.seed = WorldOptions.randomSeed();
	}

	private static Optional<Holder<WorldPreset>> findPreset(WorldCreationContext worldCreationContext, Optional<ResourceKey<WorldPreset>> optional) {
		return optional.flatMap(resourceKey -> worldCreationContext.worldgenLoadContext().registryOrThrow(Registry.WORLD_PRESET_REGISTRY).getHolder(resourceKey));
	}

	public void init(CreateWorldScreen createWorldScreen, Minecraft minecraft, Font font) {
		this.font = font;
		this.width = createWorldScreen.width;
		this.seedEdit = new EditBox(this.font, this.width / 2 - 100, 60, 200, 20, Component.translatable("selectWorld.enterSeed"));
		this.seedEdit.setValue(Long.toString(this.seed));
		this.seedEdit.setResponder(string -> this.seed = WorldOptions.parseSeedOrElseRandom(this.seedEdit.getValue()));
		createWorldScreen.addWidget(this.seedEdit);
		int i = this.width / 2 - 155;
		int j = this.width / 2 + 5;
		this.featuresButton = createWorldScreen.addRenderableWidget(
			CycleButton.onOffBuilder(this.settings.options().generateStructures())
				.withCustomNarration(
					cycleButton -> CommonComponents.joinForNarration(cycleButton.createDefaultNarrationMessage(), Component.translatable("selectWorld.mapFeatures.info"))
				)
				.create(
					i,
					100,
					150,
					20,
					Component.translatable("selectWorld.mapFeatures"),
					(cycleButton, boolean_) -> this.updateSettings(worldOptions -> worldOptions.withStructures(boolean_))
				)
		);
		this.featuresButton.visible = false;
		Registry<WorldPreset> registry = this.settings.worldgenLoadContext().registryOrThrow(Registry.WORLD_PRESET_REGISTRY);
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
				.create(j, 100, 150, 20, Component.translatable("selectWorld.mapType"), (cycleButton, holder) -> {
					this.preset = Optional.of(holder);
					this.updateSettings((frozen, worldDimensions) -> ((WorldPreset)holder.value()).createWorldDimensions());
					createWorldScreen.refreshWorldGenSettingsVisibility();
				})
		);
		this.preset.ifPresent(this.typeButton::setValue);
		this.typeButton.visible = false;
		this.customWorldDummyButton = createWorldScreen.addRenderableWidget(
			Button.builder(CommonComponents.optionNameValue(Component.translatable("selectWorld.mapType"), CUSTOM_WORLD_DESCRIPTION), button -> {
			}).bounds(j, 100, 150, 20).build()
		);
		this.customWorldDummyButton.active = false;
		this.customWorldDummyButton.visible = false;
		this.customizeTypeButton = createWorldScreen.addRenderableWidget(Button.builder(Component.translatable("selectWorld.customizeType"), button -> {
			PresetEditor presetEditor = (PresetEditor)PresetEditor.EDITORS.get(this.preset.flatMap(Holder::unwrapKey));
			if (presetEditor != null) {
				minecraft.setScreen(presetEditor.createEditScreen(createWorldScreen, this.settings));
			}
		}).bounds(j, 120, 150, 20).build());
		this.customizeTypeButton.visible = false;
		this.bonusItemsButton = createWorldScreen.addRenderableWidget(
			CycleButton.onOffBuilder(this.settings.options().generateBonusChest() && !createWorldScreen.hardCore)
				.create(
					i,
					151,
					150,
					20,
					Component.translatable("selectWorld.bonusItems"),
					(cycleButton, boolean_) -> this.updateSettings(worldOptions -> worldOptions.withBonusChest(boolean_))
				)
		);
		this.bonusItemsButton.visible = false;
		this.importSettingsButton = createWorldScreen.addRenderableWidget(
			Button.builder(
					Component.translatable("selectWorld.import_worldgen_settings"),
					button -> {
						String string = TinyFileDialogs.tinyfd_openFileDialog(SELECT_FILE_PROMPT.getString(), null, null, null, false);
						if (string != null) {
							DynamicOps<JsonElement> dynamicOps = RegistryOps.create(JsonOps.INSTANCE, this.settings.worldgenLoadContext());

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
								Component component = Component.translatable("selectWorld.import_worldgen_settings.failure");
								String string2 = ((PartialResult)dataResult.error().get()).message();
								LOGGER.error("Error parsing world settings: {}", string2);
								Component component2 = Component.literal(string2);
								minecraft.getToasts().addToast(SystemToast.multiline(minecraft, SystemToast.SystemToastIds.WORLD_GEN_SETTINGS_TRANSFER, component, component2));
							} else {
								Lifecycle lifecycle = dataResult.lifecycle();
								dataResult.resultOrPartial(LOGGER::error)
									.ifPresent(
										worldGenSettings -> WorldOpenFlows.confirmWorldCreation(
												minecraft, createWorldScreen, lifecycle, () -> this.importSettings(worldGenSettings.options(), worldGenSettings.dimensions())
											)
									);
							}
						}
					}
				)
				.bounds(i, 185, 150, 20)
				.build()
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
			.map(resourceKey -> Component.translatable(resourceKey.location().toLanguageKey("generator")))
			.orElse(CUSTOM_WORLD_DESCRIPTION);
	}

	private void importSettings(WorldOptions worldOptions, WorldDimensions worldDimensions) {
		this.settings = this.settings.withSettings(worldOptions, worldDimensions);
		this.preset = findPreset(this.settings, WorldPresets.fromSettings(worldDimensions.dimensions()));
		this.selectWorldTypeButton(true);
		this.seed = worldOptions.seed();
		this.seedEdit.setValue(Long.toString(this.seed));
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
			this.amplifiedWorldInfo.renderLeftAligned(poseStack, this.typeButton.getX() + 2, this.typeButton.getY() + 22, 9, 10526880);
		}
	}

	void updateSettings(WorldCreationContext.DimensionsUpdater dimensionsUpdater) {
		this.settings = this.settings.withDimensions(dimensionsUpdater);
	}

	private void updateSettings(WorldCreationContext.OptionsModifier optionsModifier) {
		this.settings = this.settings.withOptions(optionsModifier);
	}

	void updateSettings(WorldCreationContext worldCreationContext) {
		this.settings = worldCreationContext;
	}

	public WorldOptions createFinalOptions(boolean bl, boolean bl2) {
		long l = WorldOptions.parseSeedOrElseRandom(this.seedEdit.getValue());
		WorldOptions worldOptions = this.settings.options();
		if (bl || bl2) {
			worldOptions = worldOptions.withBonusChest(false);
		}

		if (bl) {
			worldOptions = worldOptions.withStructures(false);
		}

		return worldOptions.withSeed(l);
	}

	public boolean isDebug() {
		return this.settings.selectedDimensions().isDebug();
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
		return this.settings.worldgenLoadContext();
	}

	public void switchToHardcore() {
		this.bonusItemsButton.active = false;
		this.bonusItemsButton.setValue(false);
	}

	public void switchOutOfHardcode() {
		this.bonusItemsButton.active = true;
		this.bonusItemsButton.setValue(this.settings.options().generateBonusChest());
	}
}
