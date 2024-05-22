package net.minecraft.client.renderer.entity;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.ZombieModel;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.monster.Zombie;

@Environment(EnvType.CLIENT)
public abstract class AbstractZombieRenderer<T extends Zombie, M extends ZombieModel<T>> extends HumanoidMobRenderer<T, M> {
	private static final ResourceLocation ZOMBIE_LOCATION = ResourceLocation.withDefaultNamespace("textures/entity/zombie/zombie.png");

	protected AbstractZombieRenderer(EntityRendererProvider.Context context, M zombieModel, M zombieModel2, M zombieModel3) {
		super(context, zombieModel, 0.5F);
		this.addLayer(new HumanoidArmorLayer<>(this, zombieModel2, zombieModel3, context.getModelManager()));
	}

	public ResourceLocation getTextureLocation(Zombie zombie) {
		return ZOMBIE_LOCATION;
	}

	protected boolean isShaking(T zombie) {
		return super.isShaking(zombie) || zombie.isUnderWaterConverting();
	}
}
