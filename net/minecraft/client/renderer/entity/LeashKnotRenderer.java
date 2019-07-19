/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.renderer.entity;

import com.mojang.blaze3d.platform.GlStateManager;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.LeashKnotModel;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.decoration.LeashFenceKnotEntity;

@Environment(value=EnvType.CLIENT)
public class LeashKnotRenderer
extends EntityRenderer<LeashFenceKnotEntity> {
    private static final ResourceLocation KNOT_LOCATION = new ResourceLocation("textures/entity/lead_knot.png");
    private final LeashKnotModel<LeashFenceKnotEntity> model = new LeashKnotModel();

    public LeashKnotRenderer(EntityRenderDispatcher entityRenderDispatcher) {
        super(entityRenderDispatcher);
    }

    @Override
    public void render(LeashFenceKnotEntity leashFenceKnotEntity, double d, double e, double f, float g, float h) {
        GlStateManager.pushMatrix();
        GlStateManager.disableCull();
        GlStateManager.translatef((float)d, (float)e, (float)f);
        float i = 0.0625f;
        GlStateManager.enableRescaleNormal();
        GlStateManager.scalef(-1.0f, -1.0f, 1.0f);
        GlStateManager.enableAlphaTest();
        this.bindTexture(leashFenceKnotEntity);
        if (this.solidRender) {
            GlStateManager.enableColorMaterial();
            GlStateManager.setupSolidRenderingTextureCombine(this.getTeamColor(leashFenceKnotEntity));
        }
        this.model.render(leashFenceKnotEntity, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0625f);
        if (this.solidRender) {
            GlStateManager.tearDownSolidRenderingTextureCombine();
            GlStateManager.disableColorMaterial();
        }
        GlStateManager.popMatrix();
        super.render(leashFenceKnotEntity, d, e, f, g, h);
    }

    @Override
    protected ResourceLocation getTextureLocation(LeashFenceKnotEntity leashFenceKnotEntity) {
        return KNOT_LOCATION;
    }
}

