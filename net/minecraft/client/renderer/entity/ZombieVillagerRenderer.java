/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.renderer.entity;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.ZombieVillagerModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.HumanoidMobRenderer;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.client.renderer.entity.layers.VillagerProfessionLayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.ZombieVillager;

@Environment(value=EnvType.CLIENT)
public class ZombieVillagerRenderer
extends HumanoidMobRenderer<ZombieVillager, ZombieVillagerModel<ZombieVillager>> {
    private static final ResourceLocation ZOMBIE_VILLAGER_LOCATION = new ResourceLocation("textures/entity/zombie_villager/zombie_villager.png");

    public ZombieVillagerRenderer(EntityRendererProvider.Context context) {
        super(context, new ZombieVillagerModel(context.bakeLayer(ModelLayers.ZOMBIE_VILLAGER)), 0.5f);
        this.addLayer(new HumanoidArmorLayer(this, new ZombieVillagerModel(context.bakeLayer(ModelLayers.ZOMBIE_VILLAGER_INNER_ARMOR)), new ZombieVillagerModel(context.bakeLayer(ModelLayers.ZOMBIE_VILLAGER_OUTER_ARMOR)), context.getModelManager()));
        this.addLayer(new VillagerProfessionLayer<ZombieVillager, ZombieVillagerModel<ZombieVillager>>(this, context.getResourceManager(), "zombie_villager"));
    }

    @Override
    public ResourceLocation getTextureLocation(ZombieVillager zombieVillager) {
        return ZOMBIE_VILLAGER_LOCATION;
    }

    @Override
    protected boolean isShaking(ZombieVillager zombieVillager) {
        return super.isShaking(zombieVillager) || zombieVillager.isConverting();
    }

    @Override
    protected /* synthetic */ boolean isShaking(LivingEntity livingEntity) {
        return this.isShaking((ZombieVillager)livingEntity);
    }
}

