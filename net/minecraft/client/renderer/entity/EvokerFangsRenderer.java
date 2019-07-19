/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.renderer.entity;

import com.mojang.blaze3d.platform.GlStateManager;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.EvokerFangsModel;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.projectile.EvokerFangs;

@Environment(value=EnvType.CLIENT)
public class EvokerFangsRenderer
extends EntityRenderer<EvokerFangs> {
    private static final ResourceLocation TEXTURE_LOCATION = new ResourceLocation("textures/entity/illager/evoker_fangs.png");
    private final EvokerFangsModel<EvokerFangs> model = new EvokerFangsModel();

    public EvokerFangsRenderer(EntityRenderDispatcher entityRenderDispatcher) {
        super(entityRenderDispatcher);
    }

    @Override
    public void render(EvokerFangs evokerFangs, double d, double e, double f, float g, float h) {
        float i = evokerFangs.getAnimationProgress(h);
        if (i == 0.0f) {
            return;
        }
        float j = 2.0f;
        if (i > 0.9f) {
            j = (float)((double)j * ((1.0 - (double)i) / (double)0.1f));
        }
        GlStateManager.pushMatrix();
        GlStateManager.disableCull();
        GlStateManager.enableAlphaTest();
        this.bindTexture(evokerFangs);
        GlStateManager.translatef((float)d, (float)e, (float)f);
        GlStateManager.rotatef(90.0f - evokerFangs.yRot, 0.0f, 1.0f, 0.0f);
        GlStateManager.scalef(-j, -j, j);
        float k = 0.03125f;
        GlStateManager.translatef(0.0f, -0.626f, 0.0f);
        this.model.render(evokerFangs, i, 0.0f, 0.0f, evokerFangs.yRot, evokerFangs.xRot, 0.03125f);
        GlStateManager.popMatrix();
        GlStateManager.enableCull();
        super.render(evokerFangs, d, e, f, g, h);
    }

    @Override
    protected ResourceLocation getTextureLocation(EvokerFangs evokerFangs) {
        return TEXTURE_LOCATION;
    }
}

