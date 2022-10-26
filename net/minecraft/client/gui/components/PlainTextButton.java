/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.gui.components;

import com.mojang.blaze3d.vertex.PoseStack;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentUtils;
import net.minecraft.network.chat.Style;
import net.minecraft.util.Mth;

@Environment(value=EnvType.CLIENT)
public class PlainTextButton
extends Button {
    private final Font font;
    private final Component message;
    private final Component underlinedMessage;

    public PlainTextButton(int i, int j, int k, int l, Component component, Button.OnPress onPress, Font font) {
        super(i, j, k, l, component, onPress, NO_TOOLTIP, DEFAULT_NARRATION);
        this.font = font;
        this.message = component;
        this.underlinedMessage = ComponentUtils.mergeStyles(component.copy(), Style.EMPTY.withUnderlined(true));
    }

    @Override
    public void renderButton(PoseStack poseStack, int i, int j, float f) {
        Component component = this.isHoveredOrFocused() ? this.underlinedMessage : this.message;
        PlainTextButton.drawString(poseStack, this.font, component, this.getX(), this.getY(), 0xFFFFFF | Mth.ceil(this.alpha * 255.0f) << 24);
    }
}

