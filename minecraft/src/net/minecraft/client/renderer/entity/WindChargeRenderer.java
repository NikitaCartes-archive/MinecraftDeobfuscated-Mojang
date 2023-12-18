package net.minecraft.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.WindChargeModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.projectile.WindCharge;

@Environment(EnvType.CLIENT)
public class WindChargeRenderer extends EntityRenderer<WindCharge> {
	private static final ResourceLocation TEXTURE_LOCATION = new ResourceLocation("textures/entity/projectiles/wind_charge.png");
	private final WindChargeModel model;

	public WindChargeRenderer(EntityRendererProvider.Context context) {
		super(context);
		this.model = new WindChargeModel(context.bakeLayer(ModelLayers.WIND_CHARGE));
	}

	public void render(WindCharge windCharge, float f, float g, PoseStack poseStack, MultiBufferSource multiBufferSource, int i) {
		float h = (float)windCharge.tickCount + g;
		VertexConsumer vertexConsumer = multiBufferSource.getBuffer(RenderType.breezeWind(TEXTURE_LOCATION, this.xOffset(h) % 1.0F, 0.0F));
		this.model.setupAnim(windCharge, 0.0F, 0.0F, h, 0.0F, 0.0F);
		this.model.renderToBuffer(poseStack, vertexConsumer, i, OverlayTexture.NO_OVERLAY, 1.0F, 1.0F, 1.0F, 1.0F);
		super.render(windCharge, f, g, poseStack, multiBufferSource, i);
	}

	protected float xOffset(float f) {
		return f * 0.03F;
	}

	public ResourceLocation getTextureLocation(WindCharge windCharge) {
		return TEXTURE_LOCATION;
	}
}
