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
	private final float yHeadOffset;
	private final float zHeadOffset;
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
	public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int i, int j, float f, float g, float h) {
		if (this.young) {
			poseStack.pushPose();
			if (this.scaleHead) {
				float k = 1.5F / this.babyHeadScale;
				poseStack.scale(k, k, k);
			}

			poseStack.translate(0.0, (double)(this.yHeadOffset / 16.0F), (double)(this.zHeadOffset / 16.0F));
			this.headParts().forEach(modelPart -> modelPart.render(poseStack, vertexConsumer, 0.0625F, i, j, null, f, g, h));
			poseStack.popPose();
			poseStack.pushPose();
			float k = 1.0F / this.babyBodyScale;
			poseStack.scale(k, k, k);
			poseStack.translate(0.0, (double)(this.bodyYOffset / 16.0F), 0.0);
			this.bodyParts().forEach(modelPart -> modelPart.render(poseStack, vertexConsumer, 0.0625F, i, j, null, f, g, h));
			poseStack.popPose();
		} else {
			this.headParts().forEach(modelPart -> modelPart.render(poseStack, vertexConsumer, 0.0625F, i, j, null, f, g, h));
			this.bodyParts().forEach(modelPart -> modelPart.render(poseStack, vertexConsumer, 0.0625F, i, j, null, f, g, h));
		}
	}

	protected abstract Iterable<ModelPart> headParts();

	protected abstract Iterable<ModelPart> bodyParts();
}
