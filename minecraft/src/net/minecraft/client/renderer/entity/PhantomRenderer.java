package net.minecraft.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Vector3f;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.PhantomModel;
import net.minecraft.client.renderer.entity.layers.PhantomEyesLayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.monster.Phantom;

@Environment(EnvType.CLIENT)
public class PhantomRenderer extends MobRenderer<Phantom, PhantomModel<Phantom>> {
	private static final ResourceLocation PHANTOM_LOCATION = new ResourceLocation("textures/entity/phantom.png");

	public PhantomRenderer(EntityRenderDispatcher entityRenderDispatcher) {
		super(entityRenderDispatcher, new PhantomModel<>(), 0.75F);
		this.addLayer(new PhantomEyesLayer<>(this));
	}

	public ResourceLocation getTextureLocation(Phantom phantom) {
		return PHANTOM_LOCATION;
	}

	protected void scale(Phantom phantom, PoseStack poseStack, float f) {
		int i = phantom.getPhantomSize();
		float g = 1.0F + 0.15F * (float)i;
		poseStack.scale(g, g, g);
		poseStack.translate(0.0, 1.3125, 0.1875);
	}

	protected void setupRotations(Phantom phantom, PoseStack poseStack, float f, float g, float h) {
		super.setupRotations(phantom, poseStack, f, g, h);
		poseStack.mulPose(Vector3f.XP.rotationDegrees(phantom.xRot));
	}
}
