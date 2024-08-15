package net.minecraft.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.entity.state.EquineRenderState;
import net.minecraft.world.entity.animal.horse.AbstractHorse;

@Environment(EnvType.CLIENT)
public abstract class AbstractHorseRenderer<T extends AbstractHorse, S extends EquineRenderState, M extends EntityModel<? super S>>
	extends AgeableMobRenderer<T, S, M> {
	private final float scale;

	public AbstractHorseRenderer(EntityRendererProvider.Context context, M entityModel, M entityModel2, float f) {
		super(context, entityModel, entityModel2, 0.75F);
		this.scale = f;
	}

	protected void scale(S equineRenderState, PoseStack poseStack) {
		poseStack.scale(this.scale, this.scale, this.scale);
		super.scale(equineRenderState, poseStack);
	}

	public void extractRenderState(T abstractHorse, S equineRenderState, float f) {
		super.extractRenderState(abstractHorse, equineRenderState, f);
		equineRenderState.isSaddled = abstractHorse.isSaddled();
		equineRenderState.isRidden = abstractHorse.isVehicle();
		equineRenderState.eatAnimation = abstractHorse.getEatAnim(f);
		equineRenderState.standAnimation = abstractHorse.getStandAnim(f);
		equineRenderState.feedingAnimation = abstractHorse.getMouthAnim(f);
		equineRenderState.animateTail = abstractHorse.tailCounter > 0;
	}
}
