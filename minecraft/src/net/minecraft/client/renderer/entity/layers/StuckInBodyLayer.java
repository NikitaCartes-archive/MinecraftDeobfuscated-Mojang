package net.minecraft.client.renderer.entity.layers;

import com.mojang.blaze3d.vertex.PoseStack;
import java.util.Random;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;

@Environment(EnvType.CLIENT)
public abstract class StuckInBodyLayer<T extends LivingEntity, M extends PlayerModel<T>> extends RenderLayer<T, M> {
	public StuckInBodyLayer(LivingEntityRenderer<T, M> livingEntityRenderer) {
		super(livingEntityRenderer);
	}

	protected abstract int numStuck(T livingEntity);

	protected abstract void renderStuckItem(PoseStack poseStack, MultiBufferSource multiBufferSource, Entity entity, float f, float g, float h, float i);

	public void render(
		PoseStack poseStack, MultiBufferSource multiBufferSource, int i, T livingEntity, float f, float g, float h, float j, float k, float l, float m
	) {
		int n = this.numStuck(livingEntity);
		Random random = new Random((long)livingEntity.getId());
		if (n > 0) {
			for (int o = 0; o < n; o++) {
				poseStack.pushPose();
				ModelPart modelPart = this.getParentModel().getRandomModelPart(random);
				ModelPart.Cube cube = modelPart.getRandomCube(random);
				modelPart.translateAndRotate(poseStack, 0.0625F);
				float p = random.nextFloat();
				float q = random.nextFloat();
				float r = random.nextFloat();
				float s = Mth.lerp(p, cube.minX, cube.maxX) / 16.0F;
				float t = Mth.lerp(q, cube.minY, cube.maxY) / 16.0F;
				float u = Mth.lerp(r, cube.minZ, cube.maxZ) / 16.0F;
				poseStack.translate((double)s, (double)t, (double)u);
				p = -1.0F * (p * 2.0F - 1.0F);
				q = -1.0F * (q * 2.0F - 1.0F);
				r = -1.0F * (r * 2.0F - 1.0F);
				this.renderStuckItem(poseStack, multiBufferSource, livingEntity, p, q, r, h);
				poseStack.popPose();
			}
		}
	}
}
