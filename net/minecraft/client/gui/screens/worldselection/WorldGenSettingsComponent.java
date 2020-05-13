/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
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
import net.minecraft.client.gui.screens.worldselection.CreateWorldScreen;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.level.levelgen.WorldGenSettings;
import net.minecraft.world.level.levelgen.flat.FlatLevelGeneratorSettings;
import org.apache.commons.lang3.StringUtils;

@Environment(value=EnvType.CLIENT)
public class WorldGenSettingsComponent
implements TickableWidget,
Widget {
    private static final Map<WorldGenSettings.Preset, PresetEditor> EDITORS = ImmutableMap.of(WorldGenSettings.Preset.FLAT, (createWorldScreen, worldGenSettings) -> new CreateFlatWorldScreen(createWorldScreen, flatLevelGeneratorSettings -> createWorldScreen.worldGenSettingsComponent.updateSettings(worldGenSettings.fromFlatSettings((FlatLevelGeneratorSettings)flatLevelGeneratorSettings)), worldGenSettings.parseFlatSettings()), WorldGenSettings.Preset.BUFFET, (createWorldScreen, worldGenSettings) -> new CreateBuffetWorldScreen(createWorldScreen, pair -> createWorldScreen.worldGenSettingsComponent.updateSettings(worldGenSettings.fromBuffetSettings((WorldGenSettings.BuffetGeneratorType)((Object)((Object)((Object)pair.getFirst()))), (Set)pair.getSecond())), worldGenSettings.parseBuffetSettings()));
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

    public void init(final CreateWorldScreen createWorldScreen, Minecraft minecraft, Font font) {
        this.font = font;
        this.width = createWorldScreen.width;
        this.seedEdit = new EditBox(this.font, this.width / 2 - 100, 60, 200, 20, new TranslatableComponent("selectWorld.enterSeed"));
        this.seedEdit.setValue(this.initSeed);
        this.seedEdit.setResponder(string -> {
            this.initSeed = this.seedEdit.getValue();
        });
        createWorldScreen.addWidget(this.seedEdit);
        this.featuresButton = createWorldScreen.addButton(new Button(this.width / 2 - 155, 100, 150, 20, new TranslatableComponent("selectWorld.mapFeatures"), button -> {
            this.settings = this.settings.withFeaturesToggled();
            button.queueNarration(250);
        }){

            @Override
            public Component getMessage() {
                return super.getMessage().mutableCopy().append(" ").append(CommonComponents.optionStatus(WorldGenSettingsComponent.this.settings.generateFeatures()));
            }

            @Override
            protected MutableComponent createNarrationMessage() {
                return super.createNarrationMessage().append(". ").append(new TranslatableComponent("selectWorld.mapFeatures.info"));
            }
        });
        this.featuresButton.visible = false;
        this.typeButton = createWorldScreen.addButton(new Button(this.width / 2 + 5, 100, 150, 20, new TranslatableComponent("selectWorld.mapType"), button -> {
            do {
                ++this.presetIndex;
                if (this.presetIndex >= WorldGenSettings.Preset.PRESETS.size()) {
                    this.presetIndex = 0;
                }
                this.settings = this.settings.withPreset(WorldGenSettings.Preset.PRESETS.get(this.presetIndex));
            } while (this.settings.isDebug() && !Screen.hasShiftDown());
            createWorldScreen.updateDisplayOptions();
            button.queueNarration(250);
        }){

            @Override
            public Component getMessage() {
                return super.getMessage().mutableCopy().append(" ").append(WorldGenSettingsComponent.this.settings.preset().description());
            }

            @Override
            protected MutableComponent createNarrationMessage() {
                if (WorldGenSettingsComponent.this.settings.preset() == WorldGenSettings.Preset.AMPLIFIED) {
                    return super.createNarrationMessage().append(". ").append(AMPLIFIED_HELP_TEXT);
                }
                return super.createNarrationMessage();
            }
        });
        this.typeButton.visible = false;
        this.customizeTypeButton = createWorldScreen.addButton(new Button(createWorldScreen.width / 2 + 5, 120, 150, 20, new TranslatableComponent("selectWorld.customizeType"), button -> {
            PresetEditor presetEditor = EDITORS.get(this.settings.preset());
            if (presetEditor != null) {
                minecraft.setScreen(presetEditor.createEditScreen(createWorldScreen, this.settings));
            }
        }));
        this.customizeTypeButton.visible = false;
        this.bonusItemsButton = createWorldScreen.addButton(new Button(createWorldScreen.width / 2 + 5, 151, 150, 20, new TranslatableComponent("selectWorld.bonusItems"), button -> {
            this.settings = this.settings.withBonusChestToggled();
            button.queueNarration(250);
        }){

            @Override
            public Component getMessage() {
                return super.getMessage().mutableCopy().append(" ").append(CommonComponents.optionStatus(WorldGenSettingsComponent.this.settings.generateBonusChest() && !createWorldScreen.hardCore));
            }
        });
        this.bonusItemsButton.visible = false;
    }

    @Override
    public void tick() {
        this.seedEdit.tick();
    }

    @Override
    public void render(PoseStack poseStack, int i, int j, float f) {
        if (this.featuresButton.visible) {
            this.font.drawShadow(poseStack, I18n.get("selectWorld.mapFeatures.info", new Object[0]), (float)(this.width / 2 - 150), 122.0f, -6250336);
        }
        this.seedEdit.render(poseStack, i, j, f);
        if (this.settings.preset() == WorldGenSettings.Preset.AMPLIFIED) {
            this.font.drawWordWrap(AMPLIFIED_HELP_TEXT, this.typeButton.x + 2, this.typeButton.y + 22, this.typeButton.getWidth(), 0xA0A0A0);
        }
    }

    private void updateSettings(WorldGenSettings worldGenSettings) {
        this.settings = worldGenSettings;
    }

    private static OptionalLong parseLong(String string) {
        try {
            return OptionalLong.of(Long.parseLong(string));
        } catch (NumberFormatException numberFormatException) {
            return OptionalLong.empty();
        }
    }

    public WorldGenSettings makeSettings(boolean bl) {
        OptionalLong optionalLong2;
        String string = this.seedEdit.getValue();
        OptionalLong optionalLong = StringUtils.isEmpty(string) ? OptionalLong.empty() : ((optionalLong2 = WorldGenSettingsComponent.parseLong(string)).isPresent() && optionalLong2.getAsLong() == 0L ? OptionalLong.empty() : optionalLong2);
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

    @Environment(value=EnvType.CLIENT)
    public static interface PresetEditor {
        public Screen createEditScreen(CreateWorldScreen var1, WorldGenSettings var2);
    }
}

