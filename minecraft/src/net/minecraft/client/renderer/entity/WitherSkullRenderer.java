package net.minecraft.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.SkullModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.projectile.WitherSkull;

@Environment(EnvType.CLIENT)
public class WitherSkullRenderer extends EntityRenderer<WitherSkull> {
	private static final ResourceLocation WITHER_INVULNERABLE_LOCATION = new ResourceLocation("textures/entity/wither/wither_invulnerable.png");
	private static final ResourceLocation WITHER_LOCATION = new ResourceLocation("textures/entity/wither/wither.png");
	private final SkullModel model = new SkullModel();

	public WitherSkullRenderer(EntityRenderDispatcher entityRenderDispatcher) {
		super(entityRenderDispatcher);
	}

	public void render(WitherSkull witherSkull, double d, double e, double f, float g, float h, PoseStack poseStack, MultiBufferSource multiBufferSource) {
		poseStack.pushPose();
		poseStack.scale(-1.0F, -1.0F, 1.0F);
		float i = Mth.rotlerp(witherSkull.yRotO, witherSkull.yRot, h);
		float j = Mth.lerp(h, witherSkull.xRotO, witherSkull.xRot);
		int k = witherSkull.getLightColor();
		VertexConsumer vertexConsumer = multiBufferSource.getBuffer(this.model.renderType(this.getTextureLocation(witherSkull)));
		this.model.setupAnim(0.0F, i, j);
		this.model.renderToBuffer(poseStack, vertexConsumer, k, OverlayTexture.NO_OVERLAY, 1.0F, 1.0F, 1.0F);
		poseStack.popPose();
		super.render(witherSkull, d, e, f, g, h, poseStack, multiBufferSource);
	}

	public ResourceLocation getTextureLocation(WitherSkull witherSkull) {
		return witherSkull.isDangerous() ? WITHER_INVULNERABLE_LOCATION : WITHER_LOCATION;
	}
}
