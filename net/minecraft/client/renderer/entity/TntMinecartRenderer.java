/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.MinecartRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.vehicle.MinecartTNT;
import net.minecraft.world.level.block.state.BlockState;

@Environment(value=EnvType.CLIENT)
public class TntMinecartRenderer
extends MinecartRenderer<MinecartTNT> {
    public TntMinecartRenderer(EntityRenderDispatcher entityRenderDispatcher) {
        super(entityRenderDispatcher);
    }

    @Override
    protected void renderMinecartContents(MinecartTNT minecartTNT, float f, BlockState blockState, PoseStack poseStack, MultiBufferSource multiBufferSource, int i) {
        int j = minecartTNT.getFuse();
        if (j > -1 && (float)j - f + 1.0f < 10.0f) {
            float g = 1.0f - ((float)j - f + 1.0f) / 10.0f;
            g = Mth.clamp(g, 0.0f, 1.0f);
            g *= g;
            g *= g;
            float h = 1.0f + g * 0.3f;
            poseStack.scale(h, h, h);
        }
        TntMinecartRenderer.renderWhiteSolidBlock(blockState, poseStack, multiBufferSource, i, j > -1 && j / 5 % 2 == 0);
    }

    public static void renderWhiteSolidBlock(BlockState blockState, PoseStack poseStack, MultiBufferSource multiBufferSource, int i, boolean bl) {
        int j = bl ? OverlayTexture.pack(OverlayTexture.u(1.0f), 10) : OverlayTexture.NO_OVERLAY;
        Minecraft.getInstance().getBlockRenderer().renderSingleBlock(blockState, poseStack, multiBufferSource, i, j);
    }
}

