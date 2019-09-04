package net.minecraft.client.renderer.entity.layers;

import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.SkeletonModel;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.monster.RangedAttackMob;

@Environment(EnvType.CLIENT)
public class StrayClothingLayer<T extends Mob & RangedAttackMob, M extends EntityModel<T>> extends RenderLayer<T, M> {
	private static final ResourceLocation STRAY_CLOTHES_LOCATION = new ResourceLocation("textures/entity/skeleton/stray_overlay.png");
	private final SkeletonModel<T> layerModel = new SkeletonModel<>(0.25F, true);

	public StrayClothingLayer(RenderLayerParent<T, M> renderLayerParent) {
		super(renderLayerParent);
	}

	public void render(T mob, float f, float g, float h, float i, float j, float k, float l) {
		this.getParentModel().copyPropertiesTo(this.layerModel);
		this.layerModel.prepareMobModel(mob, f, g, h);
		RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
		this.bindTexture(STRAY_CLOTHES_LOCATION);
		this.layerModel.render(mob, f, g, i, j, k, l);
	}

	@Override
	public boolean colorsOnDamage() {
		return true;
	}
}
