/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package com.mojang.realmsclient.gui.screens;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.realmsclient.util.LevelType;
import com.mojang.realmsclient.util.WorldGenerationInfo;
import java.util.function.Consumer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.realms.RealmsScreen;

@Environment(value=EnvType.CLIENT)
public class RealmsResetNormalWorldScreen
extends RealmsScreen {
    private static final Component SEED_LABEL = Component.translatable("mco.reset.world.seed");
    private final Consumer<WorldGenerationInfo> callback;
    private EditBox seedEdit;
    private LevelType levelType = LevelType.DEFAULT;
    private boolean generateStructures = true;
    private final Component buttonTitle;

    public RealmsResetNormalWorldScreen(Consumer<WorldGenerationInfo> consumer, Component component) {
        super(Component.translatable("mco.reset.world.generate"));
        this.callback = consumer;
        this.buttonTitle = component;
    }

    @Override
    public void tick() {
        this.seedEdit.tick();
        super.tick();
    }

    @Override
    public void init() {
        this.seedEdit = new EditBox(this.minecraft.font, this.width / 2 - 100, RealmsResetNormalWorldScreen.row(2), 200, 20, null, Component.translatable("mco.reset.world.seed"));
        this.seedEdit.setMaxLength(32);
        this.addWidget(this.seedEdit);
        this.setInitialFocus(this.seedEdit);
        this.addRenderableWidget(CycleButton.builder(LevelType::getName).withValues((LevelType[])LevelType.values()).withInitialValue(this.levelType).create(this.width / 2 - 102, RealmsResetNormalWorldScreen.row(4), 205, 20, Component.translatable("selectWorld.mapType"), (cycleButton, levelType) -> {
            this.levelType = levelType;
        }));
        this.addRenderableWidget(CycleButton.onOffBuilder(this.generateStructures).create(this.width / 2 - 102, RealmsResetNormalWorldScreen.row(6) - 2, 205, 20, Component.translatable("selectWorld.mapFeatures"), (cycleButton, boolean_) -> {
            this.generateStructures = boolean_;
        }));
        this.addRenderableWidget(Button.builder(this.buttonTitle, button -> this.callback.accept(new WorldGenerationInfo(this.seedEdit.getValue(), this.levelType, this.generateStructures))).bounds(this.width / 2 - 102, RealmsResetNormalWorldScreen.row(12), 97, 20).build());
        this.addRenderableWidget(Button.builder(CommonComponents.GUI_BACK, button -> this.onClose()).bounds(this.width / 2 + 8, RealmsResetNormalWorldScreen.row(12), 97, 20).build());
    }

    @Override
    public void onClose() {
        this.callback.accept(null);
    }

    @Override
    public void render(PoseStack poseStack, int i, int j, float f) {
        this.renderBackground(poseStack);
        RealmsResetNormalWorldScreen.drawCenteredString(poseStack, this.font, this.title, this.width / 2, 17, 0xFFFFFF);
        this.font.draw(poseStack, SEED_LABEL, (float)(this.width / 2 - 100), (float)RealmsResetNormalWorldScreen.row(1), 0xA0A0A0);
        this.seedEdit.render(poseStack, i, j, f);
        super.render(poseStack, i, j, f);
    }
}

