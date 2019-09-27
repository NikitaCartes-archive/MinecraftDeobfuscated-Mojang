/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.gui.screens;

import it.unimi.dsi.fastutil.booleans.BooleanConsumer;
import java.net.IDN;
import java.util.function.Predicate;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.util.StringUtil;

@Environment(value=EnvType.CLIENT)
public class EditServerScreen
extends Screen {
    private Button addButton;
    private final BooleanConsumer callback;
    private final ServerData serverData;
    private EditBox ipEdit;
    private EditBox nameEdit;
    private Button serverPackButton;
    private final Predicate<String> addressFilter = string -> {
        if (StringUtil.isNullOrEmpty(string)) {
            return true;
        }
        String[] strings = string.split(":");
        if (strings.length == 0) {
            return true;
        }
        try {
            String string2 = IDN.toASCII(strings[0]);
            return true;
        } catch (IllegalArgumentException illegalArgumentException) {
            return false;
        }
    };

    public EditServerScreen(BooleanConsumer booleanConsumer, ServerData serverData) {
        super(new TranslatableComponent("addServer.title", new Object[0]));
        this.callback = booleanConsumer;
        this.serverData = serverData;
    }

    @Override
    public void tick() {
        this.nameEdit.tick();
        this.ipEdit.tick();
    }

    @Override
    protected void init() {
        this.minecraft.keyboardHandler.setSendRepeatsToGui(true);
        this.nameEdit = new EditBox(this.font, this.width / 2 - 100, 66, 200, 20, I18n.get("addServer.enterName", new Object[0]));
        this.nameEdit.setFocus(true);
        this.nameEdit.setValue(this.serverData.name);
        this.nameEdit.setResponder(this::onEdited);
        this.children.add(this.nameEdit);
        this.ipEdit = new EditBox(this.font, this.width / 2 - 100, 106, 200, 20, I18n.get("addServer.enterIp", new Object[0]));
        this.ipEdit.setMaxLength(128);
        this.ipEdit.setValue(this.serverData.ip);
        this.ipEdit.setFilter(this.addressFilter);
        this.ipEdit.setResponder(this::onEdited);
        this.children.add(this.ipEdit);
        this.serverPackButton = this.addButton(new Button(this.width / 2 - 100, this.height / 4 + 72, 200, 20, I18n.get("addServer.resourcePack", new Object[0]) + ": " + this.serverData.getResourcePackStatus().getName().getColoredString(), button -> {
            this.serverData.setResourcePackStatus(ServerData.ServerPackStatus.values()[(this.serverData.getResourcePackStatus().ordinal() + 1) % ServerData.ServerPackStatus.values().length]);
            this.serverPackButton.setMessage(I18n.get("addServer.resourcePack", new Object[0]) + ": " + this.serverData.getResourcePackStatus().getName().getColoredString());
        }));
        this.addButton = this.addButton(new Button(this.width / 2 - 100, this.height / 4 + 96 + 18, 200, 20, I18n.get("addServer.add", new Object[0]), button -> this.onAdd()));
        this.addButton(new Button(this.width / 2 - 100, this.height / 4 + 120 + 18, 200, 20, I18n.get("gui.cancel", new Object[0]), button -> this.callback.accept(false)));
        this.onClose();
    }

    @Override
    public void resize(Minecraft minecraft, int i, int j) {
        String string = this.ipEdit.getValue();
        String string2 = this.nameEdit.getValue();
        this.init(minecraft, i, j);
        this.ipEdit.setValue(string);
        this.nameEdit.setValue(string2);
    }

    private void onEdited(String string) {
        this.onClose();
    }

    @Override
    public void removed() {
        this.minecraft.keyboardHandler.setSendRepeatsToGui(false);
    }

    private void onAdd() {
        this.serverData.name = this.nameEdit.getValue();
        this.serverData.ip = this.ipEdit.getValue();
        this.callback.accept(true);
    }

    @Override
    public void onClose() {
        String string = this.ipEdit.getValue();
        boolean bl = !string.isEmpty() && string.split(":").length > 0 && string.indexOf(32) == -1;
        this.addButton.active = bl && !this.nameEdit.getValue().isEmpty();
    }

    @Override
    public void render(int i, int j, float f) {
        this.renderBackground();
        this.drawCenteredString(this.font, this.title.getColoredString(), this.width / 2, 17, 0xFFFFFF);
        this.drawString(this.font, I18n.get("addServer.enterName", new Object[0]), this.width / 2 - 100, 53, 0xA0A0A0);
        this.drawString(this.font, I18n.get("addServer.enterIp", new Object[0]), this.width / 2 - 100, 94, 0xA0A0A0);
        this.nameEdit.render(i, j, f);
        this.ipEdit.render(i, j, f);
        super.render(i, j, f);
    }
}

