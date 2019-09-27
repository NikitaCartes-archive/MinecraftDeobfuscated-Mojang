package net.minecraft.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.ZombieModel;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.monster.Zombie;

@Environment(EnvType.CLIENT)
public abstract class AbstractZombieRenderer<T extends Zombie, M extends ZombieModel<T>> extends HumanoidMobRenderer<T, M> {
	private static final ResourceLocation ZOMBIE_LOCATION = new ResourceLocation("textures/entity/zombie/zombie.png");

	protected AbstractZombieRenderer(EntityRenderDispatcher entityRenderDispatcher, M zombieModel, M zombieModel2, M zombieModel3) {
		super(entityRenderDispatcher, zombieModel, 0.5F);
		this.addLayer(new HumanoidArmorLayer<>(this, zombieModel2, zombieModel3));
	}

	public ResourceLocation getTextureLocation(Zombie zombie) {
		return ZOMBIE_LOCATION;
	}

	protected void setupRotations(T zombie, PoseStack poseStack, float f, float g, float h) {
		if (zombie.isUnderWaterConverting()) {
			g += (float)(Math.cos((double)zombie.tickCount * 3.25) * Math.PI * 0.25);
		}

		super.setupRotations(zombie, poseStack, f, g, h);
	}
}
