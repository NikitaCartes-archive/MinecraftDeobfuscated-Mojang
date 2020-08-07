package net.minecraft.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.SkullModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
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

	protected int getBlockLightLevel(WitherSkull witherSkull, BlockPos blockPos) {
		return 15;
	}

	public void render(WitherSkull witherSkull, float f, float g, PoseStack poseStack, MultiBufferSource multiBufferSource, int i) {
		poseStack.pushPose();
		poseStack.scale(-1.0F, -1.0F, 1.0F);
		float h = Mth.rotlerp(witherSkull.yRotO, witherSkull.yRot, g);
		float j = Mth.lerp(g, witherSkull.xRotO, witherSkull.xRot);
		VertexConsumer vertexConsumer = multiBufferSource.getBuffer(this.model.renderType(this.getTextureLocation(witherSkull)));
		this.model.setupAnim(0.0F, h, j);
		this.model.renderToBuffer(poseStack, vertexConsumer, i, OverlayTexture.NO_OVERLAY, 1.0F, 1.0F, 1.0F, 1.0F);
		poseStack.popPose();
		super.render(witherSkull, f, g, poseStack, multiBufferSource, i);
	}

	public ResourceLocation getTextureLocation(WitherSkull witherSkull) {
		return witherSkull.isDangerous() ? WITHER_INVULNERABLE_LOCATION : WITHER_LOCATION;
	}
}
