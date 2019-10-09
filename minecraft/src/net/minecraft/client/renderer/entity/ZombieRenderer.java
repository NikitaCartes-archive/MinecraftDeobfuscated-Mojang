package net.minecraft.client.renderer.entity;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.ZombieModel;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.entity.monster.Zombie;

@Environment(EnvType.CLIENT)
public class ZombieRenderer extends AbstractZombieRenderer<Zombie, ZombieModel<Zombie>> {
	public ZombieRenderer(EntityRenderDispatcher entityRenderDispatcher) {
		super(
			entityRenderDispatcher,
			new ZombieModel<>(RenderType::entitySolid, 0.0F, false),
			new ZombieModel<>(RenderType::entitySolid, 0.5F, true),
			new ZombieModel<>(RenderType::entitySolid, 1.0F, true)
		);
	}
}
