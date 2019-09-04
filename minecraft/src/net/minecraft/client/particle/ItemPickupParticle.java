package net.minecraft.client.particle;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

@Environment(EnvType.CLIENT)
public class ItemPickupParticle extends Particle {
	private final Entity itemEntity;
	private final Entity target;
	private int life;
	private final int lifeTime;
	private final float yOffs;
	private final EntityRenderDispatcher entityRenderDispatcher = Minecraft.getInstance().getEntityRenderDispatcher();

	public ItemPickupParticle(Level level, Entity entity, Entity entity2, float f) {
		this(level, entity, entity2, f, entity.getDeltaMovement());
	}

	private ItemPickupParticle(Level level, Entity entity, Entity entity2, float f, Vec3 vec3) {
		super(level, entity.x, entity.y, entity.z, vec3.x, vec3.y, vec3.z);
		this.itemEntity = entity;
		this.target = entity2;
		this.lifeTime = 3;
		this.yOffs = f;
	}

	@Override
	public ParticleRenderType getRenderType() {
		return ParticleRenderType.CUSTOM;
	}

	@Override
	public void render(BufferBuilder bufferBuilder, Camera camera, float f, float g, float h, float i, float j, float k) {
		float l = ((float)this.life + f) / (float)this.lifeTime;
		l *= l;
		double d = this.itemEntity.x;
		double e = this.itemEntity.y;
		double m = this.itemEntity.z;
		double n = Mth.lerp((double)f, this.target.xOld, this.target.x);
		double o = Mth.lerp((double)f, this.target.yOld, this.target.y) + (double)this.yOffs;
		double p = Mth.lerp((double)f, this.target.zOld, this.target.z);
		double q = Mth.lerp((double)l, d, n);
		double r = Mth.lerp((double)l, e, o);
		double s = Mth.lerp((double)l, m, p);
		int t = this.getLightColor(f);
		int u = t % 65536;
		int v = t / 65536;
		RenderSystem.glMultiTexCoord2f(33985, (float)u, (float)v);
		RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
		q -= xOff;
		r -= yOff;
		s -= zOff;
		RenderSystem.enableLighting();
		this.entityRenderDispatcher.render(this.itemEntity, q, r, s, this.itemEntity.yRot, f, false);
	}

	@Override
	public void tick() {
		this.life++;
		if (this.life == this.lifeTime) {
			this.remove();
		}
	}
}
