package net.minecraft.client.renderer.entity;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.ZombieModel;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.monster.PigZombie;

@Environment(EnvType.CLIENT)
public class PigZombieRenderer extends HumanoidMobRenderer<PigZombie, ZombieModel<PigZombie>> {
	private static final ResourceLocation ZOMBIE_PIGMAN_LOCATION = new ResourceLocation("textures/entity/zombie_pigman.png");

	public PigZombieRenderer(EntityRenderDispatcher entityRenderDispatcher) {
		super(entityRenderDispatcher, new ZombieModel<>(RenderType::entityCutoutNoCull, 0.0F, false), 0.5F);
		this.addLayer(
			new HumanoidArmorLayer<>(this, new ZombieModel(RenderType::entityCutoutNoCull, 0.5F, true), new ZombieModel(RenderType::entityCutoutNoCull, 1.0F, true))
		);
	}

	public ResourceLocation getTextureLocation(PigZombie pigZombie) {
		return ZOMBIE_PIGMAN_LOCATION;
	}
}
