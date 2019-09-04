/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.renderer.entity;

import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.dragon.EndCrystalModel;
import net.minecraft.client.renderer.culling.Culler;
import net.minecraft.client.renderer.entity.EnderDragonRenderer;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.boss.enderdragon.EndCrystal;

@Environment(value=EnvType.CLIENT)
public class EndCrystalRenderer
extends EntityRenderer<EndCrystal> {
    private static final ResourceLocation END_CRYSTAL_LOCATION = new ResourceLocation("textures/entity/end_crystal/end_crystal.png");
    private final EntityModel<EndCrystal> model = new EndCrystalModel<EndCrystal>(0.0f, true);
    private final EntityModel<EndCrystal> modelWithoutBottom = new EndCrystalModel<EndCrystal>(0.0f, false);

    public EndCrystalRenderer(EntityRenderDispatcher entityRenderDispatcher) {
        super(entityRenderDispatcher);
        this.shadowRadius = 0.5f;
    }

    @Override
    public void render(EndCrystal endCrystal, double d, double e, double f, float g, float h) {
        float i = (float)endCrystal.time + h;
        RenderSystem.pushMatrix();
        RenderSystem.translatef((float)d, (float)e, (float)f);
        this.bindTexture(END_CRYSTAL_LOCATION);
        float j = Mth.sin(i * 0.2f) / 2.0f + 0.5f;
        j = j * j + j;
        if (this.solidRender) {
            RenderSystem.enableColorMaterial();
            RenderSystem.setupSolidRenderingTextureCombine(this.getTeamColor(endCrystal));
        }
        if (endCrystal.showsBottom()) {
            this.model.render(endCrystal, 0.0f, i * 3.0f, j * 0.2f, 0.0f, 0.0f, 0.0625f);
        } else {
            this.modelWithoutBottom.render(endCrystal, 0.0f, i * 3.0f, j * 0.2f, 0.0f, 0.0f, 0.0625f);
        }
        if (this.solidRender) {
            RenderSystem.tearDownSolidRenderingTextureCombine();
            RenderSystem.disableColorMaterial();
        }
        RenderSystem.popMatrix();
        BlockPos blockPos = endCrystal.getBeamTarget();
        if (blockPos != null) {
            this.bindTexture(EnderDragonRenderer.CRYSTAL_BEAM_LOCATION);
            float k = (float)blockPos.getX() + 0.5f;
            float l = (float)blockPos.getY() + 0.5f;
            float m = (float)blockPos.getZ() + 0.5f;
            double n = (double)k - endCrystal.x;
            double o = (double)l - endCrystal.y;
            double p = (double)m - endCrystal.z;
            EnderDragonRenderer.renderCrystalBeams(d + n, e - 0.3 + (double)(j * 0.4f) + o, f + p, h, k, l, m, endCrystal.time, endCrystal.x, endCrystal.y, endCrystal.z);
        }
        super.render(endCrystal, d, e, f, g, h);
    }

    @Override
    protected ResourceLocation getTextureLocation(EndCrystal endCrystal) {
        return END_CRYSTAL_LOCATION;
    }

    @Override
    public boolean shouldRender(EndCrystal endCrystal, Culler culler, double d, double e, double f) {
        return super.shouldRender(endCrystal, culler, d, e, f) || endCrystal.getBeamTarget() != null;
    }
}

