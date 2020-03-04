package net.minecraft.world.entity.projectile;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.horse.Llama;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public class LlamaSpit extends Projectile {
	public LlamaSpit(EntityType<? extends LlamaSpit> entityType, Level level) {
		super(entityType, level);
	}

	public LlamaSpit(Level level, Llama llama) {
		this(EntityType.LLAMA_SPIT, level);
		super.setOwner(llama);
		this.setPos(
			llama.getX() - (double)(llama.getBbWidth() + 1.0F) * 0.5 * (double)Mth.sin(llama.yBodyRot * (float) (Math.PI / 180.0)),
			llama.getEyeY() - 0.1F,
			llama.getZ() + (double)(llama.getBbWidth() + 1.0F) * 0.5 * (double)Mth.cos(llama.yBodyRot * (float) (Math.PI / 180.0))
		);
	}

	@Environment(EnvType.CLIENT)
	public LlamaSpit(Level level, double d, double e, double f, double g, double h, double i) {
		this(EntityType.LLAMA_SPIT, level);
		this.setPos(d, e, f);

		for (int j = 0; j < 7; j++) {
			double k = 0.4 + 0.1 * (double)j;
			level.addParticle(ParticleTypes.SPIT, d, e, f, g * k, h, i * k);
		}

		this.setDeltaMovement(g, h, i);
	}

	@Override
	public void tick() {
		super.tick();
		Vec3 vec3 = this.getDeltaMovement();
		HitResult hitResult = ProjectileUtil.getHitResult(
			this, this.getBoundingBox().expandTowards(vec3).inflate(1.0), entity -> !entity.isSpectator() && entity != this.getOwner(), ClipContext.Block.OUTLINE, true
		);
		if (hitResult != null) {
			this.onHit(hitResult);
		}

		double d = this.getX() + vec3.x;
		double e = this.getY() + vec3.y;
		double f = this.getZ() + vec3.z;
		float g = Mth.sqrt(getHorizontalDistanceSqr(vec3));
		this.yRot = (float)(Mth.atan2(vec3.x, vec3.z) * 180.0F / (float)Math.PI);
		this.xRot = (float)(Mth.atan2(vec3.y, (double)g) * 180.0F / (float)Math.PI);

		while (this.xRot - this.xRotO < -180.0F) {
			this.xRotO -= 360.0F;
		}

		while (this.xRot - this.xRotO >= 180.0F) {
			this.xRotO += 360.0F;
		}

		while (this.yRot - this.yRotO < -180.0F) {
			this.yRotO -= 360.0F;
		}

		while (this.yRot - this.yRotO >= 180.0F) {
			this.yRotO += 360.0F;
		}

		this.xRot = Mth.lerp(0.2F, this.xRotO, this.xRot);
		this.yRot = Mth.lerp(0.2F, this.yRotO, this.yRot);
		float h = 0.99F;
		float i = 0.06F;
		if (!this.level.containsMaterial(this.getBoundingBox(), Material.AIR)) {
			this.remove();
		} else if (this.isInWaterOrBubble()) {
			this.remove();
		} else {
			this.setDeltaMovement(vec3.scale(0.99F));
			if (!this.isNoGravity()) {
				this.setDeltaMovement(this.getDeltaMovement().add(0.0, -0.06F, 0.0));
			}

			this.setPos(d, e, f);
		}
	}

	@Override
	protected void onHitEntity(EntityHitResult entityHitResult) {
		super.onHitEntity(entityHitResult);
		Entity entity = this.getOwner();
		if (entity instanceof LivingEntity) {
			entityHitResult.getEntity().hurt(DamageSource.indirectMobAttack(this, (LivingEntity)entity).setProjectile(), 1.0F);
		}
	}

	@Override
	protected void onHitBlock(BlockHitResult blockHitResult) {
		super.onHitBlock(blockHitResult);
		if (!this.level.isClientSide) {
			this.remove();
		}
	}

	@Override
	protected void defineSynchedData() {
	}

	@Override
	public Packet<?> getAddEntityPacket() {
		return new ClientboundAddEntityPacket(this);
	}
}
