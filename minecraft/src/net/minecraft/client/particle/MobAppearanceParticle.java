package net.minecraft.client.particle;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.ElderGuardian;
import net.minecraft.world.level.Level;

@Environment(EnvType.CLIENT)
public class MobAppearanceParticle extends Particle {
	private LivingEntity displayEntity;

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
	public void tick() {
		super.tick();
		if (this.displayEntity == null) {
			ElderGuardian elderGuardian = EntityType.ELDER_GUARDIAN.create(this.level);
			elderGuardian.setGhost();
			this.displayEntity = elderGuardian;
		}
	}

	@Override
	public void render(VertexConsumer vertexConsumer, Camera camera, float f) {
		if (this.displayEntity != null) {
			EntityRenderDispatcher entityRenderDispatcher = Minecraft.getInstance().getEntityRenderDispatcher();
			float g = 1.0F / ElderGuardian.ELDER_SIZE_SCALE;
			float h = ((float)this.age + f) / (float)this.lifetime;
			RenderSystem.depthMask(true);
			RenderSystem.enableBlend();
			RenderSystem.enableDepthTest();
			RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
			float i = 240.0F;
			RenderSystem.glMultiTexCoord2f(33986, 240.0F, 240.0F);
			RenderSystem.pushMatrix();
			float j = 0.05F + 0.5F * Mth.sin(h * (float) Math.PI);
			RenderSystem.color4f(1.0F, 1.0F, 1.0F, j);
			RenderSystem.translatef(0.0F, 1.8F, 0.0F);
			RenderSystem.rotatef(180.0F - camera.getYRot(), 0.0F, 1.0F, 0.0F);
			RenderSystem.rotatef(60.0F - 150.0F * h - camera.getXRot(), 1.0F, 0.0F, 0.0F);
			RenderSystem.translatef(0.0F, -0.4F, -1.5F);
			RenderSystem.scalef(g, g, g);
			this.displayEntity.yRot = 0.0F;
			this.displayEntity.yHeadRot = 0.0F;
			this.displayEntity.yRotO = 0.0F;
			this.displayEntity.yHeadRotO = 0.0F;
			MultiBufferSource.BufferSource bufferSource = Minecraft.getInstance().renderBuffers().bufferSource();
			entityRenderDispatcher.render(this.displayEntity, 0.0, 0.0, 0.0, 0.0F, f, new PoseStack(), bufferSource, 15728880);
			bufferSource.endBatch();
			RenderSystem.popMatrix();
			RenderSystem.enableDepthTest();
		}
	}

	@Environment(EnvType.CLIENT)
	public static class Provider implements ParticleProvider<SimpleParticleType> {
		public Particle createParticle(SimpleParticleType simpleParticleType, Level level, double d, double e, double f, double g, double h, double i) {
			return new MobAppearanceParticle(level, d, e, f);
		}
	}
}
