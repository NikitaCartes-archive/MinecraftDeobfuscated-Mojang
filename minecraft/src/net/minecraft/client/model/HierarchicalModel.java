package net.minecraft.client.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import java.util.Optional;
import java.util.function.Function;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.animation.AnimationDefinition;
import net.minecraft.client.animation.KeyframeAnimations;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.AnimationState;
import net.minecraft.world.entity.Entity;
import org.joml.Vector3f;

@Environment(EnvType.CLIENT)
public abstract class HierarchicalModel<E extends Entity> extends EntityModel<E> {
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
		return string.equals("root")
			? Optional.of(this.root())
			: this.root().getAllParts().filter(modelPart -> modelPart.hasChild(string)).findFirst().map(modelPart -> modelPart.getChild(string));
	}

	protected void animate(AnimationState animationState, AnimationDefinition animationDefinition, float f) {
		this.animate(animationState, animationDefinition, f, 1.0F);
	}

	protected void animateWalk(AnimationDefinition animationDefinition, float f, float g, float h, float i) {
		long l = (long)(f * 50.0F * h);
		float j = Math.min(g * i, 1.0F);
		KeyframeAnimations.animate(this, animationDefinition, l, j, ANIMATION_VECTOR_CACHE);
	}

	protected void animate(AnimationState animationState, AnimationDefinition animationDefinition, float f, float g) {
		animationState.updateTime(f, g);
		animationState.ifStarted(
			animationStatex -> KeyframeAnimations.animate(this, animationDefinition, animationStatex.getAccumulatedTime(), 1.0F, ANIMATION_VECTOR_CACHE)
		);
	}
}
