/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import java.util.Optional;
import java.util.function.Function;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.animation.AnimationDefinition;
import net.minecraft.client.animation.KeyframeAnimations;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.AnimationState;
import net.minecraft.world.entity.Entity;
import org.joml.Vector3f;

@Environment(value=EnvType.CLIENT)
public abstract class HierarchicalModel<E extends Entity>
extends EntityModel<E> {
    private static final Vector3f ANIMATION_VECTOR_CACHE = new Vector3f();

    public HierarchicalModel() {
        this(RenderType::entityCutoutNoCull);
    }

    public HierarchicalModel(Function<ResourceLocation, RenderType> function) {
        super(function);
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int i, int j, float f, float g, float h, float k) {
        this.root().render(poseStack, vertexConsumer, i, j, f, g, h, k);
    }

    public abstract ModelPart root();

    public Optional<ModelPart> getAnyDescendantWithName(String string) {
        if (string.equals("root")) {
            return Optional.of(this.root());
        }
        return this.root().getAllParts().filter(modelPart -> modelPart.hasChild(string)).findFirst().map(modelPart -> modelPart.getChild(string));
    }

    protected void animate(AnimationState animationState, AnimationDefinition animationDefinition, float f) {
        this.animate(animationState, animationDefinition, f, 1.0f);
    }

    protected void animateWalk(AnimationDefinition animationDefinition, float f, float g, float h, float i) {
        long l = (long)(f * 50.0f * h);
        float j = Math.min(g * i, 1.0f);
        KeyframeAnimations.animate(this, animationDefinition, l, j, ANIMATION_VECTOR_CACHE);
    }

    protected void animate(AnimationState animationState2, AnimationDefinition animationDefinition, float f, float g) {
        animationState2.updateTime(f, g);
        animationState2.ifStarted(animationState -> KeyframeAnimations.animate(this, animationDefinition, animationState.getAccumulatedTime(), 1.0f, ANIMATION_VECTOR_CACHE));
    }
}

