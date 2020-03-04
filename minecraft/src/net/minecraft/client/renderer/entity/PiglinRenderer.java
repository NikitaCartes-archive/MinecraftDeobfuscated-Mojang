package net.minecraft.client.renderer.entity;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.PiglinModel;
import net.minecraft.client.renderer.entity.layers.PiglinArmorLayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.monster.piglin.Piglin;

@Environment(EnvType.CLIENT)
public class PiglinRenderer extends HumanoidMobRenderer<Mob, PiglinModel<Mob>> {
	private static final ResourceLocation PIGLIN_LOCATION = new ResourceLocation("textures/entity/piglin/piglin.png");
	private static final ResourceLocation ZOMBIFIED_PIGLIN_LOCATION = new ResourceLocation("textures/entity/piglin/zombified_piglin.png");

	public PiglinRenderer(EntityRenderDispatcher entityRenderDispatcher, boolean bl) {
		super(entityRenderDispatcher, createModel(bl), 0.5F);
		this.addLayer(new PiglinArmorLayer<>(this, new HumanoidModel(0.5F), new HumanoidModel(1.0F), makeHelmetHeadModel()));
	}

	private static PiglinModel<Mob> createModel(boolean bl) {
		PiglinModel<Mob> piglinModel = new PiglinModel<>(0.0F, 128, 64);
		if (bl) {
			piglinModel.earLeft.visible = false;
		}

		return piglinModel;
	}

	private static <T extends Piglin> PiglinModel<T> makeHelmetHeadModel() {
		PiglinModel<T> piglinModel = new PiglinModel<>(1.0F, 64, 16);
		piglinModel.earLeft.visible = false;
		piglinModel.earRight.visible = false;
		return piglinModel;
	}

	@Override
	public ResourceLocation getTextureLocation(Mob mob) {
		return mob instanceof Piglin ? PIGLIN_LOCATION : ZOMBIFIED_PIGLIN_LOCATION;
	}

	protected boolean isShaking(Mob mob) {
		return mob instanceof Piglin && ((Piglin)mob).isConverting();
	}
}
