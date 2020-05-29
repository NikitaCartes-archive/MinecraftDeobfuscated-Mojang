package net.minecraft.client.particle;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderBuffers;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.phys.Vec3;

@Environment(EnvType.CLIENT)
public class ItemPickupParticle extends Particle {
	private final RenderBuffers renderBuffers;
	private final Entity itemEntity;
	private final Entity target;
	private int life;
	private final EntityRenderDispatcher entityRenderDispatcher;

	public ItemPickupParticle(EntityRenderDispatcher entityRenderDispatcher, RenderBuffers renderBuffers, ClientLevel clientLevel, Entity entity, Entity entity2) {
		this(entityRenderDispatcher, renderBuffers, clientLevel, entity, entity2, entity.getDeltaMovement());
	}

	private ItemPickupParticle(
		EntityRenderDispatcher entityRenderDispatcher, RenderBuffers renderBuffers, ClientLevel clientLevel, Entity entity, Entity entity2, Vec3 vec3
	) {
		super(clientLevel, entity.getX(), entity.getY(), entity.getZ(), vec3.x, vec3.y, vec3.z);
		this.renderBuffers = renderBuffers;
		this.itemEntity = this.getSafeCopy(entity);
		this.target = entity2;
		this.entityRenderDispatcher = entityRenderDispatcher;
	}

	private Entity getSafeCopy(Entity entity) {
		return (Entity)(!(entity instanceof ItemEntity) ? entity : ((ItemEntity)entity).copy());
	}

	@Override
	public ParticleRenderType getRenderType() {
		return ParticleRenderType.CUSTOM;
	}

	@Override
	public void render(VertexConsumer vertexConsumer, Camera camera, float f) {
		float g = ((float)this.life + f) / 3.0F;
		g *= g;
		double d = Mth.lerp((double)f, this.target.xOld, this.target.getX());
		double e = Mth.lerp((double)f, this.target.yOld, this.target.getY()) + 0.5;
		double h = Mth.lerp((double)f, this.target.zOld, this.target.getZ());
		double i = Mth.lerp((double)g, this.itemEntity.getX(), d);
		double j = Mth.lerp((double)g, this.itemEntity.getY(), e);
		double k = Mth.lerp((double)g, this.itemEntity.getZ(), h);
		MultiBufferSource.BufferSource bufferSource = this.renderBuffers.bufferSource();
		Vec3 vec3 = camera.getPosition();
		this.entityRenderDispatcher
			.render(
				this.itemEntity,
				i - vec3.x(),
				j - vec3.y(),
				k - vec3.z(),
				this.itemEntity.yRot,
				f,
				new PoseStack(),
				bufferSource,
				this.entityRenderDispatcher.getPackedLightCoords(this.itemEntity, f)
			);
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
