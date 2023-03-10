/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package com.mojang.realmsclient.gui.screens;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.realmsclient.RealmsMainScreen;
import com.mojang.realmsclient.dto.RealmsServer;
import com.mojang.realmsclient.gui.screens.RealmsLongRunningMcoTaskScreen;
import com.mojang.realmsclient.gui.screens.RealmsResetWorldScreen;
import com.mojang.realmsclient.util.task.WorldCreationTask;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.realms.RealmsScreen;

@Environment(value=EnvType.CLIENT)
public class RealmsCreateRealmScreen
extends RealmsScreen {
    private static final Component NAME_LABEL = Component.translatable("mco.configure.world.name");
    private static final Component DESCRIPTION_LABEL = Component.translatable("mco.configure.world.description");
    private final RealmsServer server;
    private final RealmsMainScreen lastScreen;
    private EditBox nameBox;
    private EditBox descriptionBox;
    private Button createButton;

    public RealmsCreateRealmScreen(RealmsServer realmsServer, RealmsMainScreen realmsMainScreen) {
        super(Component.translatable("mco.selectServer.create"));
        this.server = realmsServer;
        this.lastScreen = realmsMainScreen;
    }

    @Override
    public void tick() {
        if (this.nameBox != null) {
            this.nameBox.tick();
        }
        if (this.descriptionBox != null) {
            this.descriptionBox.tick();
        }
    }

    @Override
    public void init() {
        this.createButton = this.addRenderableWidget(Button.builder(Component.translatable("mco.create.world"), button -> this.createWorld()).bounds(this.width / 2 - 100, this.height / 4 + 120 + 17, 97, 20).build());
        this.addRenderableWidget(Button.builder(CommonComponents.GUI_CANCEL, button -> this.minecraft.setScreen(this.lastScreen)).bounds(this.width / 2 + 5, this.height / 4 + 120 + 17, 95, 20).build());
        this.createButton.active = false;
        this.nameBox = new EditBox(this.minecraft.font, this.width / 2 - 100, 65, 200, 20, null, Component.translatable("mco.configure.world.name"));
        this.addWidget(this.nameBox);
        this.setInitialFocus(this.nameBox);
        this.descriptionBox = new EditBox(this.minecraft.font, this.width / 2 - 100, 115, 200, 20, null, Component.translatable("mco.configure.world.description"));
        this.addWidget(this.descriptionBox);
    }

    @Override
    public boolean charTyped(char c, int i) {
        boolean bl = super.charTyped(c, i);
        this.createButton.active = this.valid();
        return bl;
    }

    @Override
    public boolean keyPressed(int i, int j, int k) {
        if (i == 256) {
            this.minecraft.setScreen(this.lastScreen);
            return true;
        }
        boolean bl = super.keyPressed(i, j, k);
        this.createButton.active = this.valid();
        return bl;
    }

    private void createWorld() {
        if (this.valid()) {
            RealmsResetWorldScreen realmsResetWorldScreen = new RealmsResetWorldScreen(this.lastScreen, this.server, Component.translatable("mco.selectServer.create"), Component.translatable("mco.create.world.subtitle"), 0xA0A0A0, Component.translatable("mco.create.world.skip"), () -> this.minecraft.execute(() -> this.minecraft.setScreen(this.lastScreen.newScreen())), () -> this.minecraft.setScreen(this.lastScreen.newScreen()));
            realmsResetWorldScreen.setResetTitle(Component.translatable("mco.create.world.reset.title"));
            this.minecraft.setScreen(new RealmsLongRunningMcoTaskScreen(this.lastScreen, new WorldCreationTask(this.server.id, this.nameBox.getValue(), this.descriptionBox.getValue(), realmsResetWorldScreen)));
        }
    }

    private boolean valid() {
        return !this.nameBox.getValue().trim().isEmpty();
    }

    @Override
    public void render(PoseStack poseStack, int i, int j, float f) {
        this.renderBackground(poseStack);
        RealmsCreateRealmScreen.drawCenteredString(poseStack, this.font, this.title, this.width / 2, 11, 0xFFFFFF);
        this.font.draw(poseStack, NAME_LABEL, (float)(this.width / 2 - 100), 52.0f, 0xA0A0A0);
        this.font.draw(poseStack, DESCRIPTION_LABEL, (float)(this.width / 2 - 100), 102.0f, 0xA0A0A0);
        if (this.nameBox != null) {
            this.nameBox.render(poseStack, i, j, f);
        }
        if (this.descriptionBox != null) {
            this.descriptionBox.render(poseStack, i, j, f);
        }
        super.render(poseStack, i, j, f);
    }
}

