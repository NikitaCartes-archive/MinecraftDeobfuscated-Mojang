package net.minecraft.world.entity.projectile;

import java.util.OptionalInt;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.api.EnvironmentInterface;
import net.fabricmc.api.EnvironmentInterfaces;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

@EnvironmentInterfaces({@EnvironmentInterface(
		value = EnvType.CLIENT,
		itf = ItemSupplier.class
	)})
public class FireworkRocketEntity extends Entity implements ItemSupplier, Projectile {
	private static final EntityDataAccessor<ItemStack> DATA_ID_FIREWORKS_ITEM = SynchedEntityData.defineId(
		FireworkRocketEntity.class, EntityDataSerializers.ITEM_STACK
	);
	private static final EntityDataAccessor<OptionalInt> DATA_ATTACHED_TO_TARGET = SynchedEntityData.defineId(
		FireworkRocketEntity.class, EntityDataSerializers.OPTIONAL_UNSIGNED_INT
	);
	private static final EntityDataAccessor<Boolean> DATA_SHOT_AT_ANGLE = SynchedEntityData.defineId(FireworkRocketEntity.class, EntityDataSerializers.BOOLEAN);
	private int life;
	private int lifetime;
	private LivingEntity attachedToEntity;

	public FireworkRocketEntity(EntityType<? extends FireworkRocketEntity> entityType, Level level) {
		super(entityType, level);
	}

	@Override
	protected void defineSynchedData() {
		this.entityData.define(DATA_ID_FIREWORKS_ITEM, ItemStack.EMPTY);
		this.entityData.define(DATA_ATTACHED_TO_TARGET, OptionalInt.empty());
		this.entityData.define(DATA_SHOT_AT_ANGLE, false);
	}

	@Environment(EnvType.CLIENT)
	@Override
	public boolean shouldRenderAtSqrDistance(double d) {
		return d < 4096.0 && !this.isAttachedToEntity();
	}

	@Environment(EnvType.CLIENT)
	@Override
	public boolean shouldRender(double d, double e, double f) {
		return super.shouldRender(d, e, f) && !this.isAttachedToEntity();
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

	public FireworkRocketEntity(Level level, ItemStack itemStack, LivingEntity livingEntity) {
		this(level, livingEntity.x, livingEntity.y, livingEntity.z, itemStack);
		this.entityData.set(DATA_ATTACHED_TO_TARGET, OptionalInt.of(livingEntity.getId()));
		this.attachedToEntity = livingEntity;
	}

	public FireworkRocketEntity(Level level, ItemStack itemStack, double d, double e, double f, boolean bl) {
		this(level, d, e, f, itemStack);
		this.entityData.set(DATA_SHOT_AT_ANGLE, bl);
	}

	@Environment(EnvType.CLIENT)
	@Override
	public void lerpMotion(double d, double e, double f) {
		this.setDeltaMovement(d, e, f);
		if (this.xRotO == 0.0F && this.yRotO == 0.0F) {
			float g = Mth.sqrt(d * d + f * f);
			this.yRot = (float)(Mth.atan2(d, f) * 180.0F / (float)Math.PI);
			this.xRot = (float)(Mth.atan2(e, (double)g) * 180.0F / (float)Math.PI);
			this.yRotO = this.yRot;
			this.xRotO = this.xRot;
		}
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
				if (this.attachedToEntity.isFallFlying()) {
					Vec3 vec3 = this.attachedToEntity.getLookAngle();
					double d = 1.5;
					double e = 0.1;
					Vec3 vec32 = this.attachedToEntity.getDeltaMovement();
					this.attachedToEntity
						.setDeltaMovement(
							vec32.add(vec3.x * 0.1 + (vec3.x * 1.5 - vec32.x) * 0.5, vec3.y * 0.1 + (vec3.y * 1.5 - vec32.y) * 0.5, vec3.z * 0.1 + (vec3.z * 1.5 - vec32.z) * 0.5)
						);
				}

				this.setPos(this.attachedToEntity.x, this.attachedToEntity.y, this.attachedToEntity.z);
				this.setDeltaMovement(this.attachedToEntity.getDeltaMovement());
			}
		} else {
			if (!this.isShotAtAngle()) {
				this.setDeltaMovement(this.getDeltaMovement().multiply(1.15, 1.0, 1.15).add(0.0, 0.04, 0.0));
			}

			this.move(MoverType.SELF, this.getDeltaMovement());
		}

		Vec3 vec3 = this.getDeltaMovement();
		HitResult hitResult = ProjectileUtil.getHitResult(
			this,
			this.getBoundingBox().expandTowards(vec3).inflate(1.0),
			entity -> !entity.isSpectator() && entity.isAlive() && entity.isPickable(),
			ClipContext.Block.COLLIDER,
			true
		);
		if (!this.noPhysics) {
			this.performHitChecks(hitResult);
			this.hasImpulse = true;
		}

