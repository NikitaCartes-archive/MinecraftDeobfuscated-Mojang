/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Vector3f;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.ColorableListModel;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.TropicalFishModelA;
import net.minecraft.client.model.TropicalFishModelB;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.layers.TropicalFishPatternLayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.animal.TropicalFish;

@Environment(value=EnvType.CLIENT)
public class TropicalFishRenderer
extends MobRenderer<TropicalFish, EntityModel<TropicalFish>> {
    private final TropicalFishModelA<TropicalFish> modelA = new TropicalFishModelA(0.0f);
    private final TropicalFishModelB<TropicalFish> modelB = new TropicalFishModelB(0.0f);

    public TropicalFishRenderer(EntityRenderDispatcher entityRenderDispatcher) {
        super(entityRenderDispatcher, new TropicalFishModelA(0.0f), 0.15f);
        this.addLayer(new TropicalFishPatternLayer(this));
    }

    @Override
    public ResourceLocation getTextureLocation(TropicalFish tropicalFish) {
        return tropicalFish.getBaseTextureLocation();
    }

    @Override
    public void render(TropicalFish tropicalFish, double d, double e, double f, float g, float h, PoseStack poseStack, MultiBufferSource multiBufferSource) {
        ColorableListModel colorableListModel;
        this.model = colorableListModel = tropicalFish.getBaseVariant() == 0 ? this.modelA : this.modelB;
        float[] fs = tropicalFish.getBaseColor();
        colorableListModel.setColor(fs[0], fs[1], fs[2]);
        super.render(tropicalFish, d, e, f, g, h, poseStack, multiBufferSource);
        colorableListModel.setColor(1.0f, 1.0f, 1.0f);
    }

    @Override
    protected void setupRotations(TropicalFish tropicalFish, PoseStack poseStack, float f, float g, float h) {
        super.setupRotations(tropicalFish, poseStack, f, g, h);
        float i = 4.3f * Mth.sin(0.6f * f);
        poseStack.mulPose(Vector3f.YP.rotationDegrees(i));
        if (!tropicalFish.isInWater()) {
            poseStack.translate(0.2f, 0.1f, 0.0);
            poseStack.mulPose(Vector3f.ZP.rotationDegrees(90.0f));
        }
    }
}

