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

	protected abstract void renderStuckItem(PoseStack poseStack, MultiBufferSource multiBufferSource, int i, Entity entity, float f, float g, float h, float j);

	public void render(PoseStack poseStack, MultiBufferSource multiBufferSource, int i, T livingEntity, float f, float g, float h, float j, float k, float l) {
		int m = this.numStuck(livingEntity);
		Random random = new Random((long)livingEntity.getId());
		if (m > 0) {
			for (int n = 0; n < m; n++) {
				poseStack.pushPose();
				ModelPart modelPart = this.getParentModel().getRandomModelPart(random);
				ModelPart.Cube cube = modelPart.getRandomCube(random);
				modelPart.translateAndRotate(poseStack);
				float o = random.nextFloat();
				float p = random.nextFloat();
				float q = random.nextFloat();
				float r = Mth.lerp(o, cube.minX, cube.maxX) / 16.0F;
				float s = Mth.lerp(p, cube.minY, cube.maxY) / 16.0F;
				float t = Mth.lerp(q, cube.minZ, cube.maxZ) / 16.0F;
				poseStack.translate((double)r, (double)s, (double)t);
				o = -1.0F * (o * 2.0F - 1.0F);
				p = -1.0F * (p * 2.0F - 1.0F);
				q = -1.0F * (q * 2.0F - 1.0F);
				this.renderStuckItem(poseStack, multiBufferSource, i, livingEntity, o, p, q, h);
				poseStack.popPose();
			}
		}
	}
}
