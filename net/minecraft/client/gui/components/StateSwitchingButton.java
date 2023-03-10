/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.gui.components;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.resources.ResourceLocation;

@Environment(value=EnvType.CLIENT)
public class StateSwitchingButton
extends AbstractWidget {
    protected ResourceLocation resourceLocation;
    protected boolean isStateTriggered;
    protected int xTexStart;
    protected int yTexStart;
    protected int xDiffTex;
    protected int yDiffTex;

    public StateSwitchingButton(int i, int j, int k, int l, boolean bl) {
        super(i, j, k, l, CommonComponents.EMPTY);
        this.isStateTriggered = bl;
    }

    public void initTextureValues(int i, int j, int k, int l, ResourceLocation resourceLocation) {
        this.xTexStart = i;
        this.yTexStart = j;
        this.xDiffTex = k;
        this.yDiffTex = l;
        this.resourceLocation = resourceLocation;
    }

    public void setStateTriggered(boolean bl) {
        this.isStateTriggered = bl;
    }

    public boolean isStateTriggered() {
        return this.isStateTriggered;
    }

    @Override
    public void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {
        this.defaultButtonNarrationText(narrationElementOutput);
    }

    @Override
    public void renderWidget(PoseStack poseStack, int i, int j, float f) {
        RenderSystem.setShaderTexture(0, this.resourceLocation);
        RenderSystem.disableDepthTest();
        int k = this.xTexStart;
        int l = this.yTexStart;
        if (this.isStateTriggered) {
            k += this.xDiffTex;
        }
        if (this.isHoveredOrFocused()) {
            l += this.yDiffTex;
        }
        StateSwitchingButton.blit(poseStack, this.getX(), this.getY(), k, l, this.width, this.height);
        RenderSystem.enableDepthTest();
    }
}

