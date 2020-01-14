/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.gui.screens.multiplayer;

import com.google.common.collect.Lists;
import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.chat.NarratorChatListener;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Checkbox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.multiplayer.JoinMultiplayerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;

@Environment(value=EnvType.CLIENT)
public class SafetyScreen
extends Screen {
    private final Screen previous;
    private final Component title = new TranslatableComponent("multiplayerWarning.header", new Object[0]).withStyle(ChatFormatting.BOLD);
    private final Component content = new TranslatableComponent("multiplayerWarning.message", new Object[0]);
    private final Component check = new TranslatableComponent("multiplayerWarning.check", new Object[0]);
    private final Component proceed = new TranslatableComponent("gui.proceed", new Object[0]);
    private final Component back = new TranslatableComponent("gui.back", new Object[0]);
    private Checkbox stopShowing;
    private final List<String> lines = Lists.newArrayList();

    public SafetyScreen(Screen screen) {
        super(NarratorChatListener.NO_TITLE);
        this.previous = screen;
    }

    @Override
    protected void init() {
        super.init();
        this.lines.clear();
        this.lines.addAll(this.font.split(this.content.getColoredString(), this.width - 50));
        int i = (this.lines.size() + 1) * this.font.lineHeight;
        this.addButton(new Button(this.width / 2 - 155, 100 + i, 150, 20, this.proceed.getColoredString(), button -> {
            if (this.stopShowing.selected()) {
                this.minecraft.options.skipMultiplayerWarning = true;
                this.minecraft.options.save();
            }
            this.minecraft.setScreen(new JoinMultiplayerScreen(this.previous));
        }));
        this.addButton(new Button(this.width / 2 - 155 + 160, 100 + i, 150, 20, this.back.getColoredString(), button -> this.minecraft.setScreen(this.previous)));
        this.stopShowing = new Checkbox(this.width / 2 - 155 + 80, 76 + i, 150, 20, this.check.getColoredString(), false);
        this.addButton(this.stopShowing);
    }

    @Override
    public String getNarrationMessage() {
        return this.title.getString() + "\n" + this.content.getString();
    }

    @Override
    public void render(int i, int j, float f) {
        this.renderDirtBackground(0);
        this.drawCenteredString(this.font, this.title.getColoredString(), this.width / 2, 30, 0xFFFFFF);
        int k = 70;
        for (String string : this.lines) {
            this.drawCenteredString(this.font, string, this.width / 2, k, 0xFFFFFF);
            k += this.font.lineHeight;
        }
        super.render(i, j, f);
    }
}

