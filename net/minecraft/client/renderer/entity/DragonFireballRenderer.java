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
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.projectile.DragonFireball;

@Environment(value=EnvType.CLIENT)
public class DragonFireballRenderer
extends EntityRenderer<DragonFireball> {
    private static final ResourceLocation TEXTURE_LOCATION = new ResourceLocation("textures/entity/enderdragon/dragon_fireball.png");

    public DragonFireballRenderer(EntityRenderDispatcher entityRenderDispatcher) {
        super(entityRenderDispatcher);
    }

    @Override
    public void render(DragonFireball dragonFireball, double d, double e, double f, float g, float h) {
        RenderSystem.pushMatrix();
        this.bindTexture(dragonFireball);
        RenderSystem.translatef((float)d, (float)e, (float)f);
        RenderSystem.enableRescaleNormal();
        RenderSystem.scalef(2.0f, 2.0f, 2.0f);
        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder bufferBuilder = tesselator.getBuilder();
        float i = 1.0f;
        float j = 0.5f;
        float k = 0.25f;
        RenderSystem.rotatef(180.0f - this.entityRenderDispatcher.playerRotY, 0.0f, 1.0f, 0.0f);
        RenderSystem.rotatef((float)(this.entityRenderDispatcher.options.thirdPersonView == 2 ? -1 : 1) * -this.entityRenderDispatcher.playerRotX, 1.0f, 0.0f, 0.0f);
        if (this.solidRender) {
            RenderSystem.enableColorMaterial();
            RenderSystem.setupSolidRenderingTextureCombine(this.getTeamColor(dragonFireball));
        }
        bufferBuilder.begin(7, DefaultVertexFormat.POSITION_TEX_NORMAL);
        bufferBuilder.vertex(-0.5, -0.25, 0.0).uv(0.0, 1.0).normal(0.0f, 1.0f, 0.0f).endVertex();
        bufferBuilder.vertex(0.5, -0.25, 0.0).uv(1.0, 1.0).normal(0.0f, 1.0f, 0.0f).endVertex();
        bufferBuilder.vertex(0.5, 0.75, 0.0).uv(1.0, 0.0).normal(0.0f, 1.0f, 0.0f).endVertex();
        bufferBuilder.vertex(-0.5, 0.75, 0.0).uv(0.0, 0.0).normal(0.0f, 1.0f, 0.0f).endVertex();
        tesselator.end();
        if (this.solidRender) {
            RenderSystem.tearDownSolidRenderingTextureCombine();
            RenderSystem.disableColorMaterial();
        }
        RenderSystem.disableRescaleNormal();
        RenderSystem.popMatrix();
        super.render(dragonFireball, d, e, f, g, h);
    }

    @Override
    protected ResourceLocation getTextureLocation(DragonFireball dragonFireball) {
        return TEXTURE_LOCATION;
    }
}

