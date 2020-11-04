package net.minecraft.client.renderer.entity;

import com.google.common.collect.ImmutableMap;
import java.util.Map;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.PiglinModel;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.monster.piglin.AbstractPiglin;

@Environment(EnvType.CLIENT)
public class PiglinRenderer extends HumanoidMobRenderer<Mob, PiglinModel<Mob>> {
	private static final Map<EntityType<?>, ResourceLocation> TEXTURES = ImmutableMap.of(
		EntityType.PIGLIN,
		new ResourceLocation("textures/entity/piglin/piglin.png"),
		EntityType.ZOMBIFIED_PIGLIN,
		new ResourceLocation("textures/entity/piglin/zombified_piglin.png"),
		EntityType.PIGLIN_BRUTE,
		new ResourceLocation("textures/entity/piglin/piglin_brute.png")
	);

	public PiglinRenderer(
		EntityRendererProvider.Context context,
		ModelLayerLocation modelLayerLocation,
		ModelLayerLocation modelLayerLocation2,
		ModelLayerLocation modelLayerLocation3,
		boolean bl
	) {
		super(context, createModel(context.getModelSet(), modelLayerLocation, bl), 0.5F, 1.0019531F, 1.0F, 1.0019531F);
		this.addLayer(
			new HumanoidArmorLayer<>(this, new HumanoidModel(context.getLayer(modelLayerLocation2)), new HumanoidModel(context.getLayer(modelLayerLocation3)))
		);
	}

	private static PiglinModel<Mob> createModel(EntityModelSet entityModelSet, ModelLayerLocation modelLayerLocation, boolean bl) {
		PiglinModel<Mob> piglinModel = new PiglinModel<>(entityModelSet.getLayer(modelLayerLocation));
		if (bl) {
			piglinModel.rightEar.visible = false;
		}

		return piglinModel;
	}

	@Override
	public ResourceLocation getTextureLocation(Mob mob) {
		ResourceLocation resourceLocation = (ResourceLocation)TEXTURES.get(mob.getType());
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
