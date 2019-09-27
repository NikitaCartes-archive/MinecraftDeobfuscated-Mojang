/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.renderer.entity;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.layers.CustomHeadLayer;
import net.minecraft.client.renderer.entity.layers.ElytraLayer;
import net.minecraft.client.renderer.entity.layers.ItemInHandLayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Mob;

@Environment(value=EnvType.CLIENT)
public class HumanoidMobRenderer<T extends Mob, M extends HumanoidModel<T>>
extends MobRenderer<T, M> {
    private static final ResourceLocation DEFAULT_LOCATION = new ResourceLocation("textures/entity/steve.png");

    public HumanoidMobRenderer(EntityRenderDispatcher entityRenderDispatcher, M humanoidModel, float f) {
        super(entityRenderDispatcher, humanoidModel, f);
        this.addLayer(new CustomHeadLayer(this));
        this.addLayer(new ElytraLayer(this));
        this.addLayer(new ItemInHandLayer(this));
    }

    @Override
    public ResourceLocation getTextureLocation(T mob) {
        return DEFAULT_LOCATION;
    }
}

