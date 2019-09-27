package net.minecraft.client.particle;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Camera;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderBuffers;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

@Environment(EnvType.CLIENT)
public class ItemPickupParticle extends Particle {
	private final RenderBuffers renderBuffers;
	private final Entity itemEntity;
	private final Entity target;
	private int life;
	private final EntityRenderDispatcher entityRenderDispatcher;

	public ItemPickupParticle(EntityRenderDispatcher entityRenderDispatcher, RenderBuffers renderBuffers, Level level, Entity entity, Entity entity2) {
		this(entityRenderDispatcher, renderBuffers, level, entity, entity2, entity.getDeltaMovement());
	}

	private ItemPickupParticle(EntityRenderDispatcher entityRenderDispatcher, RenderBuffers renderBuffers, Level level, Entity entity, Entity entity2, Vec3 vec3) {
		super(level, entity.x, entity.y, entity.z, vec3.x, vec3.y, vec3.z);
		this.renderBuffers = renderBuffers;
		this.itemEntity = entity;
		this.target = entity2;
		this.entityRenderDispatcher = entityRenderDispatcher;
	}

	@Override
	public ParticleRenderType getRenderType() {
		return ParticleRenderType.CUSTOM;
	}

	@Override
	public void render(VertexConsumer vertexConsumer, Camera camera, float f, float g, float h, float i, float j, float k) {
		float l = ((float)this.life + f) / 3.0F;
		l *= l;
		double d = Mth.lerp((double)f, this.target.xOld, this.target.x);
		double e = Mth.lerp((double)f, this.target.yOld, this.target.y) + 0.5;
		double m = Mth.lerp((double)f, this.target.zOld, this.target.z);
		double n = Mth.lerp((double)l, this.itemEntity.x, d);
		double o = Mth.lerp((double)l, this.itemEntity.y, e);
		double p = Mth.lerp((double)l, this.itemEntity.z, m);
		MultiBufferSource.BufferSource bufferSource = this.renderBuffers.bufferSource();
		this.entityRenderDispatcher.render(this.itemEntity, n - xOff, o - yOff, p - zOff, this.itemEntity.yRot, f, new PoseStack(), bufferSource);
		bufferSource.endBatch();
	}

	@Override
	public void tick() {
		this.life++;
		if (this.life == 3) {
			this.remove();
		}
	}
}
