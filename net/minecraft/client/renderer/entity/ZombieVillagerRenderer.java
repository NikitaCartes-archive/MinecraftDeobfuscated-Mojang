/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.renderer.entity;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.ZombieVillagerModel;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.HumanoidMobRenderer;
import net.minecraft.client.renderer.entity.VillagerProfessionLayer;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ReloadableResourceManager;
import net.minecraft.world.entity.monster.ZombieVillager;

@Environment(value=EnvType.CLIENT)
public class ZombieVillagerRenderer
extends HumanoidMobRenderer<ZombieVillager, ZombieVillagerModel<ZombieVillager>> {
    private static final ResourceLocation ZOMBIE_VILLAGER_LOCATION = new ResourceLocation("textures/entity/zombie_villager/zombie_villager.png");

    public ZombieVillagerRenderer(EntityRenderDispatcher entityRenderDispatcher, ReloadableResourceManager reloadableResourceManager) {
        super(entityRenderDispatcher, new ZombieVillagerModel(), 0.5f);
        this.addLayer(new HumanoidArmorLayer(this, new ZombieVillagerModel(0.5f, true), new ZombieVillagerModel(1.0f, true)));
        this.addLayer(new VillagerProfessionLayer<ZombieVillager, ZombieVillagerModel<ZombieVillager>>(this, reloadableResourceManager, "zombie_villager"));
    }

    @Override
    protected ResourceLocation getTextureLocation(ZombieVillager zombieVillager) {
        return ZOMBIE_VILLAGER_LOCATION;
    }

    @Override
    protected void setupRotations(ZombieVillager zombieVillager, float f, float g, float h) {
        if (zombieVillager.isConverting()) {
            g += (float)(Math.cos((double)zombieVillager.tickCount * 3.25) * Math.PI * 0.25);
        }
        super.setupRotations(zombieVillager, f, g, h);
    }
}

