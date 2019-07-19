/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.gui.screens;

import it.unimi.dsi.fastutil.booleans.BooleanConsumer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.TranslatableComponent;

@Environment(value=EnvType.CLIENT)
public class DirectJoinServerScreen
extends Screen {
    private Button selectButton;
    private final ServerData serverData;
    private EditBox ipEdit;
    private final BooleanConsumer callback;

    public DirectJoinServerScreen(BooleanConsumer booleanConsumer, ServerData serverData) {
        super(new TranslatableComponent("selectServer.direct", new Object[0]));
        this.serverData = serverData;
        this.callback = booleanConsumer;
    }

    @Override
    public void tick() {
        this.ipEdit.tick();
    }

    @Override
    public boolean keyPressed(int i, int j, int k) {
        if (this.getFocused() == this.ipEdit && (i == 257 || i == 335)) {
            this.onSelect();
            return true;
        }
        return super.keyPressed(i, j, k);
    }

    @Override
    protected void init() {
        this.minecraft.keyboardHandler.setSendRepeatsToGui(true);
        this.selectButton = this.addButton(new Button(this.width / 2 - 100, this.height / 4 + 96 + 12, 200, 20, I18n.get("selectServer.select", new Object[0]), button -> this.onSelect()));
        this.addButton(new Button(this.width / 2 - 100, this.height / 4 + 120 + 12, 200, 20, I18n.get("gui.cancel", new Object[0]), button -> this.callback.accept(false)));
        this.ipEdit = new EditBox(this.font, this.width / 2 - 100, 116, 200, 20, I18n.get("addServer.enterIp", new Object[0]));
        this.ipEdit.setMaxLength(128);
        this.ipEdit.setFocus(true);
        this.ipEdit.setValue(this.minecraft.options.lastMpIp);
        this.ipEdit.setResponder(string -> this.updateSelectButtonStatus());
        this.children.add(this.ipEdit);
        this.setInitialFocus(this.ipEdit);
        this.updateSelectButtonStatus();
    }

    @Override
    public void resize(Minecraft minecraft, int i, int j) {
        String string = this.ipEdit.getValue();
        this.init(minecraft, i, j);
        this.ipEdit.setValue(string);
    }

    private void onSelect() {
        this.serverData.ip = this.ipEdit.getValue();
        this.callback.accept(true);
    }

    @Override
    public void removed() {
        this.minecraft.keyboardHandler.setSendRepeatsToGui(false);
        this.minecraft.options.lastMpIp = this.ipEdit.getValue();
        this.minecraft.options.save();
    }

    private void updateSelectButtonStatus() {
        this.selectButton.active = !this.ipEdit.getValue().isEmpty() && this.ipEdit.getValue().split(":").length > 0;
    }

    @Override
    public void render(int i, int j, float f) {
        this.renderBackground();
        this.drawCenteredString(this.font, this.title.getColoredString(), this.width / 2, 20, 0xFFFFFF);
        this.drawString(this.font, I18n.get("addServer.enterIp", new Object[0]), this.width / 2 - 100, 100, 0xA0A0A0);
        this.ipEdit.render(i, j, f);
        super.render(i, j, f);
    }
}

