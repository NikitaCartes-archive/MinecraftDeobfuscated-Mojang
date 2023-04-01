package net.minecraft.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.layers.BeretLayer;
import net.minecraft.client.renderer.entity.layers.CustomHeadLayer;
import net.minecraft.client.renderer.entity.layers.ElytraLayer;
import net.minecraft.client.renderer.entity.layers.ItemInHandLayer;
import net.minecraft.client.renderer.entity.layers.MustacheLayer;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.phys.Vec3;

@Environment(EnvType.CLIENT)
public abstract class HumanoidMobRenderer<T extends Mob, M extends HumanoidModel<T>> extends MobRenderer<T, M> {
	public HumanoidMobRenderer(EntityRendererProvider.Context context, M humanoidModel, float f) {
		this(context, humanoidModel, f, 1.0F, 1.0F, 1.0F);
	}

	public HumanoidMobRenderer(EntityRendererProvider.Context context, M humanoidModel, float f, float g, float h, float i) {
		super(context, humanoidModel, f);
		this.addLayer(new CustomHeadLayer<>(this, context.getModelSet(), g, h, i, context.getItemInHandRenderer()));
		this.addLayer(new ElytraLayer<>(this, context.getModelSet()));
		this.addLayer(new ItemInHandLayer<>(this, context.getItemInHandRenderer()));
		this.addLayer(new BeretLayer<>(this, context.getModelSet()));
		this.addLayer(new MustacheLayer<>(this, context.getModelSet()));
	}

	@Override
	public void render(T mob, float f, float g, PoseStack poseStack, MultiBufferSource multiBufferSource, int i) {
		this.setModelProperties(mob);
		super.render(mob, f, g, poseStack, multiBufferSource, i);
	}

	private void setModelProperties(T mob) {
		this.model.setModelProperties(mob);
	}

	public Vec3 getRenderOffset(T mob, float f) {
		return mob.isCrouching() ? new Vec3(0.0, -0.125, 0.0) : super.getRenderOffset(mob, f);
	}
}
