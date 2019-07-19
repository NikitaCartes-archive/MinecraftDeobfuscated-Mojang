/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.renderer.entity;

import com.mojang.blaze3d.platform.GLX;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.TridentModel;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.ThrownTrident;

@Environment(value=EnvType.CLIENT)
public class ThrownTridentRenderer
extends EntityRenderer<ThrownTrident> {
    public static final ResourceLocation TRIDENT_LOCATION = new ResourceLocation("textures/entity/trident.png");
    private final TridentModel model = new TridentModel();

    public ThrownTridentRenderer(EntityRenderDispatcher entityRenderDispatcher) {
        super(entityRenderDispatcher);
    }

    @Override
    public void render(ThrownTrident thrownTrident, double d, double e, double f, float g, float h) {
        this.bindTexture(thrownTrident);
        GlStateManager.color4f(1.0f, 1.0f, 1.0f, 1.0f);
        GlStateManager.pushMatrix();
        GlStateManager.disableLighting();
        GlStateManager.translatef((float)d, (float)e, (float)f);
        GlStateManager.rotatef(Mth.lerp(h, thrownTrident.yRotO, thrownTrident.yRot) - 90.0f, 0.0f, 1.0f, 0.0f);
        GlStateManager.rotatef(Mth.lerp(h, thrownTrident.xRotO, thrownTrident.xRot) + 90.0f, 0.0f, 0.0f, 1.0f);
        this.model.render();
        GlStateManager.popMatrix();
        this.renderLeash(thrownTrident, d, e, f, g, h);
        super.render(thrownTrident, d, e, f, g, h);
        GlStateManager.enableLighting();
    }

    @Override
    protected ResourceLocation getTextureLocation(ThrownTrident thrownTrident) {
        return TRIDENT_LOCATION;
    }

    protected void renderLeash(ThrownTrident thrownTrident, double d, double e, double f, float g, float h) {
        float am;
        float al;
        float ak;
        double aj;
        double ai;
        double ah;
        double ag;
        float af;
        double ae;
        int ad;
        Entity entity = thrownTrident.getOwner();
        if (entity == null || !thrownTrident.isNoPhysics()) {
            return;
        }
        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder bufferBuilder = tesselator.getBuilder();
        double i = Mth.lerp(h * 0.5f, entity.yRot, entity.yRotO) * ((float)Math.PI / 180);
        double j = Math.cos(i);
        double k = Math.sin(i);
        double l = Mth.lerp((double)h, entity.xo, entity.x);
        double m = Mth.lerp((double)h, entity.yo + (double)entity.getEyeHeight() * 0.8, entity.y + (double)entity.getEyeHeight() * 0.8);
        double n = Mth.lerp((double)h, entity.zo, entity.z);
        double o = j - k;
        double p = k + j;
        double q = Mth.lerp((double)h, thrownTrident.xo, thrownTrident.x);
        double r = Mth.lerp((double)h, thrownTrident.yo, thrownTrident.y);
        double s = Mth.lerp((double)h, thrownTrident.zo, thrownTrident.z);
        double t = (float)(l - q);
        double u = (float)(m - r);
        double v = (float)(n - s);
        double w = Math.sqrt(t * t + u * u + v * v);
        int x = thrownTrident.getId() + thrownTrident.tickCount;
        double y = (double)((float)x + h) * -0.1;
        double z = Math.min(0.5, w / 30.0);
        GlStateManager.disableTexture();
        GlStateManager.disableLighting();
        GlStateManager.disableCull();
        GLX.glMultiTexCoord2f(GLX.GL_TEXTURE1, 255.0f, 255.0f);
        bufferBuilder.begin(5, DefaultVertexFormat.POSITION_COLOR);
        int aa = 37;
        int ab = 7 - x % 7;
        double ac = 0.1;
        for (ad = 0; ad <= 37; ++ad) {
            ae = (double)ad / 37.0;
            af = 1.0f - (float)((ad + ab) % 7) / 7.0f;
            ag = ae * 2.0 - 1.0;
            ag = (1.0 - ag * ag) * z;
            ah = d + t * ae + Math.sin(ae * Math.PI * 8.0 + y) * o * ag;
            ai = e + u * ae + Math.cos(ae * Math.PI * 8.0 + y) * 0.02 + (0.1 + ag) * 1.0;
            aj = f + v * ae + Math.sin(ae * Math.PI * 8.0 + y) * p * ag;
            ak = 0.87f * af + 0.3f * (1.0f - af);
            al = 0.91f * af + 0.6f * (1.0f - af);
            am = 0.85f * af + 0.5f * (1.0f - af);
            bufferBuilder.vertex(ah, ai, aj).color(ak, al, am, 1.0f).endVertex();
            bufferBuilder.vertex(ah + 0.1 * ag, ai + 0.1 * ag, aj).color(ak, al, am, 1.0f).endVertex();
            if (ad > thrownTrident.clientSideReturnTridentTickCount * 2) break;
        }
        tesselator.end();
        bufferBuilder.begin(5, DefaultVertexFormat.POSITION_COLOR);
        for (ad = 0; ad <= 37; ++ad) {
            ae = (double)ad / 37.0;
            af = 1.0f - (float)((ad + ab) % 7) / 7.0f;
            ag = ae * 2.0 - 1.0;
            ag = (1.0 - ag * ag) * z;
            ah = d + t * ae + Math.sin(ae * Math.PI * 8.0 + y) * o * ag;
            ai = e + u * ae + Math.cos(ae * Math.PI * 8.0 + y) * 0.01 + (0.1 + ag) * 1.0;
            aj = f + v * ae + Math.sin(ae * Math.PI * 8.0 + y) * p * ag;
            ak = 0.87f * af + 0.3f * (1.0f - af);
            al = 0.91f * af + 0.6f * (1.0f - af);
            am = 0.85f * af + 0.5f * (1.0f - af);
            bufferBuilder.vertex(ah, ai, aj).color(ak, al, am, 1.0f).endVertex();
            bufferBuilder.vertex(ah + 0.1 * ag, ai, aj + 0.1 * ag).color(ak, al, am, 1.0f).endVertex();
            if (ad > thrownTrident.clientSideReturnTridentTickCount * 2) break;
        }
        tesselator.end();
        GlStateManager.enableLighting();
        GlStateManager.enableTexture();
        GlStateManager.enableCull();
    }
}

