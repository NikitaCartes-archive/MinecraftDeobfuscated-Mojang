/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.ZombieModel;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.HumanoidMobRenderer;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.monster.Zombie;

@Environment(value=EnvType.CLIENT)
public abstract class AbstractZombieRenderer<T extends Zombie, M extends ZombieModel<T>>
extends HumanoidMobRenderer<T, M> {
    private static final ResourceLocation ZOMBIE_LOCATION = new ResourceLocation("textures/entity/zombie/zombie.png");

    protected AbstractZombieRenderer(EntityRenderDispatcher entityRenderDispatcher, M zombieModel, M zombieModel2, M zombieModel3) {
        super(entityRenderDispatcher, zombieModel, 0.5f);
        this.addLayer(new HumanoidArmorLayer(this, zombieModel2, zombieModel3));
    }

    @Override
    public ResourceLocation getTextureLocation(Zombie zombie) {
        return ZOMBIE_LOCATION;
    }

    @Override
    protected void setupRotations(T zombie, PoseStack poseStack, float f, float g, float h) {
        if (((Zombie)zombie).isUnderWaterConverting()) {
            g += (float)(Math.cos((double)((Zombie)zombie).tickCount * 3.25) * Math.PI * 0.25);
        }
        super.setupRotations(zombie, poseStack, f, g, h);
    }
}

