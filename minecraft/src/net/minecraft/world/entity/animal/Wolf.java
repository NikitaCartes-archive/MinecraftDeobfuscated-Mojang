package net.minecraft.world.entity.animal;

import java.util.UUID;
import java.util.function.Predicate;
import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AgableMob;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ai.goal.AvoidEntityGoal;
import net.minecraft.world.entity.ai.goal.BegGoal;
import net.minecraft.world.entity.ai.goal.BreedGoal;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.FollowOwnerGoal;
import net.minecraft.world.entity.ai.goal.LeapAtTargetGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.SitGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NonTameRandomTargetGoal;
import net.minecraft.world.entity.ai.goal.target.OwnerHurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.OwnerHurtTargetGoal;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.minecraft.world.entity.animal.horse.Llama;
import net.minecraft.world.entity.monster.AbstractSkeleton;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.monster.Ghast;
import net.minecraft.world.entity.monster.SharedMonsterAttributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.DyeItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public class Wolf extends TamableAnimal {
	private static final EntityDataAccessor<Float> DATA_HEALTH_ID = SynchedEntityData.defineId(Wolf.class, EntityDataSerializers.FLOAT);
	private static final EntityDataAccessor<Boolean> DATA_INTERESTED_ID = SynchedEntityData.defineId(Wolf.class, EntityDataSerializers.BOOLEAN);
	private static final EntityDataAccessor<Integer> DATA_COLLAR_COLOR = SynchedEntityData.defineId(Wolf.class, EntityDataSerializers.INT);
	public static final Predicate<LivingEntity> PREY_SELECTOR = livingEntity -> {
		EntityType<?> entityType = livingEntity.getType();
		return entityType == EntityType.SHEEP || entityType == EntityType.RABBIT || entityType == EntityType.FOX;
	};
	private float interestedAngle;
	private float interestedAngleO;
	private boolean isWet;
	private boolean isShaking;
	private float shakeAnim;
	private float shakeAnimO;

	public Wolf(EntityType<? extends Wolf> entityType, Level level) {
		super(entityType, level);
		this.setTame(false);
	}

	@Override
	protected void registerGoals() {
		this.sitGoal = new SitGoal(this);
		this.goalSelector.addGoal(1, new FloatGoal(this));
		this.goalSelector.addGoal(2, this.sitGoal);
		this.goalSelector.addGoal(3, new Wolf.WolfAvoidEntityGoal(this, Llama.class, 24.0F, 1.5, 1.5));
		this.goalSelector.addGoal(4, new LeapAtTargetGoal(this, 0.4F));
		this.goalSelector.addGoal(5, new MeleeAttackGoal(this, 1.0, true));
		this.goalSelector.addGoal(6, new FollowOwnerGoal(this, 1.0, 10.0F, 2.0F));
		this.goalSelector.addGoal(7, new BreedGoal(this, 1.0));
		this.goalSelector.addGoal(8, new WaterAvoidingRandomStrollGoal(this, 1.0));
		this.goalSelector.addGoal(9, new BegGoal(this, 8.0F));
		this.goalSelector.addGoal(10, new LookAtPlayerGoal(this, Player.class, 8.0F));
		this.goalSelector.addGoal(10, new RandomLookAroundGoal(this));
		this.targetSelector.addGoal(1, new OwnerHurtByTargetGoal(this));
		this.targetSelector.addGoal(2, new OwnerHurtTargetGoal(this));
		this.targetSelector.addGoal(3, new HurtByTargetGoal(this).setAlertOthers());
		this.targetSelector.addGoal(4, new NonTameRandomTargetGoal(this, Animal.class, false, PREY_SELECTOR));
		this.targetSelector.addGoal(4, new NonTameRandomTargetGoal(this, Turtle.class, false, Turtle.BABY_ON_LAND_SELECTOR));
		this.targetSelector.addGoal(5, new NearestAttackableTargetGoal(this, AbstractSkeleton.class, false));
	}

	@Override
	protected void registerAttributes() {
		super.registerAttributes();
		this.getAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(0.3F);
		if (this.isTame()) {
			this.getAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(20.0);
		} else {
			this.getAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(8.0);
		}

		this.getAttributes().registerAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).setBaseValue(2.0);
	}

	@Override
	public void setTarget(@Nullable LivingEntity livingEntity) {
		super.setTarget(livingEntity);
		if (livingEntity == null) {
			this.setAngry(false);
		} else if (!this.isTame()) {
			this.setAngry(true);
		}
	}

	@Override
	protected void customServerAiStep() {
		this.entityData.set(DATA_HEALTH_ID, this.getHealth());
	}

	@Override
	protected void defineSynchedData() {
		super.defineSynchedData();
		this.entityData.define(DATA_HEALTH_ID, this.getHealth());
		this.entityData.define(DATA_INTERESTED_ID, false);
		this.entityData.define(DATA_COLLAR_COLOR, DyeColor.RED.getId());
	}

	@Override
	protected void playStepSound(BlockPos blockPos, BlockState blockState) {
		this.playSound(SoundEvents.WOLF_STEP, 0.15F, 1.0F);
	}

	@Override
	public void addAdditionalSaveData(CompoundTag compoundTag) {
		super.addAdditionalSaveData(compoundTag);
		compoundTag.putBoolean("Angry", this.isAngry());
		compoundTag.putByte("CollarColor", (byte)this.getCollarColor().getId());
	}

	@Override
	public void readAdditionalSaveData(CompoundTag compoundTag) {
		super.readAdditionalSaveData(compoundTag);
		this.setAngry(compoundTag.getBoolean("Angry"));
		if (compoundTag.contains("CollarColor", 99)) {
			this.setCollarColor(DyeColor.byId(compoundTag.getInt("CollarColor")));
		}
	}

	@Override
	protected SoundEvent getAmbientSound() {
		if (this.isAngry()) {
			return SoundEvents.WOLF_GROWL;
		} else if (this.random.nextInt(3) == 0) {
			return this.isTame() && this.entityData.get(DATA_HEALTH_ID) < 10.0F ? SoundEvents.WOLF_WHINE : SoundEvents.WOLF_PANT;
		} else {
			return SoundEvents.WOLF_AMBIENT;
		}
	}

	@Override
	protected SoundEvent getHurtSound(DamageSource damageSource) {
		return SoundEvents.WOLF_HURT;
	}

	@Override
	protected SoundEvent getDeathSound() {
		return SoundEvents.WOLF_DEATH;
	}

	@Override
	protected float getSoundVolume() {
		return 0.4F;
	}

	@Override
	public void aiStep() {
		super.aiStep();
		if (!this.level.isClientSide && this.isWet && !this.isShaking && !this.isPathFinding() && this.onGround) {
			this.isShaking = true;
			this.shakeAnim = 0.0F;
			this.shakeAnimO = 0.0F;
			this.level.broadcastEntityEvent(this, (byte)8);
		}

		if (!this.level.isClientSide && this.getTarget() == null && this.isAngry()) {
			this.setAngry(false);
		}
	}

	@Override
	public void tick() {
		super.tick();
		if (this.isAlive()) {
			this.interestedAngleO = this.interestedAngle;
			if (this.isInterested()) {
				this.interestedAngle = this.interestedAngle + (1.0F - this.interestedAngle) * 0.4F;
			} else {
				this.interestedAngle = this.interestedAngle + (0.0F - this.interestedAngle) * 0.4F;
			}

			if (this.isInWaterRainOrBubble()) {
				this.isWet = true;
				this.isShaking = false;
				this.shakeAnim = 0.0F;
				this.shakeAnimO = 0.0F;
			} else if ((this.isWet || this.isShaking) && this.isShaking) {
				if (this.shakeAnim == 0.0F) {
					this.playSound(SoundEvents.WOLF_SHAKE, this.getSoundVolume(), (this.random.nextFloat() - this.random.nextFloat()) * 0.2F + 1.0F);
				}

				this.shakeAnimO = this.shakeAnim;
				this.shakeAnim += 0.05F;
				if (this.shakeAnimO >= 2.0F) {
					this.isWet = false;
					this.isShaking = false;
					this.shakeAnimO = 0.0F;
					this.shakeAnim = 0.0F;
				}

				if (this.shakeAnim > 0.4F) {
					float f = (float)this.getBoundingBox().minY;
					int i = (int)(Mth.sin((this.shakeAnim - 0.4F) * (float) Math.PI) * 7.0F);
					Vec3 vec3 = this.getDeltaMovement();

					for (int j = 0; j < i; j++) {
						float g = (this.random.nextFloat() * 2.0F - 1.0F) * this.getBbWidth() * 0.5F;
						float h = (this.random.nextFloat() * 2.0F - 1.0F) * this.getBbWidth() * 0.5F;
						this.level.addParticle(ParticleTypes.SPLASH, this.x + (double)g, (double)(f + 0.8F), this.z + (double)h, vec3.x, vec3.y, vec3.z);
					}
				}
			}
		}
	}

	@Override
	public void die(DamageSource damageSource) {
		this.isWet = false;
		this.isShaking = false;
		this.shakeAnimO = 0.0F;
		this.shakeAnim = 0.0F;
		super.die(damageSource);
	}

	@Environment(EnvType.CLIENT)
	public boolean isWet() {
		return this.isWet;
	}

	@Environment(EnvType.CLIENT)
	public float getWetShade(float f) {
		return 0.75F + Mth.lerp(f, this.shakeAnimO, this.shakeAnim) / 2.0F * 0.25F;
	}

	@Environment(EnvType.CLIENT)
	public float getBodyRollAngle(float f, float g) {
		float h = (Mth.lerp(f, this.shakeAnimO, this.shakeAnim) + g) / 1.8F;
		if (h < 0.0F) {
			h = 0.0F;
		} else if (h > 1.0F) {
			h = 1.0F;
		}

		return Mth.sin(h * (float) Math.PI) * Mth.sin(h * (float) Math.PI * 11.0F) * 0.15F * (float) Math.PI;
	}

	@Environment(EnvType.CLIENT)
	public float getHeadRollAngle(float f) {
		return Mth.lerp(f, this.interestedAngleO, this.interestedAngle) * 0.15F * (float) Math.PI;
	}

	@Override
	protected float getStandingEyeHeight(Pose pose, EntityDimensions entityDimensions) {
		return entityDimensions.height * 0.8F;
	}

	@Override
	public int getMaxHeadXRot() {
		return this.isSitting() ? 20 : super.getMaxHeadXRot();
	}

	@Override
	public boolean hurt(DamageSource damageSource, float f) {
		if (this.isInvulnerableTo(damageSource)) {
			return false;
		} else {
			Entity entity = damageSource.getEntity();
			if (this.sitGoal != null) {
				this.sitGoal.wantToSit(false);
			}

			if (entity != null && !(entity instanceof Player) && !(entity instanceof AbstractArrow)) {
				f = (f + 1.0F) / 2.0F;
			}

			return super.hurt(damageSource, f);
		}
	}

	@Override
	public boolean doHurtTarget(Entity entity) {
		boolean bl = entity.hurt(DamageSource.mobAttack(this), (float)((int)this.getAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).getValue()));
		if (bl) {
			this.doEnchantDamageEffects(this, entity);
		}

		return bl;
	}

	@Override
	public void setTame(boolean bl) {
		super.setTame(bl);
		if (bl) {
			this.getAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(20.0);
		} else {
			this.getAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(8.0);
		}

		this.getAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).setBaseValue(4.0);
	}

	@Override
	public boolean mobInteract(Player player, InteractionHand interactionHand) {
		ItemStack itemStack = player.getItemInHand(interactionHand);
		Item item = itemStack.getItem();
		if (this.isTame()) {
			if (!itemStack.isEmpty()) {
				if (item.isEdible()) {
					if (item.getFoodProperties().isMeat() && this.entityData.get(DATA_HEALTH_ID) < 20.0F) {
						if (!player.abilities.instabuild) {
							itemStack.shrink(1);
						}

						this.heal((float)item.getFoodProperties().getNutrition());
						return true;
					}
				} else if (item instanceof DyeItem) {
					DyeColor dyeColor = ((DyeItem)item).getDyeColor();
					if (dyeColor != this.getCollarColor()) {
						this.setCollarColor(dyeColor);
						if (!player.abilities.instabuild) {
							itemStack.shrink(1);
						}

						return true;
					}
				}
			}

			if (this.isOwnedBy(player) && !this.level.isClientSide && !this.isFood(itemStack)) {
				this.sitGoal.wantToSit(!this.isSitting());
				this.jumping = false;
				this.navigation.stop();
				this.setTarget(null);
			}
		} else if (item == Items.BONE && !this.isAngry()) {
			if (!player.abilities.instabuild) {
				itemStack.shrink(1);
			}

			if (!this.level.isClientSide) {
				if (this.random.nextInt(3) == 0) {
					this.tame(player);
					this.navigation.stop();
					this.setTarget(null);
					this.sitGoal.wantToSit(true);
					this.setHealth(20.0F);
					this.spawnTamingParticles(true);
					this.level.broadcastEntityEvent(this, (byte)7);
				} else {
					this.spawnTamingParticles(false);
					this.level.broadcastEntityEvent(this, (byte)6);
				}
			}

			return true;
		}

		return super.mobInteract(player, interactionHand);
	}

	@Environment(EnvType.CLIENT)
	@Override
	public void handleEntityEvent(byte b) {
		if (b == 8) {
			this.isShaking = true;
			this.shakeAnim = 0.0F;
			this.shakeAnimO = 0.0F;
		} else {
			super.handleEntityEvent(b);
		}
	}

	@Environment(EnvType.CLIENT)
	public float getTailAngle() {
		if (this.isAngry()) {
			return 1.5393804F;
		} else {
			return this.isTame() ? (0.55F - (this.getMaxHealth() - this.entityData.get(DATA_HEALTH_ID)) * 0.02F) * (float) Math.PI : (float) (Math.PI / 5);
		}
	}

	@Override
	public boolean isFood(ItemStack itemStack) {
		Item item = itemStack.getItem();
		return item.isEdible() && item.getFoodProperties().isMeat();
	}

	@Override
	public int getMaxSpawnClusterSize() {
		return 8;
	}

	public boolean isAngry() {
		return (this.entityData.get(DATA_FLAGS_ID) & 2) != 0;
	}

	public void setAngry(boolean bl) {
		byte b = this.entityData.get(DATA_FLAGS_ID);
		if (bl) {
			this.entityData.set(DATA_FLAGS_ID, (byte)(b | 2));
		} else {
			this.entityData.set(DATA_FLAGS_ID, (byte)(b & -3));
		}
	}

	public DyeColor getCollarColor() {
		return DyeColor.byId(this.entityData.get(DATA_COLLAR_COLOR));
	}

	public void setCollarColor(DyeColor dyeColor) {
		this.entityData.set(DATA_COLLAR_COLOR, dyeColor.getId());
	}

	public Wolf getBreedOffspring(AgableMob agableMob) {
		Wolf wolf = EntityType.WOLF.create(this.level);
		UUID uUID = this.getOwnerUUID();
		if (uUID != null) {
			wolf.setOwnerUUID(uUID);
			wolf.setTame(true);
		}

		return wolf;
	}

	public void setIsInterested(boolean bl) {
		this.entityData.set(DATA_INTERESTED_ID, bl);
	}

	@Override
	public boolean canMate(Animal animal) {
		if (animal == this) {
			return false;
		} else if (!this.isTame()) {
			return false;
		} else if (!(animal instanceof Wolf)) {
			return false;
		} else {
			Wolf wolf = (Wolf)animal;
			if (!wolf.isTame()) {
				return false;
			} else {
				return wolf.isSitting() ? false : this.isInLove() && wolf.isInLove();
			}
		}
	}

	public boolean isInterested() {
		return this.entityData.get(DATA_INTERESTED_ID);
	}

	@Override
	public boolean wantsToAttack(LivingEntity livingEntity, LivingEntity livingEntity2) {
		if (!(livingEntity instanceof Creeper) && !(livingEntity instanceof Ghast)) {
			if (livingEntity instanceof Wolf) {
				Wolf wolf = (Wolf)livingEntity;
				if (wolf.isTame() && wolf.getOwner() == livingEntity2) {
					return false;
				}
			}

			if (livingEntity instanceof Player && livingEntity2 instanceof Player && !((Player)livingEntity2).canHarmPlayer((Player)livingEntity)) {
				return false;
			} else {
				return livingEntity instanceof AbstractHorse && ((AbstractHorse)livingEntity).isTamed()
					? false
					: !(livingEntity instanceof Cat) || !((Cat)livingEntity).isTame();
			}
		} else {
			return false;
		}
	}

	@Override
	public boolean canBeLeashed(Player player) {
		return !this.isAngry() && super.canBeLeashed(player);
	}

	class WolfAvoidEntityGoal<T extends LivingEntity> extends AvoidEntityGoal<T> {
		private final Wolf wolf;

		public WolfAvoidEntityGoal(Wolf wolf2, Class<T> class_, float f, double d, double e) {
			super(wolf2, class_, f, d, e);
			this.wolf = wolf2;
		}

		@Override
		public boolean canUse() {
			return super.canUse() && this.toAvoid instanceof Llama ? !this.wolf.isTame() && this.avoidLlama((Llama)this.toAvoid) : false;
		}

		private boolean avoidLlama(Llama llama) {
			return llama.getStrength() >= Wolf.this.random.nextInt(5);
		}

		@Override
		public void start() {
			Wolf.this.setTarget(null);
			super.start();
		}

		@Override
		public void tick() {
			Wolf.this.setTarget(null);
			super.tick();
		}
	}
}
