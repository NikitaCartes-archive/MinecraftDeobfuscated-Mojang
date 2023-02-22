/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.gui.components.spectator;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.spectator.SpectatorMenu;
import net.minecraft.client.gui.spectator.SpectatorMenuItem;
import net.minecraft.client.gui.spectator.SpectatorMenuListener;
import net.minecraft.client.gui.spectator.categories.SpectatorPage;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.Nullable;

@Environment(value=EnvType.CLIENT)
public class SpectatorGui
extends GuiComponent
implements SpectatorMenuListener {
    private static final ResourceLocation WIDGETS_LOCATION = new ResourceLocation("textures/gui/widgets.png");
    public static final ResourceLocation SPECTATOR_LOCATION = new ResourceLocation("textures/gui/spectator_widgets.png");
    private static final long FADE_OUT_DELAY = 5000L;
    private static final long FADE_OUT_TIME = 2000L;
    private final Minecraft minecraft;
    private long lastSelectionTime;
    @Nullable
    private SpectatorMenu menu;

    public SpectatorGui(Minecraft minecraft) {
        this.minecraft = minecraft;
    }

    public void onHotbarSelected(int i) {
        this.lastSelectionTime = Util.getMillis();
        if (this.menu != null) {
            this.menu.selectSlot(i);
        } else {
            this.menu = new SpectatorMenu(this);
        }
    }

    private float getHotbarAlpha() {
        long l = this.lastSelectionTime - Util.getMillis() + 5000L;
        return Mth.clamp((float)l / 2000.0f, 0.0f, 1.0f);
    }

    public void renderHotbar(PoseStack poseStack) {
        if (this.menu == null) {
            return;
        }
        float f = this.getHotbarAlpha();
        if (f <= 0.0f) {
            this.menu.exit();
            return;
        }
        int i = this.minecraft.getWindow().getGuiScaledWidth() / 2;
        poseStack.pushPose();
        poseStack.translate(0.0f, 0.0f, -90.0f);
        int j = Mth.floor((float)this.minecraft.getWindow().getGuiScaledHeight() - 22.0f * f);
        SpectatorPage spectatorPage = this.menu.getCurrentPage();
        this.renderPage(poseStack, f, i, j, spectatorPage);
        poseStack.popPose();
    }

    protected void renderPage(PoseStack poseStack, float f, int i, int j, SpectatorPage spectatorPage) {
        RenderSystem.enableBlend();
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, f);
        RenderSystem.setShaderTexture(0, WIDGETS_LOCATION);
        SpectatorGui.blit(poseStack, i - 91, j, 0, 0, 182, 22);
        if (spectatorPage.getSelectedSlot() >= 0) {
            SpectatorGui.blit(poseStack, i - 91 - 1 + spectatorPage.getSelectedSlot() * 20, j - 1, 0, 22, 24, 22);
        }
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
        for (int k = 0; k < 9; ++k) {
            this.renderSlot(poseStack, k, this.minecraft.getWindow().getGuiScaledWidth() / 2 - 90 + k * 20 + 2, j + 3, f, spectatorPage.getItem(k));
        }
        RenderSystem.disableBlend();
    }

    private void renderSlot(PoseStack poseStack, int i, int j, float f, float g, SpectatorMenuItem spectatorMenuItem) {
        RenderSystem.setShaderTexture(0, SPECTATOR_LOCATION);
        if (spectatorMenuItem != SpectatorMenu.EMPTY_SLOT) {
            int k = (int)(g * 255.0f);
            poseStack.pushPose();
            poseStack.translate(j, f, 0.0f);
            float h = spectatorMenuItem.isEnabled() ? 1.0f : 0.25f;
            RenderSystem.setShaderColor(h, h, h, g);
            spectatorMenuItem.renderIcon(poseStack, h, k);
            RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
            poseStack.popPose();
            if (k > 3 && spectatorMenuItem.isEnabled()) {
                Component component = this.minecraft.options.keyHotbarSlots[i].getTranslatedKeyMessage();
                this.minecraft.font.drawShadow(poseStack, component, (float)(j + 19 - 2 - this.minecraft.font.width(component)), f + 6.0f + 3.0f, 0xFFFFFF + (k << 24));
            }
        }
    }

    public void renderTooltip(PoseStack poseStack) {
        int i = (int)(this.getHotbarAlpha() * 255.0f);
        if (i > 3 && this.menu != null) {
            Component component;
            SpectatorMenuItem spectatorMenuItem = this.menu.getSelectedItem();
            Component component2 = component = spectatorMenuItem == SpectatorMenu.EMPTY_SLOT ? this.menu.getSelectedCategory().getPrompt() : spectatorMenuItem.getName();
            if (component != null) {
                int j = (this.minecraft.getWindow().getGuiScaledWidth() - this.minecraft.font.width(component)) / 2;
                int k = this.minecraft.getWindow().getGuiScaledHeight() - 35;
                this.minecraft.font.drawShadow(poseStack, component, (float)j, (float)k, 0xFFFFFF + (i << 24));
            }
        }
    }

    @Override
    public void onSpectatorMenuClosed(SpectatorMenu spectatorMenu) {
        this.menu = null;
        this.lastSelectionTime = 0L;
    }

    public boolean isMenuActive() {
        return this.menu != null;
    }

    public void onMouseScrolled(int i) {
        int j;
        for (j = this.menu.getSelectedSlot() + i; !(j < 0 || j > 8 || this.menu.getItem(j) != SpectatorMenu.EMPTY_SLOT && this.menu.getItem(j).isEnabled()); j += i) {
        }
        if (j >= 0 && j <= 8) {
            this.menu.selectSlot(j);
            this.lastSelectionTime = Util.getMillis();
        }
    }

    public void onMouseMiddleClick() {
        this.lastSelectionTime = Util.getMillis();
        if (this.isMenuActive()) {
            int i = this.menu.getSelectedSlot();
            if (i != -1) {
                this.menu.selectSlot(i);
            }
        } else {
            this.menu = new SpectatorMenu(this);
        }
    }
}

