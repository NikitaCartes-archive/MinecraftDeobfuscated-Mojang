package net.minecraft.world.entity.projectile;

import com.google.common.collect.Lists;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import java.util.Arrays;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.protocol.game.ClientboundGameEventPacket;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.util.Unit;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;

public abstract class AbstractArrow extends Projectile {
	private static final double ARROW_BASE_DAMAGE = 2.0;
	private static final EntityDataAccessor<Byte> ID_FLAGS = SynchedEntityData.defineId(AbstractArrow.class, EntityDataSerializers.BYTE);
	private static final EntityDataAccessor<Byte> PIERCE_LEVEL = SynchedEntityData.defineId(AbstractArrow.class, EntityDataSerializers.BYTE);
	private static final int FLAG_CRIT = 1;
	private static final int FLAG_NOPHYSICS = 2;
	private static final int FLAG_CROSSBOW = 4;
	@Nullable
	private BlockState lastState;
	protected boolean inGround;
	protected int inGroundTime;
	public AbstractArrow.Pickup pickup = AbstractArrow.Pickup.DISALLOWED;
	public int shakeTime;
	private int life;
	private double baseDamage = 2.0;
	private int knockback;
	private SoundEvent soundEvent = this.getDefaultHitGroundSoundEvent();
	@Nullable
	private IntOpenHashSet piercingIgnoreEntityIds;
	@Nullable
	private List<Entity> piercedAndKilledEntities;
	private ItemStack pickupItemStack = this.getDefaultPickupItem();

	protected AbstractArrow(EntityType<? extends AbstractArrow> entityType, Level level) {
		super(entityType, level);
	}

	protected AbstractArrow(EntityType<? extends AbstractArrow> entityType, Level level, ItemStack itemStack) {
		this(entityType, level);
		this.pickupItemStack = itemStack.copy();
		this.setCustomName(itemStack.get(DataComponents.CUSTOM_NAME));
		Unit unit = itemStack.remove(DataComponents.INTANGIBLE_PROJECTILE);
		if (unit != null) {
			this.pickup = AbstractArrow.Pickup.CREATIVE_ONLY;
		}
	}

	protected AbstractArrow(EntityType<? extends AbstractArrow> entityType, double d, double e, double f, Level level, ItemStack itemStack) {
		this(entityType, level, itemStack);
		this.setPos(d, e, f);
	}

	protected AbstractArrow(EntityType<? extends AbstractArrow> entityType, LivingEntity livingEntity, Level level, ItemStack itemStack) {
		this(entityType, livingEntity.getX(), livingEntity.getEyeY() - 0.1F, livingEntity.getZ(), level, itemStack);
		this.setOwner(livingEntity);
	}

	public void setSoundEvent(SoundEvent soundEvent) {
		this.soundEvent = soundEvent;
	}

	@Override
	public boolean shouldRenderAtSqrDistance(double d) {
		double e = this.getBoundingBox().getSize() * 10.0;
		if (Double.isNaN(e)) {
			e = 1.0;
		}

		e *= 64.0 * getViewScale();
		return d < e * e;
	}

	@Override
	protected void defineSynchedData(SynchedEntityData.Builder builder) {
		builder.define(ID_FLAGS, (byte)0);
		builder.define(PIERCE_LEVEL, (byte)0);
	}

	@Override
	public void shoot(double d, double e, double f, float g, float h) {
		super.shoot(d, e, f, g, h);
		this.life = 0;
	}

	@Override
	public void lerpTo(double d, double e, double f, float g, float h, int i) {
		this.setPos(d, e, f);
		this.setRot(g, h);
	}

	@Override
	public void lerpMotion(double d, double e, double f) {
		super.lerpMotion(d, e, f);
		this.life = 0;
	}

