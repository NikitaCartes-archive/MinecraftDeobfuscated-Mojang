/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.gui.screens;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.util.HttpUtil;
import net.minecraft.world.level.GameType;

@Environment(value=EnvType.CLIENT)
public class ShareToLanScreen
extends Screen {
    private final Screen lastScreen;
    private Button commandsButton;
    private Button modeButton;
    private String gameModeName = "survival";
    private boolean commands;

    public ShareToLanScreen(Screen screen) {
        super(new TranslatableComponent("lanServer.title", new Object[0]));
        this.lastScreen = screen;
    }

    @Override
    protected void init() {
        this.addButton(new Button(this.width / 2 - 155, this.height - 28, 150, 20, I18n.get("lanServer.start", new Object[0]), button -> {
            this.minecraft.setScreen(null);
            int i = HttpUtil.getAvailablePort();
            TranslatableComponent component = this.minecraft.getSingleplayerServer().publishServer(GameType.byName(this.gameModeName), this.commands, i) ? new TranslatableComponent("commands.publish.started", i) : new TranslatableComponent("commands.publish.failed", new Object[0]);
            this.minecraft.gui.getChat().addMessage(component);
        }));
        this.addButton(new Button(this.width / 2 + 5, this.height - 28, 150, 20, I18n.get("gui.cancel", new Object[0]), button -> this.minecraft.setScreen(this.lastScreen)));
        this.modeButton = this.addButton(new Button(this.width / 2 - 155, 100, 150, 20, I18n.get("selectWorld.gameMode", new Object[0]), button -> {
            this.gameModeName = "spectator".equals(this.gameModeName) ? "creative" : ("creative".equals(this.gameModeName) ? "adventure" : ("adventure".equals(this.gameModeName) ? "survival" : "spectator"));
            this.updateSelectionStrings();
        }));
        this.commandsButton = this.addButton(new Button(this.width / 2 + 5, 100, 150, 20, I18n.get("selectWorld.allowCommands", new Object[0]), button -> {
            this.commands = !this.commands;
            this.updateSelectionStrings();
        }));
        this.updateSelectionStrings();
    }

    private void updateSelectionStrings() {
        this.modeButton.setMessage(I18n.get("selectWorld.gameMode", new Object[0]) + ": " + I18n.get("selectWorld.gameMode." + this.gameModeName, new Object[0]));
        this.commandsButton.setMessage(I18n.get("selectWorld.allowCommands", new Object[0]) + ' ' + I18n.get(this.commands ? "options.on" : "options.off", new Object[0]));
    }

    @Override
    public void render(int i, int j, float f) {
        this.renderBackground();
        this.drawCenteredString(this.font, this.title.getColoredString(), this.width / 2, 50, 0xFFFFFF);
        this.drawCenteredString(this.font, I18n.get("lanServer.otherPlayers", new Object[0]), this.width / 2, 82, 0xFFFFFF);
        super.render(i, j, f);
    }
}

