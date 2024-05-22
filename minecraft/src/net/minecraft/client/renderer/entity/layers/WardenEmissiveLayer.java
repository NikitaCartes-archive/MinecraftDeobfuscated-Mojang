package net.minecraft.client.renderer.entity.layers;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.WardenModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FastColor;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.monster.warden.Warden;

@Environment(EnvType.CLIENT)
public class WardenEmissiveLayer<T extends Warden, M extends WardenModel<T>> extends RenderLayer<T, M> {
	private final ResourceLocation texture;
	private final WardenEmissiveLayer.AlphaFunction<T> alphaFunction;
	private final WardenEmissiveLayer.DrawSelector<T, M> drawSelector;

	public WardenEmissiveLayer(
		RenderLayerParent<T, M> renderLayerParent,
		ResourceLocation resourceLocation,
		WardenEmissiveLayer.AlphaFunction<T> alphaFunction,
		WardenEmissiveLayer.DrawSelector<T, M> drawSelector
	) {
		super(renderLayerParent);
		this.texture = resourceLocation;
		this.alphaFunction = alphaFunction;
		this.drawSelector = drawSelector;
	}

	public void render(PoseStack poseStack, MultiBufferSource multiBufferSource, int i, T warden, float f, float g, float h, float j, float k, float l) {
		if (!warden.isInvisible()) {
			this.onlyDrawSelectedParts();
			VertexConsumer vertexConsumer = multiBufferSource.getBuffer(RenderType.entityTranslucentEmissive(this.texture));
			float m = this.alphaFunction.apply(warden, h, j);
			int n = FastColor.ARGB32.color(Mth.floor(m * 255.0F), 255, 255, 255);
			this.getParentModel().renderToBuffer(poseStack, vertexConsumer, i, LivingEntityRenderer.getOverlayCoords(warden, 0.0F), n);
			this.resetDrawForAllParts();
		}
	}

	private void onlyDrawSelectedParts() {
		List<ModelPart> list = this.drawSelector.getPartsToDraw(this.getParentModel());
		this.getParentModel().root().getAllParts().forEach(modelPart -> modelPart.skipDraw = true);
		list.forEach(modelPart -> modelPart.skipDraw = false);
	}

	private void resetDrawForAllParts() {
		this.getParentModel().root().getAllParts().forEach(modelPart -> modelPart.skipDraw = false);
	}

	@Environment(EnvType.CLIENT)
	public interface AlphaFunction<T extends Warden> {
		float apply(T warden, float f, float g);
	}

	@Environment(EnvType.CLIENT)
	public interface DrawSelector<T extends Warden, M extends EntityModel<T>> {
		List<ModelPart> getPartsToDraw(M entityModel);
	}
}
