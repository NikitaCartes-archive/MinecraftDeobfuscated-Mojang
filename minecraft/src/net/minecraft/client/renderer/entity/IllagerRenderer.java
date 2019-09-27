package net.minecraft.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.IllagerModel;
import net.minecraft.client.renderer.entity.layers.CustomHeadLayer;
import net.minecraft.world.entity.monster.AbstractIllager;

@Environment(EnvType.CLIENT)
public abstract class IllagerRenderer<T extends AbstractIllager> extends MobRenderer<T, IllagerModel<T>> {
	protected IllagerRenderer(EntityRenderDispatcher entityRenderDispatcher, IllagerModel<T> illagerModel, float f) {
		super(entityRenderDispatcher, illagerModel, f);
		this.addLayer(new CustomHeadLayer<>(this));
	}

	protected void scale(T abstractIllager, PoseStack poseStack, float f) {
		float g = 0.9375F;
		poseStack.scale(0.9375F, 0.9375F, 0.9375F);
	}
}