	@Override
	public void tick() {
		super.tick();
		boolean bl = this.isNoPhysics();
		Vec3 vec3 = this.getDeltaMovement();
		if (this.xRotO == 0.0F && this.yRotO == 0.0F) {
			double d = vec3.horizontalDistance();
			this.setYRot((float)(Mth.atan2(vec3.x, vec3.z) * 180.0F / (float)Math.PI));
			this.setXRot((float)(Mth.atan2(vec3.y, d) * 180.0F / (float)Math.PI));
			this.yRotO = this.getYRot();
			this.xRotO = this.getXRot();
		}

		BlockPos blockPos = this.blockPosition();
		BlockState blockState = this.level().getBlockState(blockPos);
		if (!blockState.isAir() && !bl) {
			VoxelShape voxelShape = blockState.getCollisionShape(this.level(), blockPos);
			if (!voxelShape.isEmpty()) {
				Vec3 vec32 = this.position();

				for(AABB aABB : voxelShape.toAabbs()) {
					if (aABB.move(blockPos).contains(vec32)) {
						this.inGround = true;
						break;
					}
				}
			}
		}

		if (this.shakeTime > 0) {
			--this.shakeTime;
		}

		if (this.isInWaterOrRain() || blockState.is(Blocks.POWDER_SNOW)) {
			this.clearFire();
		}

		if (this.inGround && !bl) {
			if (this.lastState != blockState && this.shouldFall()) {
				this.startFalling();
			} else if (!this.level().isClientSide) {
				this.tickDespawn();
			}

			++this.inGroundTime;
		} else {
			this.inGroundTime = 0;
			Vec3 vec33 = this.position();
			Vec3 vec32 = vec33.add(vec3);
			HitResult hitResult = this.level().clip(new ClipContext(vec33, vec32, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, this));
			if (hitResult.getType() != HitResult.Type.MISS) {
				vec32 = hitResult.getLocation();
			}

			while(!this.isRemoved() && !this.isDeflected) {
				EntityHitResult entityHitResult = this.findHitEntity(vec33, vec32);
				if (entityHitResult != null) {
					hitResult = entityHitResult;
				}

				if (hitResult != null && hitResult.getType() == HitResult.Type.ENTITY) {
					Entity entity = ((EntityHitResult)hitResult).getEntity();
					Entity entity2 = this.getOwner();
					if (entity instanceof Player && entity2 instanceof Player && !((Player)entity2).canHarmPlayer((Player)entity)) {
						hitResult = null;
						entityHitResult = null;
					}
				}

				if (hitResult != null && !bl) {
					this.onHit(hitResult);
					this.hasImpulse = true;
				}

				if (entityHitResult == null || this.getPierceLevel() <= 0) {
					break;
				}

				hitResult = null;
			}

			this.isDeflected = false;
			vec3 = this.getDeltaMovement();
			double e = vec3.x;
			double f = vec3.y;
			double g = vec3.z;
			if (this.isCritArrow()) {
				for(int i = 0; i < 4; ++i) {
					this.level()
						.addParticle(
							ParticleTypes.CRIT, this.getX() + e * (double)i / 4.0, this.getY() + f * (double)i / 4.0, this.getZ() + g * (double)i / 4.0, -e, -f + 0.2, -g
						);
				}
			}

			double h = this.getX() + e;
			double j = this.getY() + f;
			double k = this.getZ() + g;
			double l = vec3.horizontalDistance();
			if (bl) {
				this.setYRot((float)(Mth.atan2(-e, -g) * 180.0F / (float)Math.PI));
			} else {
				this.setYRot((float)(Mth.atan2(e, g) * 180.0F / (float)Math.PI));
			}

			this.setXRot((float)(Mth.atan2(f, l) * 180.0F / (float)Math.PI));
			this.setXRot(lerpRotation(this.xRotO, this.getXRot()));
			this.setYRot(lerpRotation(this.yRotO, this.getYRot()));
			float m = 0.99F;
			if (this.isInWater()) {
				for(int n = 0; n < 4; ++n) {
					float o = 0.25F;
					this.level().addParticle(ParticleTypes.BUBBLE, h - e * 0.25, j - f * 0.25, k - g * 0.25, e, f, g);
				}

				m = this.getWaterInertia();
			}

			this.setDeltaMovement(vec3.scale((double)m));
			if (!bl) {
				this.applyGravity();
			}

			this.setPos(h, j, k);
			this.checkInsideBlocks();
		}
	}

	@Override
	protected double getDefaultGravity() {
		return 0.05;
	}

	private boolean shouldFall() {
		return this.inGround && this.level().noCollision(new AABB(this.position(), this.position()).inflate(0.06));
	}

	private void startFalling() {
		this.inGround = false;
		Vec3 vec3 = this.getDeltaMovement();
		this.setDeltaMovement(
			vec3.multiply((double)(this.random.nextFloat() * 0.2F), (double)(this.random.nextFloat() * 0.2F), (double)(this.random.nextFloat() * 0.2F))
		);
		this.life = 0;
	}

	@Override
	public void move(MoverType moverType, Vec3 vec3) {
		super.move(moverType, vec3);
		if (moverType != MoverType.SELF && this.shouldFall()) {
			this.startFalling();
		}
	}

