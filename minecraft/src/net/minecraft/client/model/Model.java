package net.minecraft.client.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import java.util.List;
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
import org.joml.Vector3f;

@Environment(EnvType.CLIENT)
public abstract class Model {
	private static final Vector3f ANIMATION_VECTOR_CACHE = new Vector3f();
	protected final ModelPart root;
	protected final Function<ResourceLocation, RenderType> renderType;
	private final List<ModelPart> allParts;

	public Model(ModelPart modelPart, Function<ResourceLocation, RenderType> function) {
		this.root = modelPart;
		this.renderType = function;
		this.allParts = modelPart.getAllParts().toList();
	}

	public final RenderType renderType(ResourceLocation resourceLocation) {
		return (RenderType)this.renderType.apply(resourceLocation);
	}

	public final void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int i, int j, int k) {
		this.root().render(poseStack, vertexConsumer, i, j, k);
	}

	public final void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int i, int j) {
		this.renderToBuffer(poseStack, vertexConsumer, i, j, -1);
	}

	public final ModelPart root() {
		return this.root;
	}

	public Optional<ModelPart> getAnyDescendantWithName(String string) {
		return string.equals("root")
			? Optional.of(this.root())
			: this.root().getAllParts().filter(modelPart -> modelPart.hasChild(string)).findFirst().map(modelPart -> modelPart.getChild(string));
	}

	public final List<ModelPart> allParts() {
		return this.allParts;
	}

	public final void resetPose() {
		for (ModelPart modelPart : this.allParts) {
			modelPart.resetPose();
		}
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
		animationState.ifStarted(
			animationStatex -> KeyframeAnimations.animate(this, animationDefinition, (long)((float)animationStatex.getTimeInMillis(f) * g), 1.0F, ANIMATION_VECTOR_CACHE)
		);
	}

	protected void applyStatic(AnimationDefinition animationDefinition) {
		KeyframeAnimations.animate(this, animationDefinition, 0L, 1.0F, ANIMATION_VECTOR_CACHE);
	}

	@Environment(EnvType.CLIENT)
	public static class Simple extends Model {
		public Simple(ModelPart modelPart, Function<ResourceLocation, RenderType> function) {
			super(modelPart, function);
		}
	}
}
