/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.renderer.entity;

import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.GiantZombieModel;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.client.renderer.entity.layers.ItemInHandLayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.monster.Giant;

@Environment(value=EnvType.CLIENT)
public class GiantMobRenderer
extends MobRenderer<Giant, HumanoidModel<Giant>> {
    private static final ResourceLocation ZOMBIE_LOCATION = new ResourceLocation("textures/entity/zombie/zombie.png");
    private final float scale;

    public GiantMobRenderer(EntityRenderDispatcher entityRenderDispatcher, float f) {
        super(entityRenderDispatcher, new GiantZombieModel(), 0.5f * f);
        this.scale = f;
        this.addLayer(new ItemInHandLayer<Giant, HumanoidModel<Giant>>(this));
        this.addLayer(new HumanoidArmorLayer<Giant, HumanoidModel<Giant>, GiantZombieModel>(this, new GiantZombieModel(0.5f, true), new GiantZombieModel(1.0f, true)));
    }

    @Override
    protected void scale(Giant giant, float f) {
        RenderSystem.scalef(this.scale, this.scale, this.scale);
    }

    @Override
    protected ResourceLocation getTextureLocation(Giant giant) {
        return ZOMBIE_LOCATION;
    }
}

