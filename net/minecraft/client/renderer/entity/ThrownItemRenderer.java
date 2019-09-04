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
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.ItemSupplier;

@Environment(value=EnvType.CLIENT)
public class ThrownItemRenderer<T extends Entity>
extends EntityRenderer<T> {
    private final ItemRenderer itemRenderer;
    private final float scale;

    public ThrownItemRenderer(EntityRenderDispatcher entityRenderDispatcher, ItemRenderer itemRenderer, float f) {
        super(entityRenderDispatcher);
        this.itemRenderer = itemRenderer;
        this.scale = f;
    }

    public ThrownItemRenderer(EntityRenderDispatcher entityRenderDispatcher, ItemRenderer itemRenderer) {
        this(entityRenderDispatcher, itemRenderer, 1.0f);
    }

    @Override
    public void render(T entity, double d, double e, double f, float g, float h) {
        RenderSystem.pushMatrix();
        RenderSystem.translatef((float)d, (float)e, (float)f);
        RenderSystem.enableRescaleNormal();
        RenderSystem.scalef(this.scale, this.scale, this.scale);
        RenderSystem.rotatef(-this.entityRenderDispatcher.playerRotY, 0.0f, 1.0f, 0.0f);
        RenderSystem.rotatef((float)(this.entityRenderDispatcher.options.thirdPersonView == 2 ? -1 : 1) * this.entityRenderDispatcher.playerRotX, 1.0f, 0.0f, 0.0f);
        RenderSystem.rotatef(180.0f, 0.0f, 1.0f, 0.0f);
        this.bindTexture(TextureAtlas.LOCATION_BLOCKS);
        if (this.solidRender) {
            RenderSystem.enableColorMaterial();
            RenderSystem.setupSolidRenderingTextureCombine(this.getTeamColor(entity));
        }
        this.itemRenderer.renderStatic(((ItemSupplier)entity).getItem(), ItemTransforms.TransformType.GROUND);
        if (this.solidRender) {
            RenderSystem.tearDownSolidRenderingTextureCombine();
            RenderSystem.disableColorMaterial();
        }
        RenderSystem.disableRescaleNormal();
        RenderSystem.popMatrix();
        super.render(entity, d, e, f, g, h);
    }

    @Override
    protected ResourceLocation getTextureLocation(Entity entity) {
        return TextureAtlas.LOCATION_BLOCKS;
    }
}

