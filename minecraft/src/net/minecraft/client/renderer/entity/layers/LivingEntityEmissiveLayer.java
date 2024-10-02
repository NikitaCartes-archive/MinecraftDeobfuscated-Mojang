package net.minecraft.client.renderer.entity.layers;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import java.util.List;
import java.util.function.Function;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;

@Environment(EnvType.CLIENT)
public class LivingEntityEmissiveLayer<S extends LivingEntityRenderState, M extends EntityModel<S>> extends RenderLayer<S, M> {
	private final ResourceLocation texture;
	private final LivingEntityEmissiveLayer.AlphaFunction<S> alphaFunction;
	private final LivingEntityEmissiveLayer.DrawSelector<S, M> drawSelector;
	private final Function<ResourceLocation, RenderType> bufferProvider;

	public LivingEntityEmissiveLayer(
		RenderLayerParent<S, M> renderLayerParent,
		ResourceLocation resourceLocation,
		LivingEntityEmissiveLayer.AlphaFunction<S> alphaFunction,
		LivingEntityEmissiveLayer.DrawSelector<S, M> drawSelector,
		Function<ResourceLocation, RenderType> function
	) {
		super(renderLayerParent);
		this.texture = resourceLocation;
		this.alphaFunction = alphaFunction;
		this.drawSelector = drawSelector;
		this.bufferProvider = function;
	}

	public void render(PoseStack poseStack, MultiBufferSource multiBufferSource, int i, S livingEntityRenderState, float f, float g) {
		if (!livingEntityRenderState.isInvisible) {
			if (this.onlyDrawSelectedParts(livingEntityRenderState)) {
				VertexConsumer vertexConsumer = multiBufferSource.getBuffer((RenderType)this.bufferProvider.apply(this.texture));
				float h = this.alphaFunction.apply(livingEntityRenderState, livingEntityRenderState.ageInTicks);
				int j = ARGB.color(Mth.floor(h * 255.0F), 255, 255, 255);
				this.getParentModel().renderToBuffer(poseStack, vertexConsumer, i, LivingEntityRenderer.getOverlayCoords(livingEntityRenderState, 0.0F), j);
				this.resetDrawForAllParts();
			}
		}
	}

	private boolean onlyDrawSelectedParts(S livingEntityRenderState) {
		List<ModelPart> list = this.drawSelector.getPartsToDraw(this.getParentModel(), livingEntityRenderState);
		if (list.isEmpty()) {
			return false;
		} else {
			this.getParentModel().allParts().forEach(modelPart -> modelPart.skipDraw = true);
			list.forEach(modelPart -> modelPart.skipDraw = false);
			return true;
		}
	}

	private void resetDrawForAllParts() {
		this.getParentModel().allParts().forEach(modelPart -> modelPart.skipDraw = false);
	}

	@Environment(EnvType.CLIENT)
	public interface AlphaFunction<S extends LivingEntityRenderState> {
		float apply(S livingEntityRenderState, float f);
	}

	@Environment(EnvType.CLIENT)
	public interface DrawSelector<S extends LivingEntityRenderState, M extends EntityModel<S>> {
		List<ModelPart> getPartsToDraw(M entityModel, S livingEntityRenderState);
	}
}
