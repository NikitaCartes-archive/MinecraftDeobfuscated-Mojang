/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.renderer.entity.layers;

import com.mojang.blaze3d.vertex.PoseStack;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.ColorableListModel;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.TropicalFishModelA;
import net.minecraft.client.model.TropicalFishModelB;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.world.entity.animal.TropicalFish;

@Environment(value=EnvType.CLIENT)
public class TropicalFishPatternLayer
extends RenderLayer<TropicalFish, EntityModel<TropicalFish>> {
    private final TropicalFishModelA<TropicalFish> modelA = new TropicalFishModelA(0.008f);
    private final TropicalFishModelB<TropicalFish> modelB = new TropicalFishModelB(0.008f);

    public TropicalFishPatternLayer(RenderLayerParent<TropicalFish, EntityModel<TropicalFish>> renderLayerParent) {
        super(renderLayerParent);
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource multiBufferSource, int i, TropicalFish tropicalFish, float f, float g, float h, float j, float k, float l) {
        ColorableListModel entityModel = tropicalFish.getBaseVariant() == 0 ? this.modelA : this.modelB;
        float[] fs = tropicalFish.getPatternColor();
        TropicalFishPatternLayer.coloredCutoutModelCopyLayerRender(this.getParentModel(), entityModel, tropicalFish.getPatternTextureLocation(), poseStack, multiBufferSource, i, tropicalFish, f, g, j, k, l, h, fs[0], fs[1], fs[2]);
    }
}

