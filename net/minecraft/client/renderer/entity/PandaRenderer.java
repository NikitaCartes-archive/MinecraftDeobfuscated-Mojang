/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.renderer.entity;

import com.google.common.collect.Maps;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Vector3f;
import java.util.Map;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.Util;
import net.minecraft.client.model.PandaModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.layers.PandaHoldsItemLayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.animal.Panda;

@Environment(value=EnvType.CLIENT)
public class PandaRenderer
extends MobRenderer<Panda, PandaModel<Panda>> {
    private static final Map<Panda.Gene, ResourceLocation> TEXTURES = Util.make(Maps.newEnumMap(Panda.Gene.class), enumMap -> {
        enumMap.put(Panda.Gene.NORMAL, new ResourceLocation("textures/entity/panda/panda.png"));
        enumMap.put(Panda.Gene.LAZY, new ResourceLocation("textures/entity/panda/lazy_panda.png"));
        enumMap.put(Panda.Gene.WORRIED, new ResourceLocation("textures/entity/panda/worried_panda.png"));
        enumMap.put(Panda.Gene.PLAYFUL, new ResourceLocation("textures/entity/panda/playful_panda.png"));
        enumMap.put(Panda.Gene.BROWN, new ResourceLocation("textures/entity/panda/brown_panda.png"));
        enumMap.put(Panda.Gene.WEAK, new ResourceLocation("textures/entity/panda/weak_panda.png"));
        enumMap.put(Panda.Gene.AGGRESSIVE, new ResourceLocation("textures/entity/panda/aggressive_panda.png"));
    });

    public PandaRenderer(EntityRendererProvider.Context context) {
        super(context, new PandaModel(context.bakeLayer(ModelLayers.PANDA)), 0.9f);
        this.addLayer(new PandaHoldsItemLayer(this, context.getItemInHandRenderer()));
    }

    @Override
    public ResourceLocation getTextureLocation(Panda panda) {
        return TEXTURES.getOrDefault((Object)panda.getVariant(), TEXTURES.get((Object)Panda.Gene.NORMAL));
    }

    @Override
    protected void setupRotations(Panda panda, PoseStack poseStack, float f, float g, float h) {
        float r;
        float q;
        float k;
        super.setupRotations(panda, poseStack, f, g, h);
        if (panda.rollCounter > 0) {
            float l;
            int i = panda.rollCounter;
            int j = i + 1;
            k = 7.0f;
            float f2 = l = panda.isBaby() ? 0.3f : 0.8f;
            if (i < 8) {
                float m = (float)(90 * i) / 7.0f;
                float n = (float)(90 * j) / 7.0f;
                float o = this.getAngle(m, n, j, h, 8.0f);
                poseStack.translate(0.0, (l + 0.2f) * (o / 90.0f), 0.0);
                poseStack.mulPose(Vector3f.XP.rotationDegrees(-o));
            } else if (i < 16) {
                float m = ((float)i - 8.0f) / 7.0f;
                float n = 90.0f + 90.0f * m;
                float p = 90.0f + 90.0f * ((float)j - 8.0f) / 7.0f;
                float o = this.getAngle(n, p, j, h, 16.0f);
                poseStack.translate(0.0, l + 0.2f + (l - 0.2f) * (o - 90.0f) / 90.0f, 0.0);
                poseStack.mulPose(Vector3f.XP.rotationDegrees(-o));
            } else if ((float)i < 24.0f) {
                float m = ((float)i - 16.0f) / 7.0f;
                float n = 180.0f + 90.0f * m;
                float p = 180.0f + 90.0f * ((float)j - 16.0f) / 7.0f;
                float o = this.getAngle(n, p, j, h, 24.0f);
                poseStack.translate(0.0, l + l * (270.0f - o) / 90.0f, 0.0);
                poseStack.mulPose(Vector3f.XP.rotationDegrees(-o));
            } else if (i < 32) {
                float m = ((float)i - 24.0f) / 7.0f;
                float n = 270.0f + 90.0f * m;
                float p = 270.0f + 90.0f * ((float)j - 24.0f) / 7.0f;
                float o = this.getAngle(n, p, j, h, 32.0f);
                poseStack.translate(0.0, l * ((360.0f - o) / 90.0f), 0.0);
                poseStack.mulPose(Vector3f.XP.rotationDegrees(-o));
            }
        }
        if ((q = panda.getSitAmount(h)) > 0.0f) {
            poseStack.translate(0.0, 0.8f * q, 0.0);
            poseStack.mulPose(Vector3f.XP.rotationDegrees(Mth.lerp(q, panda.getXRot(), panda.getXRot() + 90.0f)));
            poseStack.translate(0.0, -1.0f * q, 0.0);
            if (panda.isScared()) {
                float r2 = (float)(Math.cos((double)panda.tickCount * 1.25) * Math.PI * (double)0.05f);
                poseStack.mulPose(Vector3f.YP.rotationDegrees(r2));
                if (panda.isBaby()) {
                    poseStack.translate(0.0, 0.8f, 0.55f);
                }
            }
        }
        if ((r = panda.getLieOnBackAmount(h)) > 0.0f) {
            k = panda.isBaby() ? 0.5f : 1.3f;
            poseStack.translate(0.0, k * r, 0.0);
            poseStack.mulPose(Vector3f.XP.rotationDegrees(Mth.lerp(r, panda.getXRot(), panda.getXRot() + 180.0f)));
        }
    }

    private float getAngle(float f, float g, int i, float h, float j) {
        if ((float)i < j) {
            return Mth.lerp(h, f, g);
        }
        return f;
    }
}

