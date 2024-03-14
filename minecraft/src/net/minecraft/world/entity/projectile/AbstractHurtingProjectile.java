package net.minecraft.world.entity.projectile;

import javax.annotation.Nullable;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.ClipContext;
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

	protected AbstractHurtingProjectile(EntityType<? extends AbstractHurtingProjectile> entityType, double d, double e, double f, Level level) {
		this(entityType, level);
		this.setPos(d, e, f);
	}

	public AbstractHurtingProjectile(
		EntityType<? extends AbstractHurtingProjectile> entityType, double d, double e, double f, double g, double h, double i, Level level
	) {
		this(entityType, level);
		this.moveTo(d, e, f, this.getYRot(), this.getXRot());
		this.reapplyPosition();
		this.assignPower(g, h, i);
	}

	public AbstractHurtingProjectile(
		EntityType<? extends AbstractHurtingProjectile> entityType, LivingEntity livingEntity, double d, double e, double f, Level level
	) {
		this(entityType, livingEntity.getX(), livingEntity.getY(), livingEntity.getZ(), d, e, f, level);
		this.setOwner(livingEntity);
		this.setRot(livingEntity.getYRot(), livingEntity.getXRot());
	}

	@Override
	protected void defineSynchedData(SynchedEntityData.Builder builder) {
	}

	@Override
	public boolean shouldRenderAtSqrDistance(double d) {
		double e = this.getBoundingBox().getSize() * 4.0;
		if (Double.isNaN(e)) {
			e = 4.0;
		}

		e *= 64.0;
		return d < e * e;
	}

	protected ClipContext.Block getClipType() {
		return ClipContext.Block.COLLIDER;
	}

	@Override
	public void tick() {
		Entity entity = this.getOwner();
		if (this.level().isClientSide || (entity == null || !entity.isRemoved()) && this.level().hasChunkAt(this.blockPosition())) {
			super.tick();
			if (this.shouldBurn()) {
				this.igniteForSeconds(1);
			}

			HitResult hitResult = ProjectileUtil.getHitResultOnMoveVector(this, this::canHitEntity, this.getClipType());
			if (hitResult.getType() != HitResult.Type.MISS) {
				this.onHit(hitResult);
			}

			this.checkInsideBlocks();
			Vec3 vec3 = this.getDeltaMovement();
			double d = this.getX() + vec3.x;
			double e = this.getY() + vec3.y;
			double f = this.getZ() + vec3.z;
			ProjectileUtil.rotateTowardsMovement(this, 0.2F);
			float h;
			if (this.isInWater()) {
				for (int i = 0; i < 4; i++) {
					float g = 0.25F;
					this.level().addParticle(ParticleTypes.BUBBLE, d - vec3.x * 0.25, e - vec3.y * 0.25, f - vec3.z * 0.25, vec3.x, vec3.y, vec3.z);
				}

				h = this.getLiquidInertia();
			} else {
				h = this.getInertia();
			}

			this.setDeltaMovement(vec3.add(this.xPower, this.yPower, this.zPower).scale((double)h));
			ParticleOptions particleOptions = this.getTrailParticle();
			if (particleOptions != null) {
				this.level().addParticle(particleOptions, d, e + 0.5, f, 0.0, 0.0, 0.0);
			}

			this.setPos(d, e, f);
		} else {
			this.discard();
		}
	}

	@Override
	public void lerpMotion(double d, double e, double f) {
		super.lerpMotion(d, e, f);
		this.assignPower(d, e, f);
	}

	@Override
	protected boolean canHitEntity(Entity entity) {
		return super.canHitEntity(entity) && !entity.noPhysics;
	}

	protected boolean shouldBurn() {
		return true;
	}

	@Nullable
	protected ParticleOptions getTrailParticle() {
		return ParticleTypes.SMOKE;
	}

	protected float getInertia() {
		return 0.95F;
	}

	protected float getLiquidInertia() {
		return 0.8F;
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
				if (!this.level().isClientSide) {
					Vec3 vec3 = entity.getLookAngle();
					this.setDeltaMovement(vec3);
					this.xPower = vec3.x * 0.1;
					this.yPower = vec3.y * 0.1;
					this.zPower = vec3.z * 0.1;
					this.setOwner(entity);
				}

				return true;
			} else {
				return false;
			}
		}
	}

	@Override
	public float getLightLevelDependentMagicValue() {
		return 1.0F;
	}

	@Override
	public Packet<ClientGamePacketListener> getAddEntityPacket() {
		Entity entity = this.getOwner();
		int i = entity == null ? 0 : entity.getId();
		return new ClientboundAddEntityPacket(
			this.getId(),
			this.getUUID(),
			this.getX(),
			this.getY(),
			this.getZ(),
			this.getXRot(),
			this.getYRot(),
			this.getType(),
			i,
			new Vec3(this.xPower, this.yPower, this.zPower),
			0.0
		);
	}

	@Override
	public void recreateFromPacket(ClientboundAddEntityPacket clientboundAddEntityPacket) {
		super.recreateFromPacket(clientboundAddEntityPacket);
		double d = clientboundAddEntityPacket.getXa();
		double e = clientboundAddEntityPacket.getYa();
		double f = clientboundAddEntityPacket.getZa();
		this.assignPower(d, e, f);
	}

	private void assignPower(double d, double e, double f) {
		double g = Math.sqrt(d * d + e * e + f * f);
		if (g != 0.0) {
			this.xPower = d / g * 0.1;
			this.yPower = e / g * 0.1;
			this.zPower = f / g * 0.1;
		}
	}
}