	protected void tickDespawn() {
		++this.life;
		if (this.life >= 1200) {
			this.discard();
		}
	}

	private void resetPiercedEntities() {
		if (this.piercedAndKilledEntities != null) {
			this.piercedAndKilledEntities.clear();
		}

		if (this.piercingIgnoreEntityIds != null) {
			this.piercingIgnoreEntityIds.clear();
		}
	}

	@Override
	protected void onHitEntity(EntityHitResult entityHitResult) {
		super.onHitEntity(entityHitResult);
		Entity entity = entityHitResult.getEntity();
		float f = (float)this.getDeltaMovement().length();
		int i = Mth.ceil(Mth.clamp((double)f * this.baseDamage, 0.0, 2.147483647E9));
		if (this.getPierceLevel() > 0) {
			if (this.piercingIgnoreEntityIds == null) {
				this.piercingIgnoreEntityIds = new IntOpenHashSet(5);
			}

			if (this.piercedAndKilledEntities == null) {
				this.piercedAndKilledEntities = Lists.<Entity>newArrayListWithCapacity(5);
			}

			if (this.piercingIgnoreEntityIds.size() >= this.getPierceLevel() + 1) {
				this.discard();
				return;
			}

			this.piercingIgnoreEntityIds.add(entity.getId());
		}

		if (this.isCritArrow()) {
			long l = (long)this.random.nextInt(i / 2 + 2);
			i = (int)Math.min(l + (long)i, 2147483647L);
		}

		Entity entity2 = this.getOwner();
		DamageSource damageSource;
		if (entity2 == null) {
			damageSource = this.damageSources().arrow(this, this);
		} else {
			damageSource = this.damageSources().arrow(this, entity2);
			if (entity2 instanceof LivingEntity) {
				((LivingEntity)entity2).setLastHurtMob(entity);
			}
		}

		boolean bl = entity.getType() == EntityType.ENDERMAN;
		int j = entity.getRemainingFireTicks();
		if (this.isOnFire() && !bl) {
			entity.igniteForSeconds(5);
		}

		if (entity.hurt(damageSource, (float)i)) {
			if (bl) {
				return;
			}

			if (entity instanceof LivingEntity livingEntity) {
				if (!this.level().isClientSide && this.getPierceLevel() <= 0) {
					livingEntity.setArrowCount(livingEntity.getArrowCount() + 1);
				}

				if (this.knockback > 0) {
					double d = Math.max(0.0, 1.0 - livingEntity.getAttributeValue(Attributes.KNOCKBACK_RESISTANCE));
					Vec3 vec3 = this.getDeltaMovement().multiply(1.0, 0.0, 1.0).normalize().scale((double)this.knockback * 0.6 * d);
					if (vec3.lengthSqr() > 0.0) {
						livingEntity.push(vec3.x, 0.1, vec3.z);
					}
				}

				if (!this.level().isClientSide && entity2 instanceof LivingEntity) {
					EnchantmentHelper.doPostHurtEffects(livingEntity, entity2);
					EnchantmentHelper.doPostDamageEffects((LivingEntity)entity2, livingEntity);
				}

				this.doPostHurtEffects(livingEntity);
				if (entity2 != null && livingEntity != entity2 && livingEntity instanceof Player && entity2 instanceof ServerPlayer && !this.isSilent()) {
					((ServerPlayer)entity2).connection.send(new ClientboundGameEventPacket(ClientboundGameEventPacket.ARROW_HIT_PLAYER, 0.0F));
				}

				if (!entity.isAlive() && this.piercedAndKilledEntities != null) {
					this.piercedAndKilledEntities.add(livingEntity);
				}

				if (!this.level().isClientSide && entity2 instanceof ServerPlayer serverPlayer) {
					if (this.piercedAndKilledEntities != null && this.shotFromCrossbow()) {
						CriteriaTriggers.KILLED_BY_CROSSBOW.trigger(serverPlayer, this.piercedAndKilledEntities);
					} else if (!entity.isAlive() && this.shotFromCrossbow()) {
						CriteriaTriggers.KILLED_BY_CROSSBOW.trigger(serverPlayer, Arrays.asList(entity));
					}
				}
			}

			this.playSound(this.soundEvent, 1.0F, 1.2F / (this.random.nextFloat() * 0.2F + 0.9F));
			if (this.getPierceLevel() <= 0) {
				this.discard();
			}
		} else {
			entity.setRemainingFireTicks(j);
			this.setDeltaMovement(this.getDeltaMovement().scale(-0.1));
			this.setYRot(this.getYRot() + 180.0F);
			this.yRotO += 180.0F;
			if (!this.level().isClientSide && this.getDeltaMovement().lengthSqr() < 1.0E-7) {
				if (this.pickup == AbstractArrow.Pickup.ALLOWED) {
					this.spawnAtLocation(this.getPickupItem(), 0.1F);
				}

				this.discard();
			}
		}
	}

