/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.renderer.entity;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.HoglinModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.hoglin.Hoglin;

@Environment(value=EnvType.CLIENT)
public class HoglinRenderer
extends MobRenderer<Hoglin, HoglinModel<Hoglin>> {
    private static final ResourceLocation HOGLIN_LOCATION = new ResourceLocation("textures/entity/hoglin/hoglin.png");

    public HoglinRenderer(EntityRendererProvider.Context context) {
        super(context, new HoglinModel(context.getLayer(ModelLayers.HOGLIN)), 0.7f);
    }

    @Override
    public ResourceLocation getTextureLocation(Hoglin hoglin) {
        return HOGLIN_LOCATION;
    }

    @Override
    protected boolean isShaking(Hoglin hoglin) {
        return hoglin.isConverting();
    }

    @Override
    protected /* synthetic */ boolean isShaking(LivingEntity livingEntity) {
        return this.isShaking((Hoglin)livingEntity);
    }
}

