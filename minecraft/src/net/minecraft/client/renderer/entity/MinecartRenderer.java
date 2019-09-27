package net.minecraft.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Vector3f;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.MinecartModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.vehicle.AbstractMinecart;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

@Environment(EnvType.CLIENT)
public class MinecartRenderer<T extends AbstractMinecart> extends EntityRenderer<T> {
	private static final ResourceLocation MINECART_LOCATION = new ResourceLocation("textures/entity/minecart.png");
	protected final EntityModel<T> model = new MinecartModel<>();

	public MinecartRenderer(EntityRenderDispatcher entityRenderDispatcher) {
		super(entityRenderDispatcher);
		this.shadowRadius = 0.7F;
	}

	public void render(T abstractMinecart, double d, double e, double f, float g, float h, PoseStack poseStack, MultiBufferSource multiBufferSource) {
		super.render(abstractMinecart, d, e, f, g, h, poseStack, multiBufferSource);
		poseStack.pushPose();
		long l = (long)abstractMinecart.getId() * 493286711L;
		l = l * l * 4392167121L + l * 98761L;
		float i = (((float)(l >> 16 & 7L) + 0.5F) / 8.0F - 0.5F) * 0.004F;
		float j = (((float)(l >> 20 & 7L) + 0.5F) / 8.0F - 0.5F) * 0.004F;
		float k = (((float)(l >> 24 & 7L) + 0.5F) / 8.0F - 0.5F) * 0.004F;
		poseStack.translate((double)i, (double)j, (double)k);
		double m = Mth.lerp((double)h, abstractMinecart.xOld, abstractMinecart.x);
		double n = Mth.lerp((double)h, abstractMinecart.yOld, abstractMinecart.y);
		double o = Mth.lerp((double)h, abstractMinecart.zOld, abstractMinecart.z);
		double p = 0.3F;
		Vec3 vec3 = abstractMinecart.getPos(m, n, o);
		float q = Mth.lerp(h, abstractMinecart.xRotO, abstractMinecart.xRot);
		if (vec3 != null) {
			Vec3 vec32 = abstractMinecart.getPosOffs(m, n, o, 0.3F);
			Vec3 vec33 = abstractMinecart.getPosOffs(m, n, o, -0.3F);
			if (vec32 == null) {
				vec32 = vec3;
			}

			if (vec33 == null) {
				vec33 = vec3;
			}

			poseStack.translate(vec3.x - m, (vec32.y + vec33.y) / 2.0 - n, vec3.z - o);
			Vec3 vec34 = vec33.add(-vec32.x, -vec32.y, -vec32.z);
			if (vec34.length() != 0.0) {
				vec34 = vec34.normalize();
				g = (float)(Math.atan2(vec34.z, vec34.x) * 180.0 / Math.PI);
				q = (float)(Math.atan(vec34.y) * 73.0);
			}
		}

		poseStack.translate(0.0, 0.375, 0.0);
		poseStack.mulPose(Vector3f.YP.rotation(180.0F - g, true));
		poseStack.mulPose(Vector3f.ZP.rotation(-q, true));
		float r = (float)abstractMinecart.getHurtTime() - h;
		float s = abstractMinecart.getDamage() - h;
		if (s < 0.0F) {
			s = 0.0F;
		}

		if (r > 0.0F) {
			poseStack.mulPose(Vector3f.XP.rotation(Mth.sin(r) * r * s / 10.0F * (float)abstractMinecart.getHurtDir(), true));
		}

		int t = abstractMinecart.getDisplayOffset();
		int u = abstractMinecart.getLightColor();
		BlockState blockState = abstractMinecart.getDisplayBlockState();
		if (blockState.getRenderShape() != RenderShape.INVISIBLE) {
			poseStack.pushPose();
			float v = 0.75F;
			poseStack.scale(0.75F, 0.75F, 0.75F);
			poseStack.translate(-0.5, (double)((float)(t - 8) / 16.0F), 0.5);
			this.renderMinecartContents(abstractMinecart, h, blockState, poseStack, multiBufferSource, u);
			poseStack.popPose();
		}

		poseStack.scale(-1.0F, -1.0F, 1.0F);
		this.model.setupAnim(abstractMinecart, 0.0F, 0.0F, -0.1F, 0.0F, 0.0F, 0.0625F);
		VertexConsumer vertexConsumer = multiBufferSource.getBuffer(RenderType.NEW_ENTITY(this.getTextureLocation(abstractMinecart)));
		OverlayTexture.setDefault(vertexConsumer);
		this.model.renderToBuffer(poseStack, vertexConsumer, u);
		vertexConsumer.unsetDefaultOverlayCoords();
		poseStack.popPose();
	}

	public ResourceLocation getTextureLocation(T abstractMinecart) {
		return MINECART_LOCATION;
	}

	protected void renderMinecartContents(T abstractMinecart, float f, BlockState blockState, PoseStack poseStack, MultiBufferSource multiBufferSource, int i) {
		Minecraft.getInstance().getBlockRenderer().renderSingleBlock(blockState, poseStack, multiBufferSource, i, 0, 10);
	}
}
