/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.renderer.entity;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.ChickenModel;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.Chicken;

@Environment(value=EnvType.CLIENT)
public class ChickenRenderer
extends MobRenderer<Chicken, ChickenModel<Chicken>> {
    private static final ResourceLocation CHICKEN_LOCATION = new ResourceLocation("textures/entity/chicken.png");

    public ChickenRenderer(EntityRenderDispatcher entityRenderDispatcher) {
        super(entityRenderDispatcher, new ChickenModel(), 0.3f);
    }

    @Override
    protected ResourceLocation getTextureLocation(Chicken chicken) {
        return CHICKEN_LOCATION;
    }

    @Override
    protected float getBob(Chicken chicken, float f) {
        float g = Mth.lerp(f, chicken.oFlap, chicken.flap);
        float h = Mth.lerp(f, chicken.oFlapSpeed, chicken.flapSpeed);
        return (Mth.sin(g) + 1.0f) * h;
    }

    @Override
    protected /* synthetic */ float getBob(LivingEntity livingEntity, float f) {
        return this.getBob((Chicken)livingEntity, f);
    }
}

