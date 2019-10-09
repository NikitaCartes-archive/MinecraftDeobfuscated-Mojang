package net.minecraft.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Vector3f;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.item.PrimedTnt;
import net.minecraft.world.level.block.Blocks;

@Environment(EnvType.CLIENT)
public class TntRenderer extends EntityRenderer<PrimedTnt> {
	public TntRenderer(EntityRenderDispatcher entityRenderDispatcher) {
		super(entityRenderDispatcher);
		this.shadowRadius = 0.5F;
	}

	public void render(PrimedTnt primedTnt, double d, double e, double f, float g, float h, PoseStack poseStack, MultiBufferSource multiBufferSource) {
		poseStack.pushPose();
		poseStack.translate(0.0, 0.5, 0.0);
		if ((float)primedTnt.getLife() - h + 1.0F < 10.0F) {
			float i = 1.0F - ((float)primedTnt.getLife() - h + 1.0F) / 10.0F;
			i = Mth.clamp(i, 0.0F, 1.0F);
			i *= i;
			i *= i;
			float j = 1.0F + i * 0.3F;
			poseStack.scale(j, j, j);
		}

		int k = primedTnt.getLightColor();
		poseStack.mulPose(Vector3f.YP.rotationDegrees(-90.0F));
		poseStack.translate(-0.5, -0.5, 0.5);
		TntMinecartRenderer.renderWhiteSolidBlock(Blocks.TNT.defaultBlockState(), poseStack, multiBufferSource, k, primedTnt.getLife() / 5 % 2 == 0);
		poseStack.popPose();
		super.render(primedTnt, d, e, f, g, h, poseStack, multiBufferSource);
	}

	public ResourceLocation getTextureLocation(PrimedTnt primedTnt) {
		return TextureAtlas.LOCATION_BLOCKS;
	}
}
