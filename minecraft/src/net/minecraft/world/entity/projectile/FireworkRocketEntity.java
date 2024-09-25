package net.minecraft.world.entity.projectile;

import it.unimi.dsi.fastutil.doubles.DoubleDoubleImmutablePair;
import java.util.List;
import java.util.OptionalInt;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.FireworkExplosion;
import net.minecraft.world.item.component.Fireworks;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public class FireworkRocketEntity extends Projectile implements ItemSupplier {
	private static final EntityDataAccessor<ItemStack> DATA_ID_FIREWORKS_ITEM = SynchedEntityData.defineId(
		FireworkRocketEntity.class, EntityDataSerializers.ITEM_STACK
	);
	private static final EntityDataAccessor<OptionalInt> DATA_ATTACHED_TO_TARGET = SynchedEntityData.defineId(
		FireworkRocketEntity.class, EntityDataSerializers.OPTIONAL_UNSIGNED_INT
	);
	private static final EntityDataAccessor<Boolean> DATA_SHOT_AT_ANGLE = SynchedEntityData.defineId(FireworkRocketEntity.class, EntityDataSerializers.BOOLEAN);
	private int life;
	private int lifetime;
	@Nullable
	private LivingEntity attachedToEntity;

	public FireworkRocketEntity(EntityType<? extends FireworkRocketEntity> entityType, Level level) {
		super(entityType, level);
	}

	public FireworkRocketEntity(Level level, double d, double e, double f, ItemStack itemStack) {
		super(EntityType.FIREWORK_ROCKET, level);
		this.life = 0;
		this.setPos(d, e, f);
		this.entityData.set(DATA_ID_FIREWORKS_ITEM, itemStack.copy());
		int i = 1;
		Fireworks fireworks = itemStack.get(DataComponents.FIREWORKS);
		if (fireworks != null) {
			i += fireworks.flightDuration();
		}

		this.setDeltaMovement(this.random.triangle(0.0, 0.002297), 0.05, this.random.triangle(0.0, 0.002297));
		this.lifetime = 10 * i + this.random.nextInt(6) + this.random.nextInt(7);
	}

	public FireworkRocketEntity(Level level, @Nullable Entity entity, double d, double e, double f, ItemStack itemStack) {
		this(level, d, e, f, itemStack);
		this.setOwner(entity);
	}

	public FireworkRocketEntity(Level level, ItemStack itemStack, LivingEntity livingEntity) {
		this(level, livingEntity, livingEntity.getX(), livingEntity.getY(), livingEntity.getZ(), itemStack);
		this.entityData.set(DATA_ATTACHED_TO_TARGET, OptionalInt.of(livingEntity.getId()));
		this.attachedToEntity = livingEntity;
	}

	public FireworkRocketEntity(Level level, ItemStack itemStack, double d, double e, double f, boolean bl) {
		this(level, d, e, f, itemStack);
		this.entityData.set(DATA_SHOT_AT_ANGLE, bl);
	}

	public FireworkRocketEntity(Level level, ItemStack itemStack, Entity entity, double d, double e, double f, boolean bl) {
		this(level, itemStack, d, e, f, bl);
		this.setOwner(entity);
	}

	@Override
	protected void defineSynchedData(SynchedEntityData.Builder builder) {
		builder.define(DATA_ID_FIREWORKS_ITEM, getDefaultItem());
		builder.define(DATA_ATTACHED_TO_TARGET, OptionalInt.empty());
		builder.define(DATA_SHOT_AT_ANGLE, false);
	}

	@Override
	public boolean shouldRenderAtSqrDistance(double d) {
		return d < 4096.0 && !this.isAttachedToEntity();
	}

	@Override
	public boolean shouldRender(double d, double e, double f) {
		return super.shouldRender(d, e, f) && !this.isAttachedToEntity();
	}

	@Override
	public void tick() {
		super.tick();
		HitResult hitResult;
		if (this.isAttachedToEntity()) {
			if (this.attachedToEntity == null) {
				this.entityData.get(DATA_ATTACHED_TO_TARGET).ifPresent(i -> {
					Entity entity = this.level().getEntity(i);
					if (entity instanceof LivingEntity) {
						this.attachedToEntity = (LivingEntity)entity;
					}
				});
			}

			if (this.attachedToEntity != null) {
				Vec3 vec33;
				if (this.attachedToEntity.isFallFlying()) {
					Vec3 vec3 = this.attachedToEntity.getLookAngle();
					double d = 1.5;
					double e = 0.1;
					Vec3 vec32 = this.attachedToEntity.getDeltaMovement();
					this.attachedToEntity
						.setDeltaMovement(
							vec32.add(vec3.x * 0.1 + (vec3.x * 1.5 - vec32.x) * 0.5, vec3.y * 0.1 + (vec3.y * 1.5 - vec32.y) * 0.5, vec3.z * 0.1 + (vec3.z * 1.5 - vec32.z) * 0.5)
						);
					vec33 = this.attachedToEntity.getHandHoldingItemAngle(Items.FIREWORK_ROCKET);
				} else {
					vec33 = Vec3.ZERO;
				}

				this.setPos(this.attachedToEntity.getX() + vec33.x, this.attachedToEntity.getY() + vec33.y, this.attachedToEntity.getZ() + vec33.z);
				this.setDeltaMovement(this.attachedToEntity.getDeltaMovement());
			}

			hitResult = ProjectileUtil.getHitResultOnMoveVector(this, this::canHitEntity);
		} else {
			if (!this.isShotAtAngle()) {
				double f = this.horizontalCollision ? 1.0 : 1.15;
				this.setDeltaMovement(this.getDeltaMovement().multiply(f, 1.0, f).add(0.0, 0.04, 0.0));
			}

			Vec3 vec33 = this.getDeltaMovement();
			hitResult = ProjectileUtil.getHitResultOnMoveVector(this, this::canHitEntity);
			this.move(MoverType.SELF, vec33);
			this.applyEffectsFromBlocks();
			this.setDeltaMovement(vec33);
		}

		if (!this.noPhysics && this.isAlive() && hitResult.getType() != HitResult.Type.MISS) {
			this.hitTargetOrDeflectSelf(hitResult);
			this.hasImpulse = true;
		}

		this.updateRotation();
		if (this.life == 0 && !this.isSilent()) {
			this.level().playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.FIREWORK_ROCKET_LAUNCH, SoundSource.AMBIENT, 3.0F, 1.0F);
		}

		this.life++;
		if (this.level().isClientSide && this.life % 2 < 2) {
			this.level()
				.addParticle(
					ParticleTypes.FIREWORK,
					this.getX(),
					this.getY(),
					this.getZ(),
					this.random.nextGaussian() * 0.05,
					-this.getDeltaMovement().y * 0.5,
					this.random.nextGaussian() * 0.05
				);
		}

		if (this.life > this.lifetime && this.level() instanceof ServerLevel serverLevel) {
			this.explode(serverLevel);
		}
	}

	private void explode(ServerLevel serverLevel) {
		serverLevel.broadcastEntityEvent(this, (byte)17);
		this.gameEvent(GameEvent.EXPLODE, this.getOwner());
		this.dealExplosionDamage(serverLevel);
		this.discard();
	}

	@Override
	protected void onHitEntity(EntityHitResult entityHitResult) {
		super.onHitEntity(entityHitResult);
		if (this.level() instanceof ServerLevel serverLevel) {
			this.explode(serverLevel);
		}
	}

	@Override
	protected void onHitBlock(BlockHitResult blockHitResult) {
		BlockPos blockPos = new BlockPos(blockHitResult.getBlockPos());
		this.level().getBlockState(blockPos).entityInside(this.level(), blockPos, this);
		if (this.level() instanceof ServerLevel serverLevel && this.hasExplosion()) {
			this.explode(serverLevel);
		}

		super.onHitBlock(blockHitResult);
	}

	private boolean hasExplosion() {
		return !this.getExplosions().isEmpty();
	}

	private void dealExplosionDamage(ServerLevel serverLevel) {
		float f = 0.0F;
		List<FireworkExplosion> list = this.getExplosions();
		if (!list.isEmpty()) {
			f = 5.0F + (float)(list.size() * 2);
		}

		if (f > 0.0F) {
			if (this.attachedToEntity != null) {
				this.attachedToEntity.hurtServer(serverLevel, this.damageSources().fireworks(this, this.getOwner()), 5.0F + (float)(list.size() * 2));
			}

			double d = 5.0;
			Vec3 vec3 = this.position();

			for (LivingEntity livingEntity : this.level().getEntitiesOfClass(LivingEntity.class, this.getBoundingBox().inflate(5.0))) {
				if (livingEntity != this.attachedToEntity && !(this.distanceToSqr(livingEntity) > 25.0)) {
					boolean bl = false;

					for (int i = 0; i < 2; i++) {
						Vec3 vec32 = new Vec3(livingEntity.getX(), livingEntity.getY(0.5 * (double)i), livingEntity.getZ());
						HitResult hitResult = this.level().clip(new ClipContext(vec3, vec32, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, this));
						if (hitResult.getType() == HitResult.Type.MISS) {
							bl = true;
							break;
						}
					}

					if (bl) {
						float g = f * (float)Math.sqrt((5.0 - (double)this.distanceTo(livingEntity)) / 5.0);
						livingEntity.hurtServer(serverLevel, this.damageSources().fireworks(this, this.getOwner()), g);
					}
				}
			}
		}
	}

	private boolean isAttachedToEntity() {
		return this.entityData.get(DATA_ATTACHED_TO_TARGET).isPresent();
	}

	public boolean isShotAtAngle() {
		return this.entityData.get(DATA_SHOT_AT_ANGLE);
	}

	@Override
	public void handleEntityEvent(byte b) {
		if (b == 17 && this.level().isClientSide) {
			Vec3 vec3 = this.getDeltaMovement();
			this.level().createFireworks(this.getX(), this.getY(), this.getZ(), vec3.x, vec3.y, vec3.z, this.getExplosions());
		}

		super.handleEntityEvent(b);
	}

	@Override
	public void addAdditionalSaveData(CompoundTag compoundTag) {
		super.addAdditionalSaveData(compoundTag);
		compoundTag.putInt("Life", this.life);
		compoundTag.putInt("LifeTime", this.lifetime);
		compoundTag.put("FireworksItem", this.getItem().save(this.registryAccess()));
		compoundTag.putBoolean("ShotAtAngle", this.entityData.get(DATA_SHOT_AT_ANGLE));
	}

	@Override
	public void readAdditionalSaveData(CompoundTag compoundTag) {
		super.readAdditionalSaveData(compoundTag);
		this.life = compoundTag.getInt("Life");
		this.lifetime = compoundTag.getInt("LifeTime");
		if (compoundTag.contains("FireworksItem", 10)) {
			this.entityData
				.set(
					DATA_ID_FIREWORKS_ITEM,
					(ItemStack)ItemStack.parse(this.registryAccess(), compoundTag.getCompound("FireworksItem")).orElseGet(FireworkRocketEntity::getDefaultItem)
				);
		} else {
			this.entityData.set(DATA_ID_FIREWORKS_ITEM, getDefaultItem());
		}

		if (compoundTag.contains("ShotAtAngle")) {
			this.entityData.set(DATA_SHOT_AT_ANGLE, compoundTag.getBoolean("ShotAtAngle"));
		}
	}

	private List<FireworkExplosion> getExplosions() {
		ItemStack itemStack = this.entityData.get(DATA_ID_FIREWORKS_ITEM);
		Fireworks fireworks = itemStack.get(DataComponents.FIREWORKS);
		return fireworks != null ? fireworks.explosions() : List.of();
	}

	@Override
	public ItemStack getItem() {
		return this.entityData.get(DATA_ID_FIREWORKS_ITEM);
	}

	@Override
	public boolean isAttackable() {
		return false;
	}

	private static ItemStack getDefaultItem() {
		return new ItemStack(Items.FIREWORK_ROCKET);
	}

	@Override
	public DoubleDoubleImmutablePair calculateHorizontalHurtKnockbackDirection(LivingEntity livingEntity, DamageSource damageSource) {
		double d = livingEntity.position().x - this.position().x;
		double e = livingEntity.position().z - this.position().z;
		return DoubleDoubleImmutablePair.of(d, e);
	}
}
