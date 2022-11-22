/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.ColorableHierarchicalModel;
import net.minecraft.client.model.TropicalFishModelA;
import net.minecraft.client.model.TropicalFishModelB;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.layers.TropicalFishPatternLayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.animal.TropicalFish;

@Environment(value=EnvType.CLIENT)
public class TropicalFishRenderer
extends MobRenderer<TropicalFish, ColorableHierarchicalModel<TropicalFish>> {
    private final ColorableHierarchicalModel<TropicalFish> modelA = (ColorableHierarchicalModel)this.getModel();
    private final ColorableHierarchicalModel<TropicalFish> modelB;
    private static final ResourceLocation MODEL_A_TEXTURE = new ResourceLocation("textures/entity/fish/tropical_a.png");
    private static final ResourceLocation MODEL_B_TEXTURE = new ResourceLocation("textures/entity/fish/tropical_b.png");

    public TropicalFishRenderer(EntityRendererProvider.Context context) {
        super(context, new TropicalFishModelA(context.bakeLayer(ModelLayers.TROPICAL_FISH_SMALL)), 0.15f);
        this.modelB = new TropicalFishModelB<TropicalFish>(context.bakeLayer(ModelLayers.TROPICAL_FISH_LARGE));
        this.addLayer(new TropicalFishPatternLayer(this, context.getModelSet()));
    }

    @Override
    public ResourceLocation getTextureLocation(TropicalFish tropicalFish) {
        return switch (tropicalFish.getVariant().base()) {
            default -> throw new IncompatibleClassChangeError();
            case TropicalFish.Base.SMALL -> MODEL_A_TEXTURE;
            case TropicalFish.Base.LARGE -> MODEL_B_TEXTURE;
        };
    }

    @Override
    public void render(TropicalFish tropicalFish, float f, float g, PoseStack poseStack, MultiBufferSource multiBufferSource, int i) {
        ColorableHierarchicalModel<TropicalFish> colorableHierarchicalModel;
        this.model = colorableHierarchicalModel = (switch (tropicalFish.getVariant().base()) {
            default -> throw new IncompatibleClassChangeError();
            case TropicalFish.Base.SMALL -> this.modelA;
            case TropicalFish.Base.LARGE -> this.modelB;
        });
        float[] fs = tropicalFish.getBaseColor().getTextureDiffuseColors();
        colorableHierarchicalModel.setColor(fs[0], fs[1], fs[2]);
        super.render(tropicalFish, f, g, poseStack, multiBufferSource, i);
        colorableHierarchicalModel.setColor(1.0f, 1.0f, 1.0f);
    }

    @Override
    protected void setupRotations(TropicalFish tropicalFish, PoseStack poseStack, float f, float g, float h) {
        super.setupRotations(tropicalFish, poseStack, f, g, h);
        float i = 4.3f * Mth.sin(0.6f * f);
        poseStack.mulPose(Axis.YP.rotationDegrees(i));
        if (!tropicalFish.isInWater()) {
            poseStack.translate(0.2f, 0.1f, 0.0f);
            poseStack.mulPose(Axis.ZP.rotationDegrees(90.0f));
        }
    }
}

