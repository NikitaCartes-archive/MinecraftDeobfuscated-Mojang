package net.minecraft.client.renderer.entity;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.entity.layers.CustomHeadLayer;
import net.minecraft.client.renderer.entity.layers.ElytraLayer;
import net.minecraft.client.renderer.entity.layers.ItemInHandLayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Mob;

@Environment(EnvType.CLIENT)
public class HumanoidMobRenderer<T extends Mob, M extends HumanoidModel<T>> extends MobRenderer<T, M> {
	private static final ResourceLocation DEFAULT_LOCATION = new ResourceLocation("textures/entity/steve.png");

	public HumanoidMobRenderer(EntityRenderDispatcher entityRenderDispatcher, M humanoidModel, float f) {
		super(entityRenderDispatcher, humanoidModel, f);
		this.addLayer(new CustomHeadLayer<>(this));
		this.addLayer(new ElytraLayer<>(this));
		this.addLayer(new ItemInHandLayer<>(this));
	}

	public ResourceLocation getTextureLocation(T mob) {
		return DEFAULT_LOCATION;
	}
}
