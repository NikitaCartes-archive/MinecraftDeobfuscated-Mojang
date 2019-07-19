/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.renderer.entity;

import com.mojang.blaze3d.platform.GlStateManager;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.Nullable;

@Environment(value=EnvType.CLIENT)
public class DefaultRenderer
extends EntityRenderer<Entity> {
    public DefaultRenderer(EntityRenderDispatcher entityRenderDispatcher) {
        super(entityRenderDispatcher);
    }

    @Override
    public void render(Entity entity, double d, double e, double f, float g, float h) {
        GlStateManager.pushMatrix();
        DefaultRenderer.render(entity.getBoundingBox(), d - entity.xOld, e - entity.yOld, f - entity.zOld);
        GlStateManager.popMatrix();
        super.render(entity, d, e, f, g, h);
    }

    @Override
    @Nullable
    protected ResourceLocation getTextureLocation(Entity entity) {
        return null;
    }
}