	@Override
	protected void onHitBlock(BlockHitResult blockHitResult) {
		this.lastState = this.level().getBlockState(blockHitResult.getBlockPos());
		super.onHitBlock(blockHitResult);
		Vec3 vec3 = blockHitResult.getLocation().subtract(this.getX(), this.getY(), this.getZ());
		this.setDeltaMovement(vec3);
		Vec3 vec32 = vec3.normalize().scale(0.05F);
		this.setPosRaw(this.getX() - vec32.x, this.getY() - vec32.y, this.getZ() - vec32.z);
		this.playSound(this.getHitGroundSoundEvent(), 1.0F, 1.2F / (this.random.nextFloat() * 0.2F + 0.9F));
		this.inGround = true;
		this.shakeTime = 7;
		this.setCritArrow(false);
		this.setPierceLevel((byte)0);
		this.setSoundEvent(SoundEvents.ARROW_HIT);
		this.setShotFromCrossbow(false);
		this.resetPiercedEntities();
	}

	protected SoundEvent getDefaultHitGroundSoundEvent() {
		return SoundEvents.ARROW_HIT;
	}

	protected final SoundEvent getHitGroundSoundEvent() {
		return this.soundEvent;
	}

	protected void doPostHurtEffects(LivingEntity livingEntity) {
	}

	@Nullable
	protected EntityHitResult findHitEntity(Vec3 vec3, Vec3 vec32) {
		return ProjectileUtil.getEntityHitResult(
			this.level(), this, vec3, vec32, this.getBoundingBox().expandTowards(this.getDeltaMovement()).inflate(1.0), this::canHitEntity
		);
	}

	@Override
	protected boolean canHitEntity(Entity entity) {
		return super.canHitEntity(entity) && (this.piercingIgnoreEntityIds == null || !this.piercingIgnoreEntityIds.contains(entity.getId()));
	}

	@Override
	public void addAdditionalSaveData(CompoundTag compoundTag) {
		super.addAdditionalSaveData(compoundTag);
		compoundTag.putShort("life", (short)this.life);
		if (this.lastState != null) {
			compoundTag.put("inBlockState", NbtUtils.writeBlockState(this.lastState));
		}

		compoundTag.putByte("shake", (byte)this.shakeTime);
		compoundTag.putBoolean("inGround", this.inGround);
		compoundTag.putByte("pickup", (byte)this.pickup.ordinal());
		compoundTag.putDouble("damage", this.baseDamage);
		compoundTag.putBoolean("crit", this.isCritArrow());
		compoundTag.putByte("PierceLevel", this.getPierceLevel());
		compoundTag.putString("SoundEvent", BuiltInRegistries.SOUND_EVENT.getKey(this.soundEvent).toString());
		compoundTag.putBoolean("ShotFromCrossbow", this.shotFromCrossbow());
		compoundTag.put("item", this.pickupItemStack.save(this.registryAccess()));
	}

	@Override
	public void readAdditionalSaveData(CompoundTag compoundTag) {
		super.readAdditionalSaveData(compoundTag);
		this.life = compoundTag.getShort("life");
		if (compoundTag.contains("inBlockState", 10)) {
			this.lastState = NbtUtils.readBlockState(this.level().holderLookup(Registries.BLOCK), compoundTag.getCompound("inBlockState"));
		}

		this.shakeTime = compoundTag.getByte("shake") & 255;
		this.inGround = compoundTag.getBoolean("inGround");
		if (compoundTag.contains("damage", 99)) {
			this.baseDamage = compoundTag.getDouble("damage");
		}

		this.pickup = AbstractArrow.Pickup.byOrdinal(compoundTag.getByte("pickup"));
		this.setCritArrow(compoundTag.getBoolean("crit"));
		this.setPierceLevel(compoundTag.getByte("PierceLevel"));
		if (compoundTag.contains("SoundEvent", 8)) {
			this.soundEvent = (SoundEvent)BuiltInRegistries.SOUND_EVENT
				.getOptional(new ResourceLocation(compoundTag.getString("SoundEvent")))
				.orElse(this.getDefaultHitGroundSoundEvent());
		}

		this.setShotFromCrossbow(compoundTag.getBoolean("ShotFromCrossbow"));
		if (compoundTag.contains("item", 10)) {
			this.setPickupItemStack((ItemStack)ItemStack.parse(this.registryAccess(), compoundTag.getCompound("item")).orElse(this.getDefaultPickupItem()));
		} else {
			this.setPickupItemStack(this.pickupItemStack);
		}
	}

