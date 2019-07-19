/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.gui.screens;

import com.google.common.collect.Lists;
import it.unimi.dsi.fastutil.booleans.BooleanConsumer;
import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;

@Environment(value=EnvType.CLIENT)
public class ConfirmScreen
extends Screen {
    private final Component title2;
    private final List<String> lines = Lists.newArrayList();
    protected String yesButton;
    protected String noButton;
    private int delayTicker;
    protected final BooleanConsumer callback;

    public ConfirmScreen(BooleanConsumer booleanConsumer, Component component, Component component2) {
        this(booleanConsumer, component, component2, I18n.get("gui.yes", new Object[0]), I18n.get("gui.no", new Object[0]));
    }

    public ConfirmScreen(BooleanConsumer booleanConsumer, Component component, Component component2, String string, String string2) {
        super(component);
        this.callback = booleanConsumer;
        this.title2 = component2;
        this.yesButton = string;
        this.noButton = string2;
    }

    @Override
    public String getNarrationMessage() {
        return super.getNarrationMessage() + ". " + this.title2.getString();
    }

    @Override
    protected void init() {
        super.init();
        this.addButton(new Button(this.width / 2 - 155, this.height / 6 + 96, 150, 20, this.yesButton, button -> this.callback.accept(true)));
        this.addButton(new Button(this.width / 2 - 155 + 160, this.height / 6 + 96, 150, 20, this.noButton, button -> this.callback.accept(false)));
        this.lines.clear();
        this.lines.addAll(this.font.split(this.title2.getColoredString(), this.width - 50));
    }

    @Override
    public void render(int i, int j, float f) {
        this.renderBackground();
        this.drawCenteredString(this.font, this.title.getColoredString(), this.width / 2, 70, 0xFFFFFF);
        int k = 90;
        for (String string : this.lines) {
            this.drawCenteredString(this.font, string, this.width / 2, k, 0xFFFFFF);
            k += this.font.lineHeight;
        }
        super.render(i, j, f);
    }

    public void setDelay(int i) {
        this.delayTicker = i;
        for (AbstractWidget abstractWidget : this.buttons) {
            abstractWidget.active = false;
        }
    }

    @Override
    public void tick() {
        super.tick();
        if (--this.delayTicker == 0) {
            for (AbstractWidget abstractWidget : this.buttons) {
                abstractWidget.active = true;
            }
        }
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return false;
    }

    @Override
    public boolean keyPressed(int i, int j, int k) {
        if (i == 256) {
            this.callback.accept(false);
            return true;
        }
        return super.keyPressed(i, j, k);
    }
}

