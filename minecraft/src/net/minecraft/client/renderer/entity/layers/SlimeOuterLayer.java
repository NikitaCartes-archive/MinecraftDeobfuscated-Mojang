package net.minecraft.client.renderer.entity.layers;

import com.mojang.blaze3d.vertex.PoseStack;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.SlimeModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.world.entity.LivingEntity;

@Environment(EnvType.CLIENT)
public class SlimeOuterLayer<T extends LivingEntity> extends RenderLayer<T, SlimeModel<T>> {
	private final EntityModel<T> model = new SlimeModel<>(0);

	public SlimeOuterLayer(RenderLayerParent<T, SlimeModel<T>> renderLayerParent) {
		super(renderLayerParent);
	}

	public void render(
		PoseStack poseStack, MultiBufferSource multiBufferSource, int i, T livingEntity, float f, float g, float h, float j, float k, float l, float m
	) {
		coloredModelCopyLayerRender(
			this.getParentModel(), this.model, this.getTextureLocation(livingEntity), poseStack, multiBufferSource, i, livingEntity, f, g, j, k, l, m, h
		);
	}
}
