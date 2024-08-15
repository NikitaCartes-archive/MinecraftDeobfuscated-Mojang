package net.minecraft.client.renderer.entity;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.world.entity.Mob;

@Environment(EnvType.CLIENT)
public abstract class MobRenderer<T extends Mob, S extends LivingEntityRenderState, M extends EntityModel<? super S>> extends LivingEntityRenderer<T, S, M> {
	public MobRenderer(EntityRendererProvider.Context context, M entityModel, float f) {
		super(context, entityModel, f);
	}

	protected boolean shouldShowName(T mob, double d) {
		return super.shouldShowName(mob, d) && (mob.shouldShowName() || mob.hasCustomName() && mob == this.entityRenderDispatcher.crosshairPickEntity);
	}

	@Override
	protected float getShadowRadius(S livingEntityRenderState) {
		return super.getShadowRadius(livingEntityRenderState) * livingEntityRenderState.ageScale;
	}
}
