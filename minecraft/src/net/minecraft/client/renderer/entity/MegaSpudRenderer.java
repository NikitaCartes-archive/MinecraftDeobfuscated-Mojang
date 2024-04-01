package net.minecraft.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.MegaSpudModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.layers.MegaSpudArmorLayer;
import net.minecraft.client.renderer.entity.layers.MegaSpudOuterLayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.monster.MegaSpud;

@Environment(EnvType.CLIENT)
public class MegaSpudRenderer extends MobRenderer<MegaSpud, MegaSpudModel<MegaSpud>> {
	private static final ResourceLocation SLIME_LOCATION = new ResourceLocation("textures/entity/slime/mega_spud.png");

	public MegaSpudRenderer(EntityRendererProvider.Context context) {
		super(context, new MegaSpudModel<>(context.bakeLayer(ModelLayers.MEGA_SPUD)), 0.25F);
		this.addLayer(new MegaSpudOuterLayer<>(this, context.getModelSet()));
		this.addLayer(new MegaSpudArmorLayer(this, context.getModelSet()));
	}

	public void render(MegaSpud megaSpud, float f, float g, PoseStack poseStack, MultiBufferSource multiBufferSource, int i) {
		this.shadowRadius = 0.25F * (float)megaSpud.getSize();
		super.render(megaSpud, f, g, poseStack, multiBufferSource, i);
	}

	protected void scale(MegaSpud megaSpud, PoseStack poseStack, float f) {
		float g = 0.999F;
		poseStack.scale(0.999F, 0.999F, 0.999F);
		poseStack.translate(0.0F, 0.001F, 0.0F);
		float h = (float)megaSpud.getSize();
		float i = Mth.lerp(f, megaSpud.oSquish, megaSpud.squish) / (h * 0.5F + 1.0F);
		float j = 1.0F / (i + 1.0F);
		poseStack.scale(j * h, 1.5F / j * h, j * h);
	}

	public ResourceLocation getTextureLocation(MegaSpud megaSpud) {
		return SLIME_LOCATION;
	}
}
