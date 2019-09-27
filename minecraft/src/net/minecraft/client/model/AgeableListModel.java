package net.minecraft.client.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.world.entity.Entity;

@Environment(EnvType.CLIENT)
public abstract class AgeableListModel<E extends Entity> extends EntityModel<E> {
	private final boolean scaleHead;
	private final float yHeadOffset;
	private final float zHeadOffset;
	private final float babyHeadScale;
	private final float babyBodyScale;
	private final float bodyYOffset;

	protected AgeableListModel(boolean bl, float f, float g) {
		this(bl, f, g, 2.0F, 2.0F, 24.0F);
	}

	protected AgeableListModel(boolean bl, float f, float g, float h, float i, float j) {
		this.scaleHead = bl;
		this.yHeadOffset = f;
		this.zHeadOffset = g;
		this.babyHeadScale = h;
		this.babyBodyScale = i;
		this.bodyYOffset = j;
	}

	protected AgeableListModel() {
		this(false, 5.0F, 2.0F);
	}

	@Override
	public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int i, float f, float g, float h) {
		if (this.young) {
			poseStack.pushPose();
			if (this.scaleHead) {
				float j = 1.5F / this.babyHeadScale;
				poseStack.scale(j, j, j);
			}

			poseStack.translate(0.0, (double)(this.yHeadOffset / 16.0F), (double)(this.zHeadOffset / 16.0F));
			this.headParts().forEach(modelPart -> modelPart.render(poseStack, vertexConsumer, 0.0625F, i, null, f, g, h));
			poseStack.popPose();
			poseStack.pushPose();
			float j = 1.0F / this.babyBodyScale;
			poseStack.scale(j, j, j);
			poseStack.translate(0.0, (double)(this.bodyYOffset / 16.0F), 0.0);
			this.bodyParts().forEach(modelPart -> modelPart.render(poseStack, vertexConsumer, 0.0625F, i, null, f, g, h));
			poseStack.popPose();
		} else {
			this.headParts().forEach(modelPart -> modelPart.render(poseStack, vertexConsumer, 0.0625F, i, null, f, g, h));
			this.bodyParts().forEach(modelPart -> modelPart.render(poseStack, vertexConsumer, 0.0625F, i, null, f, g, h));
		}
	}

	protected abstract Iterable<ModelPart> headParts();

	protected abstract Iterable<ModelPart> bodyParts();
}
