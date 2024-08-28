package net.minecraft.client.renderer.entity.layers;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.WardenModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.state.WardenRenderState;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;

@Environment(EnvType.CLIENT)
public class WardenEmissiveLayer extends RenderLayer<WardenRenderState, WardenModel> {
	private final ResourceLocation texture;
	private final WardenEmissiveLayer.AlphaFunction alphaFunction;
	private final WardenEmissiveLayer.DrawSelector drawSelector;

	public WardenEmissiveLayer(
		RenderLayerParent<WardenRenderState, WardenModel> renderLayerParent,
		ResourceLocation resourceLocation,
		WardenEmissiveLayer.AlphaFunction alphaFunction,
		WardenEmissiveLayer.DrawSelector drawSelector
	) {
		super(renderLayerParent);
		this.texture = resourceLocation;
		this.alphaFunction = alphaFunction;
		this.drawSelector = drawSelector;
	}

	public void render(PoseStack poseStack, MultiBufferSource multiBufferSource, int i, WardenRenderState wardenRenderState, float f, float g) {
		if (!wardenRenderState.isInvisible) {
			this.onlyDrawSelectedParts();
			VertexConsumer vertexConsumer = multiBufferSource.getBuffer(RenderType.entityTranslucentEmissive(this.texture));
			float h = this.alphaFunction.apply(wardenRenderState, wardenRenderState.ageInTicks);
			int j = ARGB.color(Mth.floor(h * 255.0F), 255, 255, 255);
			this.getParentModel().renderToBuffer(poseStack, vertexConsumer, i, LivingEntityRenderer.getOverlayCoords(wardenRenderState, 0.0F), j);
			this.resetDrawForAllParts();
		}
	}

	private void onlyDrawSelectedParts() {
		List<ModelPart> list = this.drawSelector.getPartsToDraw(this.getParentModel());
		this.getParentModel().allParts().forEach(modelPart -> modelPart.skipDraw = true);
		list.forEach(modelPart -> modelPart.skipDraw = false);
	}

	private void resetDrawForAllParts() {
		this.getParentModel().allParts().forEach(modelPart -> modelPart.skipDraw = false);
	}

	@Environment(EnvType.CLIENT)
	public interface AlphaFunction {
		float apply(WardenRenderState wardenRenderState, float f);
	}

	@Environment(EnvType.CLIENT)
	public interface DrawSelector {
		List<ModelPart> getPartsToDraw(WardenModel wardenModel);
	}
}
