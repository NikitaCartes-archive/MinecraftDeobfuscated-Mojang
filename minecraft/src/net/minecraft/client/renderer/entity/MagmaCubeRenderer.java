package net.minecraft.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.LavaSlimeModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.state.SlimeRenderState;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.monster.MagmaCube;

@Environment(EnvType.CLIENT)
public class MagmaCubeRenderer extends MobRenderer<MagmaCube, SlimeRenderState, LavaSlimeModel> {
	private static final ResourceLocation MAGMACUBE_LOCATION = ResourceLocation.withDefaultNamespace("textures/entity/slime/magmacube.png");

	public MagmaCubeRenderer(EntityRendererProvider.Context context) {
		super(context, new LavaSlimeModel(context.bakeLayer(ModelLayers.MAGMA_CUBE)), 0.25F);
	}

	protected int getBlockLightLevel(MagmaCube magmaCube, BlockPos blockPos) {
		return 15;
	}

	public ResourceLocation getTextureLocation(SlimeRenderState slimeRenderState) {
		return MAGMACUBE_LOCATION;
	}

	public SlimeRenderState createRenderState() {
		return new SlimeRenderState();
	}

	public void extractRenderState(MagmaCube magmaCube, SlimeRenderState slimeRenderState, float f) {
		super.extractRenderState(magmaCube, slimeRenderState, f);
		slimeRenderState.squish = Mth.lerp(f, magmaCube.oSquish, magmaCube.squish);
		slimeRenderState.size = magmaCube.getSize();
	}

	public void render(SlimeRenderState slimeRenderState, PoseStack poseStack, MultiBufferSource multiBufferSource, int i) {
		this.shadowRadius = 0.25F * (float)slimeRenderState.size;
		super.render(slimeRenderState, poseStack, multiBufferSource, i);
	}

	protected void scale(SlimeRenderState slimeRenderState, PoseStack poseStack) {
		int i = slimeRenderState.size;
		float f = slimeRenderState.squish / ((float)i * 0.5F + 1.0F);
		float g = 1.0F / (f + 1.0F);
		poseStack.scale(g * (float)i, 1.0F / g * (float)i, g * (float)i);
	}
}
