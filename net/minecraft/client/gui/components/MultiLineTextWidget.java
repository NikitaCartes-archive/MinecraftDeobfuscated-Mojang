/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.gui.components;

import com.mojang.blaze3d.vertex.PoseStack;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.MultiLineLabel;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;

@Environment(value=EnvType.CLIENT)
public class MultiLineTextWidget
extends AbstractWidget {
    private final MultiLineLabel multiLineLabel;
    private final int lineHeight;
    private final boolean centered;

    protected MultiLineTextWidget(MultiLineLabel multiLineLabel, Font font, Component component, boolean bl) {
        super(0, 0, multiLineLabel.getWidth(), multiLineLabel.getLineCount() * font.lineHeight, component);
        this.multiLineLabel = multiLineLabel;
        this.lineHeight = font.lineHeight;
        this.centered = bl;
        this.active = false;
    }

    public static MultiLineTextWidget createCentered(int i, Font font, Component component) {
        MultiLineLabel multiLineLabel = MultiLineLabel.create(font, (FormattedText)component, i);
        return new MultiLineTextWidget(multiLineLabel, font, component, true);
    }

    public static MultiLineTextWidget create(int i, Font font, Component component) {
        MultiLineLabel multiLineLabel = MultiLineLabel.create(font, (FormattedText)component, i);
        return new MultiLineTextWidget(multiLineLabel, font, component, false);
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {
    }

    @Override
    public void renderButton(PoseStack poseStack, int i, int j, float f) {
        if (this.centered) {
            this.multiLineLabel.renderCentered(poseStack, this.getX() + this.getWidth() / 2, this.getY(), this.lineHeight, 0xFFFFFF);
        } else {
            this.multiLineLabel.renderLeftAligned(poseStack, this.getX(), this.getY(), this.lineHeight, 0xFFFFFF);
        }
    }
}

