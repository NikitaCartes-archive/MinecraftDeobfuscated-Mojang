/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.renderer.entity;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.LlamaModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.layers.LlamaDecorLayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.animal.horse.Llama;

@Environment(value=EnvType.CLIENT)
public class LlamaRenderer
extends MobRenderer<Llama, LlamaModel<Llama>> {
    private static final ResourceLocation CREAMY = new ResourceLocation("textures/entity/llama/creamy.png");
    private static final ResourceLocation WHITE = new ResourceLocation("textures/entity/llama/white.png");
    private static final ResourceLocation BROWN = new ResourceLocation("textures/entity/llama/brown.png");
    private static final ResourceLocation GRAY = new ResourceLocation("textures/entity/llama/gray.png");

    public LlamaRenderer(EntityRendererProvider.Context context, ModelLayerLocation modelLayerLocation) {
        super(context, new LlamaModel(context.bakeLayer(modelLayerLocation)), 0.7f);
        this.addLayer(new LlamaDecorLayer(this, context.getModelSet()));
    }

    @Override
    public ResourceLocation getTextureLocation(Llama llama) {
        return switch (llama.getVariant()) {
            default -> throw new IncompatibleClassChangeError();
            case Llama.Variant.CREAMY -> CREAMY;
            case Llama.Variant.WHITE -> WHITE;
            case Llama.Variant.BROWN -> BROWN;
            case Llama.Variant.GRAY -> GRAY;
        };
    }
}

