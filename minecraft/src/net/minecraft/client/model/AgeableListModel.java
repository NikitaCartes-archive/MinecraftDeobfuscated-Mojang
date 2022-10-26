package net.minecraft.client.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import java.util.function.Function;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;

@Environment(EnvType.CLIENT)
public abstract class AgeableListModel<E extends Entity> extends EntityModel<E> {
	private final boolean scaleHead;
	private final float babyYHeadOffset;
	private final float babyZHeadOffset;
	private final float babyHeadScale;
	private final float babyBodyScale;
	private final float bodyYOffset;

	protected AgeableListModel(boolean bl, float f, float g) {
		this(bl, f, g, 2.0F, 2.0F, 24.0F);
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
		this(false, 5.0F, 2.0F);
	}

	@Override
	public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int i, int j, float f, float g, float h, float k) {
		if (this.young) {
			poseStack.pushPose();
			if (this.scaleHead) {
				float l = 1.5F / this.babyHeadScale;
				poseStack.scale(l, l, l);
			}

			poseStack.translate(0.0F, this.babyYHeadOffset / 16.0F, this.babyZHeadOffset / 16.0F);
			this.headParts().forEach(modelPart -> modelPart.render(poseStack, vertexConsumer, i, j, f, g, h, k));
			poseStack.popPose();
			poseStack.pushPose();
			float l = 1.0F / this.babyBodyScale;
			poseStack.scale(l, l, l);
			poseStack.translate(0.0F, this.bodyYOffset / 16.0F, 0.0F);
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
