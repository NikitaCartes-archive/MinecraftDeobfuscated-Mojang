package net.minecraft.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.world.entity.Mob;

@Deprecated
@Environment(EnvType.CLIENT)
public abstract class AgeableMobRenderer<T extends Mob, S extends LivingEntityRenderState, M extends EntityModel<? super S>> extends MobRenderer<T, S, M> {
	private final M adultModel;
	private final M babyModel;

	public AgeableMobRenderer(EntityRendererProvider.Context context, M entityModel, M entityModel2, float f) {
		super(context, entityModel, f);
		this.adultModel = entityModel;
		this.babyModel = entityModel2;
	}

	@Override
	public void render(S livingEntityRenderState, PoseStack poseStack, MultiBufferSource multiBufferSource, int i) {
		this.model = livingEntityRenderState.isBaby ? this.babyModel : this.adultModel;
		super.render(livingEntityRenderState, poseStack, multiBufferSource, i);
	}
}
