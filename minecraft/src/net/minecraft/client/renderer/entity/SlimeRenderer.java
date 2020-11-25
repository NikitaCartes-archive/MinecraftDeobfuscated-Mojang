package net.minecraft.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.SlimeModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.layers.SlimeOuterLayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.monster.Slime;

@Environment(EnvType.CLIENT)
public class SlimeRenderer extends MobRenderer<Slime, SlimeModel<Slime>> {
	private static final ResourceLocation SLIME_LOCATION = new ResourceLocation("textures/entity/slime/slime.png");

	public SlimeRenderer(EntityRendererProvider.Context context) {
		super(context, new SlimeModel<>(context.bakeLayer(ModelLayers.SLIME)), 0.25F);
		this.addLayer(new SlimeOuterLayer<>(this, context.getModelSet()));
	}

	public void render(Slime slime, float f, float g, PoseStack poseStack, MultiBufferSource multiBufferSource, int i) {
		this.shadowRadius = 0.25F * (float)slime.getSize();
		super.render(slime, f, g, poseStack, multiBufferSource, i);
	}

	protected void scale(Slime slime, PoseStack poseStack, float f) {
		float g = 0.999F;
		poseStack.scale(0.999F, 0.999F, 0.999F);
		poseStack.translate(0.0, 0.001F, 0.0);
		float h = (float)slime.getSize();
		float i = Mth.lerp(f, slime.oSquish, slime.squish) / (h * 0.5F + 1.0F);
		float j = 1.0F / (i + 1.0F);
		poseStack.scale(j * h, 1.0F / j * h, j * h);
	}

	public ResourceLocation getTextureLocation(Slime slime) {
		return SLIME_LOCATION;
	}
}
