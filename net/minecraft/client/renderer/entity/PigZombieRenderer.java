/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.renderer.entity;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.ZombieModel;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.HumanoidMobRenderer;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.monster.PigZombie;

@Environment(value=EnvType.CLIENT)
public class PigZombieRenderer
extends HumanoidMobRenderer<PigZombie, ZombieModel<PigZombie>> {
    private static final ResourceLocation ZOMBIE_PIGMAN_LOCATION = new ResourceLocation("textures/entity/zombie_pigman.png");

    public PigZombieRenderer(EntityRenderDispatcher entityRenderDispatcher) {
        super(entityRenderDispatcher, new ZombieModel(0.0f, false), 0.5f);
        this.addLayer(new HumanoidArmorLayer(this, new ZombieModel(0.5f, true), new ZombieModel(1.0f, true)));
    }

    @Override
    public ResourceLocation getTextureLocation(PigZombie pigZombie) {
        return ZOMBIE_PIGMAN_LOCATION;
    }
}

