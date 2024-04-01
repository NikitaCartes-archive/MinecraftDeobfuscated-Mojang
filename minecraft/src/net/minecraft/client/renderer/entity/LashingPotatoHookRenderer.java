package net.minecraft.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.LashingPotatoHook;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.Vec3;

@Environment(EnvType.CLIENT)
public class LashingPotatoHookRenderer extends EntityRenderer<LashingPotatoHook> {
	private final ItemRenderer itemRenderer;

	public LashingPotatoHookRenderer(EntityRendererProvider.Context context) {
		super(context);
		this.itemRenderer = context.getItemRenderer();
	}

	public void render(LashingPotatoHook lashingPotatoHook, float f, float g, PoseStack poseStack, MultiBufferSource multiBufferSource, int i) {
		Player player = lashingPotatoHook.getPlayerOwner();
		if (player != null) {
			poseStack.pushPose();
			this.itemRenderer
				.renderStatic(
					new ItemStack(Items.POISONOUS_POTATO),
					ItemDisplayContext.GROUND,
					i,
					OverlayTexture.NO_OVERLAY,
					poseStack,
					multiBufferSource,
					lashingPotatoHook.level(),
					lashingPotatoHook.getId()
				);
			Vec3 vec3 = FishingHookRenderer.getPlayerHandPos(player, g, Items.LASHING_POTATO, this.entityRenderDispatcher);
			Vec3 vec32 = new Vec3(
				Mth.lerp((double)g, lashingPotatoHook.xo, lashingPotatoHook.getX()),
				Mth.lerp((double)g, lashingPotatoHook.yo, lashingPotatoHook.getY()) + (double)lashingPotatoHook.getEyeHeight(),
				Mth.lerp((double)g, lashingPotatoHook.zo, lashingPotatoHook.getZ())
			);
			float h = (float)lashingPotatoHook.tickCount + g;
			float j = h * 0.15F % 1.0F;
			Vec3 vec33 = vec3.subtract(vec32);
			float k = (float)(vec33.length() + 0.1);
			vec33 = vec33.normalize();
			float l = (float)Math.acos(vec33.y);
			float m = (float)Math.atan2(vec33.z, vec33.x);
			poseStack.mulPose(Axis.YP.rotationDegrees(((float) (Math.PI / 2) - m) * (180.0F / (float)Math.PI)));
			poseStack.mulPose(Axis.XP.rotationDegrees(l * (180.0F / (float)Math.PI)));
			float n = h * 0.05F * -1.5F;
			float o = 0.2F;
			float p = Mth.cos(n + (float) Math.PI) * 0.2F;
			float q = Mth.sin(n + (float) Math.PI) * 0.2F;
			float r = Mth.cos(n + 0.0F) * 0.2F;
			float s = Mth.sin(n + 0.0F) * 0.2F;
			float t = Mth.cos(n + (float) (Math.PI / 2)) * 0.2F;
			float u = Mth.sin(n + (float) (Math.PI / 2)) * 0.2F;
			float v = Mth.cos(n + (float) (Math.PI * 3.0 / 2.0)) * 0.2F;
			float w = Mth.sin(n + (float) (Math.PI * 3.0 / 2.0)) * 0.2F;
			float y = 0.0F;
			float z = 0.4999F;
			float aa = -1.0F + j;
			float ab = k * 2.5F + aa;
			VertexConsumer vertexConsumer = multiBufferSource.getBuffer(RenderType.entityCutoutNoCull(GuardianRenderer.TOXIFIN_BEAM_LOCATION));
			PoseStack.Pose pose = poseStack.last();
			vertex(vertexConsumer, pose, p, k, q, 0.4999F, ab);
			vertex(vertexConsumer, pose, p, 0.0F, q, 0.4999F, aa);
			vertex(vertexConsumer, pose, r, 0.0F, s, 0.0F, aa);
			vertex(vertexConsumer, pose, r, k, s, 0.0F, ab);
			vertex(vertexConsumer, pose, t, k, u, 0.4999F, ab);
			vertex(vertexConsumer, pose, t, 0.0F, u, 0.4999F, aa);
			vertex(vertexConsumer, pose, v, 0.0F, w, 0.0F, aa);
			vertex(vertexConsumer, pose, v, k, w, 0.0F, ab);
			poseStack.popPose();
			super.render(lashingPotatoHook, f, g, poseStack, multiBufferSource, i);
		}
	}

	private static void vertex(VertexConsumer vertexConsumer, PoseStack.Pose pose, float f, float g, float h, float i, float j) {
		vertexConsumer.vertex(pose, f, g, h)
			.color(128, 255, 128, 255)
			.uv(i, j)
			.overlayCoords(OverlayTexture.NO_OVERLAY)
			.uv2(15728880)
			.normal(0.0F, 1.0F, 0.0F)
			.endVertex();
	}

	public ResourceLocation getTextureLocation(LashingPotatoHook lashingPotatoHook) {
		return TextureAtlas.LOCATION_BLOCKS;
	}
}
