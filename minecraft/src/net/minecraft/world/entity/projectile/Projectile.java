package net.minecraft.world.entity.projectile;

import com.google.common.base.MoreObjects;
import it.unimi.dsi.fastutil.doubles.DoubleDoubleImmutablePair;
import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.TraceableEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public abstract class Projectile extends Entity implements TraceableEntity {
	@Nullable
	private UUID ownerUUID;
	@Nullable
	private Entity cachedOwner;
	private boolean leftOwner;
	private boolean hasBeenShot;
	@Nullable
	private Entity lastDeflectedBy;

	Projectile(EntityType<? extends Projectile> entityType, Level level) {
		super(entityType, level);
	}

	public void setOwner(@Nullable Entity entity) {
		if (entity != null) {
			this.ownerUUID = entity.getUUID();
			this.cachedOwner = entity;
		}
	}

	@Nullable
	@Override
	public Entity getOwner() {
		if (this.cachedOwner != null && !this.cachedOwner.isRemoved()) {
			return this.cachedOwner;
		} else if (this.ownerUUID != null && this.level() instanceof ServerLevel serverLevel) {
			this.cachedOwner = serverLevel.getEntity(this.ownerUUID);
			return this.cachedOwner;
		} else {
			return null;
		}
	}

	public Entity getEffectSource() {
		return MoreObjects.firstNonNull(this.getOwner(), this);
	}

	@Override
	protected void addAdditionalSaveData(CompoundTag compoundTag) {
		if (this.ownerUUID != null) {
			compoundTag.putUUID("Owner", this.ownerUUID);
		}

		if (this.leftOwner) {
			compoundTag.putBoolean("LeftOwner", true);
		}

		compoundTag.putBoolean("HasBeenShot", this.hasBeenShot);
	}

	protected boolean ownedBy(Entity entity) {
		return entity.getUUID().equals(this.ownerUUID);
	}

	@Override
	protected void readAdditionalSaveData(CompoundTag compoundTag) {
		if (compoundTag.hasUUID("Owner")) {
			this.ownerUUID = compoundTag.getUUID("Owner");
			this.cachedOwner = null;
		}

		this.leftOwner = compoundTag.getBoolean("LeftOwner");
		this.hasBeenShot = compoundTag.getBoolean("HasBeenShot");
	}

	@Override
	public void restoreFrom(Entity entity) {
		super.restoreFrom(entity);
		if (entity instanceof Projectile projectile) {
			this.cachedOwner = projectile.cachedOwner;
		}
	}

	@Override
	public void tick() {
		if (!this.hasBeenShot) {
			this.gameEvent(GameEvent.PROJECTILE_SHOOT, this.getOwner());
			this.hasBeenShot = true;
		}

		if (!this.leftOwner) {
			this.leftOwner = this.checkLeftOwner();
		}

		super.tick();
	}

	private boolean checkLeftOwner() {
		Entity entity = this.getOwner();
		if (entity != null) {
			for (Entity entity2 : this.level()
				.getEntities(this, this.getBoundingBox().expandTowards(this.getDeltaMovement()).inflate(1.0), entityx -> !entityx.isSpectator() && entityx.isPickable())) {
				if (entity2.getRootVehicle() == entity.getRootVehicle()) {
					return false;
				}
			}
		}

		return true;
	}

	public Vec3 getMovementToShoot(double d, double e, double f, float g, float h) {
		return new Vec3(d, e, f)
			.normalize()
			.add(this.random.triangle(0.0, 0.0172275 * (double)h), this.random.triangle(0.0, 0.0172275 * (double)h), this.random.triangle(0.0, 0.0172275 * (double)h))
			.scale((double)g);
	}

	public void shoot(double d, double e, double f, float g, float h) {
		Vec3 vec3 = this.getMovementToShoot(d, e, f, g, h);
		this.setDeltaMovement(vec3);
		this.hasImpulse = true;
		double i = vec3.horizontalDistance();
		this.setYRot((float)(Mth.atan2(vec3.x, vec3.z) * 180.0F / (float)Math.PI));
		this.setXRot((float)(Mth.atan2(vec3.y, i) * 180.0F / (float)Math.PI));
		this.yRotO = this.getYRot();
		this.xRotO = this.getXRot();
	}

	public void shootFromRotation(Entity entity, float f, float g, float h, float i, float j) {
		float k = -Mth.sin(g * (float) (Math.PI / 180.0)) * Mth.cos(f * (float) (Math.PI / 180.0));
		float l = -Mth.sin((f + h) * (float) (Math.PI / 180.0));
		float m = Mth.cos(g * (float) (Math.PI / 180.0)) * Mth.cos(f * (float) (Math.PI / 180.0));
		this.shoot((double)k, (double)l, (double)m, i, j);
		Vec3 vec3 = entity.getDeltaMovement();
		this.setDeltaMovement(this.getDeltaMovement().add(vec3.x, entity.onGround() ? 0.0 : vec3.y, vec3.z));
	}

	protected ProjectileDeflection hitTargetOrDeflectSelf(HitResult hitResult) {
		if (hitResult.getType() == HitResult.Type.ENTITY) {
			EntityHitResult entityHitResult = (EntityHitResult)hitResult;
			Entity entity = entityHitResult.getEntity();
			ProjectileDeflection projectileDeflection = entity.deflection(this);
			if (projectileDeflection != ProjectileDeflection.NONE) {
				if (entity != this.lastDeflectedBy) {
					this.lastDeflectedBy = entity;
					this.deflect(projectileDeflection, entity, this.getOwner(), false);
				}

				return projectileDeflection;
			}
		}

		this.onHit(hitResult);
		return ProjectileDeflection.NONE;
	}

	public void deflect(ProjectileDeflection projectileDeflection, @Nullable Entity entity, @Nullable Entity entity2, boolean bl) {
		if (!this.level().isClientSide) {
			projectileDeflection.deflect(this, entity, this.random);
			this.setOwner(entity2);
			this.onDeflection(entity, bl);
		}
	}

	protected void onDeflection(@Nullable Entity entity, boolean bl) {
	}

	protected void onHit(HitResult hitResult) {
		HitResult.Type type = hitResult.getType();
		if (type == HitResult.Type.ENTITY) {
			EntityHitResult entityHitResult = (EntityHitResult)hitResult;
			Entity entity = entityHitResult.getEntity();
			if (entity.getType().is(EntityTypeTags.REDIRECTABLE_PROJECTILE) && entity instanceof Projectile projectile) {
				projectile.deflect(ProjectileDeflection.AIM_DEFLECT, this.getOwner(), this.getOwner(), true);
			}

			this.onHitEntity(entityHitResult);
			this.level().gameEvent(GameEvent.PROJECTILE_LAND, hitResult.getLocation(), GameEvent.Context.of(this, null));
		} else if (type == HitResult.Type.BLOCK) {
			BlockHitResult blockHitResult = (BlockHitResult)hitResult;
			this.onHitBlock(blockHitResult);
			BlockPos blockPos = blockHitResult.getBlockPos();
			this.level().gameEvent(GameEvent.PROJECTILE_LAND, blockPos, GameEvent.Context.of(this, this.level().getBlockState(blockPos)));
		}
	}

	protected void onHitEntity(EntityHitResult entityHitResult) {
	}

	protected void onHitBlock(BlockHitResult blockHitResult) {
		BlockState blockState = this.level().getBlockState(blockHitResult.getBlockPos());
		blockState.onProjectileHit(this.level(), blockState, blockHitResult, this);
	}

	@Override
	public void lerpMotion(double d, double e, double f) {
		this.setDeltaMovement(d, e, f);
		if (this.xRotO == 0.0F && this.yRotO == 0.0F) {
			double g = Math.sqrt(d * d + f * f);
			this.setXRot((float)(Mth.atan2(e, g) * 180.0F / (float)Math.PI));
			this.setYRot((float)(Mth.atan2(d, f) * 180.0F / (float)Math.PI));
			this.xRotO = this.getXRot();
			this.yRotO = this.getYRot();
			this.moveTo(this.getX(), this.getY(), this.getZ(), this.getYRot(), this.getXRot());
		}
	}

	protected boolean canHitEntity(Entity entity) {
		if (!entity.canBeHitByProjectile()) {
			return false;
		} else {
			Entity entity2 = this.getOwner();
			return entity2 == null || this.leftOwner || !entity2.isPassengerOfSameVehicle(entity);
		}
	}

	protected void updateRotation() {
		Vec3 vec3 = this.getDeltaMovement();
		double d = vec3.horizontalDistance();
		this.setXRot(lerpRotation(this.xRotO, (float)(Mth.atan2(vec3.y, d) * 180.0F / (float)Math.PI)));
		this.setYRot(lerpRotation(this.yRotO, (float)(Mth.atan2(vec3.x, vec3.z) * 180.0F / (float)Math.PI)));
	}

	protected static float lerpRotation(float f, float g) {
		while (g - f < -180.0F) {
			f -= 360.0F;
		}

		while (g - f >= 180.0F) {
			f += 360.0F;
		}

		return Mth.lerp(0.2F, f, g);
	}

	@Override
	public Packet<ClientGamePacketListener> getAddEntityPacket() {
		Entity entity = this.getOwner();
		return new ClientboundAddEntityPacket(this, entity == null ? 0 : entity.getId());
	}

	@Override
	public void recreateFromPacket(ClientboundAddEntityPacket clientboundAddEntityPacket) {
		super.recreateFromPacket(clientboundAddEntityPacket);
		Entity entity = this.level().getEntity(clientboundAddEntityPacket.getData());
		if (entity != null) {
			this.setOwner(entity);
		}
	}

	@Override
	public boolean mayInteract(Level level, BlockPos blockPos) {
		Entity entity = this.getOwner();
		return entity instanceof Player ? entity.mayInteract(level, blockPos) : entity == null || level.getGameRules().getBoolean(GameRules.RULE_MOBGRIEFING);
	}

	public boolean mayBreak(Level level) {
		return this.getType().is(EntityTypeTags.IMPACT_PROJECTILES) && level.getGameRules().getBoolean(GameRules.RULE_PROJECTILESCANBREAKBLOCKS);
	}

	@Override
	public boolean isPickable() {
		return this.getType().is(EntityTypeTags.REDIRECTABLE_PROJECTILE);
	}

	@Override
	public float getPickRadius() {
		return this.isPickable() ? 1.0F : 0.0F;
	}

	public DoubleDoubleImmutablePair calculateHorizontalHurtKnockbackDirection(LivingEntity livingEntity, DamageSource damageSource) {
		double d = this.getDeltaMovement().x;
		double e = this.getDeltaMovement().z;
		return DoubleDoubleImmutablePair.of(d, e);
	}
}
