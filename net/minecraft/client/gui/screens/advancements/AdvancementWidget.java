/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.gui.screens.advancements;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.systems.RenderSystem;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementProgress;
import net.minecraft.advancements.DisplayInfo;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.screens.advancements.AdvancementTab;
import net.minecraft.client.gui.screens.advancements.AdvancementWidgetType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.Nullable;

@Environment(value=EnvType.CLIENT)
public class AdvancementWidget
extends GuiComponent {
    private static final ResourceLocation WIDGETS_LOCATION = new ResourceLocation("textures/gui/advancements/widgets.png");
    private static final Pattern LAST_WORD = Pattern.compile("(.+) \\S+");
    private final AdvancementTab tab;
    private final Advancement advancement;
    private final DisplayInfo display;
    private final String title;
    private final int width;
    private final List<String> description;
    private final Minecraft minecraft;
    private AdvancementWidget parent;
    private final List<AdvancementWidget> children = Lists.newArrayList();
    private AdvancementProgress progress;
    private final int x;
    private final int y;

    public AdvancementWidget(AdvancementTab advancementTab, Minecraft minecraft, Advancement advancement, DisplayInfo displayInfo) {
        this.tab = advancementTab;
        this.advancement = advancement;
        this.display = displayInfo;
        this.minecraft = minecraft;
        this.title = minecraft.font.substrByWidth(displayInfo.getTitle().getColoredString(), 163);
        this.x = Mth.floor(displayInfo.getX() * 28.0f);
        this.y = Mth.floor(displayInfo.getY() * 27.0f);
        int i = advancement.getMaxCriteraRequired();
        int j = String.valueOf(i).length();
        int k = i > 1 ? minecraft.font.width("  ") + minecraft.font.width("0") * j * 2 + minecraft.font.width("/") : 0;
        int l = 29 + minecraft.font.width(this.title) + k;
        String string = displayInfo.getDescription().getColoredString();
        this.description = this.findOptimalLines(string, l);
        for (String string2 : this.description) {
            l = Math.max(l, minecraft.font.width(string2));
        }
        this.width = l + 3 + 5;
    }

    private List<String> findOptimalLines(String string, int i) {
        int k;
        if (string.isEmpty()) {
            return Collections.emptyList();
        }
        List<String> list = this.minecraft.font.split(string, i);
        if (list.size() < 2) {
            return list;
        }
        String string2 = list.get(0);
        String string3 = list.get(1);
        int j = this.minecraft.font.width(string2 + ' ' + string3.split(" ")[0]);
        if (j - i <= 10) {
            return this.minecraft.font.split(string, j);
        }
        Matcher matcher = LAST_WORD.matcher(string2);
        if (matcher.matches() && i - (k = this.minecraft.font.width(matcher.group(1))) <= 10) {
            return this.minecraft.font.split(string, k);
        }
        return list;
    }

    @Nullable
    private AdvancementWidget getFirstVisibleParent(Advancement advancement) {
        while ((advancement = advancement.getParent()) != null && advancement.getDisplay() == null) {
        }
        if (advancement == null || advancement.getDisplay() == null) {
            return null;
        }
        return this.tab.getWidget(advancement);
    }

    public void drawConnectivity(int i, int j, boolean bl) {
        if (this.parent != null) {
            int p;
            int k = i + this.parent.x + 13;
            int l = i + this.parent.x + 26 + 4;
            int m = j + this.parent.y + 13;
            int n = i + this.x + 13;
            int o = j + this.y + 13;
            int n2 = p = bl ? -16777216 : -1;
            if (bl) {
                this.hLine(l, k, m - 1, p);
                this.hLine(l + 1, k, m, p);
                this.hLine(l, k, m + 1, p);
                this.hLine(n, l - 1, o - 1, p);
                this.hLine(n, l - 1, o, p);
                this.hLine(n, l - 1, o + 1, p);
                this.vLine(l - 1, o, m, p);
                this.vLine(l + 1, o, m, p);
            } else {
                this.hLine(l, k, m, p);
                this.hLine(n, l, o, p);
                this.vLine(l, o, m, p);
            }
        }
        for (AdvancementWidget advancementWidget : this.children) {
            advancementWidget.drawConnectivity(i, j, bl);
        }
    }

    public void draw(int i, int j) {
        if (!this.display.isHidden() || this.progress != null && this.progress.isDone()) {
            float f = this.progress == null ? 0.0f : this.progress.getPercent();
            AdvancementWidgetType advancementWidgetType = f >= 1.0f ? AdvancementWidgetType.OBTAINED : AdvancementWidgetType.UNOBTAINED;
            this.minecraft.getTextureManager().bind(WIDGETS_LOCATION);
            RenderSystem.color4f(1.0f, 1.0f, 1.0f, 1.0f);
            RenderSystem.enableBlend();
            this.blit(i + this.x + 3, j + this.y, this.display.getFrame().getTexture(), 128 + advancementWidgetType.getIndex() * 26, 26, 26);
            this.minecraft.getItemRenderer().renderAndDecorateItem(null, this.display.getIcon(), i + this.x + 8, j + this.y + 5);
        }
        for (AdvancementWidget advancementWidget : this.children) {
            advancementWidget.draw(i, j);
        }
    }

    public void setProgress(AdvancementProgress advancementProgress) {
        this.progress = advancementProgress;
    }

    public void addChild(AdvancementWidget advancementWidget) {
        this.children.add(advancementWidget);
    }

    public void drawHover(int i, int j, float f, int k, int l) {
        AdvancementWidgetType advancementWidgetType3;
        AdvancementWidgetType advancementWidgetType2;
        AdvancementWidgetType advancementWidgetType;
        boolean bl = k + i + this.x + this.width + 26 >= this.tab.getScreen().width;
        String string = this.progress == null ? null : this.progress.getProgressText();
        int m = string == null ? 0 : this.minecraft.font.width(string);
        boolean bl2 = 113 - j - this.y - 26 <= 6 + this.description.size() * this.minecraft.font.lineHeight;
        float g = this.progress == null ? 0.0f : this.progress.getPercent();
        int n = Mth.floor(g * (float)this.width);
        if (g >= 1.0f) {
            n = this.width / 2;
            advancementWidgetType = AdvancementWidgetType.OBTAINED;
            advancementWidgetType2 = AdvancementWidgetType.OBTAINED;
            advancementWidgetType3 = AdvancementWidgetType.OBTAINED;
        } else if (n < 2) {
            n = this.width / 2;
            advancementWidgetType = AdvancementWidgetType.UNOBTAINED;
            advancementWidgetType2 = AdvancementWidgetType.UNOBTAINED;
            advancementWidgetType3 = AdvancementWidgetType.UNOBTAINED;
        } else if (n > this.width - 2) {
            n = this.width / 2;
            advancementWidgetType = AdvancementWidgetType.OBTAINED;
            advancementWidgetType2 = AdvancementWidgetType.OBTAINED;
            advancementWidgetType3 = AdvancementWidgetType.UNOBTAINED;
        } else {
            advancementWidgetType = AdvancementWidgetType.OBTAINED;
            advancementWidgetType2 = AdvancementWidgetType.UNOBTAINED;
            advancementWidgetType3 = AdvancementWidgetType.UNOBTAINED;
        }
        int o = this.width - n;
        this.minecraft.getTextureManager().bind(WIDGETS_LOCATION);
        RenderSystem.color4f(1.0f, 1.0f, 1.0f, 1.0f);
        RenderSystem.enableBlend();
        int p = j + this.y;
        int q = bl ? i + this.x - this.width + 26 + 6 : i + this.x;
        int r = 32 + this.description.size() * this.minecraft.font.lineHeight;
        if (!this.description.isEmpty()) {
            if (bl2) {
                this.render9Sprite(q, p + 26 - r, this.width, r, 10, 200, 26, 0, 52);
            } else {
                this.render9Sprite(q, p, this.width, r, 10, 200, 26, 0, 52);
            }
        }
        this.blit(q, p, 0, advancementWidgetType.getIndex() * 26, n, 26);
        this.blit(q + n, p, 200 - o, advancementWidgetType2.getIndex() * 26, o, 26);
        this.blit(i + this.x + 3, j + this.y, this.display.getFrame().getTexture(), 128 + advancementWidgetType3.getIndex() * 26, 26, 26);
        if (bl) {
            this.minecraft.font.drawShadow(this.title, q + 5, j + this.y + 9, -1);
            if (string != null) {
                this.minecraft.font.drawShadow(string, i + this.x - m, j + this.y + 9, -1);
            }
        } else {
            this.minecraft.font.drawShadow(this.title, i + this.x + 32, j + this.y + 9, -1);
            if (string != null) {
                this.minecraft.font.drawShadow(string, i + this.x + this.width - m - 5, j + this.y + 9, -1);
            }
        }
        if (bl2) {
            for (int s = 0; s < this.description.size(); ++s) {
                this.minecraft.font.draw(this.description.get(s), q + 5, p + 26 - r + 7 + s * this.minecraft.font.lineHeight, -5592406);
            }
        } else {
            for (int s = 0; s < this.description.size(); ++s) {
                this.minecraft.font.draw(this.description.get(s), q + 5, j + this.y + 9 + 17 + s * this.minecraft.font.lineHeight, -5592406);
            }
        }
        this.minecraft.getItemRenderer().renderAndDecorateItem(null, this.display.getIcon(), i + this.x + 8, j + this.y + 5);
    }

    protected void render9Sprite(int i, int j, int k, int l, int m, int n, int o, int p, int q) {
        this.blit(i, j, p, q, m, m);
        this.renderRepeating(i + m, j, k - m - m, m, p + m, q, n - m - m, o);
        this.blit(i + k - m, j, p + n - m, q, m, m);
        this.blit(i, j + l - m, p, q + o - m, m, m);
        this.renderRepeating(i + m, j + l - m, k - m - m, m, p + m, q + o - m, n - m - m, o);
        this.blit(i + k - m, j + l - m, p + n - m, q + o - m, m, m);
        this.renderRepeating(i, j + m, m, l - m - m, p, q + m, n, o - m - m);
        this.renderRepeating(i + m, j + m, k - m - m, l - m - m, p + m, q + m, n - m - m, o - m - m);
        this.renderRepeating(i + k - m, j + m, m, l - m - m, p + n - m, q + m, n, o - m - m);
    }

    protected void renderRepeating(int i, int j, int k, int l, int m, int n, int o, int p) {
        for (int q = 0; q < k; q += o) {
            int r = i + q;
            int s = Math.min(o, k - q);
            for (int t = 0; t < l; t += p) {
                int u = j + t;
                int v = Math.min(p, l - t);
                this.blit(r, u, m, n, s, v);
            }
        }
    }

    public boolean isMouseOver(int i, int j, int k, int l) {
        if (this.display.isHidden() && (this.progress == null || !this.progress.isDone())) {
            return false;
        }
        int m = i + this.x;
        int n = m + 26;
        int o = j + this.y;
        int p = o + 26;
        return k >= m && k <= n && l >= o && l <= p;
    }

    public void attachToParent() {
        if (this.parent == null && this.advancement.getParent() != null) {
            this.parent = this.getFirstVisibleParent(this.advancement);
            if (this.parent != null) {
                this.parent.addChild(this);
            }
        }
    }

    public int getY() {
        return this.y;
    }

    public int getX() {
        return this.x;
    }
}

