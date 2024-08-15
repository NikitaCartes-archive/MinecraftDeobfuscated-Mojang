package net.minecraft.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.CatModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.layers.CatCollarLayer;
import net.minecraft.client.renderer.entity.state.CatRenderState;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.animal.Cat;

@Environment(EnvType.CLIENT)
public class CatRenderer extends AgeableMobRenderer<Cat, CatRenderState, CatModel> {
	public CatRenderer(EntityRendererProvider.Context context) {
		super(context, new CatModel(context.bakeLayer(ModelLayers.CAT)), new CatModel(context.bakeLayer(ModelLayers.CAT_BABY)), 0.4F);
		this.addLayer(new CatCollarLayer(this, context.getModelSet()));
	}

	public ResourceLocation getTextureLocation(CatRenderState catRenderState) {
		return catRenderState.texture;
	}

	public CatRenderState createRenderState() {
		return new CatRenderState();
	}

	public void extractRenderState(Cat cat, CatRenderState catRenderState, float f) {
		super.extractRenderState(cat, catRenderState, f);
		catRenderState.texture = cat.getVariant().value().texture();
		catRenderState.isCrouching = cat.isCrouching();
		catRenderState.isSprinting = cat.isSprinting();
		catRenderState.isSitting = cat.isInSittingPose();
		catRenderState.lieDownAmount = cat.getLieDownAmount(f);
		catRenderState.lieDownAmountTail = cat.getLieDownAmountTail(f);
		catRenderState.relaxStateOneAmount = cat.getRelaxStateOneAmount(f);
		catRenderState.isLyingOnTopOfSleepingPlayer = cat.isLyingOnTopOfSleepingPlayer();
		catRenderState.collarColor = cat.isTame() ? cat.getCollarColor() : null;
	}

	protected void setupRotations(CatRenderState catRenderState, PoseStack poseStack, float f, float g) {
		super.setupRotations(catRenderState, poseStack, f, g);
		float h = catRenderState.lieDownAmount;
		if (h > 0.0F) {
			poseStack.translate(0.4F * h, 0.15F * h, 0.1F * h);
			poseStack.mulPose(Axis.ZP.rotationDegrees(Mth.rotLerp(h, 0.0F, 90.0F)));
			if (catRenderState.isLyingOnTopOfSleepingPlayer) {
				poseStack.translate(0.15F * h, 0.0F, 0.0F);
			}
		}
	}
}
