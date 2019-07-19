/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.renderer.entity;

import com.mojang.blaze3d.platform.GlStateManager;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.VexModel;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.HumanoidMobRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.monster.Vex;

@Environment(value=EnvType.CLIENT)
public class VexRenderer
extends HumanoidMobRenderer<Vex, VexModel> {
    private static final ResourceLocation VEX_LOCATION = new ResourceLocation("textures/entity/illager/vex.png");
    private static final ResourceLocation VEX_CHARGING_LOCATION = new ResourceLocation("textures/entity/illager/vex_charging.png");

    public VexRenderer(EntityRenderDispatcher entityRenderDispatcher) {
        super(entityRenderDispatcher, new VexModel(), 0.3f);
    }

    @Override
    protected ResourceLocation getTextureLocation(Vex vex) {
        if (vex.isCharging()) {
            return VEX_CHARGING_LOCATION;
        }
        return VEX_LOCATION;
    }

    @Override
    protected void scale(Vex vex, float f) {
        GlStateManager.scalef(0.4f, 0.4f, 0.4f);
    }
}

