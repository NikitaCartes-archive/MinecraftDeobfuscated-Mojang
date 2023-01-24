/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.renderer.entity;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.SkeletonModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.HumanoidMobRenderer;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.AbstractSkeleton;

@Environment(value=EnvType.CLIENT)
public class SkeletonRenderer
extends HumanoidMobRenderer<AbstractSkeleton, SkeletonModel<AbstractSkeleton>> {
    private static final ResourceLocation SKELETON_LOCATION = new ResourceLocation("textures/entity/skeleton/skeleton.png");

    public SkeletonRenderer(EntityRendererProvider.Context context) {
        this(context, ModelLayers.SKELETON, ModelLayers.SKELETON_INNER_ARMOR, ModelLayers.SKELETON_OUTER_ARMOR);
    }

    public SkeletonRenderer(EntityRendererProvider.Context context, ModelLayerLocation modelLayerLocation, ModelLayerLocation modelLayerLocation2, ModelLayerLocation modelLayerLocation3) {
        super(context, new SkeletonModel(context.bakeLayer(modelLayerLocation)), 0.5f);
        this.addLayer(new HumanoidArmorLayer(this, new SkeletonModel(context.bakeLayer(modelLayerLocation2)), new SkeletonModel(context.bakeLayer(modelLayerLocation3)), context.getModelManager()));
    }

    @Override
    public ResourceLocation getTextureLocation(AbstractSkeleton abstractSkeleton) {
        return SKELETON_LOCATION;
    }

    @Override
    protected boolean isShaking(AbstractSkeleton abstractSkeleton) {
        return abstractSkeleton.isShaking();
    }

    @Override
    protected /* synthetic */ boolean isShaking(LivingEntity livingEntity) {
        return this.isShaking((AbstractSkeleton)livingEntity);
    }
}

