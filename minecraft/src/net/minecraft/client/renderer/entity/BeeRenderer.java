package net.minecraft.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.BeeModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FastColor;
import net.minecraft.util.Mth;
import net.minecraft.voting.rules.Rules;
import net.minecraft.world.entity.animal.Bee;
import net.minecraft.world.level.LevelTimeAccess;
import org.joml.Matrix4f;

@Environment(EnvType.CLIENT)
public class BeeRenderer extends MobRenderer<Bee, BeeModel<Bee>> {
	public static final StencilRenderer.Triangle[] FACES = StencilRenderer.createNSphere(2);
	private static final ResourceLocation ANGRY_BEE_TEXTURE = new ResourceLocation("textures/entity/bee/bee_angry.png");
	private static final ResourceLocation ANGRY_NECTAR_BEE_TEXTURE = new ResourceLocation("textures/entity/bee/bee_angry_nectar.png");
	private static final ResourceLocation BEE_TEXTURE = new ResourceLocation("textures/entity/bee/bee.png");
	private static final ResourceLocation NECTAR_BEE_TEXTURE = new ResourceLocation("textures/entity/bee/bee_nectar.png");

	public BeeRenderer(EntityRendererProvider.Context context) {
		super(context, new BeeModel<>(context.bakeLayer(ModelLayers.BEE)), 0.4F);
	}

	public static boolean isGlowTime(LevelTimeAccess levelTimeAccess) {
		long l = levelTimeAccess.dayTime() % 24000L;
		return l >= 13000L && l < 23000L;
	}

	public void render(Bee bee, float f, float g, PoseStack poseStack, MultiBufferSource multiBufferSource, int i) {
		if (Rules.GLOW_BEES.get() && Rules.GLOWING_GLOW_SQUIDS.get() && isGlowTime(bee.level)) {
			int j = FastColor.ARGB32.color(32, 0, 192, 255);
			float h = 1.5F + Mth.cos(this.getBob(bee, g) * 2.0F) / 32.0F;
			poseStack.pushPose();
			poseStack.scale(h, h, h);
			Matrix4f matrix4f = poseStack.last().pose();
			StencilRenderer.render(FACES, matrix4f, multiBufferSource, j);
			poseStack.popPose();
		}

		super.render(bee, f, g, poseStack, multiBufferSource, i);
	}

	public ResourceLocation getTextureLocation(Bee bee) {
		if (bee.isAngry()) {
			return bee.hasNectar() ? ANGRY_NECTAR_BEE_TEXTURE : ANGRY_BEE_TEXTURE;
		} else {
			return bee.hasNectar() ? NECTAR_BEE_TEXTURE : BEE_TEXTURE;
		}
	}
}
