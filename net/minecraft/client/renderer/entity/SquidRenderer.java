/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.renderer.entity;

import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.SquidModel;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.Squid;

@Environment(value=EnvType.CLIENT)
public class SquidRenderer
extends MobRenderer<Squid, SquidModel<Squid>> {
    private static final ResourceLocation SQUID_LOCATION = new ResourceLocation("textures/entity/squid.png");

    public SquidRenderer(EntityRenderDispatcher entityRenderDispatcher) {
        super(entityRenderDispatcher, new SquidModel(), 0.7f);
    }

    @Override
    protected ResourceLocation getTextureLocation(Squid squid) {
        return SQUID_LOCATION;
    }

    @Override
    protected void setupRotations(Squid squid, float f, float g, float h) {
        float i = Mth.lerp(h, squid.xBodyRotO, squid.xBodyRot);
        float j = Mth.lerp(h, squid.zBodyRotO, squid.zBodyRot);
        RenderSystem.translatef(0.0f, 0.5f, 0.0f);
        RenderSystem.rotatef(180.0f - g, 0.0f, 1.0f, 0.0f);
        RenderSystem.rotatef(i, 1.0f, 0.0f, 0.0f);
        RenderSystem.rotatef(j, 0.0f, 1.0f, 0.0f);
        RenderSystem.translatef(0.0f, -1.2f, 0.0f);
    }

    @Override
    protected float getBob(Squid squid, float f) {
        return Mth.lerp(f, squid.oldTentacleAngle, squid.tentacleAngle);
    }

    @Override
    protected /* synthetic */ float getBob(LivingEntity livingEntity, float f) {
        return this.getBob((Squid)livingEntity, f);
    }
}

