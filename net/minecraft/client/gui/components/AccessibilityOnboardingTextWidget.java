/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.gui.components;

import com.mojang.blaze3d.vertex.PoseStack;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.MultiLineLabel;
import net.minecraft.client.gui.components.MultiLineTextWidget;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;

@Environment(value=EnvType.CLIENT)
public class AccessibilityOnboardingTextWidget
extends MultiLineTextWidget {
    private final Component message;
    private static final int BORDER_COLOR_FOCUSED = -1;
    private static final int BORDER_COLOR = -6250336;
    private static final int BACKGROUND_COLOR = 0x55000000;
    private static final int PADDING = 3;
    private static final int BORDER = 1;

    public AccessibilityOnboardingTextWidget(Font font, Component component, int i) {
        super(MultiLineLabel.create(font, (FormattedText)component, i), font, component, true);
        this.message = component;
        this.active = true;
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {
        narrationElementOutput.add(NarratedElementType.TITLE, this.message);
    }

    @Override
    protected void renderBg(PoseStack poseStack, Minecraft minecraft, int i, int j) {
        int k = this.getX() - 3;
        int l = this.getY() - 3;
        int m = this.getX() + this.width + 3;
        int n = this.getY() + this.height + 3;
        int o = this.isFocused() ? -1 : -6250336;
        AccessibilityOnboardingTextWidget.fill(poseStack, k - 1, l - 1, k, n + 1, o);
        AccessibilityOnboardingTextWidget.fill(poseStack, m, l - 1, m + 1, n + 1, o);
        AccessibilityOnboardingTextWidget.fill(poseStack, k, l, m, l - 1, o);
        AccessibilityOnboardingTextWidget.fill(poseStack, k, n, m, n + 1, o);
        AccessibilityOnboardingTextWidget.fill(poseStack, k, l, m, n, 0x55000000);
        super.renderBg(poseStack, minecraft, i, j);
    }

    @Override
    public void renderButton(PoseStack poseStack, int i, int j, float f) {
        this.renderBg(poseStack, Minecraft.getInstance(), i, j);
        super.renderButton(poseStack, i, j, f);
    }

    @Override
    public void playDownSound(SoundManager soundManager) {
    }
}

