package net.minecraft.world.entity.projectile;

import java.util.OptionalInt;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
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
		int i = 1;
		if (!itemStack.isEmpty() && itemStack.hasTag()) {
			this.entityData.set(DATA_ID_FIREWORKS_ITEM, itemStack.copy());
			i += itemStack.getOrCreateTagElement("Fireworks").getByte("Flight");
		}

		this.setDeltaMovement(this.random.nextGaussian() * 0.001, 0.05, this.random.nextGaussian() * 0.001);
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
	protected void defineSynchedData() {
		this.entityData.define(DATA_ID_FIREWORKS_ITEM, ItemStack.EMPTY);
		this.entityData.define(DATA_ATTACHED_TO_TARGET, OptionalInt.empty());
		this.entityData.define(DATA_SHOT_AT_ANGLE, false);
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
		if (this.isAttachedToEntity()) {
			if (this.attachedToEntity == null) {
				this.entityData.get(DATA_ATTACHED_TO_TARGET).ifPresent(i -> {
					Entity entity = this.level.getEntity(i);
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
		} else {
			if (!this.isShotAtAngle()) {
				double f = this.horizontalCollision ? 1.0 : 1.15;
				this.setDeltaMovement(this.getDeltaMovement().multiply(f, 1.0, f).add(0.0, 0.04, 0.0));
			}

			Vec3 vec33 = this.getDeltaMovement();
			this.move(MoverType.SELF, vec33);
			this.setDeltaMovement(vec33);
		}

		HitResult hitResult = ProjectileUtil.getHitResult(this, this::canHitEntity);
		if (!this.noPhysics) {
			this.onHit(hitResult);
			this.hasImpulse = true;
		}

		this.updateRotation();
		if (this.life == 0 && !this.isSilent()) {
			this.level.playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.FIREWORK_ROCKET_LAUNCH, SoundSource.AMBIENT, 3.0F, 1.0F);
		}

		this.life++;
		if (this.level.isClientSide && this.life % 2 < 2) {
			this.level
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

		if (!this.level.isClientSide && this.life > this.lifetime) {
			this.explode();
		}
	}

	private void explode() {
		this.level.broadcastEntityEvent(this, (byte)17);
		this.gameEvent(GameEvent.EXPLODE, this.getOwner());
		this.dealExplosionDamage();
		this.discard();
	}

	@Override
	protected void onHitEntity(EntityHitResult entityHitResult) {
		super.onHitEntity(entityHitResult);
		if (!this.level.isClientSide) {
			this.explode();
		}
	}

	@Override
	protected void onHitBlock(BlockHitResult blockHitResult) {
		BlockPos blockPos = new BlockPos(blockHitResult.getBlockPos());
		this.level.getBlockState(blockPos).entityInside(this.level, blockPos, this);
		if (!this.level.isClientSide() && this.hasExplosion()) {
			this.explode();
		}

		super.onHitBlock(blockHitResult);
	}

	private boolean hasExplosion() {
		ItemStack itemStack = this.entityData.get(DATA_ID_FIREWORKS_ITEM);
		CompoundTag compoundTag = itemStack.isEmpty() ? null : itemStack.getTagElement("Fireworks");
		ListTag listTag = compoundTag != null ? compoundTag.getList("Explosions", 10) : null;
		return listTag != null && !listTag.isEmpty();
	}

	private void dealExplosionDamage() {
		float f = 0.0F;
		ItemStack itemStack = this.entityData.get(DATA_ID_FIREWORKS_ITEM);
		CompoundTag compoundTag = itemStack.isEmpty() ? null : itemStack.getTagElement("Fireworks");
		ListTag listTag = compoundTag != null ? compoundTag.getList("Explosions", 10) : null;
		if (listTag != null && !listTag.isEmpty()) {
			f = 5.0F + (float)(listTag.size() * 2);
		}

		if (f > 0.0F) {
			if (this.attachedToEntity != null) {
				this.attachedToEntity.hurt(DamageSource.fireworks(this, this.getOwner()), 5.0F + (float)(listTag.size() * 2));
			}

			double d = 5.0;
			Vec3 vec3 = this.position();

			for (LivingEntity livingEntity : this.level.getEntitiesOfClass(LivingEntity.class, this.getBoundingBox().inflate(5.0))) {
				if (livingEntity != this.attachedToEntity && !(this.distanceToSqr(livingEntity) > 25.0)) {
					boolean bl = false;

					for (int i = 0; i < 2; i++) {
						Vec3 vec32 = new Vec3(livingEntity.getX(), livingEntity.getY(0.5 * (double)i), livingEntity.getZ());
						HitResult hitResult = this.level.clip(new ClipContext(vec3, vec32, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, this));
						if (hitResult.getType() == HitResult.Type.MISS) {
							bl = true;
							break;
						}
					}

					if (bl) {
						float g = f * (float)Math.sqrt((5.0 - (double)this.distanceTo(livingEntity)) / 5.0);
						livingEntity.hurt(DamageSource.fireworks(this, this.getOwner()), g);
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
		if (b == 17 && this.level.isClientSide) {
			if (!this.hasExplosion()) {
				for (int i = 0; i < this.random.nextInt(3) + 2; i++) {
					this.level
						.addParticle(ParticleTypes.POOF, this.getX(), this.getY(), this.getZ(), this.random.nextGaussian() * 0.05, 0.005, this.random.nextGaussian() * 0.05);
				}
			} else {
				ItemStack itemStack = this.entityData.get(DATA_ID_FIREWORKS_ITEM);
				CompoundTag compoundTag = itemStack.isEmpty() ? null : itemStack.getTagElement("Fireworks");
				Vec3 vec3 = this.getDeltaMovement();
				this.level.createFireworks(this.getX(), this.getY(), this.getZ(), vec3.x, vec3.y, vec3.z, compoundTag);
			}
		}

		super.handleEntityEvent(b);
	}

	@Override
	public void addAdditionalSaveData(CompoundTag compoundTag) {
		super.addAdditionalSaveData(compoundTag);
		compoundTag.putInt("Life", this.life);
		compoundTag.putInt("LifeTime", this.lifetime);
		ItemStack itemStack = this.entityData.get(DATA_ID_FIREWORKS_ITEM);
		if (!itemStack.isEmpty()) {
			compoundTag.put("FireworksItem", itemStack.save(new CompoundTag()));
		}

		compoundTag.putBoolean("ShotAtAngle", this.entityData.get(DATA_SHOT_AT_ANGLE));
	}

	@Override
	public void readAdditionalSaveData(CompoundTag compoundTag) {
		super.readAdditionalSaveData(compoundTag);
		this.life = compoundTag.getInt("Life");
		this.lifetime = compoundTag.getInt("LifeTime");
		ItemStack itemStack = ItemStack.of(compoundTag.getCompound("FireworksItem"));
		if (!itemStack.isEmpty()) {
			this.entityData.set(DATA_ID_FIREWORKS_ITEM, itemStack);
		}

		if (compoundTag.contains("ShotAtAngle")) {
			this.entityData.set(DATA_SHOT_AT_ANGLE, compoundTag.getBoolean("ShotAtAngle"));
		}
	}

	@Override
	public ItemStack getItem() {
		ItemStack itemStack = this.entityData.get(DATA_ID_FIREWORKS_ITEM);
		return itemStack.isEmpty() ? new ItemStack(Items.FIREWORK_ROCKET) : itemStack;
	}

	@Override
	public boolean isAttackable() {
		return false;
	}
}
