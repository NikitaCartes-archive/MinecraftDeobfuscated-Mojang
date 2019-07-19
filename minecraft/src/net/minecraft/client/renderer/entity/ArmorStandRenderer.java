package net.minecraft.client.renderer.entity;

import com.mojang.blaze3d.platform.GlStateManager;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.ArmorStandArmorModel;
import net.minecraft.client.model.ArmorStandModel;
import net.minecraft.client.renderer.entity.layers.CustomHeadLayer;
import net.minecraft.client.renderer.entity.layers.ElytraLayer;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.client.renderer.entity.layers.ItemInHandLayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.decoration.ArmorStand;

@Environment(EnvType.CLIENT)
public class ArmorStandRenderer extends LivingEntityRenderer<ArmorStand, ArmorStandArmorModel> {
	public static final ResourceLocation DEFAULT_SKIN_LOCATION = new ResourceLocation("textures/entity/armorstand/wood.png");

	public ArmorStandRenderer(EntityRenderDispatcher entityRenderDispatcher) {
		super(entityRenderDispatcher, new ArmorStandModel(), 0.0F);
		this.addLayer(new HumanoidArmorLayer<>(this, new ArmorStandArmorModel(0.5F), new ArmorStandArmorModel(1.0F)));
		this.addLayer(new ItemInHandLayer<>(this));
		this.addLayer(new ElytraLayer<>(this));
		this.addLayer(new CustomHeadLayer<>(this));
	}

	protected ResourceLocation getTextureLocation(ArmorStand armorStand) {
		return DEFAULT_SKIN_LOCATION;
	}

	protected void setupRotations(ArmorStand armorStand, float f, float g, float h) {
		GlStateManager.rotatef(180.0F - g, 0.0F, 1.0F, 0.0F);
		float i = (float)(armorStand.level.getGameTime() - armorStand.lastHit) + h;
		if (i < 5.0F) {
			GlStateManager.rotatef(Mth.sin(i / 1.5F * (float) Math.PI) * 3.0F, 0.0F, 1.0F, 0.0F);
		}
	}

	protected boolean shouldShowName(ArmorStand armorStand) {
		return armorStand.isCustomNameVisible();
	}

	public void render(ArmorStand armorStand, double d, double e, double f, float g, float h) {
		if (armorStand.isMarker()) {
			this.onlySolidLayers = true;
		}

		super.render(armorStand, d, e, f, g, h);
		if (armorStand.isMarker()) {
			this.onlySolidLayers = false;
		}
	}
}
