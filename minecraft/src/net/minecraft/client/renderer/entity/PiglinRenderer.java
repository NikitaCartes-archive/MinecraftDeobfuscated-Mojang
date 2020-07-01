package net.minecraft.client.renderer.entity;

import com.google.common.collect.ImmutableMap;
import java.util.Map;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.PiglinModel;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.monster.piglin.AbstractPiglin;

@Environment(EnvType.CLIENT)
public class PiglinRenderer extends HumanoidMobRenderer<Mob, PiglinModel<Mob>> {
	private static final Map<EntityType<?>, ResourceLocation> resourceLocations = ImmutableMap.of(
		EntityType.PIGLIN,
		new ResourceLocation("textures/entity/piglin/piglin.png"),
		EntityType.ZOMBIFIED_PIGLIN,
		new ResourceLocation("textures/entity/piglin/zombified_piglin.png"),
		EntityType.PIGLIN_BRUTE,
		new ResourceLocation("textures/entity/piglin/piglin_brute.png")
	);

	public PiglinRenderer(EntityRenderDispatcher entityRenderDispatcher, boolean bl) {
		super(entityRenderDispatcher, createModel(bl), 0.5F, 1.0019531F, 1.0F, 1.0019531F);
		this.addLayer(new HumanoidArmorLayer<>(this, new HumanoidModel(0.5F), new HumanoidModel(1.02F)));
	}

	private static PiglinModel<Mob> createModel(boolean bl) {
		PiglinModel<Mob> piglinModel = new PiglinModel<>(0.0F, 64, 64);
		if (bl) {
			piglinModel.earLeft.visible = false;
		}

		return piglinModel;
	}

	@Override
	public ResourceLocation getTextureLocation(Mob mob) {
		ResourceLocation resourceLocation = (ResourceLocation)resourceLocations.get(mob.getType());
		if (resourceLocation == null) {
			throw new IllegalArgumentException("I don't know what texture to use for " + mob.getType());
		} else {
			return resourceLocation;
		}
	}

	protected boolean isShaking(Mob mob) {
		return mob instanceof AbstractPiglin && ((AbstractPiglin)mob).isConverting();
	}
}
