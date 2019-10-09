package net.minecraft.client.renderer.entity.layers;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Vector3f;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;

@Environment(EnvType.CLIENT)
public class SpinAttackEffectLayer<T extends LivingEntity> extends RenderLayer<T, PlayerModel<T>> {
	public static final ResourceLocation TEXTURE = new ResourceLocation("textures/entity/trident_riptide.png");
	private final ModelPart box = new ModelPart(64, 64, 0, 0);

	public SpinAttackEffectLayer(RenderLayerParent<T, PlayerModel<T>> renderLayerParent) {
		super(renderLayerParent);
		this.box.addBox(-8.0F, -16.0F, -8.0F, 16.0F, 32.0F, 16.0F);
	}

	public void render(
		PoseStack poseStack, MultiBufferSource multiBufferSource, int i, T livingEntity, float f, float g, float h, float j, float k, float l, float m
	) {
		if (livingEntity.isAutoSpinAttack()) {
			VertexConsumer vertexConsumer = multiBufferSource.getBuffer(RenderType.entityCutoutNoCull(TEXTURE));

			for (int n = 0; n < 3; n++) {
				poseStack.pushPose();
				float o = j * (float)(-(45 + n * 5));
				poseStack.mulPose(Vector3f.YP.rotationDegrees(o));
				float p = 0.75F * (float)n;
				poseStack.scale(p, p, p);
				poseStack.translate(0.0, (double)(-0.2F + 0.6F * (float)n), 0.0);
				this.box.render(poseStack, vertexConsumer, m, i, OverlayTexture.NO_OVERLAY, null);
				poseStack.popPose();
			}
		}
	}
}
