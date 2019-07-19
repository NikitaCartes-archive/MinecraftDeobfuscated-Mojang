/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.gui.screens;

import com.mojang.blaze3d.platform.GlStateManager;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ComponentRenderUtils;
import net.minecraft.client.gui.screens.ConfirmScreen;
import net.minecraft.client.gui.screens.GenericDirtMessageScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import org.jetbrains.annotations.Nullable;

@Environment(value=EnvType.CLIENT)
public class DeathScreen
extends Screen {
    private int delayTicker;
    private final Component causeOfDeath;
    private final boolean hardcore;

    public DeathScreen(@Nullable Component component, boolean bl) {
        super(new TranslatableComponent(bl ? "deathScreen.title.hardcore" : "deathScreen.title", new Object[0]));
        this.causeOfDeath = component;
        this.hardcore = bl;
    }

    @Override
    protected void init() {
        String string2;
        String string;
        this.delayTicker = 0;
        if (this.hardcore) {
            string = I18n.get("deathScreen.spectate", new Object[0]);
            string2 = I18n.get("deathScreen." + (this.minecraft.isLocalServer() ? "deleteWorld" : "leaveServer"), new Object[0]);
        } else {
            string = I18n.get("deathScreen.respawn", new Object[0]);
            string2 = I18n.get("deathScreen.titleScreen", new Object[0]);
        }
        this.addButton(new Button(this.width / 2 - 100, this.height / 4 + 72, 200, 20, string, button -> {
            this.minecraft.player.respawn();
            this.minecraft.setScreen(null);
        }));
        Button button2 = this.addButton(new Button(this.width / 2 - 100, this.height / 4 + 96, 200, 20, string2, button -> {
            if (this.hardcore) {
                this.minecraft.setScreen(new TitleScreen());
                return;
            }
            ConfirmScreen confirmScreen = new ConfirmScreen(this::confirmResult, new TranslatableComponent("deathScreen.quit.confirm", new Object[0]), new TextComponent(""), I18n.get("deathScreen.titleScreen", new Object[0]), I18n.get("deathScreen.respawn", new Object[0]));
            this.minecraft.setScreen(confirmScreen);
            confirmScreen.setDelay(20);
        }));
        if (!this.hardcore && this.minecraft.getUser() == null) {
            button2.active = false;
        }
        for (AbstractWidget abstractWidget : this.buttons) {
            abstractWidget.active = false;
        }
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return false;
    }

    private void confirmResult(boolean bl) {
        if (bl) {
            if (this.minecraft.level != null) {
                this.minecraft.level.disconnect();
            }
            this.minecraft.clearLevel(new GenericDirtMessageScreen(new TranslatableComponent("menu.savingLevel", new Object[0])));
            this.minecraft.setScreen(new TitleScreen());
        } else {
            this.minecraft.player.respawn();
            this.minecraft.setScreen(null);
        }
    }

    @Override
    public void render(int i, int j, float f) {
        Component component;
        this.fillGradient(0, 0, this.width, this.height, 0x60500000, -1602211792);
        GlStateManager.pushMatrix();
        GlStateManager.scalef(2.0f, 2.0f, 2.0f);
        this.drawCenteredString(this.font, this.title.getColoredString(), this.width / 2 / 2, 30, 0xFFFFFF);
        GlStateManager.popMatrix();
        if (this.causeOfDeath != null) {
            this.drawCenteredString(this.font, this.causeOfDeath.getColoredString(), this.width / 2, 85, 0xFFFFFF);
        }
        this.drawCenteredString(this.font, I18n.get("deathScreen.score", new Object[0]) + ": " + (Object)((Object)ChatFormatting.YELLOW) + this.minecraft.player.getScore(), this.width / 2, 100, 0xFFFFFF);
        if (this.causeOfDeath != null && j > 85 && j < 85 + this.font.lineHeight && (component = this.getClickedComponentAt(i)) != null && component.getStyle().getHoverEvent() != null) {
            this.renderComponentHoverEffect(component, i, j);
        }
        super.render(i, j, f);
    }

    @Nullable
    public Component getClickedComponentAt(int i) {
        if (this.causeOfDeath == null) {
            return null;
        }
        int j = this.minecraft.font.width(this.causeOfDeath.getColoredString());
        int k = this.width / 2 - j / 2;
        int l = this.width / 2 + j / 2;
        int m = k;
        if (i < k || i > l) {
            return null;
        }
        for (Component component : this.causeOfDeath) {
            if ((m += this.minecraft.font.width(ComponentRenderUtils.stripColor(component.getContents(), false))) <= i) continue;
            return component;
        }
        return null;
    }

    @Override
    public boolean mouseClicked(double d, double e, int i) {
        Component component;
        if (this.causeOfDeath != null && e > 85.0 && e < (double)(85 + this.font.lineHeight) && (component = this.getClickedComponentAt((int)d)) != null && component.getStyle().getClickEvent() != null && component.getStyle().getClickEvent().getAction() == ClickEvent.Action.OPEN_URL) {
            this.handleComponentClicked(component);
            return false;
        }
        return super.mouseClicked(d, e, i);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public void tick() {
        super.tick();
        ++this.delayTicker;
        if (this.delayTicker == 20) {
            for (AbstractWidget abstractWidget : this.buttons) {
                abstractWidget.active = true;
            }
        }
    }
}

