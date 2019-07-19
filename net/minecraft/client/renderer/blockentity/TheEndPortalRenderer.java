/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.renderer.blockentity;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.platform.MemoryTracker;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import java.nio.FloatBuffer;
import java.util.Random;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.entity.TheEndPortalBlockEntity;

@Environment(value=EnvType.CLIENT)
public class TheEndPortalRenderer
extends BlockEntityRenderer<TheEndPortalBlockEntity> {
    private static final ResourceLocation END_SKY_LOCATION = new ResourceLocation("textures/environment/end_sky.png");
    private static final ResourceLocation END_PORTAL_LOCATION = new ResourceLocation("textures/entity/end_portal.png");
    private static final Random RANDOM = new Random(31100L);
    private static final FloatBuffer MODELVIEW = MemoryTracker.createFloatBuffer(16);
    private static final FloatBuffer PROJECTION = MemoryTracker.createFloatBuffer(16);
    private final FloatBuffer buffer = MemoryTracker.createFloatBuffer(16);

    @Override
    public void render(TheEndPortalBlockEntity theEndPortalBlockEntity, double d, double e, double f, float g, int i) {
        GlStateManager.disableLighting();
        RANDOM.setSeed(31100L);
        GlStateManager.getMatrix(2982, MODELVIEW);
        GlStateManager.getMatrix(2983, PROJECTION);
        double h = d * d + e * e + f * f;
        int j = this.getPasses(h);
        float k = this.getOffset();
        boolean bl = false;
        GameRenderer gameRenderer = Minecraft.getInstance().gameRenderer;
        for (int l = 0; l < j; ++l) {
            GlStateManager.pushMatrix();
            float m = 2.0f / (float)(18 - l);
            if (l == 0) {
                this.bindTexture(END_SKY_LOCATION);
                m = 0.15f;
                GlStateManager.enableBlend();
                GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
            }
            if (l >= 1) {
                this.bindTexture(END_PORTAL_LOCATION);
                bl = true;
                gameRenderer.resetFogColor(true);
            }
            if (l == 1) {
                GlStateManager.enableBlend();
                GlStateManager.blendFunc(GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ONE);
            }
            GlStateManager.texGenMode(GlStateManager.TexGen.S, 9216);
            GlStateManager.texGenMode(GlStateManager.TexGen.T, 9216);
            GlStateManager.texGenMode(GlStateManager.TexGen.R, 9216);
            GlStateManager.texGenParam(GlStateManager.TexGen.S, 9474, this.getBuffer(1.0f, 0.0f, 0.0f, 0.0f));
            GlStateManager.texGenParam(GlStateManager.TexGen.T, 9474, this.getBuffer(0.0f, 1.0f, 0.0f, 0.0f));
            GlStateManager.texGenParam(GlStateManager.TexGen.R, 9474, this.getBuffer(0.0f, 0.0f, 1.0f, 0.0f));
            GlStateManager.enableTexGen(GlStateManager.TexGen.S);
            GlStateManager.enableTexGen(GlStateManager.TexGen.T);
            GlStateManager.enableTexGen(GlStateManager.TexGen.R);
            GlStateManager.popMatrix();
            GlStateManager.matrixMode(5890);
            GlStateManager.pushMatrix();
            GlStateManager.loadIdentity();
            GlStateManager.translatef(0.5f, 0.5f, 0.0f);
            GlStateManager.scalef(0.5f, 0.5f, 1.0f);
            float n = l + 1;
            GlStateManager.translatef(17.0f / n, (2.0f + n / 1.5f) * ((float)(Util.getMillis() % 800000L) / 800000.0f), 0.0f);
            GlStateManager.rotatef((n * n * 4321.0f + n * 9.0f) * 2.0f, 0.0f, 0.0f, 1.0f);
            GlStateManager.scalef(4.5f - n / 4.0f, 4.5f - n / 4.0f, 1.0f);
            GlStateManager.multMatrix(PROJECTION);
            GlStateManager.multMatrix(MODELVIEW);
            Tesselator tesselator = Tesselator.getInstance();
            BufferBuilder bufferBuilder = tesselator.getBuilder();
            bufferBuilder.begin(7, DefaultVertexFormat.POSITION_COLOR);
            float o = (RANDOM.nextFloat() * 0.5f + 0.1f) * m;
            float p = (RANDOM.nextFloat() * 0.5f + 0.4f) * m;
            float q = (RANDOM.nextFloat() * 0.5f + 0.5f) * m;
            if (theEndPortalBlockEntity.shouldRenderFace(Direction.SOUTH)) {
                bufferBuilder.vertex(d, e, f + 1.0).color(o, p, q, 1.0f).endVertex();
                bufferBuilder.vertex(d + 1.0, e, f + 1.0).color(o, p, q, 1.0f).endVertex();
                bufferBuilder.vertex(d + 1.0, e + 1.0, f + 1.0).color(o, p, q, 1.0f).endVertex();
                bufferBuilder.vertex(d, e + 1.0, f + 1.0).color(o, p, q, 1.0f).endVertex();
            }
            if (theEndPortalBlockEntity.shouldRenderFace(Direction.NORTH)) {
                bufferBuilder.vertex(d, e + 1.0, f).color(o, p, q, 1.0f).endVertex();
                bufferBuilder.vertex(d + 1.0, e + 1.0, f).color(o, p, q, 1.0f).endVertex();
                bufferBuilder.vertex(d + 1.0, e, f).color(o, p, q, 1.0f).endVertex();
                bufferBuilder.vertex(d, e, f).color(o, p, q, 1.0f).endVertex();
            }
            if (theEndPortalBlockEntity.shouldRenderFace(Direction.EAST)) {
                bufferBuilder.vertex(d + 1.0, e + 1.0, f).color(o, p, q, 1.0f).endVertex();
                bufferBuilder.vertex(d + 1.0, e + 1.0, f + 1.0).color(o, p, q, 1.0f).endVertex();
                bufferBuilder.vertex(d + 1.0, e, f + 1.0).color(o, p, q, 1.0f).endVertex();
                bufferBuilder.vertex(d + 1.0, e, f).color(o, p, q, 1.0f).endVertex();
            }
            if (theEndPortalBlockEntity.shouldRenderFace(Direction.WEST)) {
                bufferBuilder.vertex(d, e, f).color(o, p, q, 1.0f).endVertex();
                bufferBuilder.vertex(d, e, f + 1.0).color(o, p, q, 1.0f).endVertex();
                bufferBuilder.vertex(d, e + 1.0, f + 1.0).color(o, p, q, 1.0f).endVertex();
                bufferBuilder.vertex(d, e + 1.0, f).color(o, p, q, 1.0f).endVertex();
            }
            if (theEndPortalBlockEntity.shouldRenderFace(Direction.DOWN)) {
                bufferBuilder.vertex(d, e, f).color(o, p, q, 1.0f).endVertex();
                bufferBuilder.vertex(d + 1.0, e, f).color(o, p, q, 1.0f).endVertex();
                bufferBuilder.vertex(d + 1.0, e, f + 1.0).color(o, p, q, 1.0f).endVertex();
                bufferBuilder.vertex(d, e, f + 1.0).color(o, p, q, 1.0f).endVertex();
            }
            if (theEndPortalBlockEntity.shouldRenderFace(Direction.UP)) {
                bufferBuilder.vertex(d, e + (double)k, f + 1.0).color(o, p, q, 1.0f).endVertex();
                bufferBuilder.vertex(d + 1.0, e + (double)k, f + 1.0).color(o, p, q, 1.0f).endVertex();
                bufferBuilder.vertex(d + 1.0, e + (double)k, f).color(o, p, q, 1.0f).endVertex();
                bufferBuilder.vertex(d, e + (double)k, f).color(o, p, q, 1.0f).endVertex();
            }
            tesselator.end();
            GlStateManager.popMatrix();
            GlStateManager.matrixMode(5888);
            this.bindTexture(END_SKY_LOCATION);
        }
        GlStateManager.disableBlend();
        GlStateManager.disableTexGen(GlStateManager.TexGen.S);
        GlStateManager.disableTexGen(GlStateManager.TexGen.T);
        GlStateManager.disableTexGen(GlStateManager.TexGen.R);
        GlStateManager.enableLighting();
        if (bl) {
            gameRenderer.resetFogColor(false);
        }
    }

    protected int getPasses(double d) {
        int i = d > 36864.0 ? 1 : (d > 25600.0 ? 3 : (d > 16384.0 ? 5 : (d > 9216.0 ? 7 : (d > 4096.0 ? 9 : (d > 1024.0 ? 11 : (d > 576.0 ? 13 : (d > 256.0 ? 14 : 15)))))));
        return i;
    }

    protected float getOffset() {
        return 0.75f;
    }

    private FloatBuffer getBuffer(float f, float g, float h, float i) {
        this.buffer.clear();
        this.buffer.put(f).put(g).put(h).put(i);
        this.buffer.flip();
        return this.buffer;
    }
}

