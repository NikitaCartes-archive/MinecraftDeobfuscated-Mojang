/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.gui.components;

import com.mojang.blaze3d.vertex.PoseStack;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.MultiLineTextWidget;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.network.chat.Component;

@Environment(value=EnvType.CLIENT)
public class AccessibilityOnboardingTextWidget
extends MultiLineTextWidget {
    private static final int BORDER_COLOR_FOCUSED = -1;
    private static final int BORDER_COLOR = -6250336;
    private static final int BACKGROUND_COLOR = 0x55000000;
    private static final int PADDING = 3;
    private static final int BORDER = 1;

    public AccessibilityOnboardingTextWidget(Font font, Component component, int i) {
        super(component, font);
        this.setMaxWidth(i);
        this.setCentered(true);
        this.active = true;
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {
        narrationElementOutput.add(NarratedElementType.TITLE, this.getMessage());
    }

    @Override
    public void renderWidget(PoseStack poseStack, int i, int j, float f) {
        int k = this.getX() - 3;
        int l = this.getY() - 3;
        int m = this.getX() + this.getWidth() + 3;
        int n = this.getY() + this.getHeight() + 3;
        int o = this.isFocused() ? -1 : -6250336;
        AccessibilityOnboardingTextWidget.fill(poseStack, k - 1, l - 1, k, n + 1, o);
        AccessibilityOnboardingTextWidget.fill(poseStack, m, l - 1, m + 1, n + 1, o);
        AccessibilityOnboardingTextWidget.fill(poseStack, k, l, m, l - 1, o);
        AccessibilityOnboardingTextWidget.fill(poseStack, k, n, m, n + 1, o);
        AccessibilityOnboardingTextWidget.fill(poseStack, k, l, m, n, 0x55000000);
        super.renderWidget(poseStack, i, j, f);
    }

    @Override
    public void playDownSound(SoundManager soundManager) {
    }
}

