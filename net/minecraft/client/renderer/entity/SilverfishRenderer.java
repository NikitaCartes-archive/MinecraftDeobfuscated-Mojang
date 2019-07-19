/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.renderer.entity;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.SilverfishModel;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Silverfish;

@Environment(value=EnvType.CLIENT)
public class SilverfishRenderer
extends MobRenderer<Silverfish, SilverfishModel<Silverfish>> {
    private static final ResourceLocation SILVERFISH_LOCATION = new ResourceLocation("textures/entity/silverfish.png");

    public SilverfishRenderer(EntityRenderDispatcher entityRenderDispatcher) {
        super(entityRenderDispatcher, new SilverfishModel(), 0.3f);
    }

    @Override
    protected float getFlipDegrees(Silverfish silverfish) {
        return 180.0f;
    }

    @Override
    protected ResourceLocation getTextureLocation(Silverfish silverfish) {
        return SILVERFISH_LOCATION;
    }

    @Override
    protected /* synthetic */ float getFlipDegrees(LivingEntity livingEntity) {
        return this.getFlipDegrees((Silverfish)livingEntity);
    }

    @Override
    protected /* synthetic */ ResourceLocation getTextureLocation(Entity entity) {
        return this.getTextureLocation((Silverfish)entity);
    }
}

