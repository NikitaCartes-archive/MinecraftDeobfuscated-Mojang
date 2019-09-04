/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.renderer.entity;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.MinecartRenderer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.vehicle.MinecartTNT;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

@Environment(value=EnvType.CLIENT)
public class TntMinecartRenderer
extends MinecartRenderer<MinecartTNT> {
    public TntMinecartRenderer(EntityRenderDispatcher entityRenderDispatcher) {
        super(entityRenderDispatcher);
    }

    @Override
    protected void renderMinecartContents(MinecartTNT minecartTNT, float f, BlockState blockState) {
        int i = minecartTNT.getFuse();
        if (i > -1 && (float)i - f + 1.0f < 10.0f) {
            float g = 1.0f - ((float)i - f + 1.0f) / 10.0f;
            g = Mth.clamp(g, 0.0f, 1.0f);
            g *= g;
            g *= g;
            float h = 1.0f + g * 0.3f;
            RenderSystem.scalef(h, h, h);
        }
        super.renderMinecartContents(minecartTNT, f, blockState);
        if (i > -1 && i / 5 % 2 == 0) {
            BlockRenderDispatcher blockRenderDispatcher = Minecraft.getInstance().getBlockRenderer();
            RenderSystem.disableTexture();
            RenderSystem.disableLighting();
            RenderSystem.enableBlend();
            RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.DST_ALPHA);
            RenderSystem.color4f(1.0f, 1.0f, 1.0f, (1.0f - ((float)i - f + 1.0f) / 100.0f) * 0.8f);
            RenderSystem.pushMatrix();
            blockRenderDispatcher.renderSingleBlock(Blocks.TNT.defaultBlockState(), 1.0f);
            RenderSystem.popMatrix();
            RenderSystem.color4f(1.0f, 1.0f, 1.0f, 1.0f);
            RenderSystem.disableBlend();
            RenderSystem.enableLighting();
            RenderSystem.enableTexture();
        }
    }
}