	@Override
	public void setOwner(@Nullable Entity entity) {
		super.setOwner(entity);
		if (entity instanceof Player && this.pickup == AbstractArrow.Pickup.DISALLOWED) {
			this.pickup = AbstractArrow.Pickup.ALLOWED;
		}
	}

	@Override
	public void playerTouch(Player player) {
		if (!this.level().isClientSide && (this.inGround || this.isNoPhysics()) && this.shakeTime <= 0) {
			if (this.tryPickup(player)) {
				player.take(this, 1);
				this.discard();
			}
		}
	}

	protected boolean tryPickup(Player player) {
		return switch(this.pickup) {
			case DISALLOWED -> false;
			case ALLOWED -> player.getInventory().add(this.getPickupItem());
			case CREATIVE_ONLY -> player.hasInfiniteMaterials();
		};
	}

	protected ItemStack getPickupItem() {
		return this.pickupItemStack.copy();
	}

	protected abstract ItemStack getDefaultPickupItem();

	@Override
	protected Entity.MovementEmission getMovementEmission() {
		return Entity.MovementEmission.NONE;
	}

	public ItemStack getPickupItemStackOrigin() {
		return this.pickupItemStack;
	}

	public void setBaseDamage(double d) {
		this.baseDamage = d;
	}

	public double getBaseDamage() {
		return this.baseDamage;
	}

	public void setKnockback(int i) {
		this.knockback = i;
	}

	public int getKnockback() {
		return this.knockback;
	}

	@Override
	public boolean isAttackable() {
		return false;
	}

	public void setCritArrow(boolean bl) {
		this.setFlag(1, bl);
	}

	public void setPierceLevel(byte b) {
		this.entityData.set(PIERCE_LEVEL, b);
	}

	private void setFlag(int i, boolean bl) {
		byte b = this.entityData.get(ID_FLAGS);
		if (bl) {
			this.entityData.set(ID_FLAGS, (byte)(b | i));
		} else {
			this.entityData.set(ID_FLAGS, (byte)(b & ~i));
		}
	}

	protected void setPickupItemStack(ItemStack itemStack) {
		this.pickupItemStack = itemStack;
	}

	public boolean isCritArrow() {
		byte b = this.entityData.get(ID_FLAGS);
		return (b & 1) != 0;
	}

	public boolean shotFromCrossbow() {
		byte b = this.entityData.get(ID_FLAGS);
		return (b & 4) != 0;
	}

	public byte getPierceLevel() {
		return this.entityData.get(PIERCE_LEVEL);
	}

	public void setEnchantmentEffectsFromEntity(LivingEntity livingEntity, float f) {
		int i = EnchantmentHelper.getEnchantmentLevel(Enchantments.POWER, livingEntity);
		int j = EnchantmentHelper.getEnchantmentLevel(Enchantments.PUNCH, livingEntity);
		this.setBaseDamage((double)(f * 2.0F) + this.random.triangle((double)this.level().getDifficulty().getId() * 0.11, 0.57425));
		if (i > 0) {
			this.setBaseDamage(this.getBaseDamage() + (double)i * 0.5 + 0.5);
		}

		if (j > 0) {
			this.setKnockback(j);
		}

		if (EnchantmentHelper.getEnchantmentLevel(Enchantments.FLAME, livingEntity) > 0) {
			this.igniteForSeconds(100);
		}
	}

	protected float getWaterInertia() {
		return 0.6F;
	}

	public void setNoPhysics(boolean bl) {
		this.noPhysics = bl;
		this.setFlag(2, bl);
	}

	public boolean isNoPhysics() {
		if (!this.level().isClientSide) {
			return this.noPhysics;
		} else {
			return (this.entityData.get(ID_FLAGS) & 2) != 0;
		}
	}

	public void setShotFromCrossbow(boolean bl) {
		this.setFlag(4, bl);
	}

	public static enum Pickup {
		DISALLOWED,
		ALLOWED,
		CREATIVE_ONLY;

		public static AbstractArrow.Pickup byOrdinal(int i) {
			if (i < 0 || i > values().length) {
				i = 0;
			}

			return values()[i];
		}
	}
}
