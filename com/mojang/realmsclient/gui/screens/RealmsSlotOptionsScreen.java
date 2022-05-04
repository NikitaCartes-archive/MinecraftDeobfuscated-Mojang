/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package com.mojang.realmsclient.gui.screens;

import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.realmsclient.dto.RealmsServer;
import com.mojang.realmsclient.dto.RealmsWorldOptions;
import com.mojang.realmsclient.gui.screens.RealmsConfigureWorldScreen;
import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.ConfirmScreen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.realms.RealmsLabel;
import net.minecraft.realms.RealmsScreen;
import net.minecraft.util.Mth;
import net.minecraft.world.Difficulty;
import net.minecraft.world.level.GameType;

@Environment(value=EnvType.CLIENT)
public class RealmsSlotOptionsScreen
extends RealmsScreen {
    private static final int DEFAULT_DIFFICULTY = 2;
    public static final List<Difficulty> DIFFICULTIES = ImmutableList.of(Difficulty.PEACEFUL, Difficulty.EASY, Difficulty.NORMAL, Difficulty.HARD);
    private static final int DEFAULT_GAME_MODE = 0;
    public static final List<GameType> GAME_MODES = ImmutableList.of(GameType.SURVIVAL, GameType.CREATIVE, GameType.ADVENTURE);
    private static final Component NAME_LABEL = Component.translatable("mco.configure.world.edit.slot.name");
    static final Component SPAWN_PROTECTION_TEXT = Component.translatable("mco.configure.world.spawnProtection");
    private static final Component SPAWN_WARNING_TITLE = Component.translatable("mco.configure.world.spawn_toggle.title").withStyle(ChatFormatting.RED, ChatFormatting.BOLD);
    private EditBox nameEdit;
    protected final RealmsConfigureWorldScreen parent;
    private int column1X;
    private int columnWidth;
    private final RealmsWorldOptions options;
    private final RealmsServer.WorldType worldType;
    private Difficulty difficulty;
    private GameType gameMode;
    private final String defaultSlotName;
    private String worldName;
    private boolean pvp;
    private boolean spawnNPCs;
    private boolean spawnAnimals;
    private boolean spawnMonsters;
    int spawnProtection;
    private boolean commandBlocks;
    private boolean forceGameMode;
    SettingsSlider spawnProtectionButton;

    public RealmsSlotOptionsScreen(RealmsConfigureWorldScreen realmsConfigureWorldScreen, RealmsWorldOptions realmsWorldOptions, RealmsServer.WorldType worldType, int i) {
        super(Component.translatable("mco.configure.world.buttons.options"));
        this.parent = realmsConfigureWorldScreen;
        this.options = realmsWorldOptions;
        this.worldType = worldType;
        this.difficulty = RealmsSlotOptionsScreen.findByIndex(DIFFICULTIES, realmsWorldOptions.difficulty, 2);
        this.gameMode = RealmsSlotOptionsScreen.findByIndex(GAME_MODES, realmsWorldOptions.gameMode, 0);
        this.defaultSlotName = realmsWorldOptions.getDefaultSlotName(i);
        this.setWorldName(realmsWorldOptions.getSlotName(i));
        if (worldType == RealmsServer.WorldType.NORMAL) {
            this.pvp = realmsWorldOptions.pvp;
            this.spawnProtection = realmsWorldOptions.spawnProtection;
            this.forceGameMode = realmsWorldOptions.forceGameMode;
            this.spawnAnimals = realmsWorldOptions.spawnAnimals;
            this.spawnMonsters = realmsWorldOptions.spawnMonsters;
            this.spawnNPCs = realmsWorldOptions.spawnNPCs;
            this.commandBlocks = realmsWorldOptions.commandBlocks;
        } else {
            this.pvp = true;
            this.spawnProtection = 0;
            this.forceGameMode = false;
            this.spawnAnimals = true;
            this.spawnMonsters = true;
            this.spawnNPCs = true;
            this.commandBlocks = true;
        }
    }

    @Override
    public void removed() {
        this.minecraft.keyboardHandler.setSendRepeatsToGui(false);
    }

    @Override
    public void tick() {
        this.nameEdit.tick();
    }

    @Override
    public boolean keyPressed(int i, int j, int k) {
        if (i == 256) {
            this.minecraft.setScreen(this.parent);
            return true;
        }
        return super.keyPressed(i, j, k);
    }

    private static <T> T findByIndex(List<T> list, int i, int j) {
        try {
            return list.get(i);
        } catch (IndexOutOfBoundsException indexOutOfBoundsException) {
            return list.get(j);
        }
    }

    private static <T> int findIndex(List<T> list, T object, int i) {
        int j = list.indexOf(object);
        return j == -1 ? i : j;
    }

    @Override
    public void init() {
        this.columnWidth = 170;
        this.column1X = this.width / 2 - this.columnWidth;
        int i = this.width / 2 + 10;
        if (this.worldType != RealmsServer.WorldType.NORMAL) {
            MutableComponent component = this.worldType == RealmsServer.WorldType.ADVENTUREMAP ? Component.translatable("mco.configure.world.edit.subscreen.adventuremap") : (this.worldType == RealmsServer.WorldType.INSPIRATION ? Component.translatable("mco.configure.world.edit.subscreen.inspiration") : Component.translatable("mco.configure.world.edit.subscreen.experience"));
            this.addLabel(new RealmsLabel(component, this.width / 2, 26, 0xFF0000));
        }
        this.nameEdit = new EditBox(this.minecraft.font, this.column1X + 2, RealmsSlotOptionsScreen.row(1), this.columnWidth - 4, 20, null, Component.translatable("mco.configure.world.edit.slot.name"));
        this.nameEdit.setMaxLength(10);
        this.nameEdit.setValue(this.worldName);
        this.nameEdit.setResponder(this::setWorldName);
        this.magicalSpecialHackyFocus(this.nameEdit);
        CycleButton<Boolean> cycleButton3 = this.addRenderableWidget(CycleButton.onOffBuilder(this.pvp).create(i, RealmsSlotOptionsScreen.row(1), this.columnWidth, 20, Component.translatable("mco.configure.world.pvp"), (cycleButton, boolean_) -> {
            this.pvp = boolean_;
        }));
        this.addRenderableWidget(CycleButton.builder(GameType::getShortDisplayName).withValues((Collection<GameType>)GAME_MODES).withInitialValue(this.gameMode).create(this.column1X, RealmsSlotOptionsScreen.row(3), this.columnWidth, 20, Component.translatable("selectWorld.gameMode"), (cycleButton, gameType) -> {
            this.gameMode = gameType;
        }));
        MutableComponent component2 = Component.translatable("mco.configure.world.spawn_toggle.message");
        CycleButton<Boolean> cycleButton22 = this.addRenderableWidget(CycleButton.onOffBuilder(this.spawnAnimals).create(i, RealmsSlotOptionsScreen.row(3), this.columnWidth, 20, Component.translatable("mco.configure.world.spawnAnimals"), this.confirmDangerousOption(component2, boolean_ -> {
            this.spawnAnimals = boolean_;
        })));
        CycleButton<Boolean> cycleButton32 = CycleButton.onOffBuilder(this.difficulty != Difficulty.PEACEFUL && this.spawnMonsters).create(i, RealmsSlotOptionsScreen.row(5), this.columnWidth, 20, Component.translatable("mco.configure.world.spawnMonsters"), this.confirmDangerousOption(component2, boolean_ -> {
            this.spawnMonsters = boolean_;
        }));
        this.addRenderableWidget(CycleButton.builder(Difficulty::getDisplayName).withValues((Collection<Difficulty>)DIFFICULTIES).withInitialValue(this.difficulty).create(this.column1X, RealmsSlotOptionsScreen.row(5), this.columnWidth, 20, Component.translatable("options.difficulty"), (cycleButton2, difficulty) -> {
            this.difficulty = difficulty;
            if (this.worldType == RealmsServer.WorldType.NORMAL) {
                boolean bl;
                cycleButton.active = bl = this.difficulty != Difficulty.PEACEFUL;
                cycleButton32.setValue(bl && this.spawnMonsters);
            }
        }));
        this.addRenderableWidget(cycleButton32);
        this.spawnProtectionButton = this.addRenderableWidget(new SettingsSlider(this.column1X, RealmsSlotOptionsScreen.row(7), this.columnWidth, this.spawnProtection, 0.0f, 16.0f));
        CycleButton<Boolean> cycleButton4 = this.addRenderableWidget(CycleButton.onOffBuilder(this.spawnNPCs).create(i, RealmsSlotOptionsScreen.row(7), this.columnWidth, 20, Component.translatable("mco.configure.world.spawnNPCs"), this.confirmDangerousOption(Component.translatable("mco.configure.world.spawn_toggle.message.npc"), boolean_ -> {
            this.spawnNPCs = boolean_;
        })));
        CycleButton<Boolean> cycleButton5 = this.addRenderableWidget(CycleButton.onOffBuilder(this.forceGameMode).create(this.column1X, RealmsSlotOptionsScreen.row(9), this.columnWidth, 20, Component.translatable("mco.configure.world.forceGameMode"), (cycleButton, boolean_) -> {
            this.forceGameMode = boolean_;
        }));
        CycleButton<Boolean> cycleButton6 = this.addRenderableWidget(CycleButton.onOffBuilder(this.commandBlocks).create(i, RealmsSlotOptionsScreen.row(9), this.columnWidth, 20, Component.translatable("mco.configure.world.commandBlocks"), (cycleButton, boolean_) -> {
            this.commandBlocks = boolean_;
        }));
        if (this.worldType != RealmsServer.WorldType.NORMAL) {
            cycleButton3.active = false;
            cycleButton22.active = false;
            cycleButton4.active = false;
            cycleButton32.active = false;
            this.spawnProtectionButton.active = false;
            cycleButton6.active = false;
            cycleButton5.active = false;
        }
        if (this.difficulty == Difficulty.PEACEFUL) {
            cycleButton32.active = false;
        }
        this.addRenderableWidget(new Button(this.column1X, RealmsSlotOptionsScreen.row(13), this.columnWidth, 20, Component.translatable("mco.configure.world.buttons.done"), button -> this.saveSettings()));
        this.addRenderableWidget(new Button(i, RealmsSlotOptionsScreen.row(13), this.columnWidth, 20, CommonComponents.GUI_CANCEL, button -> this.minecraft.setScreen(this.parent)));
        this.addWidget(this.nameEdit);
    }

    private CycleButton.OnValueChange<Boolean> confirmDangerousOption(Component component, Consumer<Boolean> consumer) {
        return (cycleButton, boolean_) -> {
            if (boolean_.booleanValue()) {
                consumer.accept(true);
            } else {
                this.minecraft.setScreen(new ConfirmScreen(bl -> {
                    if (bl) {
                        consumer.accept(false);
                    }
                    this.minecraft.setScreen(this);
                }, SPAWN_WARNING_TITLE, component, CommonComponents.GUI_PROCEED, CommonComponents.GUI_CANCEL));
            }
        };
    }

    @Override
    public Component getNarrationMessage() {
        return CommonComponents.joinForNarration(this.getTitle(), this.createLabelNarration());
    }

    @Override
    public void render(PoseStack poseStack, int i, int j, float f) {
        this.renderBackground(poseStack);
        RealmsSlotOptionsScreen.drawCenteredString(poseStack, this.font, this.title, this.width / 2, 17, 0xFFFFFF);
        this.font.draw(poseStack, NAME_LABEL, (float)(this.column1X + this.columnWidth / 2 - this.font.width(NAME_LABEL) / 2), (float)(RealmsSlotOptionsScreen.row(0) - 5), 0xFFFFFF);
        this.nameEdit.render(poseStack, i, j, f);
        super.render(poseStack, i, j, f);
    }

    private void setWorldName(String string) {
        this.worldName = string.equals(this.defaultSlotName) ? "" : string;
    }

    private void saveSettings() {
        int i = RealmsSlotOptionsScreen.findIndex(DIFFICULTIES, this.difficulty, 2);
        int j = RealmsSlotOptionsScreen.findIndex(GAME_MODES, this.gameMode, 0);
        if (this.worldType == RealmsServer.WorldType.ADVENTUREMAP || this.worldType == RealmsServer.WorldType.EXPERIENCE || this.worldType == RealmsServer.WorldType.INSPIRATION) {
            this.parent.saveSlotSettings(new RealmsWorldOptions(this.options.pvp, this.options.spawnAnimals, this.options.spawnMonsters, this.options.spawnNPCs, this.options.spawnProtection, this.options.commandBlocks, i, j, this.options.forceGameMode, this.worldName));
        } else {
            boolean bl = this.worldType == RealmsServer.WorldType.NORMAL && this.difficulty != Difficulty.PEACEFUL && this.spawnMonsters;
            this.parent.saveSlotSettings(new RealmsWorldOptions(this.pvp, this.spawnAnimals, bl, this.spawnNPCs, this.spawnProtection, this.commandBlocks, i, j, this.forceGameMode, this.worldName));
        }
    }

    @Environment(value=EnvType.CLIENT)
    class SettingsSlider
    extends AbstractSliderButton {
        private final double minValue;
        private final double maxValue;

        public SettingsSlider(int i, int j, int k, int l, float f, float g) {
            super(i, j, k, 20, CommonComponents.EMPTY, 0.0);
            this.minValue = f;
            this.maxValue = g;
            this.value = (Mth.clamp((float)l, f, g) - f) / (g - f);
            this.updateMessage();
        }

        @Override
        public void applyValue() {
            if (!RealmsSlotOptionsScreen.this.spawnProtectionButton.active) {
                return;
            }
            RealmsSlotOptionsScreen.this.spawnProtection = (int)Mth.lerp(Mth.clamp(this.value, 0.0, 1.0), this.minValue, this.maxValue);
        }

        @Override
        protected void updateMessage() {
            this.setMessage(CommonComponents.optionNameValue(SPAWN_PROTECTION_TEXT, RealmsSlotOptionsScreen.this.spawnProtection == 0 ? CommonComponents.OPTION_OFF : Component.literal(String.valueOf(RealmsSlotOptionsScreen.this.spawnProtection))));
        }

        @Override
        public void onClick(double d, double e) {
        }

        @Override
        public void onRelease(double d, double e) {
        }
    }
}

