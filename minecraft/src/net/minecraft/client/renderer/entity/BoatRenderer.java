package net.minecraft.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Quaternion;
import com.mojang.math.Vector3f;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.BoatModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.vehicle.Boat;

@Environment(EnvType.CLIENT)
public class BoatRenderer extends EntityRenderer<Boat> {
	private static final ResourceLocation[] BOAT_TEXTURE_LOCATIONS = new ResourceLocation[]{
		new ResourceLocation("textures/entity/boat/oak.png"),
		new ResourceLocation("textures/entity/boat/spruce.png"),
		new ResourceLocation("textures/entity/boat/birch.png"),
		new ResourceLocation("textures/entity/boat/jungle.png"),
		new ResourceLocation("textures/entity/boat/acacia.png"),
		new ResourceLocation("textures/entity/boat/dark_oak.png")
	};
	protected final BoatModel model = new BoatModel();

	public BoatRenderer(EntityRenderDispatcher entityRenderDispatcher) {
		super(entityRenderDispatcher);
		this.shadowRadius = 0.8F;
	}

	public void render(Boat boat, double d, double e, double f, float g, float h, PoseStack poseStack, MultiBufferSource multiBufferSource) {
		poseStack.pushPose();
		poseStack.translate(0.0, 0.375, 0.0);
		poseStack.mulPose(Vector3f.YP.rotation(180.0F - g, true));
		float i = (float)boat.getHurtTime() - h;
		float j = boat.getDamage() - h;
		if (j < 0.0F) {
			j = 0.0F;
		}

		if (i > 0.0F) {
			poseStack.mulPose(Vector3f.XP.rotation(Mth.sin(i) * i * j / 10.0F * (float)boat.getHurtDir(), true));
		}

		float k = boat.getBubbleAngle(h);
		if (!Mth.equal(k, 0.0F)) {
			poseStack.mulPose(new Quaternion(new Vector3f(1.0F, 0.0F, 1.0F), boat.getBubbleAngle(h), true));
		}

		poseStack.scale(-1.0F, -1.0F, 1.0F);
		int l = boat.getLightColor();
		VertexConsumer vertexConsumer = multiBufferSource.getBuffer(RenderType.NEW_ENTITY(this.getTextureLocation(boat)));
		OverlayTexture.setDefault(vertexConsumer);
		poseStack.mulPose(Vector3f.YP.rotation(90.0F, true));
		this.model.setupAnim(boat, h, 0.0F, -0.1F, 0.0F, 0.0F, 0.0625F);
		this.model.renderToBuffer(poseStack, vertexConsumer, l);
		VertexConsumer vertexConsumer2 = multiBufferSource.getBuffer(RenderType.WATER_MASK);
		this.model.waterPatch().render(poseStack, vertexConsumer2, 0.0625F, l, null);
		poseStack.popPose();
		vertexConsumer.unsetDefaultOverlayCoords();
		super.render(boat, d, e, f, g, h, poseStack, multiBufferSource);
	}

	public ResourceLocation getTextureLocation(Boat boat) {
		return BOAT_TEXTURE_LOCATIONS[boat.getBoatType().ordinal()];
	}
}
