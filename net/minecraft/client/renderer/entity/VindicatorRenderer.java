/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.IllagerModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.IllagerRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.ItemInHandLayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.monster.Vindicator;

@Environment(value=EnvType.CLIENT)
public class VindicatorRenderer
extends IllagerRenderer<Vindicator> {
    private static final ResourceLocation VINDICATOR = new ResourceLocation("textures/entity/illager/vindicator.png");

    public VindicatorRenderer(EntityRendererProvider.Context context) {
        super(context, new IllagerModel(context.bakeLayer(ModelLayers.VINDICATOR)), 0.5f);
        this.addLayer(new ItemInHandLayer<Vindicator, IllagerModel<Vindicator>>((RenderLayerParent)this, context.getItemInHandRenderer()){

            @Override
            public void render(PoseStack poseStack, MultiBufferSource multiBufferSource, int i, Vindicator vindicator, float f, float g, float h, float j, float k, float l) {
                if (vindicator.isAggressive()) {
                    super.render(poseStack, multiBufferSource, i, vindicator, f, g, h, j, k, l);
                }
            }
        });
    }

    @Override
    public ResourceLocation getTextureLocation(Vindicator vindicator) {
        return VINDICATOR;
    }
}

