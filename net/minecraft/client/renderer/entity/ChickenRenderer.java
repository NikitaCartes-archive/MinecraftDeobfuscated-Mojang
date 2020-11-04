/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.renderer.entity;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.ChickenModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.animal.Chicken;

@Environment(value=EnvType.CLIENT)
public class ChickenRenderer
extends MobRenderer<Chicken, ChickenModel<Chicken>> {
    private static final ResourceLocation CHICKEN_LOCATION = new ResourceLocation("textures/entity/chicken.png");

    public ChickenRenderer(EntityRendererProvider.Context context) {
        super(context, new ChickenModel(context.getLayer(ModelLayers.CHICKEN)), 0.3f);
    }

    @Override
    public ResourceLocation getTextureLocation(Chicken chicken) {
        return CHICKEN_LOCATION;
    }

    @Override
    protected float getBob(Chicken chicken, float f) {
        float g = Mth.lerp(f, chicken.oFlap, chicken.flap);
        float h = Mth.lerp(f, chicken.oFlapSpeed, chicken.flapSpeed);
        return (Mth.sin(g) + 1.0f) * h;
    }
}

