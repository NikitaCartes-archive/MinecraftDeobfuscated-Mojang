package net.minecraft.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.PhantomModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.layers.PhantomEyesLayer;
import net.minecraft.client.renderer.entity.state.PhantomRenderState;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.monster.Phantom;

@Environment(EnvType.CLIENT)
public class PhantomRenderer extends MobRenderer<Phantom, PhantomRenderState, PhantomModel> {
	private static final ResourceLocation PHANTOM_LOCATION = ResourceLocation.withDefaultNamespace("textures/entity/phantom.png");

	public PhantomRenderer(EntityRendererProvider.Context context) {
		super(context, new PhantomModel(context.bakeLayer(ModelLayers.PHANTOM)), 0.75F);
		this.addLayer(new PhantomEyesLayer(this));
	}

	public ResourceLocation getTextureLocation(PhantomRenderState phantomRenderState) {
		return PHANTOM_LOCATION;
	}

	public PhantomRenderState createRenderState() {
		return new PhantomRenderState();
	}

	public void extractRenderState(Phantom phantom, PhantomRenderState phantomRenderState, float f) {
		super.extractRenderState(phantom, phantomRenderState, f);
		phantomRenderState.flapTime = (float)phantom.getUniqueFlapTickOffset() + phantomRenderState.ageInTicks;
		phantomRenderState.size = phantom.getPhantomSize();
	}

	protected void scale(PhantomRenderState phantomRenderState, PoseStack poseStack) {
		float f = 1.0F + 0.15F * (float)phantomRenderState.size;
		poseStack.scale(f, f, f);
		poseStack.translate(0.0F, 1.3125F, 0.1875F);
	}

	protected void setupRotations(PhantomRenderState phantomRenderState, PoseStack poseStack, float f, float g) {
		super.setupRotations(phantomRenderState, poseStack, f, g);
		poseStack.mulPose(Axis.XP.rotationDegrees(phantomRenderState.xRot));
	}
}
