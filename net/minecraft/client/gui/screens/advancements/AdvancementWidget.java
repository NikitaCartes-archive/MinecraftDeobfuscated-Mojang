/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.gui.screens.advancements;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementProgress;
import net.minecraft.advancements.DisplayInfo;
import net.minecraft.client.Minecraft;
import net.minecraft.client.StringSplitter;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.screens.advancements.AdvancementTab;
import net.minecraft.client.gui.screens.advancements.AdvancementWidgetType;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentUtils;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.Nullable;

@Environment(value=EnvType.CLIENT)
public class AdvancementWidget
extends GuiComponent {
    private static final ResourceLocation WIDGETS_LOCATION = new ResourceLocation("textures/gui/advancements/widgets.png");
    private static final int HEIGHT = 26;
    private static final int BOX_X = 0;
    private static final int BOX_WIDTH = 200;
    private static final int FRAME_WIDTH = 26;
    private static final int ICON_X = 8;
    private static final int ICON_Y = 5;
    private static final int ICON_WIDTH = 26;
    private static final int TITLE_PADDING_LEFT = 3;
    private static final int TITLE_PADDING_RIGHT = 5;
    private static final int TITLE_X = 32;
    private static final int TITLE_Y = 9;
    private static final int TITLE_MAX_WIDTH = 163;
    private static final int[] TEST_SPLIT_OFFSETS = new int[]{0, 10, -10, 25, -25};
    private final AdvancementTab tab;
    private final Advancement advancement;
    private final DisplayInfo display;
    private final FormattedCharSequence title;
    private final int width;
    private final List<FormattedCharSequence> description;
    private final Minecraft minecraft;
    @Nullable
    private AdvancementWidget parent;
    private final List<AdvancementWidget> children = Lists.newArrayList();
    @Nullable
    private AdvancementProgress progress;
    private final int x;
    private final int y;

    public AdvancementWidget(AdvancementTab advancementTab, Minecraft minecraft, Advancement advancement, DisplayInfo displayInfo) {
        this.tab = advancementTab;
        this.advancement = advancement;
        this.display = displayInfo;
        this.minecraft = minecraft;
        this.title = Language.getInstance().getVisualOrder(minecraft.font.substrByWidth(displayInfo.getTitle(), 163));
        this.x = Mth.floor(displayInfo.getX() * 28.0f);
        this.y = Mth.floor(displayInfo.getY() * 27.0f);
        int i = advancement.getMaxCriteraRequired();
        int j = String.valueOf(i).length();
        int k = i > 1 ? minecraft.font.width("  ") + minecraft.font.width("0") * j * 2 + minecraft.font.width("/") : 0;
        int l = 29 + minecraft.font.width(this.title) + k;
        this.description = Language.getInstance().getVisualOrder(this.findOptimalLines(ComponentUtils.mergeStyles(displayInfo.getDescription().copy(), Style.EMPTY.withColor(displayInfo.getFrame().getChatColor())), l));
        for (FormattedCharSequence formattedCharSequence : this.description) {
            l = Math.max(l, minecraft.font.width(formattedCharSequence));
        }
        this.width = l + 3 + 5;
    }

    private static float getMaxWidth(StringSplitter stringSplitter, List<FormattedText> list) {
        return (float)list.stream().mapToDouble(stringSplitter::stringWidth).max().orElse(0.0);
    }

    private List<FormattedText> findOptimalLines(Component component, int i) {
        StringSplitter stringSplitter = this.minecraft.font.getSplitter();
        List<FormattedText> list = null;
        float f = Float.MAX_VALUE;
        for (int j : TEST_SPLIT_OFFSETS) {
            List<FormattedText> list2 = stringSplitter.splitLines(component, i - j, Style.EMPTY);
            float g = Math.abs(AdvancementWidget.getMaxWidth(stringSplitter, list2) - (float)i);
            if (g <= 10.0f) {
                return list2;
            }
            if (!(g < f)) continue;
            f = g;
            list = list2;
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

    public void drawConnectivity(PoseStack poseStack, int i, int j, boolean bl) {
        if (this.parent != null) {
            int p;
            int k = i + this.parent.x + 13;
            int l = i + this.parent.x + 26 + 4;
            int m = j + this.parent.y + 13;
            int n = i + this.x + 13;
            int o = j + this.y + 13;
            int n2 = p = bl ? -16777216 : -1;
            if (bl) {
                AdvancementWidget.hLine(poseStack, l, k, m - 1, p);
                AdvancementWidget.hLine(poseStack, l + 1, k, m, p);
                AdvancementWidget.hLine(poseStack, l, k, m + 1, p);
                AdvancementWidget.hLine(poseStack, n, l - 1, o - 1, p);
                AdvancementWidget.hLine(poseStack, n, l - 1, o, p);
                AdvancementWidget.hLine(poseStack, n, l - 1, o + 1, p);
                AdvancementWidget.vLine(poseStack, l - 1, o, m, p);
                AdvancementWidget.vLine(poseStack, l + 1, o, m, p);
            } else {
                AdvancementWidget.hLine(poseStack, l, k, m, p);
                AdvancementWidget.hLine(poseStack, n, l, o, p);
                AdvancementWidget.vLine(poseStack, l, o, m, p);
            }
        }
        for (AdvancementWidget advancementWidget : this.children) {
            advancementWidget.drawConnectivity(poseStack, i, j, bl);
        }
    }

    public void draw(PoseStack poseStack, int i, int j) {
        if (!this.display.isHidden() || this.progress != null && this.progress.isDone()) {
            float f = this.progress == null ? 0.0f : this.progress.getPercent();
            AdvancementWidgetType advancementWidgetType = f >= 1.0f ? AdvancementWidgetType.OBTAINED : AdvancementWidgetType.UNOBTAINED;
            RenderSystem.setShaderTexture(0, WIDGETS_LOCATION);
            AdvancementWidget.blit(poseStack, i + this.x + 3, j + this.y, this.display.getFrame().getTexture(), 128 + advancementWidgetType.getIndex() * 26, 26, 26);
            this.minecraft.getItemRenderer().renderAndDecorateFakeItem(poseStack, this.display.getIcon(), i + this.x + 8, j + this.y + 5);
        }
        for (AdvancementWidget advancementWidget : this.children) {
            advancementWidget.draw(poseStack, i, j);
        }
    }

    public int getWidth() {
        return this.width;
    }

    public void setProgress(AdvancementProgress advancementProgress) {
        this.progress = advancementProgress;
    }

    public void addChild(AdvancementWidget advancementWidget) {
        this.children.add(advancementWidget);
    }

    public void drawHover(PoseStack poseStack, int i, int j, float f, int k, int l) {
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
        RenderSystem.setShaderTexture(0, WIDGETS_LOCATION);
        RenderSystem.enableBlend();
        int p = j + this.y;
        int q = bl ? i + this.x - this.width + 26 + 6 : i + this.x;
        int r = 32 + this.description.size() * this.minecraft.font.lineHeight;
        if (!this.description.isEmpty()) {
            if (bl2) {
                AdvancementWidget.blitNineSliced(poseStack, q, p + 26 - r, this.width, r, 10, 200, 26, 0, 52);
            } else {
                AdvancementWidget.blitNineSliced(poseStack, q, p, this.width, r, 10, 200, 26, 0, 52);
            }
        }
        AdvancementWidget.blit(poseStack, q, p, 0, advancementWidgetType.getIndex() * 26, n, 26);
        AdvancementWidget.blit(poseStack, q + n, p, 200 - o, advancementWidgetType2.getIndex() * 26, o, 26);
        AdvancementWidget.blit(poseStack, i + this.x + 3, j + this.y, this.display.getFrame().getTexture(), 128 + advancementWidgetType3.getIndex() * 26, 26, 26);
        if (bl) {
            this.minecraft.font.drawShadow(poseStack, this.title, (float)(q + 5), (float)(j + this.y + 9), -1);
            if (string != null) {
                this.minecraft.font.drawShadow(poseStack, string, (float)(i + this.x - m), (float)(j + this.y + 9), -1);
            }
        } else {
            this.minecraft.font.drawShadow(poseStack, this.title, (float)(i + this.x + 32), (float)(j + this.y + 9), -1);
            if (string != null) {
                this.minecraft.font.drawShadow(poseStack, string, (float)(i + this.x + this.width - m - 5), (float)(j + this.y + 9), -1);
            }
        }
        if (bl2) {
            for (int s = 0; s < this.description.size(); ++s) {
                this.minecraft.font.draw(poseStack, this.description.get(s), (float)(q + 5), (float)(p + 26 - r + 7 + s * this.minecraft.font.lineHeight), -5592406);
            }
        } else {
            for (int s = 0; s < this.description.size(); ++s) {
                this.minecraft.font.draw(poseStack, this.description.get(s), (float)(q + 5), (float)(j + this.y + 9 + 17 + s * this.minecraft.font.lineHeight), -5592406);
            }
        }
        this.minecraft.getItemRenderer().renderAndDecorateFakeItem(poseStack, this.display.getIcon(), i + this.x + 8, j + this.y + 5);
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

