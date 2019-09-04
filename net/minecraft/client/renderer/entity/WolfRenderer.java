/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.renderer.entity;

import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.WolfModel;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.layers.WolfCollarLayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.Wolf;

@Environment(value=EnvType.CLIENT)
public class WolfRenderer
extends MobRenderer<Wolf, WolfModel<Wolf>> {
    private static final ResourceLocation WOLF_LOCATION = new ResourceLocation("textures/entity/wolf/wolf.png");
    private static final ResourceLocation WOLF_TAME_LOCATION = new ResourceLocation("textures/entity/wolf/wolf_tame.png");
    private static final ResourceLocation WOLF_ANGRY_LOCATION = new ResourceLocation("textures/entity/wolf/wolf_angry.png");

    public WolfRenderer(EntityRenderDispatcher entityRenderDispatcher) {
        super(entityRenderDispatcher, new WolfModel(), 0.5f);
        this.addLayer(new WolfCollarLayer(this));
    }

    @Override
    protected float getBob(Wolf wolf, float f) {
        return wolf.getTailAngle();
    }

    @Override
    public void render(Wolf wolf, double d, double e, double f, float g, float h) {
        if (wolf.isWet()) {
            float i = wolf.getBrightness() * wolf.getWetShade(h);
            RenderSystem.color3f(i, i, i);
        }
        super.render(wolf, d, e, f, g, h);
    }

    @Override
    protected ResourceLocation getTextureLocation(Wolf wolf) {
        if (wolf.isTame()) {
            return WOLF_TAME_LOCATION;
        }
        if (wolf.isAngry()) {
            return WOLF_ANGRY_LOCATION;
        }
        return WOLF_LOCATION;
    }

    @Override
    protected /* synthetic */ float getBob(LivingEntity livingEntity, float f) {
        return this.getBob((Wolf)livingEntity, f);
    }
}

