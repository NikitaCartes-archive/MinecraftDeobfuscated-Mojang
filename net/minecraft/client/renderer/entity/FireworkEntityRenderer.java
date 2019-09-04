/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.renderer.entity;

import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.projectile.FireworkRocketEntity;

@Environment(value=EnvType.CLIENT)
public class FireworkEntityRenderer
extends EntityRenderer<FireworkRocketEntity> {
    private final ItemRenderer itemRenderer;

    public FireworkEntityRenderer(EntityRenderDispatcher entityRenderDispatcher, ItemRenderer itemRenderer) {
        super(entityRenderDispatcher);
        this.itemRenderer = itemRenderer;
    }

    @Override
    public void render(FireworkRocketEntity fireworkRocketEntity, double d, double e, double f, float g, float h) {
        RenderSystem.pushMatrix();
        RenderSystem.translatef((float)d, (float)e, (float)f);
        RenderSystem.enableRescaleNormal();
        RenderSystem.rotatef(-this.entityRenderDispatcher.playerRotY, 0.0f, 1.0f, 0.0f);
        RenderSystem.rotatef((float)(this.entityRenderDispatcher.options.thirdPersonView == 2 ? -1 : 1) * this.entityRenderDispatcher.playerRotX, 1.0f, 0.0f, 0.0f);
        if (fireworkRocketEntity.isShotAtAngle()) {
            RenderSystem.rotatef(90.0f, 1.0f, 0.0f, 0.0f);
        } else {
            RenderSystem.rotatef(180.0f, 0.0f, 1.0f, 0.0f);
        }
        this.bindTexture(TextureAtlas.LOCATION_BLOCKS);
        if (this.solidRender) {
            RenderSystem.enableColorMaterial();
            RenderSystem.setupSolidRenderingTextureCombine(this.getTeamColor(fireworkRocketEntity));
        }
        this.itemRenderer.renderStatic(fireworkRocketEntity.getItem(), ItemTransforms.TransformType.GROUND);
        if (this.solidRender) {
            RenderSystem.tearDownSolidRenderingTextureCombine();
            RenderSystem.disableColorMaterial();
        }
        RenderSystem.disableRescaleNormal();
        RenderSystem.popMatrix();
        super.render(fireworkRocketEntity, d, e, f, g, h);
    }

    @Override
    protected ResourceLocation getTextureLocation(FireworkRocketEntity fireworkRocketEntity) {
        return TextureAtlas.LOCATION_BLOCKS;
    }
}

