/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.renderer.entity;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.PaintingTextureManager;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.decoration.Motive;
import net.minecraft.world.entity.decoration.Painting;

@Environment(value=EnvType.CLIENT)
public class PaintingRenderer
extends EntityRenderer<Painting> {
    public PaintingRenderer(EntityRenderDispatcher entityRenderDispatcher) {
        super(entityRenderDispatcher);
    }

    @Override
    public void render(Painting painting, double d, double e, double f, float g, float h) {
        RenderSystem.pushMatrix();
        RenderSystem.translated(d, e, f);
        RenderSystem.rotatef(180.0f - g, 0.0f, 1.0f, 0.0f);
        RenderSystem.enableRescaleNormal();
        this.bindTexture(painting);
        Motive motive = painting.motive;
        float i = 0.0625f;
        RenderSystem.scalef(0.0625f, 0.0625f, 0.0625f);
        if (this.solidRender) {
            RenderSystem.enableColorMaterial();
            RenderSystem.setupSolidRenderingTextureCombine(this.getTeamColor(painting));
        }
        PaintingTextureManager paintingTextureManager = Minecraft.getInstance().getPaintingTextures();
        this.renderPainting(painting, motive.getWidth(), motive.getHeight(), paintingTextureManager.get(motive), paintingTextureManager.getBackSprite());
        if (this.solidRender) {
            RenderSystem.tearDownSolidRenderingTextureCombine();
            RenderSystem.disableColorMaterial();
        }
        RenderSystem.disableRescaleNormal();
        RenderSystem.popMatrix();
        super.render(painting, d, e, f, g, h);
    }

    @Override
    protected ResourceLocation getTextureLocation(Painting painting) {
        return TextureAtlas.LOCATION_PAINTINGS;
    }

    private void renderPainting(Painting painting, int i, int j, TextureAtlasSprite textureAtlasSprite, TextureAtlasSprite textureAtlasSprite2) {
        float f = (float)(-i) / 2.0f;
        float g = (float)(-j) / 2.0f;
        float h = 0.5f;
        float k = textureAtlasSprite2.getU0();
        float l = textureAtlasSprite2.getU1();
        float m = textureAtlasSprite2.getV0();
        float n = textureAtlasSprite2.getV1();
        float o = textureAtlasSprite2.getU0();
        float p = textureAtlasSprite2.getU1();
        float q = textureAtlasSprite2.getV0();
        float r = textureAtlasSprite2.getV(1.0);
        float s = textureAtlasSprite2.getU0();
        float t = textureAtlasSprite2.getU(1.0);
        float u = textureAtlasSprite2.getV0();
        float v = textureAtlasSprite2.getV1();
        int w = i / 16;
        int x = j / 16;
        double d = 16.0 / (double)w;
        double e = 16.0 / (double)x;
        for (int y = 0; y < w; ++y) {
            for (int z = 0; z < x; ++z) {
                float aa = f + (float)((y + 1) * 16);
                float ab = f + (float)(y * 16);
                float ac = g + (float)((z + 1) * 16);
                float ad = g + (float)(z * 16);
                this.setBrightness(painting, (aa + ab) / 2.0f, (ac + ad) / 2.0f);
                float ae = textureAtlasSprite.getU(d * (double)(w - y));
                float af = textureAtlasSprite.getU(d * (double)(w - (y + 1)));
                float ag = textureAtlasSprite.getV(e * (double)(x - z));
                float ah = textureAtlasSprite.getV(e * (double)(x - (z + 1)));
                Tesselator tesselator = Tesselator.getInstance();
                BufferBuilder bufferBuilder = tesselator.getBuilder();
                bufferBuilder.begin(7, DefaultVertexFormat.POSITION_TEX_NORMAL);
                bufferBuilder.vertex(aa, ad, -0.5).uv(af, ag).normal(0.0f, 0.0f, -1.0f).endVertex();
                bufferBuilder.vertex(ab, ad, -0.5).uv(ae, ag).normal(0.0f, 0.0f, -1.0f).endVertex();
                bufferBuilder.vertex(ab, ac, -0.5).uv(ae, ah).normal(0.0f, 0.0f, -1.0f).endVertex();
                bufferBuilder.vertex(aa, ac, -0.5).uv(af, ah).normal(0.0f, 0.0f, -1.0f).endVertex();
                bufferBuilder.vertex(aa, ac, 0.5).uv(k, m).normal(0.0f, 0.0f, 1.0f).endVertex();
                bufferBuilder.vertex(ab, ac, 0.5).uv(l, m).normal(0.0f, 0.0f, 1.0f).endVertex();
                bufferBuilder.vertex(ab, ad, 0.5).uv(l, n).normal(0.0f, 0.0f, 1.0f).endVertex();
                bufferBuilder.vertex(aa, ad, 0.5).uv(k, n).normal(0.0f, 0.0f, 1.0f).endVertex();
                bufferBuilder.vertex(aa, ac, -0.5).uv(o, q).normal(0.0f, 1.0f, 0.0f).endVertex();
                bufferBuilder.vertex(ab, ac, -0.5).uv(p, q).normal(0.0f, 1.0f, 0.0f).endVertex();
                bufferBuilder.vertex(ab, ac, 0.5).uv(p, r).normal(0.0f, 1.0f, 0.0f).endVertex();
                bufferBuilder.vertex(aa, ac, 0.5).uv(o, r).normal(0.0f, 1.0f, 0.0f).endVertex();
                bufferBuilder.vertex(aa, ad, 0.5).uv(o, q).normal(0.0f, -1.0f, 0.0f).endVertex();
                bufferBuilder.vertex(ab, ad, 0.5).uv(p, q).normal(0.0f, -1.0f, 0.0f).endVertex();
                bufferBuilder.vertex(ab, ad, -0.5).uv(p, r).normal(0.0f, -1.0f, 0.0f).endVertex();
                bufferBuilder.vertex(aa, ad, -0.5).uv(o, r).normal(0.0f, -1.0f, 0.0f).endVertex();
                bufferBuilder.vertex(aa, ac, 0.5).uv(t, u).normal(-1.0f, 0.0f, 0.0f).endVertex();
                bufferBuilder.vertex(aa, ad, 0.5).uv(t, v).normal(-1.0f, 0.0f, 0.0f).endVertex();
                bufferBuilder.vertex(aa, ad, -0.5).uv(s, v).normal(-1.0f, 0.0f, 0.0f).endVertex();
                bufferBuilder.vertex(aa, ac, -0.5).uv(s, u).normal(-1.0f, 0.0f, 0.0f).endVertex();
                bufferBuilder.vertex(ab, ac, -0.5).uv(t, u).normal(1.0f, 0.0f, 0.0f).endVertex();
                bufferBuilder.vertex(ab, ad, -0.5).uv(t, v).normal(1.0f, 0.0f, 0.0f).endVertex();
                bufferBuilder.vertex(ab, ad, 0.5).uv(s, v).normal(1.0f, 0.0f, 0.0f).endVertex();
                bufferBuilder.vertex(ab, ac, 0.5).uv(s, u).normal(1.0f, 0.0f, 0.0f).endVertex();
                tesselator.end();
            }
        }
    }

    private void setBrightness(Painting painting, float f, float g) {
        int i = Mth.floor(painting.x);
        int j = Mth.floor(painting.y + (double)(g / 16.0f));
        int k = Mth.floor(painting.z);
        Direction direction = painting.getDirection();
        if (direction == Direction.NORTH) {
            i = Mth.floor(painting.x + (double)(f / 16.0f));
        }
        if (direction == Direction.WEST) {
            k = Mth.floor(painting.z - (double)(f / 16.0f));
        }
        if (direction == Direction.SOUTH) {
            i = Mth.floor(painting.x - (double)(f / 16.0f));
        }
        if (direction == Direction.EAST) {
            k = Mth.floor(painting.z + (double)(f / 16.0f));
        }
        int l = this.entityRenderDispatcher.level.getLightColor(new BlockPos(i, j, k));
        int m = l % 65536;
        int n = l / 65536;
        RenderSystem.glMultiTexCoord2f(33985, m, n);
        RenderSystem.color3f(1.0f, 1.0f, 1.0f);
    }
}

