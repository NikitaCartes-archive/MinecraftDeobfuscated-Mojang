package net.minecraft.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.LavaSlimeModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.monster.MagmaCube;

@Environment(EnvType.CLIENT)
public class MagmaCubeRenderer extends MobRenderer<MagmaCube, LavaSlimeModel<MagmaCube>> {
	private static final ResourceLocation MAGMACUBE_LOCATION = new ResourceLocation("textures/entity/slime/magmacube.png");

	public MagmaCubeRenderer(EntityRendererProvider.Context context) {
		super(context, new LavaSlimeModel<>(context.bakeLayer(ModelLayers.MAGMA_CUBE)), 0.25F);
	}

	protected int getBlockLightLevel(MagmaCube magmaCube, BlockPos blockPos) {
		return 15;
	}

	public ResourceLocation getTextureLocation(MagmaCube magmaCube) {
		return MAGMACUBE_LOCATION;
	}

	protected void scale(MagmaCube magmaCube, PoseStack poseStack, float f) {
		int i = magmaCube.getSize();
		float g = Mth.lerp(f, magmaCube.oSquish, magmaCube.squish) / ((float)i * 0.5F + 1.0F);
		float h = 1.0F / (g + 1.0F);
		poseStack.scale(h * (float)i, 1.0F / h * (float)i, h * (float)i);
	}
}
