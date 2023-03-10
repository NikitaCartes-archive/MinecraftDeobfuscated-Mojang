/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.gui.screens.advancements;

import com.google.common.collect.Maps;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import java.util.Map;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.DisplayInfo;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.screens.advancements.AdvancementTabType;
import net.minecraft.client.gui.screens.advancements.AdvancementWidget;
import net.minecraft.client.gui.screens.advancements.AdvancementsScreen;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

@Environment(value=EnvType.CLIENT)
public class AdvancementTab
extends GuiComponent {
    private final Minecraft minecraft;
    private final AdvancementsScreen screen;
    private final AdvancementTabType type;
    private final int index;
    private final Advancement advancement;
    private final DisplayInfo display;
    private final ItemStack icon;
    private final Component title;
    private final AdvancementWidget root;
    private final Map<Advancement, AdvancementWidget> widgets = Maps.newLinkedHashMap();
    private double scrollX;
    private double scrollY;
    private int minX = Integer.MAX_VALUE;
    private int minY = Integer.MAX_VALUE;
    private int maxX = Integer.MIN_VALUE;
    private int maxY = Integer.MIN_VALUE;
    private float fade;
    private boolean centered;

    public AdvancementTab(Minecraft minecraft, AdvancementsScreen advancementsScreen, AdvancementTabType advancementTabType, int i, Advancement advancement, DisplayInfo displayInfo) {
        this.minecraft = minecraft;
        this.screen = advancementsScreen;
        this.type = advancementTabType;
        this.index = i;
        this.advancement = advancement;
        this.display = displayInfo;
        this.icon = displayInfo.getIcon();
        this.title = displayInfo.getTitle();
        this.root = new AdvancementWidget(this, minecraft, advancement, displayInfo);
        this.addWidget(this.root, advancement);
    }

    public AdvancementTabType getType() {
        return this.type;
    }

    public int getIndex() {
        return this.index;
    }

    public Advancement getAdvancement() {
        return this.advancement;
    }

    public Component getTitle() {
        return this.title;
    }

    public DisplayInfo getDisplay() {
        return this.display;
    }

    public void drawTab(PoseStack poseStack, int i, int j, boolean bl) {
        this.type.draw(poseStack, i, j, bl, this.index);
    }

    public void drawIcon(PoseStack poseStack, int i, int j, ItemRenderer itemRenderer) {
        this.type.drawIcon(poseStack, i, j, this.index, itemRenderer, this.icon);
    }

    public void drawContents(PoseStack poseStack, int i, int j) {
        if (!this.centered) {
            this.scrollX = 117 - (this.maxX + this.minX) / 2;
            this.scrollY = 56 - (this.maxY + this.minY) / 2;
            this.centered = true;
        }
        AdvancementTab.enableScissor(i, j, i + 234, j + 113);
        poseStack.pushPose();
        poseStack.translate(i, j, 0.0f);
        ResourceLocation resourceLocation = this.display.getBackground();
        if (resourceLocation != null) {
            RenderSystem.setShaderTexture(0, resourceLocation);
        } else {
            RenderSystem.setShaderTexture(0, TextureManager.INTENTIONAL_MISSING_TEXTURE);
        }
        int k = Mth.floor(this.scrollX);
        int l = Mth.floor(this.scrollY);
        int m = k % 16;
        int n = l % 16;
        for (int o = -1; o <= 15; ++o) {
            for (int p = -1; p <= 8; ++p) {
                AdvancementTab.blit(poseStack, m + 16 * o, n + 16 * p, 0.0f, 0.0f, 16, 16, 16, 16);
            }
        }
        this.root.drawConnectivity(poseStack, k, l, true);
        this.root.drawConnectivity(poseStack, k, l, false);
        this.root.draw(poseStack, k, l);
        poseStack.popPose();
        AdvancementTab.disableScissor();
    }

    public void drawTooltips(PoseStack poseStack, int i, int j, int k, int l) {
        poseStack.pushPose();
        poseStack.translate(0.0f, 0.0f, -200.0f);
        AdvancementTab.fill(poseStack, 0, 0, 234, 113, Mth.floor(this.fade * 255.0f) << 24);
        boolean bl = false;
        int m = Mth.floor(this.scrollX);
        int n = Mth.floor(this.scrollY);
        if (i > 0 && i < 234 && j > 0 && j < 113) {
            for (AdvancementWidget advancementWidget : this.widgets.values()) {
                if (!advancementWidget.isMouseOver(m, n, i, j)) continue;
                bl = true;
                advancementWidget.drawHover(poseStack, m, n, this.fade, k, l);
                break;
            }
        }
        poseStack.popPose();
        this.fade = bl ? Mth.clamp(this.fade + 0.02f, 0.0f, 0.3f) : Mth.clamp(this.fade - 0.04f, 0.0f, 1.0f);
    }

    public boolean isMouseOver(int i, int j, double d, double e) {
        return this.type.isMouseOver(i, j, this.index, d, e);
    }

    @Nullable
    public static AdvancementTab create(Minecraft minecraft, AdvancementsScreen advancementsScreen, int i, Advancement advancement) {
        if (advancement.getDisplay() == null) {
            return null;
        }
        for (AdvancementTabType advancementTabType : AdvancementTabType.values()) {
            if (i >= advancementTabType.getMax()) {
                i -= advancementTabType.getMax();
                continue;
            }
            return new AdvancementTab(minecraft, advancementsScreen, advancementTabType, i, advancement, advancement.getDisplay());
        }
        return null;
    }

    public void scroll(double d, double e) {
        if (this.maxX - this.minX > 234) {
            this.scrollX = Mth.clamp(this.scrollX + d, (double)(-(this.maxX - 234)), 0.0);
        }
        if (this.maxY - this.minY > 113) {
            this.scrollY = Mth.clamp(this.scrollY + e, (double)(-(this.maxY - 113)), 0.0);
        }
    }

    public void addAdvancement(Advancement advancement) {
        if (advancement.getDisplay() == null) {
            return;
        }
        AdvancementWidget advancementWidget = new AdvancementWidget(this, this.minecraft, advancement, advancement.getDisplay());
        this.addWidget(advancementWidget, advancement);
    }

    private void addWidget(AdvancementWidget advancementWidget, Advancement advancement) {
        this.widgets.put(advancement, advancementWidget);
        int i = advancementWidget.getX();
        int j = i + 28;
        int k = advancementWidget.getY();
        int l = k + 27;
        this.minX = Math.min(this.minX, i);
        this.maxX = Math.max(this.maxX, j);
        this.minY = Math.min(this.minY, k);
        this.maxY = Math.max(this.maxY, l);
        for (AdvancementWidget advancementWidget2 : this.widgets.values()) {
            advancementWidget2.attachToParent();
        }
    }

    @Nullable
    public AdvancementWidget getWidget(Advancement advancement) {
        return this.widgets.get(advancement);
    }

    public AdvancementsScreen getScreen() {
        return this.screen;
    }
}

