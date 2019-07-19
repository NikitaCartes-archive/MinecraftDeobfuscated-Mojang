/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.realms;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.chat.NarratorChatListener;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.realms.AbstractRealmsButton;
import net.minecraft.realms.Realms;
import net.minecraft.realms.RealmsAbstractButtonProxy;
import net.minecraft.realms.RealmsButton;
import net.minecraft.realms.RealmsGuiEventListener;
import net.minecraft.realms.RealmsLabelProxy;
import net.minecraft.realms.RealmsScreen;
import net.minecraft.world.item.ItemStack;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Environment(value=EnvType.CLIENT)
public class RealmsScreenProxy
extends Screen {
    private final RealmsScreen screen;
    private static final Logger LOGGER = LogManager.getLogger();

    public RealmsScreenProxy(RealmsScreen realmsScreen) {
        super(NarratorChatListener.NO_TITLE);
        this.screen = realmsScreen;
    }

    public RealmsScreen getScreen() {
        return this.screen;
    }

    @Override
    public void init(Minecraft minecraft, int i, int j) {
        this.screen.init(minecraft, i, j);
        super.init(minecraft, i, j);
    }

    @Override
    public void init() {
        this.screen.init();
        super.init();
    }

    public void drawCenteredString(String string, int i, int j, int k) {
        super.drawCenteredString(this.font, string, i, j, k);
    }

    public void drawString(String string, int i, int j, int k, boolean bl) {
        if (bl) {
            super.drawString(this.font, string, i, j, k);
        } else {
            this.font.draw(string, i, j, k);
        }
    }

    @Override
    public void blit(int i, int j, int k, int l, int m, int n) {
        this.screen.blit(i, j, k, l, m, n);
        super.blit(i, j, k, l, m, n);
    }

    public static void blit(int i, int j, float f, float g, int k, int l, int m, int n, int o, int p) {
        GuiComponent.blit(i, j, m, n, f, g, k, l, o, p);
    }

    public static void blit(int i, int j, float f, float g, int k, int l, int m, int n) {
        GuiComponent.blit(i, j, f, g, k, l, m, n);
    }

    @Override
    public void fillGradient(int i, int j, int k, int l, int m, int n) {
        super.fillGradient(i, j, k, l, m, n);
    }

    @Override
    public void renderBackground() {
        super.renderBackground();
    }

    @Override
    public boolean isPauseScreen() {
        return super.isPauseScreen();
    }

    @Override
    public void renderBackground(int i) {
        super.renderBackground(i);
    }

    @Override
    public void render(int i, int j, float f) {
        this.screen.render(i, j, f);
    }

    @Override
    public void renderTooltip(ItemStack itemStack, int i, int j) {
        super.renderTooltip(itemStack, i, j);
    }

    @Override
    public void renderTooltip(String string, int i, int j) {
        super.renderTooltip(string, i, j);
    }

    @Override
    public void renderTooltip(List<String> list, int i, int j) {
        super.renderTooltip(list, i, j);
    }

    @Override
    public void tick() {
        this.screen.tick();
        super.tick();
    }

    public int width() {
        return this.width;
    }

    public int height() {
        return this.height;
    }

    public int fontLineHeight() {
        return this.font.lineHeight;
    }

    public int fontWidth(String string) {
        return this.font.width(string);
    }

    public void fontDrawShadow(String string, int i, int j, int k) {
        this.font.drawShadow(string, i, j, k);
    }

    public List<String> fontSplit(String string, int i) {
        return this.font.split(string, i);
    }

    public void childrenClear() {
        this.children.clear();
    }

    public void addWidget(RealmsGuiEventListener realmsGuiEventListener) {
        if (this.hasWidget(realmsGuiEventListener) || !this.children.add(realmsGuiEventListener.getProxy())) {
            LOGGER.error("Tried to add the same widget multiple times: " + realmsGuiEventListener);
        }
    }

    public void narrateLabels() {
        List<String> list = this.children.stream().filter(guiEventListener -> guiEventListener instanceof RealmsLabelProxy).map(guiEventListener -> ((RealmsLabelProxy)guiEventListener).getLabel().getText()).collect(Collectors.toList());
        Realms.narrateNow(list);
    }

    public void removeWidget(RealmsGuiEventListener realmsGuiEventListener) {
        if (!this.hasWidget(realmsGuiEventListener) || !this.children.remove(realmsGuiEventListener.getProxy())) {
            LOGGER.error("Tried to add the same widget multiple times: " + realmsGuiEventListener);
        }
    }

    public boolean hasWidget(RealmsGuiEventListener realmsGuiEventListener) {
        return this.children.contains(realmsGuiEventListener.getProxy());
    }

    public void buttonsAdd(AbstractRealmsButton<?> abstractRealmsButton) {
        this.addButton(abstractRealmsButton.getProxy());
    }

    public List<AbstractRealmsButton<?>> buttons() {
        ArrayList<AbstractRealmsButton<?>> list = Lists.newArrayListWithExpectedSize(this.buttons.size());
        for (AbstractWidget abstractWidget : this.buttons) {
            list.add((AbstractRealmsButton<?>)((RealmsAbstractButtonProxy)((Object)abstractWidget)).getButton());
        }
        return list;
    }

    public void buttonsClear() {
        HashSet set = Sets.newHashSet(this.buttons);
        this.children.removeIf(set::contains);
        this.buttons.clear();
    }

    public void removeButton(RealmsButton realmsButton) {
        this.children.remove(realmsButton.getProxy());
        this.buttons.remove(realmsButton.getProxy());
    }

    @Override
    public boolean mouseClicked(double d, double e, int i) {
        if (this.screen.mouseClicked(d, e, i)) {
            return true;
        }
        return super.mouseClicked(d, e, i);
    }

    @Override
    public boolean mouseReleased(double d, double e, int i) {
        return this.screen.mouseReleased(d, e, i);
    }

    @Override
    public boolean mouseDragged(double d, double e, int i, double f, double g) {
        if (this.screen.mouseDragged(d, e, i, f, g)) {
            return true;
        }
        return super.mouseDragged(d, e, i, f, g);
    }

    @Override
    public boolean keyPressed(int i, int j, int k) {
        if (this.screen.keyPressed(i, j, k)) {
            return true;
        }
        return super.keyPressed(i, j, k);
    }

    @Override
    public boolean charTyped(char c, int i) {
        if (this.screen.charTyped(c, i)) {
            return true;
        }
        return super.charTyped(c, i);
    }

    @Override
    public void removed() {
        this.screen.removed();
        super.removed();
    }

    public int draw(String string, int i, int j, int k, boolean bl) {
        if (bl) {
            return this.font.drawShadow(string, i, j, k);
        }
        return this.font.draw(string, i, j, k);
    }

    public Font getFont() {
        return this.font;
    }
}

