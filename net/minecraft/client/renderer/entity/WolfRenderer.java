/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.WolfModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.layers.WolfCollarLayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
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
    public void render(Wolf wolf, float f, float g, PoseStack poseStack, MultiBufferSource multiBufferSource, int i) {
        if (wolf.isWet()) {
            float h = Mth.clamp(wolf.getBrightness() * wolf.getWetShade(g), 0.0f, 1.0f);
            ((WolfModel)this.model).setColor(h, h, h);
        }
        super.render(wolf, f, g, poseStack, multiBufferSource, i);
        if (wolf.isWet()) {
            ((WolfModel)this.model).setColor(1.0f, 1.0f, 1.0f);
        }
    }

    @Override
    public ResourceLocation getTextureLocation(Wolf wolf) {
        if (wolf.isTame()) {
            return WOLF_TAME_LOCATION;
        }
        if (wolf.isAngry()) {
            return WOLF_ANGRY_LOCATION;
        }
        return WOLF_LOCATION;
    }
}

