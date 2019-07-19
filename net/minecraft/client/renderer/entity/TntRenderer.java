/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.renderer.entity;

import com.mojang.blaze3d.platform.GlStateManager;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.item.PrimedTnt;
import net.minecraft.world.level.block.Blocks;

@Environment(value=EnvType.CLIENT)
public class TntRenderer
extends EntityRenderer<PrimedTnt> {
    public TntRenderer(EntityRenderDispatcher entityRenderDispatcher) {
        super(entityRenderDispatcher);
        this.shadowRadius = 0.5f;
    }

    @Override
    public void render(PrimedTnt primedTnt, double d, double e, double f, float g, float h) {
        float i;
        BlockRenderDispatcher blockRenderDispatcher = Minecraft.getInstance().getBlockRenderer();
        GlStateManager.pushMatrix();
        GlStateManager.translatef((float)d, (float)e + 0.5f, (float)f);
        if ((float)primedTnt.getLife() - h + 1.0f < 10.0f) {
            i = 1.0f - ((float)primedTnt.getLife() - h + 1.0f) / 10.0f;
            i = Mth.clamp(i, 0.0f, 1.0f);
            i *= i;
            i *= i;
            float j = 1.0f + i * 0.3f;
            GlStateManager.scalef(j, j, j);
        }
        i = (1.0f - ((float)primedTnt.getLife() - h + 1.0f) / 100.0f) * 0.8f;
        this.bindTexture(primedTnt);
        GlStateManager.rotatef(-90.0f, 0.0f, 1.0f, 0.0f);
        GlStateManager.translatef(-0.5f, -0.5f, 0.5f);
        blockRenderDispatcher.renderSingleBlock(Blocks.TNT.defaultBlockState(), primedTnt.getBrightness());
        GlStateManager.translatef(0.0f, 0.0f, 1.0f);
        if (this.solidRender) {
            GlStateManager.enableColorMaterial();
            GlStateManager.setupSolidRenderingTextureCombine(this.getTeamColor(primedTnt));
            blockRenderDispatcher.renderSingleBlock(Blocks.TNT.defaultBlockState(), 1.0f);
            GlStateManager.tearDownSolidRenderingTextureCombine();
            GlStateManager.disableColorMaterial();
        } else if (primedTnt.getLife() / 5 % 2 == 0) {
            GlStateManager.disableTexture();
            GlStateManager.disableLighting();
            GlStateManager.enableBlend();
            GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.DST_ALPHA);
            GlStateManager.color4f(1.0f, 1.0f, 1.0f, i);
            GlStateManager.polygonOffset(-3.0f, -3.0f);
            GlStateManager.enablePolygonOffset();
            blockRenderDispatcher.renderSingleBlock(Blocks.TNT.defaultBlockState(), 1.0f);
            GlStateManager.polygonOffset(0.0f, 0.0f);
            GlStateManager.disablePolygonOffset();
            GlStateManager.color4f(1.0f, 1.0f, 1.0f, 1.0f);
            GlStateManager.disableBlend();
            GlStateManager.enableLighting();
            GlStateManager.enableTexture();
        }
        GlStateManager.popMatrix();
        super.render(primedTnt, d, e, f, g, h);
    }

    @Override
    protected ResourceLocation getTextureLocation(PrimedTnt primedTnt) {
        return TextureAtlas.LOCATION_BLOCKS;
    }
}

