/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.renderer.entity;

import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.SkeletonRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.monster.AbstractSkeleton;

@Environment(value=EnvType.CLIENT)
public class WitherSkeletonRenderer
extends SkeletonRenderer {
    private static final ResourceLocation WITHER_SKELETON_LOCATION = new ResourceLocation("textures/entity/skeleton/wither_skeleton.png");

    public WitherSkeletonRenderer(EntityRenderDispatcher entityRenderDispatcher) {
        super(entityRenderDispatcher);
    }

    @Override
    protected ResourceLocation getTextureLocation(AbstractSkeleton abstractSkeleton) {
        return WITHER_SKELETON_LOCATION;
    }

    @Override
    protected void scale(AbstractSkeleton abstractSkeleton, float f) {
        RenderSystem.scalef(1.2f, 1.2f, 1.2f);
    }
}

