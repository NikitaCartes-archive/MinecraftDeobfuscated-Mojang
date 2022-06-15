/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.gui.components;

import com.mojang.blaze3d.vertex.PoseStack;
import java.util.function.Consumer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;

@Environment(value=EnvType.CLIENT)
public class Button
extends AbstractButton {
    public static final OnTooltip NO_TOOLTIP = (button, poseStack, i, j) -> {};
    public static final int SMALL_WIDTH = 120;
    public static final int DEFAULT_WIDTH = 150;
    public static final int DEFAULT_HEIGHT = 20;
    protected final OnPress onPress;
    protected final OnTooltip onTooltip;

    public Button(int i, int j, int k, int l, Component component, OnPress onPress) {
        this(i, j, k, l, component, onPress, NO_TOOLTIP);
    }

    public Button(int i, int j, int k, int l, Component component, OnPress onPress, OnTooltip onTooltip) {
        super(i, j, k, l, component);
        this.onPress = onPress;
        this.onTooltip = onTooltip;
    }

    @Override
    public void onPress() {
        this.onPress.onPress(this);
    }

    @Override
    public void renderButton(PoseStack poseStack, int i, int j, float f) {
        super.renderButton(poseStack, i, j, f);
        if (this.isHoveredOrFocused()) {
            this.renderToolTip(poseStack, i, j);
        }
    }

    @Override
    public void renderToolTip(PoseStack poseStack, int i, int j) {
        this.onTooltip.onTooltip(this, poseStack, i, j);
    }

    @Override
    public void updateNarration(NarrationElementOutput narrationElementOutput) {
        this.defaultButtonNarrationText(narrationElementOutput);
        this.onTooltip.narrateTooltip(component -> narrationElementOutput.add(NarratedElementType.HINT, (Component)component));
    }

    @Environment(value=EnvType.CLIENT)
    public static interface OnTooltip {
        public void onTooltip(Button var1, PoseStack var2, int var3, int var4);

        default public void narrateTooltip(Consumer<Component> consumer) {
        }
    }

    @Environment(value=EnvType.CLIENT)
    public static interface OnPress {
        public void onPress(Button var1);
    }
}

