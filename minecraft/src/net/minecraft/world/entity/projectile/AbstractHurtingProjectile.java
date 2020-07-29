package net.minecraft.world.entity.projectile;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public abstract class AbstractHurtingProjectile extends Projectile {
	public double xPower;
	public double yPower;
	public double zPower;

	protected AbstractHurtingProjectile(EntityType<? extends AbstractHurtingProjectile> entityType, Level level) {
		super(entityType, level);
	}

	public AbstractHurtingProjectile(
		EntityType<? extends AbstractHurtingProjectile> entityType, double d, double e, double f, double g, double h, double i, Level level
	) {
		this(entityType, level);
		this.moveTo(d, e, f, this.yRot, this.xRot);
		this.reapplyPosition();
		double j = (double)Mth.sqrt(g * g + h * h + i * i);
		if (j != 0.0) {
			this.xPower = g / j * 0.1;
			this.yPower = h / j * 0.1;
			this.zPower = i / j * 0.1;
		}
	}

	public AbstractHurtingProjectile(
		EntityType<? extends AbstractHurtingProjectile> entityType, LivingEntity livingEntity, double d, double e, double f, Level level
	) {
		this(entityType, livingEntity.getX(), livingEntity.getY(), livingEntity.getZ(), d, e, f, level);
		this.setOwner(livingEntity);
		this.setRot(livingEntity.yRot, livingEntity.xRot);
	}

	@Override
	protected void defineSynchedData() {
	}

	@Environment(EnvType.CLIENT)
	@Override
	public boolean shouldRenderAtSqrDistance(double d) {
		double e = this.getBoundingBox().getSize() * 4.0;
		if (Double.isNaN(e)) {
			e = 4.0;
		}

		e *= 64.0;
		return d < e * e;
	}

	@Override
	public void tick() {
		Entity entity = this.getOwner();
		if (this.level.isClientSide || (entity == null || !entity.removed) && this.level.hasChunkAt(this.blockPosition())) {
			super.tick();
			if (this.shouldBurn()) {
				this.setSecondsOnFire(1);
			}

			HitResult hitResult = ProjectileUtil.getHitResult(this, this::canHitEntity);
			if (hitResult.getType() != HitResult.Type.MISS) {
				this.onHit(hitResult);
			}

			this.checkInsideBlocks();
			Vec3 vec3 = this.getDeltaMovement();
			double d = this.getX() + vec3.x;
			double e = this.getY() + vec3.y;
			double f = this.getZ() + vec3.z;
			ProjectileUtil.rotateTowardsMovement(this, 0.2F);
			float g = this.getInertia();
			if (this.isInWater()) {
				for (int i = 0; i < 4; i++) {
					float h = 0.25F;
					this.level.addParticle(ParticleTypes.BUBBLE, d - vec3.x * 0.25, e - vec3.y * 0.25, f - vec3.z * 0.25, vec3.x, vec3.y, vec3.z);
				}

				g = 0.8F;
			}

			this.setDeltaMovement(vec3.add(this.xPower, this.yPower, this.zPower).scale((double)g));
			this.level.addParticle(this.getTrailParticle(), d, e + 0.5, f, 0.0, 0.0, 0.0);
			this.setPos(d, e, f);
		} else {
			this.remove();
		}
	}

	@Override
	protected boolean canHitEntity(Entity entity) {
		return super.canHitEntity(entity) && !entity.noPhysics;
	}

	protected boolean shouldBurn() {
		return true;
	}

	protected ParticleOptions getTrailParticle() {
		return ParticleTypes.SMOKE;
	}

	protected float getInertia() {
		return 0.95F;
	}

	@Override
	public void addAdditionalSaveData(CompoundTag compoundTag) {
		super.addAdditionalSaveData(compoundTag);
		compoundTag.put("power", this.newDoubleList(new double[]{this.xPower, this.yPower, this.zPower}));
	}

	@Override
	public void readAdditionalSaveData(CompoundTag compoundTag) {
		super.readAdditionalSaveData(compoundTag);
		if (compoundTag.contains("power", 9)) {
			ListTag listTag = compoundTag.getList("power", 6);
			if (listTag.size() == 3) {
				this.xPower = listTag.getDouble(0);
				this.yPower = listTag.getDouble(1);
				this.zPower = listTag.getDouble(2);
			}
		}
	}

	@Override
	public boolean isPickable() {
		return true;
	}

	@Override
	public float getPickRadius() {
		return 1.0F;
	}

	@Override
	public boolean hurt(DamageSource damageSource, float f) {
		if (this.isInvulnerableTo(damageSource)) {
			return false;
		} else {
			this.markHurt();
			Entity entity = damageSource.getEntity();
			if (entity != null) {
				Vec3 vec3 = entity.getLookAngle();
				this.setDeltaMovement(vec3);
				this.xPower = vec3.x * 0.1;
				this.yPower = vec3.y * 0.1;
				this.zPower = vec3.z * 0.1;
				this.setOwner(entity);
				return true;
			} else {
				return false;
			}
		}
	}

	@Override
	public float getBrightness() {
		return 1.0F;
	}

	@Override
	public Packet<?> getAddEntityPacket() {
		Entity entity = this.getOwner();
		int i = entity == null ? 0 : entity.getId();
		return new ClientboundAddEntityPacket(
			this.getId(),
			this.getUUID(),
			this.getX(),
			this.getY(),
			this.getZ(),
			this.xRot,
			this.yRot,
			this.getType(),
			i,
			new Vec3(this.xPower, this.yPower, this.zPower)
		);
	}
}
