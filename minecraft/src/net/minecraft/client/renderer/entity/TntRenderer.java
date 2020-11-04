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
	public TntRenderer(EntityRendererProvider.Context context) {
		super(context);
		this.shadowRadius = 0.5F;
	}

	public void render(PrimedTnt primedTnt, float f, float g, PoseStack poseStack, MultiBufferSource multiBufferSource, int i) {
		poseStack.pushPose();
		poseStack.translate(0.0, 0.5, 0.0);
		if ((float)primedTnt.getLife() - g + 1.0F < 10.0F) {
			float h = 1.0F - ((float)primedTnt.getLife() - g + 1.0F) / 10.0F;
			h = Mth.clamp(h, 0.0F, 1.0F);
			h *= h;
			h *= h;
			float j = 1.0F + h * 0.3F;
			poseStack.scale(j, j, j);
		}

		poseStack.mulPose(Vector3f.YP.rotationDegrees(-90.0F));
		poseStack.translate(-0.5, -0.5, 0.5);
		poseStack.mulPose(Vector3f.YP.rotationDegrees(90.0F));
		TntMinecartRenderer.renderWhiteSolidBlock(Blocks.TNT.defaultBlockState(), poseStack, multiBufferSource, i, primedTnt.getLife() / 5 % 2 == 0);
		poseStack.popPose();
		super.render(primedTnt, f, g, poseStack, multiBufferSource, i);
	}

	public ResourceLocation getTextureLocation(PrimedTnt primedTnt) {
		return TextureAtlas.LOCATION_BLOCKS;
	}
}
