/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.PiglinModel;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.HumanoidMobRenderer;
import net.minecraft.client.renderer.entity.layers.PiglinArmorLayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.monster.piglin.Piglin;

@Environment(value=EnvType.CLIENT)
public class PiglinRenderer
extends HumanoidMobRenderer<Piglin, PiglinModel<Piglin>> {
    private static final ResourceLocation PIGLIN_LOCATION = new ResourceLocation("textures/entity/piglin/piglin.png");

    public PiglinRenderer(EntityRenderDispatcher entityRenderDispatcher) {
        super(entityRenderDispatcher, new PiglinModel(0.0f, 128, 64), 0.5f);
        this.addLayer(new PiglinArmorLayer(this, new HumanoidModel(0.5f), new HumanoidModel(1.0f), PiglinRenderer.makeHelmetHeadModel()));
    }

    private static <T extends Piglin> PiglinModel<T> makeHelmetHeadModel() {
        PiglinModel piglinModel = new PiglinModel(1.0f, 64, 16);
        piglinModel.earLeft.visible = false;
        piglinModel.earRight.visible = false;
        return piglinModel;
    }

    @Override
    public ResourceLocation getTextureLocation(Piglin piglin) {
        return PIGLIN_LOCATION;
    }

    @Override
    protected void setupRotations(Piglin piglin, PoseStack poseStack, float f, float g, float h) {
        if (piglin.isConverting()) {
            g += (float)(Math.cos((double)piglin.tickCount * 3.25) * Math.PI * 0.5);
        }
        super.setupRotations(piglin, poseStack, f, g, h);
    }
}

