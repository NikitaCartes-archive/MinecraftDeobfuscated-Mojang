package net.minecraft.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.IllagerModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.layers.ItemInHandLayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.monster.Illusioner;
import net.minecraft.world.phys.Vec3;

@Environment(EnvType.CLIENT)
public class IllusionerRenderer extends IllagerRenderer<Illusioner> {
	private static final ResourceLocation ILLUSIONER = new ResourceLocation("textures/entity/illager/illusioner.png");

	public IllusionerRenderer(EntityRendererProvider.Context context) {
		super(context, new IllagerModel<>(context.bakeLayer(ModelLayers.ILLUSIONER)), 0.5F);
		this.addLayer(
			new ItemInHandLayer<Illusioner, IllagerModel<Illusioner>>(this) {
				public void render(
					PoseStack poseStack, MultiBufferSource multiBufferSource, int i, Illusioner illusioner, float f, float g, float h, float j, float k, float l
				) {
					if (illusioner.isCastingSpell() || illusioner.isAggressive()) {
						super.render(poseStack, multiBufferSource, i, illusioner, f, g, h, j, k, l);
					}
				}
			}
		);
		this.model.getHat().visible = true;
	}

	public ResourceLocation getTextureLocation(Illusioner illusioner) {
		return ILLUSIONER;
	}

	public void render(Illusioner illusioner, float f, float g, PoseStack poseStack, MultiBufferSource multiBufferSource, int i) {
		if (illusioner.isInvisible()) {
			Vec3[] vec3s = illusioner.getIllusionOffsets(g);
			float h = this.getBob(illusioner, g);

			for (int j = 0; j < vec3s.length; j++) {
				poseStack.pushPose();
				poseStack.translate(
					vec3s[j].x + (double)Mth.cos((float)j + h * 0.5F) * 0.025,
					vec3s[j].y + (double)Mth.cos((float)j + h * 0.75F) * 0.0125,
					vec3s[j].z + (double)Mth.cos((float)j + h * 0.7F) * 0.025
				);
				super.render(illusioner, f, g, poseStack, multiBufferSource, i);
				poseStack.popPose();
			}
		} else {
			super.render(illusioner, f, g, poseStack, multiBufferSource, i);
		}
	}

	protected boolean isBodyVisible(Illusioner illusioner) {
		return true;
	}
}
