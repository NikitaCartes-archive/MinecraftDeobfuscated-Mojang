/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.renderer.entity;

import com.mojang.blaze3d.platform.GlStateManager;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.SkullModel;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.projectile.WitherSkull;

@Environment(value=EnvType.CLIENT)
public class WitherSkullRenderer
extends EntityRenderer<WitherSkull> {
    private static final ResourceLocation WITHER_INVULNERABLE_LOCATION = new ResourceLocation("textures/entity/wither/wither_invulnerable.png");
    private static final ResourceLocation WITHER_LOCATION = new ResourceLocation("textures/entity/wither/wither.png");
    private final SkullModel model = new SkullModel();

    public WitherSkullRenderer(EntityRenderDispatcher entityRenderDispatcher) {
        super(entityRenderDispatcher);
    }

    private float rotlerp(float f, float g, float h) {
        float i;
        for (i = g - f; i < -180.0f; i += 360.0f) {
        }
        while (i >= 180.0f) {
            i -= 360.0f;
        }
        return f + h * i;
    }

    @Override
    public void render(WitherSkull witherSkull, double d, double e, double f, float g, float h) {
        GlStateManager.pushMatrix();
        GlStateManager.disableCull();
        float i = this.rotlerp(witherSkull.yRotO, witherSkull.yRot, h);
        float j = Mth.lerp(h, witherSkull.xRotO, witherSkull.xRot);
        GlStateManager.translatef((float)d, (float)e, (float)f);
        float k = 0.0625f;
        GlStateManager.enableRescaleNormal();
        GlStateManager.scalef(-1.0f, -1.0f, 1.0f);
        GlStateManager.enableAlphaTest();
        this.bindTexture(witherSkull);
        if (this.solidRender) {
            GlStateManager.enableColorMaterial();
            GlStateManager.setupSolidRenderingTextureCombine(this.getTeamColor(witherSkull));
        }
        this.model.render(0.0f, 0.0f, 0.0f, i, j, 0.0625f);
        if (this.solidRender) {
            GlStateManager.tearDownSolidRenderingTextureCombine();
            GlStateManager.disableColorMaterial();
        }
        GlStateManager.popMatrix();
        super.render(witherSkull, d, e, f, g, h);
    }

    @Override
    protected ResourceLocation getTextureLocation(WitherSkull witherSkull) {
        return witherSkull.isDangerous() ? WITHER_INVULNERABLE_LOCATION : WITHER_LOCATION;
    }
}

