package net.minecraft.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import java.util.Arrays;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.IllagerModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.layers.ItemInHandLayer;
import net.minecraft.client.renderer.entity.state.IllusionerRenderState;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.monster.Illusioner;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

@Environment(EnvType.CLIENT)
public class IllusionerRenderer extends IllagerRenderer<Illusioner, IllusionerRenderState> {
	private static final ResourceLocation ILLUSIONER = ResourceLocation.withDefaultNamespace("textures/entity/illager/illusioner.png");

	public IllusionerRenderer(EntityRendererProvider.Context context) {
		super(context, new IllagerModel<>(context.bakeLayer(ModelLayers.ILLUSIONER)), 0.5F);
		this.addLayer(new ItemInHandLayer<IllusionerRenderState, IllagerModel<IllusionerRenderState>>(this, context.getItemRenderer()) {
			public void render(PoseStack poseStack, MultiBufferSource multiBufferSource, int i, IllusionerRenderState illusionerRenderState, float f, float g) {
				if (illusionerRenderState.isCastingSpell || illusionerRenderState.isAggressive) {
					super.render(poseStack, multiBufferSource, i, illusionerRenderState, f, g);
				}
			}
		});
		this.model.getHat().visible = true;
	}

	public ResourceLocation getTextureLocation(IllusionerRenderState illusionerRenderState) {
		return ILLUSIONER;
	}

	public IllusionerRenderState createRenderState() {
		return new IllusionerRenderState();
	}

	public void extractRenderState(Illusioner illusioner, IllusionerRenderState illusionerRenderState, float f) {
		super.extractRenderState(illusioner, illusionerRenderState, f);
		Vec3[] vec3s = illusioner.getIllusionOffsets(f);
		illusionerRenderState.illusionOffsets = (Vec3[])Arrays.copyOf(vec3s, vec3s.length);
		illusionerRenderState.isCastingSpell = illusioner.isCastingSpell();
	}

	public void render(IllusionerRenderState illusionerRenderState, PoseStack poseStack, MultiBufferSource multiBufferSource, int i) {
		if (illusionerRenderState.isInvisible) {
			Vec3[] vec3s = illusionerRenderState.illusionOffsets;

			for (int j = 0; j < vec3s.length; j++) {
				poseStack.pushPose();
				poseStack.translate(
					vec3s[j].x + (double)Mth.cos((float)j + illusionerRenderState.ageInTicks * 0.5F) * 0.025,
					vec3s[j].y + (double)Mth.cos((float)j + illusionerRenderState.ageInTicks * 0.75F) * 0.0125,
					vec3s[j].z + (double)Mth.cos((float)j + illusionerRenderState.ageInTicks * 0.7F) * 0.025
				);
				super.render(illusionerRenderState, poseStack, multiBufferSource, i);
				poseStack.popPose();
			}
		} else {
			super.render(illusionerRenderState, poseStack, multiBufferSource, i);
		}
	}

	protected boolean isBodyVisible(IllusionerRenderState illusionerRenderState) {
		return true;
	}

	protected AABB getBoundingBoxForCulling(Illusioner illusioner) {
		return super.getBoundingBoxForCulling(illusioner).inflate(3.0, 0.0, 3.0);
	}
}