		float f = Mth.sqrt(getHorizontalDistanceSqr(vec3));
		this.yRot = (float)(Mth.atan2(vec3.x, vec3.z) * 180.0F / (float)Math.PI);
		this.xRot = (float)(Mth.atan2(vec3.y, (double)f) * 180.0F / (float)Math.PI);

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
		if (this.life == 0 && !this.isSilent()) {
			this.level.playSound(null, this.x, this.y, this.z, SoundEvents.FIREWORK_ROCKET_LAUNCH, SoundSource.AMBIENT, 3.0F, 1.0F);
		}

		this.life++;
		if (this.level.isClientSide && this.life % 2 < 2) {
			this.level
				.addParticle(
					ParticleTypes.FIREWORK,
					this.x,
					this.y - 0.3,
					this.z,
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
		this.dealExplosionDamage();
		this.remove();
	}

	protected void performHitChecks(HitResult hitResult) {
		if (hitResult.getType() == HitResult.Type.ENTITY && !this.level.isClientSide) {
			this.explode();
		} else if (this.collision) {
			BlockPos blockPos;
			if (hitResult.getType() == HitResult.Type.BLOCK) {
				blockPos = new BlockPos(((BlockHitResult)hitResult).getBlockPos());
			} else {
				blockPos = new BlockPos(this);
			}

			this.level.getBlockState(blockPos).entityInside(this.level, blockPos, this);
			if (this.hasExplosion()) {
				this.explode();
			}
		}
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
				this.attachedToEntity.hurt(DamageSource.FIREWORKS, 5.0F + (float)(listTag.size() * 2));
			}

			double d = 5.0;
			Vec3 vec3 = new Vec3(this.x, this.y, this.z);

			for (LivingEntity livingEntity : this.level.getEntitiesOfClass(LivingEntity.class, this.getBoundingBox().inflate(5.0))) {
				if (livingEntity != this.attachedToEntity && !(this.distanceToSqr(livingEntity) > 25.0)) {
					boolean bl = false;

					for (int i = 0; i < 2; i++) {
						Vec3 vec32 = new Vec3(livingEntity.x, livingEntity.y + (double)livingEntity.getBbHeight() * 0.5 * (double)i, livingEntity.z);
						HitResult hitResult = this.level.clip(new ClipContext(vec3, vec32, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, this));
						if (hitResult.getType() == HitResult.Type.MISS) {
							bl = true;
							break;
						}
					}

					if (bl) {
						float g = f * (float)Math.sqrt((5.0 - (double)this.distanceTo(livingEntity)) / 5.0);
						livingEntity.hurt(DamageSource.FIREWORKS, g);
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

	@Environment(EnvType.CLIENT)
	@Override
	public void handleEntityEvent(byte b) {
		if (b == 17 && this.level.isClientSide) {
			if (!this.hasExplosion()) {
				for (int i = 0; i < this.random.nextInt(3) + 2; i++) {
					this.level.addParticle(ParticleTypes.POOF, this.x, this.y, this.z, this.random.nextGaussian() * 0.05, 0.005, this.random.nextGaussian() * 0.05);
				}
			} else {
				ItemStack itemStack = this.entityData.get(DATA_ID_FIREWORKS_ITEM);
				CompoundTag compoundTag = itemStack.isEmpty() ? null : itemStack.getTagElement("Fireworks");
				Vec3 vec3 = this.getDeltaMovement();
				this.level.createFireworks(this.x, this.y, this.z, vec3.x, vec3.y, vec3.z, compoundTag);
			}
		}

		super.handleEntityEvent(b);
	}

	@Override
	public void addAdditionalSaveData(CompoundTag compoundTag) {
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

	@Environment(EnvType.CLIENT)
	@Override
	public ItemStack getItem() {
		ItemStack itemStack = this.entityData.get(DATA_ID_FIREWORKS_ITEM);
		return itemStack.isEmpty() ? new ItemStack(Items.FIREWORK_ROCKET) : itemStack;
	}

	@Override
	public boolean isAttackable() {
		return false;
	}

	@Override
	public Packet<?> getAddEntityPacket() {
		return new ClientboundAddEntityPacket(this);
	}

	@Override
	public void shoot(double d, double e, double f, float g, float h) {
		float i = Mth.sqrt(d * d + e * e + f * f);
		d /= (double)i;
		e /= (double)i;
		f /= (double)i;
		d += this.random.nextGaussian() * 0.0075F * (double)h;
		e += this.random.nextGaussian() * 0.0075F * (double)h;
		f += this.random.nextGaussian() * 0.0075F * (double)h;
		d *= (double)g;
		e *= (double)g;
		f *= (double)g;
		this.setDeltaMovement(d, e, f);
	}
}
