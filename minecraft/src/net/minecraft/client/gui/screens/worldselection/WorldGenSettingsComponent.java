package net.minecraft.client.gui.screens.worldselection;

import com.google.common.collect.ImmutableMap;
import com.mojang.blaze3d.vertex.PoseStack;
import java.util.Map;
import java.util.OptionalLong;
import java.util.Set;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.TickableWidget;
import net.minecraft.client.gui.components.Widget;
import net.minecraft.client.gui.screens.CreateBuffetWorldScreen;
import net.minecraft.client.gui.screens.CreateFlatWorldScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.WorldGenSettings;
import org.apache.commons.lang3.StringUtils;

@Environment(EnvType.CLIENT)
public class WorldGenSettingsComponent implements TickableWidget, Widget {
	private static final Map<WorldGenSettings.Preset, WorldGenSettingsComponent.PresetEditor> EDITORS = ImmutableMap.of(
		WorldGenSettings.Preset.FLAT,
		(createWorldScreen, worldGenSettings) -> new CreateFlatWorldScreen(
				createWorldScreen,
				flatLevelGeneratorSettings -> createWorldScreen.worldGenSettingsComponent.updateSettings(worldGenSettings.fromFlatSettings(flatLevelGeneratorSettings)),
				worldGenSettings.parseFlatSettings()
			),
		WorldGenSettings.Preset.BUFFET,
		(createWorldScreen, worldGenSettings) -> new CreateBuffetWorldScreen(
				createWorldScreen,
				pair -> createWorldScreen.worldGenSettingsComponent
						.updateSettings(worldGenSettings.fromBuffetSettings((WorldGenSettings.BuffetGeneratorType)pair.getFirst(), (Set<Biome>)pair.getSecond())),
				worldGenSettings.parseBuffetSettings()
			)
	);
	private static final Component AMPLIFIED_HELP_TEXT = new TranslatableComponent("generator.amplified.info");
	private Font font;
	private int width;
	private EditBox seedEdit;
	private Button featuresButton;
	public Button bonusItemsButton;
	private Button typeButton;
	private Button customizeTypeButton;
	private WorldGenSettings settings;
	private int presetIndex;
	private String initSeed;

	public WorldGenSettingsComponent() {
		this(WorldGenSettings.makeDefault(), "");
	}

	public WorldGenSettingsComponent(WorldGenSettings worldGenSettings) {
		this(worldGenSettings, Long.toString(worldGenSettings.seed()));
	}

	private WorldGenSettingsComponent(WorldGenSettings worldGenSettings, String string) {
		this.settings = worldGenSettings;
		this.presetIndex = WorldGenSettings.Preset.PRESETS.indexOf(worldGenSettings.preset());
		this.initSeed = string;
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
				do {
					this.presetIndex++;
					if (this.presetIndex >= WorldGenSettings.Preset.PRESETS.size()) {
						this.presetIndex = 0;
					}

					this.settings = this.settings.withPreset((WorldGenSettings.Preset)WorldGenSettings.Preset.PRESETS.get(this.presetIndex));
				} while (this.settings.isDebug() && !Screen.hasShiftDown());

				createWorldScreen.updateDisplayOptions();
				button.queueNarration(250);
			}) {
				@Override
				public Component getMessage() {
					return super.getMessage().mutableCopy().append(" ").append(WorldGenSettingsComponent.this.settings.preset().description());
				}

				@Override
				protected MutableComponent createNarrationMessage() {
					return WorldGenSettingsComponent.this.settings.preset() == WorldGenSettings.Preset.AMPLIFIED
						? super.createNarrationMessage().append(". ").append(WorldGenSettingsComponent.AMPLIFIED_HELP_TEXT)
						: super.createNarrationMessage();
				}
			}
		);
		this.typeButton.visible = false;
		this.customizeTypeButton = createWorldScreen.addButton(
			new Button(createWorldScreen.width / 2 + 5, 120, 150, 20, new TranslatableComponent("selectWorld.customizeType"), button -> {
				WorldGenSettingsComponent.PresetEditor presetEditor = (WorldGenSettingsComponent.PresetEditor)EDITORS.get(this.settings.preset());
				if (presetEditor != null) {
					minecraft.setScreen(presetEditor.createEditScreen(createWorldScreen, this.settings));
				}
			})
		);
		this.customizeTypeButton.visible = false;
		this.bonusItemsButton = createWorldScreen.addButton(
			new Button(createWorldScreen.width / 2 + 5, 151, 150, 20, new TranslatableComponent("selectWorld.bonusItems"), button -> {
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
		if (this.settings.preset() == WorldGenSettings.Preset.AMPLIFIED) {
			this.font.drawWordWrap(AMPLIFIED_HELP_TEXT, this.typeButton.x + 2, this.typeButton.y + 22, this.typeButton.getWidth(), 10526880);
		}
	}

	private void updateSettings(WorldGenSettings worldGenSettings) {
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
			if (optionalLong2.isPresent() && optionalLong2.getAsLong() == 0L) {
				optionalLong = OptionalLong.empty();
			} else {
				optionalLong = optionalLong2;
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
		} else {
			this.featuresButton.visible = bl;
			this.bonusItemsButton.visible = bl;
			this.customizeTypeButton.visible = bl && EDITORS.containsKey(this.settings.preset());
		}

		this.seedEdit.setVisible(bl);
	}

	@Environment(EnvType.CLIENT)
	public interface PresetEditor {
		Screen createEditScreen(CreateWorldScreen createWorldScreen, WorldGenSettings worldGenSettings);
	}
}
