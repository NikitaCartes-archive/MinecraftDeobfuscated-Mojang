package net.minecraft.client.renderer.entity.layers;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.monster.warden.Warden;

@Environment(EnvType.CLIENT)
public class WardenEmissiveLayer<T extends Warden, M extends EntityModel<T>> extends RenderLayer<T, M> {
	private final ResourceLocation texture;
	private final WardenEmissiveLayer.AlphaFunction<T> alphaFunction;

	public WardenEmissiveLayer(RenderLayerParent<T, M> renderLayerParent, ResourceLocation resourceLocation, WardenEmissiveLayer.AlphaFunction<T> alphaFunction) {
		super(renderLayerParent);
		this.texture = resourceLocation;
		this.alphaFunction = alphaFunction;
	}

	public void render(PoseStack poseStack, MultiBufferSource multiBufferSource, int i, T warden, float f, float g, float h, float j, float k, float l) {
		if (!warden.isInvisible()) {
			VertexConsumer vertexConsumer = multiBufferSource.getBuffer(RenderType.entityTranslucentEmissive(this.texture));
			this.getParentModel()
				.renderToBuffer(poseStack, vertexConsumer, i, LivingEntityRenderer.getOverlayCoords(warden, 0.0F), 1.0F, 1.0F, 1.0F, this.alphaFunction.apply(warden, h, j));
		}
	}

	@Environment(EnvType.CLIENT)
	public interface AlphaFunction<T extends Warden> {
		float apply(T warden, float f, float g);
	}
}
