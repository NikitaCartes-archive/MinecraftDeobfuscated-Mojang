/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import java.util.function.Function;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;

@Environment(value=EnvType.CLIENT)
public abstract class AgeableListModel<E extends Entity>
extends EntityModel<E> {
    private final boolean scaleHead;
    private final float babyYHeadOffset;
    private final float babyZHeadOffset;
    private final float babyHeadScale;
    private final float babyBodyScale;
    private final float bodyYOffset;

    protected AgeableListModel(boolean bl, float f, float g) {
        this(bl, f, g, 2.0f, 2.0f, 24.0f);
    }

    protected AgeableListModel(boolean bl, float f, float g, float h, float i, float j) {
        this(RenderType::entityCutoutNoCull, bl, f, g, h, i, j);
    }

    protected AgeableListModel(Function<ResourceLocation, RenderType> function, boolean bl, float f, float g, float h, float i, float j) {
        super(function);
        this.scaleHead = bl;
        this.babyYHeadOffset = f;
        this.babyZHeadOffset = g;
        this.babyHeadScale = h;
        this.babyBodyScale = i;
        this.bodyYOffset = j;
    }

    protected AgeableListModel() {
        this(false, 5.0f, 2.0f);
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int i, int j, float f, float g, float h, float k) {
        if (this.young) {
            float l;
            poseStack.pushPose();
            if (this.scaleHead) {
                l = 1.5f / this.babyHeadScale;
                poseStack.scale(l, l, l);
            }
            poseStack.translate(0.0f, this.babyYHeadOffset / 16.0f, this.babyZHeadOffset / 16.0f);
            this.headParts().forEach(modelPart -> modelPart.render(poseStack, vertexConsumer, i, j, f, g, h, k));
            poseStack.popPose();
            poseStack.pushPose();
            l = 1.0f / this.babyBodyScale;
            poseStack.scale(l, l, l);
            poseStack.translate(0.0f, this.bodyYOffset / 16.0f, 0.0f);
            this.bodyParts().forEach(modelPart -> modelPart.render(poseStack, vertexConsumer, i, j, f, g, h, k));
            poseStack.popPose();
        } else {
            this.headParts().forEach(modelPart -> modelPart.render(poseStack, vertexConsumer, i, j, f, g, h, k));
            this.bodyParts().forEach(modelPart -> modelPart.render(poseStack, vertexConsumer, i, j, f, g, h, k));
        }
    }

    protected abstract Iterable<ModelPart> headParts();

    protected abstract Iterable<ModelPart> bodyParts();
}

