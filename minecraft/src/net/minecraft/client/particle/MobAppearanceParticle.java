package net.minecraft.client.particle;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Vector3f;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.GuardianModel;
import net.minecraft.client.model.Model;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.ElderGuardianRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;

@Environment(EnvType.CLIENT)
public class MobAppearanceParticle extends Particle {
	private final Model model = new GuardianModel();
	private final RenderType renderType = RenderType.entityTranslucent(ElderGuardianRenderer.GUARDIAN_ELDER_LOCATION);

	private MobAppearanceParticle(Level level, double d, double e, double f) {
		super(level, d, e, f);
		this.gravity = 0.0F;
		this.lifetime = 30;
	}

	@Override
	public ParticleRenderType getRenderType() {
		return ParticleRenderType.CUSTOM;
	}

	@Override
	public void render(VertexConsumer vertexConsumer, Camera camera, float f) {
		float g = ((float)this.age + f) / (float)this.lifetime;
		float h = 0.05F + 0.5F * Mth.sin(g * (float) Math.PI);
		PoseStack poseStack = new PoseStack();
		poseStack.mulPose(camera.rotation());
		poseStack.mulPose(Vector3f.XP.rotationDegrees(150.0F * g - 60.0F));
		poseStack.scale(-1.0F, -1.0F, 1.0F);
		poseStack.translate(0.0, -1.101F, 1.5);
		MultiBufferSource.BufferSource bufferSource = Minecraft.getInstance().renderBuffers().bufferSource();
		VertexConsumer vertexConsumer2 = bufferSource.getBuffer(this.renderType);
		this.model.renderToBuffer(poseStack, vertexConsumer2, 15728880, OverlayTexture.NO_OVERLAY, 1.0F, 1.0F, 1.0F, h);
		bufferSource.endBatch();
	}

	@Environment(EnvType.CLIENT)
	public static class Provider implements ParticleProvider<SimpleParticleType> {
		public Particle createParticle(SimpleParticleType simpleParticleType, Level level, double d, double e, double f, double g, double h, double i) {
			return new MobAppearanceParticle(level, d, e, f);
		}
	}
}
