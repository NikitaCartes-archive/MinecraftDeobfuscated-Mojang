/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.renderer.entity;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.ZombieModel;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.HumanoidMobRenderer;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Zombie;

@Environment(value=EnvType.CLIENT)
public abstract class AbstractZombieRenderer<T extends Zombie, M extends ZombieModel<T>>
extends HumanoidMobRenderer<T, M> {
    private static final ResourceLocation ZOMBIE_LOCATION = new ResourceLocation("textures/entity/zombie/zombie.png");

    protected AbstractZombieRenderer(EntityRendererProvider.Context context, M zombieModel, M zombieModel2, M zombieModel3) {
        super(context, zombieModel, 0.5f);
        this.addLayer(new HumanoidArmorLayer(this, zombieModel2, zombieModel3, context.getModelManager()));
    }

    @Override
    public ResourceLocation getTextureLocation(Zombie zombie) {
        return ZOMBIE_LOCATION;
    }

    @Override
    protected boolean isShaking(T zombie) {
        return super.isShaking(zombie) || ((Zombie)zombie).isUnderWaterConverting();
    }

    @Override
    protected /* synthetic */ boolean isShaking(LivingEntity livingEntity) {
        return this.isShaking((T)((Zombie)livingEntity));
    }
}

