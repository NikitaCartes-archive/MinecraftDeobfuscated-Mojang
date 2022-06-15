/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.gui.components;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.GuiComponent;

@Environment(value=EnvType.CLIENT)
public class PlayerFaceRenderer {
    public static final int SKIN_HEAD_U = 8;
    public static final int SKIN_HEAD_V = 8;
    public static final int SKIN_HEAD_WIDTH = 8;
    public static final int SKIN_HEAD_HEIGHT = 8;
    public static final int SKIN_HAT_U = 40;
    public static final int SKIN_HAT_V = 8;
    public static final int SKIN_HAT_WIDTH = 8;
    public static final int SKIN_HAT_HEIGHT = 8;
    public static final int SKIN_TEX_WIDTH = 64;
    public static final int SKIN_TEX_HEIGHT = 64;

    public static void draw(PoseStack poseStack, int i, int j, int k) {
        PlayerFaceRenderer.draw(poseStack, i, j, k, true, false);
    }

    public static void draw(PoseStack poseStack, int i, int j, int k, boolean bl, boolean bl2) {
        int l = 8 + (bl2 ? 8 : 0);
        int m = 8 * (bl2 ? -1 : 1);
        GuiComponent.blit(poseStack, i, j, k, k, 8.0f, l, 8, m, 64, 64);
        if (bl) {
            PlayerFaceRenderer.drawHat(poseStack, i, j, k, bl2);
        }
    }

    private static void drawHat(PoseStack poseStack, int i, int j, int k, boolean bl) {
        int l = 8 + (bl ? 8 : 0);
        int m = 8 * (bl ? -1 : 1);
        RenderSystem.enableBlend();
        GuiComponent.blit(poseStack, i, j, k, k, 40.0f, l, 8, m, 64, 64);
        RenderSystem.disableBlend();
    }
}

