package net.minecraft.client.renderer.entity;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.EntityModel;
import net.minecraft.world.entity.Mob;

@Environment(EnvType.CLIENT)
public abstract class MobRenderer<T extends Mob, M extends EntityModel<T>> extends LivingEntityRenderer<T, M> {
	public MobRenderer(EntityRendererProvider.Context context, M entityModel, float f) {
		super(context, entityModel, f);
	}

	protected boolean shouldShowName(T mob) {
		return super.shouldShowName(mob) && (mob.shouldShowName() || mob.hasCustomName() && mob == this.entityRenderDispatcher.crosshairPickEntity);
	}

	protected float getShadowRadius(T mob) {
		return super.getShadowRadius(mob) * mob.getAgeScale();
	}
}
