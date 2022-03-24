/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.WardenModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.layers.WardenEmissiveLayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.monster.warden.Warden;

@Environment(value=EnvType.CLIENT)
public class WardenRenderer
extends MobRenderer<Warden, WardenModel<Warden>> {
    private static final ResourceLocation TEXTURE = new ResourceLocation("textures/entity/warden/warden.png");
    private static final ResourceLocation BIOLUMINESCENT_LAYER_TEXTURE = new ResourceLocation("textures/entity/warden/warden_bioluminescent_layer.png");
    private static final ResourceLocation EARS_TEXTURE = new ResourceLocation("textures/entity/warden/warden_ears.png");
    private static final ResourceLocation HEART_TEXTURE = new ResourceLocation("textures/entity/warden/warden_heart.png");
    private static final ResourceLocation PULSATING_SPOTS_TEXTURE_1 = new ResourceLocation("textures/entity/warden/warden_pulsating_spots_1.png");
    private static final ResourceLocation PULSATING_SPOTS_TEXTURE_2 = new ResourceLocation("textures/entity/warden/warden_pulsating_spots_2.png");

    public WardenRenderer(EntityRendererProvider.Context context) {
        super(context, new WardenModel(context.bakeLayer(ModelLayers.WARDEN)), 0.5f);
        this.addLayer(new WardenEmissiveLayer<Warden, WardenModel<Warden>>(this, BIOLUMINESCENT_LAYER_TEXTURE, (warden, f, g) -> 1.0f));
        this.addLayer(new WardenEmissiveLayer<Warden, WardenModel<Warden>>(this, PULSATING_SPOTS_TEXTURE_1, (warden, f, g) -> Math.max(0.0f, Mth.cos(g * 0.045f) * 0.25f)));
        this.addLayer(new WardenEmissiveLayer<Warden, WardenModel<Warden>>(this, PULSATING_SPOTS_TEXTURE_2, (warden, f, g) -> Math.max(0.0f, Mth.cos(g * 0.045f + (float)Math.PI) * 0.25f)));
        this.addLayer(new WardenEmissiveLayer<Warden, WardenModel<Warden>>(this, EARS_TEXTURE, (warden, f, g) -> warden.getEarAnimation(f)));
        this.addLayer(new WardenEmissiveLayer<Warden, WardenModel<Warden>>(this, HEART_TEXTURE, (warden, f, g) -> warden.getHeartAnimation(f)));
    }

    @Override
    public void render(Warden warden, float f, float g, PoseStack poseStack, MultiBufferSource multiBufferSource, int i) {
        if (warden.tickCount <= 2 && !warden.hasPose(Pose.EMERGING)) {
            return;
        }
        super.render(warden, f, g, poseStack, multiBufferSource, i);
    }

    @Override
    public ResourceLocation getTextureLocation(Warden warden) {
        return TEXTURE;
    }
}

