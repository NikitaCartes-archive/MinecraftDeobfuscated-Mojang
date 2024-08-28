package net.minecraft.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.SlimeModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.layers.SlimeOuterLayer;
import net.minecraft.client.renderer.entity.state.SlimeRenderState;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.monster.Slime;

@Environment(EnvType.CLIENT)
public class SlimeRenderer extends MobRenderer<Slime, SlimeRenderState, SlimeModel> {
	public static final ResourceLocation SLIME_LOCATION = ResourceLocation.withDefaultNamespace("textures/entity/slime/slime.png");

	public SlimeRenderer(EntityRendererProvider.Context context) {
		super(context, new SlimeModel(context.bakeLayer(ModelLayers.SLIME)), 0.25F);
		this.addLayer(new SlimeOuterLayer(this, context.getModelSet()));
	}

	public void render(SlimeRenderState slimeRenderState, PoseStack poseStack, MultiBufferSource multiBufferSource, int i) {
		this.shadowRadius = 0.25F * (float)slimeRenderState.size;
		super.render(slimeRenderState, poseStack, multiBufferSource, i);
	}

	protected void scale(SlimeRenderState slimeRenderState, PoseStack poseStack) {
		float f = 0.999F;
		poseStack.scale(0.999F, 0.999F, 0.999F);
		poseStack.translate(0.0F, 0.001F, 0.0F);
		float g = (float)slimeRenderState.size;
		float h = slimeRenderState.squish / (g * 0.5F + 1.0F);
		float i = 1.0F / (h + 1.0F);
		poseStack.scale(i * g, 1.0F / i * g, i * g);
	}

	public ResourceLocation getTextureLocation(SlimeRenderState slimeRenderState) {
		return SLIME_LOCATION;
	}

	public SlimeRenderState createRenderState() {
		return new SlimeRenderState();
	}

	public void extractRenderState(Slime slime, SlimeRenderState slimeRenderState, float f) {
		super.extractRenderState(slime, slimeRenderState, f);
		slimeRenderState.squish = Mth.lerp(f, slime.oSquish, slime.squish);
		slimeRenderState.size = slime.getSize();
	}
}
