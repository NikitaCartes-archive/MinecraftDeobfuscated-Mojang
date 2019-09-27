package net.minecraft.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Vector3f;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.FoxModel;
import net.minecraft.client.renderer.entity.layers.FoxHeldItemLayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.animal.Fox;

@Environment(EnvType.CLIENT)
public class FoxRenderer extends MobRenderer<Fox, FoxModel<Fox>> {
	private static final ResourceLocation RED_FOX_TEXTURE = new ResourceLocation("textures/entity/fox/fox.png");
	private static final ResourceLocation RED_FOX_SLEEP_TEXTURE = new ResourceLocation("textures/entity/fox/fox_sleep.png");
	private static final ResourceLocation SNOW_FOX_TEXTURE = new ResourceLocation("textures/entity/fox/snow_fox.png");
	private static final ResourceLocation SNOW_FOX_SLEEP_TEXTURE = new ResourceLocation("textures/entity/fox/snow_fox_sleep.png");

	public FoxRenderer(EntityRenderDispatcher entityRenderDispatcher) {
		super(entityRenderDispatcher, new FoxModel<>(), 0.4F);
		this.addLayer(new FoxHeldItemLayer(this));
	}

	protected void setupRotations(Fox fox, PoseStack poseStack, float f, float g, float h) {
		super.setupRotations(fox, poseStack, f, g, h);
		if (fox.isPouncing() || fox.isFaceplanted()) {
			poseStack.mulPose(Vector3f.XP.rotation(-Mth.lerp(h, fox.xRotO, fox.xRot), true));
		}
	}

	public ResourceLocation getTextureLocation(Fox fox) {
		if (fox.getFoxType() == Fox.Type.RED) {
			return fox.isSleeping() ? RED_FOX_SLEEP_TEXTURE : RED_FOX_TEXTURE;
		} else {
			return fox.isSleeping() ? SNOW_FOX_SLEEP_TEXTURE : SNOW_FOX_TEXTURE;
		}
	}
}
