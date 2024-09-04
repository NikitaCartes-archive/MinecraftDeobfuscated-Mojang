package net.minecraft.client.renderer.entity;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.ZombieModel;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.client.renderer.entity.state.ZombieRenderState;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.monster.Zombie;

@Environment(EnvType.CLIENT)
public abstract class AbstractZombieRenderer<T extends Zombie, S extends ZombieRenderState, M extends ZombieModel<S>> extends HumanoidMobRenderer<T, S, M> {
	private static final ResourceLocation ZOMBIE_LOCATION = ResourceLocation.withDefaultNamespace("textures/entity/zombie/zombie.png");

	protected AbstractZombieRenderer(
		EntityRendererProvider.Context context, M zombieModel, M zombieModel2, M zombieModel3, M zombieModel4, M zombieModel5, M zombieModel6
	) {
		super(context, zombieModel, zombieModel2, 0.5F);
		this.addLayer(new HumanoidArmorLayer<>(this, zombieModel3, zombieModel4, zombieModel5, zombieModel6, context.getEquipmentRenderer()));
	}

	public ResourceLocation getTextureLocation(S zombieRenderState) {
		return ZOMBIE_LOCATION;
	}

	public void extractRenderState(T zombie, S zombieRenderState, float f) {
		super.extractRenderState(zombie, zombieRenderState, f);
		zombieRenderState.isAggressive = zombie.isAggressive();
		zombieRenderState.isConverting = zombie.isUnderWaterConverting();
	}

	protected boolean isShaking(S zombieRenderState) {
		return super.isShaking(zombieRenderState) || zombieRenderState.isConverting;
	}
}
