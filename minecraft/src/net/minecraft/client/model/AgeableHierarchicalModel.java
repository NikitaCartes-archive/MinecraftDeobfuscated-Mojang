package net.minecraft.client.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import java.util.function.Function;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;

@Environment(EnvType.CLIENT)
public abstract class AgeableHierarchicalModel<E extends Entity> extends HierarchicalModel<E> {
	private final float youngScaleFactor;
	private final float bodyYOffset;

	public AgeableHierarchicalModel(float f, float g) {
		this(f, g, RenderType::entityCutoutNoCull);
	}

	public AgeableHierarchicalModel(float f, float g, Function<ResourceLocation, RenderType> function) {
		super(function);
		this.bodyYOffset = g;
		this.youngScaleFactor = f;
	}

	@Override
	public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int i, int j, float f, float g, float h, float k) {
		if (!this.miniMe() && !this.young) {
			this.root().render(poseStack, vertexConsumer, i, j, f, g, h, k);
		} else {
			poseStack.pushPose();
			poseStack.scale(this.youngScaleFactor, this.youngScaleFactor, this.youngScaleFactor);
			poseStack.translate(0.0F, this.bodyYOffset / 16.0F, 0.0F);
			this.root().render(poseStack, vertexConsumer, i, j, f, g, h, k);
			poseStack.popPose();
		}
	}
}
