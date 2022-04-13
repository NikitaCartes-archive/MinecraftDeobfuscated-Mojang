/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.GiantZombieModel;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
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

    public GiantMobRenderer(EntityRendererProvider.Context context, float f) {
        super(context, new GiantZombieModel(context.bakeLayer(ModelLayers.GIANT)), 0.5f * f);
        this.scale = f;
        this.addLayer(new ItemInHandLayer<Giant, HumanoidModel<Giant>>(this, context.getItemInHandRenderer()));
        this.addLayer(new HumanoidArmorLayer<Giant, HumanoidModel<Giant>, GiantZombieModel>(this, new GiantZombieModel(context.bakeLayer(ModelLayers.GIANT_INNER_ARMOR)), new GiantZombieModel(context.bakeLayer(ModelLayers.GIANT_OUTER_ARMOR))));
    }

    @Override
    protected void scale(Giant giant, PoseStack poseStack, float f) {
        poseStack.scale(this.scale, this.scale, this.scale);
    }

    @Override
    public ResourceLocation getTextureLocation(Giant giant) {
        return ZOMBIE_LOCATION;
    }
}

