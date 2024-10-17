package net.minecraft.world.entity.projectile;

import com.google.common.collect.Lists;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import java.util.List;
import java.util.Objects;
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
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.util.Mth;
import net.minecraft.util.Unit;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.OminousItemSpawner;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
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
	public static final int SHAKE_TIME = 7;
	private static final EntityDataAccessor<Byte> ID_FLAGS = SynchedEntityData.defineId(AbstractArrow.class, EntityDataSerializers.BYTE);
	private static final EntityDataAccessor<Byte> PIERCE_LEVEL = SynchedEntityData.defineId(AbstractArrow.class, EntityDataSerializers.BYTE);
	private static final EntityDataAccessor<Boolean> IN_GROUND = SynchedEntityData.defineId(AbstractArrow.class, EntityDataSerializers.BOOLEAN);
	private static final int FLAG_CRIT = 1;
	private static final int FLAG_NOPHYSICS = 2;
	@Nullable
	private BlockState lastState;
	protected int inGroundTime;
	public AbstractArrow.Pickup pickup = AbstractArrow.Pickup.DISALLOWED;
	public int shakeTime;
	private int life;
	private double baseDamage = 2.0;
	private SoundEvent soundEvent = this.getDefaultHitGroundSoundEvent();
	@Nullable
	private IntOpenHashSet piercingIgnoreEntityIds;
	@Nullable
	private List<Entity> piercedAndKilledEntities;
	private ItemStack pickupItemStack = this.getDefaultPickupItem();
	@Nullable
	private ItemStack firedFromWeapon = null;

	protected AbstractArrow(EntityType<? extends AbstractArrow> entityType, Level level) {
		super(entityType, level);
	}

	protected AbstractArrow(
		EntityType<? extends AbstractArrow> entityType, double d, double e, double f, Level level, ItemStack itemStack, @Nullable ItemStack itemStack2
	) {
		this(entityType, level);
		this.pickupItemStack = itemStack.copy();
		this.setCustomName(itemStack.get(DataComponents.CUSTOM_NAME));
		Unit unit = itemStack.remove(DataComponents.INTANGIBLE_PROJECTILE);
		if (unit != null) {
			this.pickup = AbstractArrow.Pickup.CREATIVE_ONLY;
		}

		this.setPos(d, e, f);
		if (itemStack2 != null && level instanceof ServerLevel serverLevel) {
			if (itemStack2.isEmpty()) {
				throw new IllegalArgumentException("Invalid weapon firing an arrow");
			}

			this.firedFromWeapon = itemStack2.copy();
			int i = EnchantmentHelper.getPiercingCount(serverLevel, itemStack2, this.pickupItemStack);
			if (i > 0) {
				this.setPierceLevel((byte)i);
			}
		}
	}

	protected AbstractArrow(
		EntityType<? extends AbstractArrow> entityType, LivingEntity livingEntity, Level level, ItemStack itemStack, @Nullable ItemStack itemStack2
	) {
		this(entityType, livingEntity.getX(), livingEntity.getEyeY() - 0.1F, livingEntity.getZ(), level, itemStack, itemStack2);
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
		builder.define(IN_GROUND, false);
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
		this.setDeltaMovement(d, e, f);
		this.life = 0;
		if (this.isInGround() && Mth.lengthSquared(d, e, f) > 0.0) {
			this.setInGround(false);
		}
	}

	@Override
	public void onSyncedDataUpdated(EntityDataAccessor<?> entityDataAccessor) {
		super.onSyncedDataUpdated(entityDataAccessor);
		if (!this.firstTick && this.shakeTime <= 0 && entityDataAccessor.equals(IN_GROUND) && this.isInGround()) {
			this.shakeTime = 7;
		}
	}

	@Override
	public void tick() {
		boolean bl = !this.isNoPhysics();
		Vec3 vec3 = this.getDeltaMovement();
		BlockPos blockPos = this.blockPosition();
		BlockState blockState = this.level().getBlockState(blockPos);
		if (!blockState.isAir() && bl) {
			VoxelShape voxelShape = blockState.getCollisionShape(this.level(), blockPos);
			if (!voxelShape.isEmpty()) {
				Vec3 vec32 = this.position();

				for (AABB aABB : voxelShape.toAabbs()) {
					if (aABB.move(blockPos).contains(vec32)) {
						this.setInGround(true);
						break;
					}
				}
			}
		}

		if (this.shakeTime > 0) {
			this.shakeTime--;
		}

		if (this.isInWaterOrRain() || blockState.is(Blocks.POWDER_SNOW)) {
			this.clearFire();
		}

		if (this.isInGround() && bl) {
			if (!this.level().isClientSide()) {
				if (this.lastState != blockState && this.shouldFall()) {
					this.startFalling();
				} else {
					this.tickDespawn();
				}
			}

			this.inGroundTime++;
			if (this.isAlive()) {
				this.applyEffectsFromBlocks();
			}
		} else {
			this.inGroundTime = 0;
			Vec3 vec33 = this.position();
			if (this.isInWater()) {
				this.addBubbleParticles(vec33);
			}

			if (this.isCritArrow()) {
				for (int i = 0; i < 4; i++) {
					this.level()
						.addParticle(
							ParticleTypes.CRIT,
							vec33.x + vec3.x * (double)i / 4.0,
							vec33.y + vec3.y * (double)i / 4.0,
							vec33.z + vec3.z * (double)i / 4.0,
							-vec3.x,
							-vec3.y + 0.2,
							-vec3.z
						);
				}
			}

			float f;
			if (!bl) {
				f = (float)(Mth.atan2(-vec3.x, -vec3.z) * 180.0F / (float)Math.PI);
			} else {
				f = (float)(Mth.atan2(vec3.x, vec3.z) * 180.0F / (float)Math.PI);
			}

			float g = (float)(Mth.atan2(vec3.y, vec3.horizontalDistance()) * 180.0F / (float)Math.PI);
			this.setXRot(lerpRotation(this.getXRot(), g));
			this.setYRot(lerpRotation(this.getYRot(), f));
			if (bl) {
				BlockHitResult blockHitResult = this.level()
					.clipIncludingBorder(new ClipContext(vec33, vec33.add(vec3), ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, this));
				this.stepMoveAndHit(blockHitResult);
			} else {
				this.setPos(vec33.add(vec3));
				this.applyEffectsFromBlocks();
			}

			this.applyInertia();
			if (bl && !this.isInGround()) {
				this.applyGravity();
			}

			super.tick();
		}
	}

	private void stepMoveAndHit(BlockHitResult blockHitResult) {
		while (this.isAlive()) {
			Vec3 vec3 = this.position();
			EntityHitResult entityHitResult = this.findHitEntity(vec3, blockHitResult.getLocation());
			Vec3 vec32 = ((HitResult)Objects.requireNonNullElse(entityHitResult, blockHitResult)).getLocation();
			this.setPos(vec32);
			this.applyEffectsFromBlocks(vec3, vec32);
			if (this.portalProcess != null && this.portalProcess.isInsidePortalThisTick()) {
				this.handlePortal();
			}

			if (entityHitResult == null) {
				if (this.isAlive() && blockHitResult.getType() != HitResult.Type.MISS) {
					this.hitTargetOrDeflectSelf(blockHitResult);
					this.hasImpulse = true;
				}
				break;
			} else if (this.isAlive() && !this.noPhysics) {
				ProjectileDeflection projectileDeflection = this.hitTargetOrDeflectSelf(entityHitResult);
				this.hasImpulse = true;
				if (this.getPierceLevel() > 0 && projectileDeflection == ProjectileDeflection.NONE) {
					continue;
				}
				break;
			}
		}
	}

	private void applyInertia() {
		Vec3 vec3 = this.getDeltaMovement();
		float f = 0.99F;
		if (this.isInWater()) {
			f = this.getWaterInertia();
		}

		this.setDeltaMovement(vec3.scale((double)f));
	}

	private void addBubbleParticles(Vec3 vec3) {
		Vec3 vec32 = this.getDeltaMovement();

		for (int i = 0; i < 4; i++) {
			float f = 0.25F;
			this.level().addParticle(ParticleTypes.BUBBLE, vec3.x - vec32.x * 0.25, vec3.y - vec32.y * 0.25, vec3.z - vec32.z * 0.25, vec32.x, vec32.y, vec32.z);
		}
	}

	@Override
	protected double getDefaultGravity() {
		return 0.05;
	}

	private boolean shouldFall() {
		return this.isInGround() && this.level().noCollision(new AABB(this.position(), this.position()).inflate(0.06));
	}

	private void startFalling() {
		this.setInGround(false);
		Vec3 vec3 = this.getDeltaMovement();
		this.setDeltaMovement(
			vec3.multiply((double)(this.random.nextFloat() * 0.2F), (double)(this.random.nextFloat() * 0.2F), (double)(this.random.nextFloat() * 0.2F))
		);
		this.life = 0;
	}

	protected boolean isInGround() {
		return this.entityData.get(IN_GROUND);
	}

	protected void setInGround(boolean bl) {
		this.entityData.set(IN_GROUND, bl);
	}

	@Override
	public void move(MoverType moverType, Vec3 vec3) {
		super.move(moverType, vec3);
		if (moverType != MoverType.SELF && this.shouldFall()) {
			this.startFalling();
		}
	}

	protected void tickDespawn() {
		this.life++;
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
	protected void onItemBreak(Item item) {
		this.firedFromWeapon = null;
	}

	@Override
	protected void onHitEntity(EntityHitResult entityHitResult) {
		super.onHitEntity(entityHitResult);
		Entity entity = entityHitResult.getEntity();
		float f = (float)this.getDeltaMovement().length();
		double d = this.baseDamage;
		Entity entity2 = this.getOwner();
		DamageSource damageSource = this.damageSources().arrow(this, (Entity)(entity2 != null ? entity2 : this));
		if (this.getWeaponItem() != null && this.level() instanceof ServerLevel serverLevel) {
			d = (double)EnchantmentHelper.modifyDamage(serverLevel, this.getWeaponItem(), entity, damageSource, (float)d);
		}

		int i = Mth.ceil(Mth.clamp((double)f * d, 0.0, 2.147483647E9));
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

		if (entity2 instanceof LivingEntity livingEntity) {
			livingEntity.setLastHurtMob(entity);
		}

		boolean bl = entity.getType() == EntityType.ENDERMAN;
		int j = entity.getRemainingFireTicks();
		if (this.isOnFire() && !bl) {
			entity.igniteForSeconds(5.0F);
		}

		if (entity.hurtOrSimulate(damageSource, (float)i)) {
			if (bl) {
				return;
			}

			if (entity instanceof LivingEntity livingEntity2) {
				if (!this.level().isClientSide && this.getPierceLevel() <= 0) {
					livingEntity2.setArrowCount(livingEntity2.getArrowCount() + 1);
				}

				this.doKnockback(livingEntity2, damageSource);
				if (this.level() instanceof ServerLevel serverLevel2) {
					EnchantmentHelper.doPostAttackEffectsWithItemSource(serverLevel2, livingEntity2, damageSource, this.getWeaponItem());
				}

				this.doPostHurtEffects(livingEntity2);
				if (livingEntity2 != entity2 && livingEntity2 instanceof Player && entity2 instanceof ServerPlayer && !this.isSilent()) {
					((ServerPlayer)entity2).connection.send(new ClientboundGameEventPacket(ClientboundGameEventPacket.ARROW_HIT_PLAYER, 0.0F));
				}

				if (!entity.isAlive() && this.piercedAndKilledEntities != null) {
					this.piercedAndKilledEntities.add(livingEntity2);
				}

				if (!this.level().isClientSide && entity2 instanceof ServerPlayer serverPlayer) {
					if (this.piercedAndKilledEntities != null) {
						CriteriaTriggers.KILLED_BY_ARROW.trigger(serverPlayer, this.piercedAndKilledEntities, this.firedFromWeapon);
					} else if (!entity.isAlive()) {
						CriteriaTriggers.KILLED_BY_ARROW.trigger(serverPlayer, List.of(entity), this.firedFromWeapon);
					}
				}
			}

			this.playSound(this.soundEvent, 1.0F, 1.2F / (this.random.nextFloat() * 0.2F + 0.9F));
			if (this.getPierceLevel() <= 0) {
				this.discard();
			}
		} else {
			entity.setRemainingFireTicks(j);
			this.deflect(ProjectileDeflection.REVERSE, entity, this.getOwner(), false);
			this.setDeltaMovement(this.getDeltaMovement().scale(0.2));
			if (this.level() instanceof ServerLevel serverLevel3 && this.getDeltaMovement().lengthSqr() < 1.0E-7) {
				if (this.pickup == AbstractArrow.Pickup.ALLOWED) {
					this.spawnAtLocation(serverLevel3, this.getPickupItem(), 0.1F);
				}

				this.discard();
			}
		}
	}

	protected void doKnockback(LivingEntity livingEntity, DamageSource damageSource) {
		double d = (double)(
			this.firedFromWeapon != null && this.level() instanceof ServerLevel serverLevel
				? EnchantmentHelper.modifyKnockback(serverLevel, this.firedFromWeapon, livingEntity, damageSource, 0.0F)
				: 0.0F
		);
		if (d > 0.0) {
			double e = Math.max(0.0, 1.0 - livingEntity.getAttributeValue(Attributes.KNOCKBACK_RESISTANCE));
			Vec3 vec3 = this.getDeltaMovement().multiply(1.0, 0.0, 1.0).normalize().scale(d * 0.6 * e);
			if (vec3.lengthSqr() > 0.0) {
				livingEntity.push(vec3.x, 0.1, vec3.z);
			}
		}
	}

	@Override
	protected void onHitBlock(BlockHitResult blockHitResult) {
		this.lastState = this.level().getBlockState(blockHitResult.getBlockPos());
		super.onHitBlock(blockHitResult);
		ItemStack itemStack = this.getWeaponItem();
		if (this.level() instanceof ServerLevel serverLevel && itemStack != null) {
			this.hitBlockEnchantmentEffects(serverLevel, blockHitResult, itemStack);
		}

		Vec3 vec3 = this.getDeltaMovement();
		Vec3 vec32 = new Vec3(Math.signum(vec3.x), Math.signum(vec3.y), Math.signum(vec3.z));
		Vec3 vec33 = vec32.scale(0.05F);
		this.setPos(this.position().subtract(vec33));
		this.setDeltaMovement(Vec3.ZERO);
		this.playSound(this.getHitGroundSoundEvent(), 1.0F, 1.2F / (this.random.nextFloat() * 0.2F + 0.9F));
		this.setInGround(true);
		this.shakeTime = 7;
		this.setCritArrow(false);
		this.setPierceLevel((byte)0);
		this.setSoundEvent(SoundEvents.ARROW_HIT);
		this.resetPiercedEntities();
	}

	protected void hitBlockEnchantmentEffects(ServerLevel serverLevel, BlockHitResult blockHitResult, ItemStack itemStack) {
		Vec3 vec3 = blockHitResult.getBlockPos().clampLocationWithin(blockHitResult.getLocation());
		EnchantmentHelper.onHitBlock(
			serverLevel,
			itemStack,
			this.getOwner() instanceof LivingEntity livingEntity ? livingEntity : null,
			this,
			null,
			vec3,
			serverLevel.getBlockState(blockHitResult.getBlockPos()),
			item -> this.firedFromWeapon = null
		);
	}

	@Override
	public ItemStack getWeaponItem() {
		return this.firedFromWeapon;
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
		return entity instanceof Player && this.getOwner() instanceof Player player && !player.canHarmPlayer((Player)entity)
			? false
			: super.canHitEntity(entity) && (this.piercingIgnoreEntityIds == null || !this.piercingIgnoreEntityIds.contains(entity.getId()));
	}

	@Override
	public void addAdditionalSaveData(CompoundTag compoundTag) {
		super.addAdditionalSaveData(compoundTag);
		compoundTag.putShort("life", (short)this.life);
		if (this.lastState != null) {
			compoundTag.put("inBlockState", NbtUtils.writeBlockState(this.lastState));
		}

		compoundTag.putByte("shake", (byte)this.shakeTime);
		compoundTag.putBoolean("inGround", this.isInGround());
		compoundTag.putByte("pickup", (byte)this.pickup.ordinal());
		compoundTag.putDouble("damage", this.baseDamage);
		compoundTag.putBoolean("crit", this.isCritArrow());
		compoundTag.putByte("PierceLevel", this.getPierceLevel());
		compoundTag.putString("SoundEvent", BuiltInRegistries.SOUND_EVENT.getKey(this.soundEvent).toString());
		compoundTag.put("item", this.pickupItemStack.save(this.registryAccess()));
		if (this.firedFromWeapon != null) {
			compoundTag.put("weapon", this.firedFromWeapon.save(this.registryAccess(), new CompoundTag()));
		}
	}

	@Override
	public void readAdditionalSaveData(CompoundTag compoundTag) {
		super.readAdditionalSaveData(compoundTag);
		this.life = compoundTag.getShort("life");
		if (compoundTag.contains("inBlockState", 10)) {
			this.lastState = NbtUtils.readBlockState(this.level().holderLookup(Registries.BLOCK), compoundTag.getCompound("inBlockState"));
		}

		this.shakeTime = compoundTag.getByte("shake") & 255;
		this.setInGround(compoundTag.getBoolean("inGround"));
		if (compoundTag.contains("damage", 99)) {
			this.baseDamage = compoundTag.getDouble("damage");
		}

		this.pickup = AbstractArrow.Pickup.byOrdinal(compoundTag.getByte("pickup"));
		this.setCritArrow(compoundTag.getBoolean("crit"));
		this.setPierceLevel(compoundTag.getByte("PierceLevel"));
		if (compoundTag.contains("SoundEvent", 8)) {
			this.soundEvent = (SoundEvent)BuiltInRegistries.SOUND_EVENT
				.getOptional(ResourceLocation.parse(compoundTag.getString("SoundEvent")))
				.orElse(this.getDefaultHitGroundSoundEvent());
		}

		if (compoundTag.contains("item", 10)) {
			this.setPickupItemStack((ItemStack)ItemStack.parse(this.registryAccess(), compoundTag.getCompound("item")).orElse(this.getDefaultPickupItem()));
		} else {
			this.setPickupItemStack(this.getDefaultPickupItem());
		}

		if (compoundTag.contains("weapon", 10)) {
			this.firedFromWeapon = (ItemStack)ItemStack.parse(this.registryAccess(), compoundTag.getCompound("weapon")).orElse(null);
		} else {
			this.firedFromWeapon = null;
		}
	}

	@Override
	public void setOwner(@Nullable Entity entity) {
		super.setOwner(entity);

		this.pickup = switch (entity) {
			case null, default -> this.pickup;
			case Player player when this.pickup == AbstractArrow.Pickup.DISALLOWED -> AbstractArrow.Pickup.ALLOWED;
			case OminousItemSpawner ominousItemSpawner -> AbstractArrow.Pickup.DISALLOWED;
		};
	}

	@Override
	public void playerTouch(Player player) {
		if (!this.level().isClientSide && (this.isInGround() || this.isNoPhysics()) && this.shakeTime <= 0) {
			if (this.tryPickup(player)) {
				player.take(this, 1);
				this.discard();
			}
		}
	}

	protected boolean tryPickup(Player player) {
		return switch (this.pickup) {
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

	@Override
	public boolean isAttackable() {
		return this.getType().is(EntityTypeTags.REDIRECTABLE_PROJECTILE);
	}

	public void setCritArrow(boolean bl) {
		this.setFlag(1, bl);
	}

	private void setPierceLevel(byte b) {
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
		if (!itemStack.isEmpty()) {
			this.pickupItemStack = itemStack;
		} else {
			this.pickupItemStack = this.getDefaultPickupItem();
		}
	}

	public boolean isCritArrow() {
		byte b = this.entityData.get(ID_FLAGS);
		return (b & 1) != 0;
	}

	public byte getPierceLevel() {
		return this.entityData.get(PIERCE_LEVEL);
	}

	public void setBaseDamageFromMob(float f) {
		this.setBaseDamage((double)(f * 2.0F) + this.random.triangle((double)this.level().getDifficulty().getId() * 0.11, 0.57425));
	}

	protected float getWaterInertia() {
		return 0.6F;
	}

	public void setNoPhysics(boolean bl) {
		this.noPhysics = bl;
		this.setFlag(2, bl);
	}

	public boolean isNoPhysics() {
		return !this.level().isClientSide ? this.noPhysics : (this.entityData.get(ID_FLAGS) & 2) != 0;
	}

	@Override
	public boolean isPickable() {
		return super.isPickable() && !this.isInGround();
	}

	@Override
	public SlotAccess getSlot(int i) {
		return i == 0 ? SlotAccess.of(this::getPickupItemStackOrigin, this::setPickupItemStack) : super.getSlot(i);
	}

	@Override
	protected boolean shouldBounceOnWorldBorder() {
		return true;
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
