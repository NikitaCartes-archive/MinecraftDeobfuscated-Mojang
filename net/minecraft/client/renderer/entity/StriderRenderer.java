/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.StriderModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.layers.SaddleLayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Strider;

@Environment(value=EnvType.CLIENT)
public class StriderRenderer
extends MobRenderer<Strider, StriderModel<Strider>> {
    private static final ResourceLocation STRIDER_LOCATION = new ResourceLocation("textures/entity/strider/strider.png");
    private static final ResourceLocation COLD_LOCATION = new ResourceLocation("textures/entity/strider/strider_cold.png");

    public StriderRenderer(EntityRendererProvider.Context context) {
        super(context, new StriderModel(context.bakeLayer(ModelLayers.STRIDER)), 0.5f);
        this.addLayer(new SaddleLayer(this, new StriderModel(context.bakeLayer(ModelLayers.STRIDER_SADDLE)), new ResourceLocation("textures/entity/strider/strider_saddle.png")));
    }

    @Override
    public ResourceLocation getTextureLocation(Strider strider) {
        return strider.isSuffocating() ? COLD_LOCATION : STRIDER_LOCATION;
    }

    @Override
    protected void scale(Strider strider, PoseStack poseStack, float f) {
        if (strider.isBaby()) {
            poseStack.scale(0.5f, 0.5f, 0.5f);
            this.shadowRadius = 0.25f;
        } else {
            this.shadowRadius = 0.5f;
        }
    }

    @Override
    protected boolean isShaking(Strider strider) {
        return super.isShaking(strider) || strider.isSuffocating();
    }

    @Override
    protected /* synthetic */ boolean isShaking(LivingEntity livingEntity) {
        return this.isShaking((Strider)livingEntity);
    }
}

