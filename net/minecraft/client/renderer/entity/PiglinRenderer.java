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
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.monster.piglin.Piglin;

@Environment(value=EnvType.CLIENT)
public class PiglinRenderer
extends HumanoidMobRenderer<Mob, PiglinModel<Mob>> {
    private static final ResourceLocation PIGLIN_LOCATION = new ResourceLocation("textures/entity/piglin/piglin.png");
    private static final ResourceLocation ZOMBIFIED_PIGLIN_LOCATION = new ResourceLocation("textures/entity/piglin/zombified_piglin.png");

    public PiglinRenderer(EntityRenderDispatcher entityRenderDispatcher, boolean bl) {
        super(entityRenderDispatcher, PiglinRenderer.createModel(bl), 0.5f);
        this.addLayer(new PiglinArmorLayer(this, new HumanoidModel(0.5f), new HumanoidModel(1.0f), PiglinRenderer.makeHelmetHeadModel()));
    }

    private static PiglinModel<Mob> createModel(boolean bl) {
        PiglinModel<Mob> piglinModel = new PiglinModel<Mob>(0.0f, 128, 64);
        if (bl) {
            piglinModel.earLeft.visible = false;
        }
        return piglinModel;
    }

    private static <T extends Piglin> PiglinModel<T> makeHelmetHeadModel() {
        PiglinModel piglinModel = new PiglinModel(1.0f, 64, 16);
        piglinModel.earLeft.visible = false;
        piglinModel.earRight.visible = false;
        return piglinModel;
    }

    @Override
    public ResourceLocation getTextureLocation(Mob mob) {
        return mob instanceof Piglin ? PIGLIN_LOCATION : ZOMBIFIED_PIGLIN_LOCATION;
    }

    @Override
    protected void setupRotations(Mob mob, PoseStack poseStack, float f, float g, float h) {
        if (mob instanceof Piglin && ((Piglin)mob).isConverting()) {
            g += (float)(Math.cos((double)mob.tickCount * 3.25) * Math.PI * 0.5);
        }
        super.setupRotations(mob, poseStack, f, g, h);
    }
}

