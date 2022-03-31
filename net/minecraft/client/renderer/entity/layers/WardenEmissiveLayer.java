/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.renderer.entity.layers;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.WardenModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.monster.warden.Warden;

@Environment(value=EnvType.CLIENT)
public class WardenEmissiveLayer<T extends Warden, M extends WardenModel<T>>
extends RenderLayer<T, M> {
    private final ResourceLocation texture;
    private final AlphaFunction<T> alphaFunction;
    private final DrawSelector<T, M> drawSelector;

    public WardenEmissiveLayer(RenderLayerParent<T, M> renderLayerParent, ResourceLocation resourceLocation, AlphaFunction<T> alphaFunction, DrawSelector<T, M> drawSelector) {
        super(renderLayerParent);
        this.texture = resourceLocation;
        this.alphaFunction = alphaFunction;
        this.drawSelector = drawSelector;
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource multiBufferSource, int i, T warden, float f, float g, float h, float j, float k, float l) {
        if (((Entity)warden).isInvisible()) {
            return;
        }
        this.onlyDrawSelectedParts();
        VertexConsumer vertexConsumer = multiBufferSource.getBuffer(RenderType.entityTranslucentEmissive(this.texture));
        ((WardenModel)this.getParentModel()).renderToBuffer(poseStack, vertexConsumer, i, LivingEntityRenderer.getOverlayCoords(warden, 0.0f), 1.0f, 1.0f, 1.0f, this.alphaFunction.apply(warden, h, j));
        this.resetDrawForAllParts();
    }

    private void onlyDrawSelectedParts() {
        List<ModelPart> list = this.drawSelector.getPartsToDraw((WardenModel)this.getParentModel());
        ((WardenModel)this.getParentModel()).root().getAllParts().forEach(modelPart -> {
            modelPart.skipDraw = true;
        });
        list.forEach(modelPart -> {
            modelPart.skipDraw = false;
        });
    }

    private void resetDrawForAllParts() {
        ((WardenModel)this.getParentModel()).root().getAllParts().forEach(modelPart -> {
            modelPart.skipDraw = false;
        });
    }

    @Environment(value=EnvType.CLIENT)
    public static interface AlphaFunction<T extends Warden> {
        public float apply(T var1, float var2, float var3);
    }

    @Environment(value=EnvType.CLIENT)
    public static interface DrawSelector<T extends Warden, M extends EntityModel<T>> {
        public List<ModelPart> getPartsToDraw(M var1);
    }
}

