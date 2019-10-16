package net.minecraft.world.entity.animal.horse;

import java.util.Optional;
import java.util.UUID;
import java.util.function.Predicate;
import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.OldUsersConverter;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerListener;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.AgableMob;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.MobType;
import net.minecraft.world.entity.PlayerRideableJumping;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.RangedAttribute;
import net.minecraft.world.entity.ai.goal.BreedGoal;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.FollowParentGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.PanicGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.RunAroundLikeCrazyGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.monster.SharedMonsterAttributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public abstract class AbstractHorse extends Animal implements ContainerListener, PlayerRideableJumping {
	private static final Predicate<LivingEntity> PARENT_HORSE_SELECTOR = livingEntity -> livingEntity instanceof AbstractHorse
			&& ((AbstractHorse)livingEntity).isBred();
	private static final TargetingConditions MOMMY_TARGETING = new TargetingConditions()
		.range(16.0)
		.allowInvulnerable()
		.allowSameTeam()
		.allowUnseeable()
		.selector(PARENT_HORSE_SELECTOR);
	protected static final Attribute JUMP_STRENGTH = new RangedAttribute(null, "horse.jumpStrength", 0.7, 0.0, 2.0)
		.importLegacyName("Jump Strength")
		.setSyncable(true);
	private static final EntityDataAccessor<Byte> DATA_ID_FLAGS = SynchedEntityData.defineId(AbstractHorse.class, EntityDataSerializers.BYTE);
	private static final EntityDataAccessor<Optional<UUID>> DATA_ID_OWNER_UUID = SynchedEntityData.defineId(
		AbstractHorse.class, EntityDataSerializers.OPTIONAL_UUID
	);
	private int eatingCounter;
	private int mouthCounter;
	private int standCounter;
	public int tailCounter;
	public int sprintCounter;
	protected boolean isJumping;
	protected SimpleContainer inventory;
	protected int temper;
	protected float playerJumpPendingScale;
	private boolean allowStandSliding;
	private float eatAnim;
	private float eatAnimO;
	private float standAnim;
	private float standAnimO;
	private float mouthAnim;
	private float mouthAnimO;
	protected boolean canGallop = true;
	protected int gallopSoundCounter;

	protected AbstractHorse(EntityType<? extends AbstractHorse> entityType, Level level) {
		super(entityType, level);
		this.maxUpStep = 1.0F;
		this.createInventory();
	}

	@Override
	protected void registerGoals() {
		this.goalSelector.addGoal(1, new PanicGoal(this, 1.2));
		this.goalSelector.addGoal(1, new RunAroundLikeCrazyGoal(this, 1.2));
		this.goalSelector.addGoal(2, new BreedGoal(this, 1.0, AbstractHorse.class));
		this.goalSelector.addGoal(4, new FollowParentGoal(this, 1.0));
		this.goalSelector.addGoal(6, new WaterAvoidingRandomStrollGoal(this, 0.7));
		this.goalSelector.addGoal(7, new LookAtPlayerGoal(this, Player.class, 6.0F));
		this.goalSelector.addGoal(8, new RandomLookAroundGoal(this));
		this.addBehaviourGoals();
	}

	protected void addBehaviourGoals() {
		this.goalSelector.addGoal(0, new FloatGoal(this));
	}

	@Override
	protected void defineSynchedData() {
		super.defineSynchedData();
		this.entityData.define(DATA_ID_FLAGS, (byte)0);
		this.entityData.define(DATA_ID_OWNER_UUID, Optional.empty());
	}

	protected boolean getFlag(int i) {
		return (this.entityData.get(DATA_ID_FLAGS) & i) != 0;
	}

	protected void setFlag(int i, boolean bl) {
		byte b = this.entityData.get(DATA_ID_FLAGS);
		if (bl) {
			this.entityData.set(DATA_ID_FLAGS, (byte)(b | i));
		} else {
			this.entityData.set(DATA_ID_FLAGS, (byte)(b & ~i));
		}
	}

	public boolean isTamed() {
		return this.getFlag(2);
	}

	@Nullable
	public UUID getOwnerUUID() {
		return (UUID)this.entityData.get(DATA_ID_OWNER_UUID).orElse(null);
	}

	public void setOwnerUUID(@Nullable UUID uUID) {
		this.entityData.set(DATA_ID_OWNER_UUID, Optional.ofNullable(uUID));
	}

	public boolean isJumping() {
		return this.isJumping;
	}

	public void setTamed(boolean bl) {
		this.setFlag(2, bl);
	}

	public void setIsJumping(boolean bl) {
		this.isJumping = bl;
	}

	@Override
	public boolean canBeLeashed(Player player) {
		return super.canBeLeashed(player) && this.getMobType() != MobType.UNDEAD;
	}

	@Override
	protected void onLeashDistance(float f) {
		if (f > 6.0F && this.isEating()) {
			this.setEating(false);
		}
	}

	public boolean isEating() {
		return this.getFlag(16);
	}

	public boolean isStanding() {
		return this.getFlag(32);
	}

	public boolean isBred() {
		return this.getFlag(8);
	}

	public void setBred(boolean bl) {
		this.setFlag(8, bl);
	}

	public void setSaddled(boolean bl) {
		this.setFlag(4, bl);
	}

	public int getTemper() {
		return this.temper;
	}

	public void setTemper(int i) {
		this.temper = i;
	}

	public int modifyTemper(int i) {
		int j = Mth.clamp(this.getTemper() + i, 0, this.getMaxTemper());
		this.setTemper(j);
		return j;
	}

	@Override
	public boolean hurt(DamageSource damageSource, float f) {
		Entity entity = damageSource.getEntity();
		return this.isVehicle() && entity != null && this.hasIndirectPassenger(entity) ? false : super.hurt(damageSource, f);
	}

	@Override
	public boolean isPushable() {
		return !this.isVehicle();
	}

	private void eating() {
		this.openMouth();
		if (!this.isSilent()) {
			this.level
				.playSound(
					null,
					this.getX(),
					this.getY(),
					this.getZ(),
					SoundEvents.HORSE_EAT,
					this.getSoundSource(),
					1.0F,
					1.0F + (this.random.nextFloat() - this.random.nextFloat()) * 0.2F
				);
		}
	}

	@Override
	public boolean causeFallDamage(float f, float g) {
		if (f > 1.0F) {
			this.playSound(SoundEvents.HORSE_LAND, 0.4F, 1.0F);
		}

		int i = this.calculateFallDamage(f, g);
		if (i <= 0) {
			return false;
		} else {
			this.hurt(DamageSource.FALL, (float)i);
			if (this.isVehicle()) {
				for (Entity entity : this.getIndirectPassengers()) {
					entity.hurt(DamageSource.FALL, (float)i);
				}
			}

			this.playBlockFallSound();
			return true;
		}
	}

	@Override
	protected int calculateFallDamage(float f, float g) {
		return Mth.ceil((f * 0.5F - 3.0F) * g);
	}

	protected int getInventorySize() {
		return 2;
	}

	protected void createInventory() {
		SimpleContainer simpleContainer = this.inventory;
		this.inventory = new SimpleContainer(this.getInventorySize());
		if (simpleContainer != null) {
			simpleContainer.removeListener(this);
			int i = Math.min(simpleContainer.getContainerSize(), this.inventory.getContainerSize());

			for (int j = 0; j < i; j++) {
				ItemStack itemStack = simpleContainer.getItem(j);
				if (!itemStack.isEmpty()) {
					this.inventory.setItem(j, itemStack.copy());
				}
			}
		}

		this.inventory.addListener(this);
		this.updateEquipment();
	}

	protected void updateEquipment() {
		if (!this.level.isClientSide) {
			this.setSaddled(!this.inventory.getItem(0).isEmpty() && this.canBeSaddled());
		}
	}

	@Override
	public void containerChanged(Container container) {
		boolean bl = this.isSaddled();
		this.updateEquipment();
		if (this.tickCount > 20 && !bl && this.isSaddled()) {
			this.playSound(SoundEvents.HORSE_SADDLE, 0.5F, 1.0F);
		}
	}

	public double getCustomJump() {
		return this.getAttribute(JUMP_STRENGTH).getValue();
	}

	@Nullable
	@Override
	protected SoundEvent getDeathSound() {
		return null;
	}

	@Nullable
	@Override
	protected SoundEvent getHurtSound(DamageSource damageSource) {
		if (this.random.nextInt(3) == 0) {
			this.stand();
		}

		return null;
	}

	@Nullable
	@Override
	protected SoundEvent getAmbientSound() {
		if (this.random.nextInt(10) == 0 && !this.isImmobile()) {
			this.stand();
		}

		return null;
	}

	public boolean canBeSaddled() {
		return true;
	}

	public boolean isSaddled() {
		return this.getFlag(4);
	}

	@Nullable
	protected SoundEvent getAngrySound() {
		this.stand();
		return null;
	}

	@Override
	protected void playStepSound(BlockPos blockPos, BlockState blockState) {
		if (!blockState.getMaterial().isLiquid()) {
			BlockState blockState2 = this.level.getBlockState(blockPos.above());
			SoundType soundType = blockState.getSoundType();
			if (blockState2.getBlock() == Blocks.SNOW) {
				soundType = blockState2.getSoundType();
			}

			if (this.isVehicle() && this.canGallop) {
				this.gallopSoundCounter++;
				if (this.gallopSoundCounter > 5 && this.gallopSoundCounter % 3 == 0) {
					this.playGallopSound(soundType);
				} else if (this.gallopSoundCounter <= 5) {
					this.playSound(SoundEvents.HORSE_STEP_WOOD, soundType.getVolume() * 0.15F, soundType.getPitch());
				}
			} else if (soundType == SoundType.WOOD) {
				this.playSound(SoundEvents.HORSE_STEP_WOOD, soundType.getVolume() * 0.15F, soundType.getPitch());
			} else {
				this.playSound(SoundEvents.HORSE_STEP, soundType.getVolume() * 0.15F, soundType.getPitch());
			}
		}
	}

	protected void playGallopSound(SoundType soundType) {
		this.playSound(SoundEvents.HORSE_GALLOP, soundType.getVolume() * 0.15F, soundType.getPitch());
	}

	@Override
	protected void registerAttributes() {
		super.registerAttributes();
		this.getAttributes().registerAttribute(JUMP_STRENGTH);
		this.getAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(53.0);
		this.getAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(0.225F);
	}

	@Override
	public int getMaxSpawnClusterSize() {
		return 6;
	}

	public int getMaxTemper() {
		return 100;
	}

	@Override
	protected float getSoundVolume() {
		return 0.8F;
	}

	@Override
	public int getAmbientSoundInterval() {
		return 400;
	}

	public void openInventory(Player player) {
		if (!this.level.isClientSide && (!this.isVehicle() || this.hasPassenger(player)) && this.isTamed()) {
			player.openHorseInventory(this, this.inventory);
		}
	}

	protected boolean handleEating(Player player, ItemStack itemStack) {
		boolean bl = false;
		float f = 0.0F;
		int i = 0;
		int j = 0;
		Item item = itemStack.getItem();
		if (item == Items.WHEAT) {
			f = 2.0F;
			i = 20;
			j = 3;
		} else if (item == Items.SUGAR) {
			f = 1.0F;
			i = 30;
			j = 3;
		} else if (item == Blocks.HAY_BLOCK.asItem()) {
			f = 20.0F;
			i = 180;
		} else if (item == Items.APPLE) {
			f = 3.0F;
			i = 60;
			j = 3;
		} else if (item == Items.GOLDEN_CARROT) {
			f = 4.0F;
			i = 60;
			j = 5;
			if (this.isTamed() && this.getAge() == 0 && !this.isInLove()) {
				bl = true;
				this.setInLove(player);
			}
		} else if (item == Items.GOLDEN_APPLE || item == Items.ENCHANTED_GOLDEN_APPLE) {
			f = 10.0F;
			i = 240;
			j = 10;
			if (this.isTamed() && this.getAge() == 0 && !this.isInLove()) {
				bl = true;
				this.setInLove(player);
			}
		}

		if (this.getHealth() < this.getMaxHealth() && f > 0.0F) {
			this.heal(f);
			bl = true;
		}

		if (this.isBaby() && i > 0) {
			this.level.addParticle(ParticleTypes.HAPPY_VILLAGER, this.getRandomX(1.0), this.getRandomY() + 0.5, this.getRandomZ(1.0), 0.0, 0.0, 0.0);
			if (!this.level.isClientSide) {
				this.ageUp(i);
			}

			bl = true;
		}

		if (j > 0 && (bl || !this.isTamed()) && this.getTemper() < this.getMaxTemper()) {
			bl = true;
			if (!this.level.isClientSide) {
				this.modifyTemper(j);
			}
		}

		if (bl) {
			this.eating();
		}

		return bl;
	}

	protected void doPlayerRide(Player player) {
		this.setEating(false);
		this.setStanding(false);
		if (!this.level.isClientSide) {
			player.yRot = this.yRot;
			player.xRot = this.xRot;
			player.startRiding(this);
		}
	}

	@Override
	protected boolean isImmobile() {
		return super.isImmobile() && this.isVehicle() && this.isSaddled() || this.isEating() || this.isStanding();
	}

	@Override
	public boolean isFood(ItemStack itemStack) {
		return false;
	}

	private void moveTail() {
		this.tailCounter = 1;
	}

	@Override
	protected void dropEquipment() {
		super.dropEquipment();
		if (this.inventory != null) {
			for (int i = 0; i < this.inventory.getContainerSize(); i++) {
				ItemStack itemStack = this.inventory.getItem(i);
				if (!itemStack.isEmpty() && !EnchantmentHelper.hasVanishingCurse(itemStack)) {
					this.spawnAtLocation(itemStack);
				}
			}
		}
	}

	@Override
	public void aiStep() {
		if (this.random.nextInt(200) == 0) {
			this.moveTail();
		}

		super.aiStep();
		if (!this.level.isClientSide && this.isAlive()) {
			if (this.random.nextInt(900) == 0 && this.deathTime == 0) {
				this.heal(1.0F);
			}

			if (this.canEatGrass()) {
				if (!this.isEating()
					&& !this.isVehicle()
					&& this.random.nextInt(300) == 0
					&& this.level.getBlockState(new BlockPos(this).below()).getBlock() == Blocks.GRASS_BLOCK) {
					this.setEating(true);
				}

				if (this.isEating() && ++this.eatingCounter > 50) {
					this.eatingCounter = 0;
					this.setEating(false);
				}
			}

			this.followMommy();
		}
	}

	protected void followMommy() {
		if (this.isBred() && this.isBaby() && !this.isEating()) {
			LivingEntity livingEntity = this.level
				.getNearestEntity(AbstractHorse.class, MOMMY_TARGETING, this, this.getX(), this.getY(), this.getZ(), this.getBoundingBox().inflate(16.0));
			if (livingEntity != null && this.distanceToSqr(livingEntity) > 4.0) {
				this.navigation.createPath(livingEntity, 0);
			}
		}
	}

	public boolean canEatGrass() {
		return true;
	}

	@Override
	public void tick() {
		super.tick();
		if (this.mouthCounter > 0 && ++this.mouthCounter > 30) {
			this.mouthCounter = 0;
			this.setFlag(64, false);
		}

		if ((this.isControlledByLocalInstance() || this.isEffectiveAi()) && this.standCounter > 0 && ++this.standCounter > 20) {
			this.standCounter = 0;
			this.setStanding(false);
		}

		if (this.tailCounter > 0 && ++this.tailCounter > 8) {
			this.tailCounter = 0;
		}

		if (this.sprintCounter > 0) {
			this.sprintCounter++;
			if (this.sprintCounter > 300) {
				this.sprintCounter = 0;
			}
		}

		this.eatAnimO = this.eatAnim;
		if (this.isEating()) {
			this.eatAnim = this.eatAnim + (1.0F - this.eatAnim) * 0.4F + 0.05F;
			if (this.eatAnim > 1.0F) {
				this.eatAnim = 1.0F;
			}
		} else {
			this.eatAnim = this.eatAnim + ((0.0F - this.eatAnim) * 0.4F - 0.05F);
			if (this.eatAnim < 0.0F) {
				this.eatAnim = 0.0F;
			}
		}

		this.standAnimO = this.standAnim;
		if (this.isStanding()) {
			this.eatAnim = 0.0F;
			this.eatAnimO = this.eatAnim;
			this.standAnim = this.standAnim + (1.0F - this.standAnim) * 0.4F + 0.05F;
			if (this.standAnim > 1.0F) {
				this.standAnim = 1.0F;
			}
		} else {
			this.allowStandSliding = false;
			this.standAnim = this.standAnim + ((0.8F * this.standAnim * this.standAnim * this.standAnim - this.standAnim) * 0.6F - 0.05F);
			if (this.standAnim < 0.0F) {
				this.standAnim = 0.0F;
			}
		}

		this.mouthAnimO = this.mouthAnim;
		if (this.getFlag(64)) {
			this.mouthAnim = this.mouthAnim + (1.0F - this.mouthAnim) * 0.7F + 0.05F;
			if (this.mouthAnim > 1.0F) {
				this.mouthAnim = 1.0F;
			}
		} else {
			this.mouthAnim = this.mouthAnim + ((0.0F - this.mouthAnim) * 0.7F - 0.05F);
			if (this.mouthAnim < 0.0F) {
				this.mouthAnim = 0.0F;
			}
		}
	}

	private void openMouth() {
		if (!this.level.isClientSide) {
			this.mouthCounter = 1;
			this.setFlag(64, true);
		}
	}

	public void setEating(boolean bl) {
		this.setFlag(16, bl);
	}

	public void setStanding(boolean bl) {
		if (bl) {
			this.setEating(false);
		}

		this.setFlag(32, bl);
	}

	private void stand() {
		if (this.isControlledByLocalInstance() || this.isEffectiveAi()) {
			this.standCounter = 1;
			this.setStanding(true);
		}
	}

	public void makeMad() {
		this.stand();
		SoundEvent soundEvent = this.getAngrySound();
		if (soundEvent != null) {
			this.playSound(soundEvent, this.getSoundVolume(), this.getVoicePitch());
		}
	}

	public boolean tameWithName(Player player) {
		this.setOwnerUUID(player.getUUID());
		this.setTamed(true);
		if (player instanceof ServerPlayer) {
			CriteriaTriggers.TAME_ANIMAL.trigger((ServerPlayer)player, this);
		}

		this.level.broadcastEntityEvent(this, (byte)7);
		return true;
	}

	@Override
	public void travel(Vec3 vec3) {
		if (this.isAlive()) {
			if (this.isVehicle() && this.canBeControlledByRider() && this.isSaddled()) {
				LivingEntity livingEntity = (LivingEntity)this.getControllingPassenger();
				this.yRot = livingEntity.yRot;
				this.yRotO = this.yRot;
				this.xRot = livingEntity.xRot * 0.5F;
				this.setRot(this.yRot, this.xRot);
				this.yBodyRot = this.yRot;
				this.yHeadRot = this.yBodyRot;
				float f = livingEntity.xxa * 0.5F;
				float g = livingEntity.zza;
				if (g <= 0.0F) {
					g *= 0.25F;
					this.gallopSoundCounter = 0;
				}

				if (this.onGround && this.playerJumpPendingScale == 0.0F && this.isStanding() && !this.allowStandSliding) {
					f = 0.0F;
					g = 0.0F;
				}

				if (this.playerJumpPendingScale > 0.0F && !this.isJumping() && this.onGround) {
					double d = this.getCustomJump() * (double)this.playerJumpPendingScale * (double)this.getBlockJumpFactor();
					double e;
					if (this.hasEffect(MobEffects.JUMP)) {
						e = d + (double)((float)(this.getEffect(MobEffects.JUMP).getAmplifier() + 1) * 0.1F);
					} else {
						e = d;
					}

					Vec3 vec32 = this.getDeltaMovement();
					this.setDeltaMovement(vec32.x, e, vec32.z);
					this.setIsJumping(true);
					this.hasImpulse = true;
					if (g > 0.0F) {
						float h = Mth.sin(this.yRot * (float) (Math.PI / 180.0));
						float i = Mth.cos(this.yRot * (float) (Math.PI / 180.0));
						this.setDeltaMovement(
							this.getDeltaMovement().add((double)(-0.4F * h * this.playerJumpPendingScale), 0.0, (double)(0.4F * i * this.playerJumpPendingScale))
						);
						this.playJumpSound();
					}

					this.playerJumpPendingScale = 0.0F;
				}

				this.flyingSpeed = this.getSpeed() * 0.1F;
				if (this.isControlledByLocalInstance()) {
					this.setSpeed((float)this.getAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).getValue());
					super.travel(new Vec3((double)f, vec3.y, (double)g));
				} else if (livingEntity instanceof Player) {
					this.setDeltaMovement(Vec3.ZERO);
				}

				if (this.onGround) {
					this.playerJumpPendingScale = 0.0F;
					this.setIsJumping(false);
				}

				this.animationSpeedOld = this.animationSpeed;
				double dx = this.getX() - this.xo;
				double ex = this.getZ() - this.zo;
				float j = Mth.sqrt(dx * dx + ex * ex) * 4.0F;
				if (j > 1.0F) {
					j = 1.0F;
				}

				this.animationSpeed = this.animationSpeed + (j - this.animationSpeed) * 0.4F;
				this.animationPosition = this.animationPosition + this.animationSpeed;
			} else {
				this.flyingSpeed = 0.02F;
				super.travel(vec3);
			}
		}
	}

	protected void playJumpSound() {
		this.playSound(SoundEvents.HORSE_JUMP, 0.4F, 1.0F);
	}

	@Override
	public void addAdditionalSaveData(CompoundTag compoundTag) {
		super.addAdditionalSaveData(compoundTag);
		compoundTag.putBoolean("EatingHaystack", this.isEating());
		compoundTag.putBoolean("Bred", this.isBred());
		compoundTag.putInt("Temper", this.getTemper());
		compoundTag.putBoolean("Tame", this.isTamed());
		if (this.getOwnerUUID() != null) {
			compoundTag.putString("OwnerUUID", this.getOwnerUUID().toString());
		}

		if (!this.inventory.getItem(0).isEmpty()) {
			compoundTag.put("SaddleItem", this.inventory.getItem(0).save(new CompoundTag()));
		}
	}

	@Override
	public void readAdditionalSaveData(CompoundTag compoundTag) {
		super.readAdditionalSaveData(compoundTag);
		this.setEating(compoundTag.getBoolean("EatingHaystack"));
		this.setBred(compoundTag.getBoolean("Bred"));
		this.setTemper(compoundTag.getInt("Temper"));
		this.setTamed(compoundTag.getBoolean("Tame"));
		String string;
		if (compoundTag.contains("OwnerUUID", 8)) {
			string = compoundTag.getString("OwnerUUID");
		} else {
			String string2 = compoundTag.getString("Owner");
			string = OldUsersConverter.convertMobOwnerIfNecessary(this.getServer(), string2);
		}

		if (!string.isEmpty()) {
			this.setOwnerUUID(UUID.fromString(string));
		}

		AttributeInstance attributeInstance = this.getAttributes().getInstance("Speed");
		if (attributeInstance != null) {
			this.getAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(attributeInstance.getBaseValue() * 0.25);
		}

		if (compoundTag.contains("SaddleItem", 10)) {
			ItemStack itemStack = ItemStack.of(compoundTag.getCompound("SaddleItem"));
			if (itemStack.getItem() == Items.SADDLE) {
				this.inventory.setItem(0, itemStack);
			}
		}

		this.updateEquipment();
	}

	@Override
	public boolean canMate(Animal animal) {
		return false;
	}

	protected boolean canParent() {
		return !this.isVehicle() && !this.isPassenger() && this.isTamed() && !this.isBaby() && this.getHealth() >= this.getMaxHealth() && this.isInLove();
	}

	@Nullable
	@Override
	public AgableMob getBreedOffspring(AgableMob agableMob) {
		return null;
	}

	protected void setOffspringAttributes(AgableMob agableMob, AbstractHorse abstractHorse) {
		double d = this.getAttribute(SharedMonsterAttributes.MAX_HEALTH).getBaseValue()
			+ agableMob.getAttribute(SharedMonsterAttributes.MAX_HEALTH).getBaseValue()
			+ (double)this.generateRandomMaxHealth();
		abstractHorse.getAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(d / 3.0);
		double e = this.getAttribute(JUMP_STRENGTH).getBaseValue() + agableMob.getAttribute(JUMP_STRENGTH).getBaseValue() + this.generateRandomJumpStrength();
		abstractHorse.getAttribute(JUMP_STRENGTH).setBaseValue(e / 3.0);
		double f = this.getAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).getBaseValue()
			+ agableMob.getAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).getBaseValue()
			+ this.generateRandomSpeed();
		abstractHorse.getAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(f / 3.0);
	}

	@Override
	public boolean canBeControlledByRider() {
		return this.getControllingPassenger() instanceof LivingEntity;
	}

	@Environment(EnvType.CLIENT)
	public float getEatAnim(float f) {
		return Mth.lerp(f, this.eatAnimO, this.eatAnim);
	}

	@Environment(EnvType.CLIENT)
	public float getStandAnim(float f) {
		return Mth.lerp(f, this.standAnimO, this.standAnim);
	}

	@Environment(EnvType.CLIENT)
	public float getMouthAnim(float f) {
		return Mth.lerp(f, this.mouthAnimO, this.mouthAnim);
	}

	@Environment(EnvType.CLIENT)
	@Override
	public void onPlayerJump(int i) {
		if (this.isSaddled()) {
			if (i < 0) {
				i = 0;
			} else {
				this.allowStandSliding = true;
				this.stand();
			}

			if (i >= 90) {
				this.playerJumpPendingScale = 1.0F;
			} else {
				this.playerJumpPendingScale = 0.4F + 0.4F * (float)i / 90.0F;
			}
		}
	}

	@Override
	public boolean canJump() {
		return this.isSaddled();
	}

	@Override
	public void handleStartJump(int i) {
		this.allowStandSliding = true;
		this.stand();
	}

	@Override
	public void handleStopJump() {
	}

	@Environment(EnvType.CLIENT)
	protected void spawnTamingParticles(boolean bl) {
		ParticleOptions particleOptions = bl ? ParticleTypes.HEART : ParticleTypes.SMOKE;

		for (int i = 0; i < 7; i++) {
			double d = this.random.nextGaussian() * 0.02;
			double e = this.random.nextGaussian() * 0.02;
			double f = this.random.nextGaussian() * 0.02;
			this.level.addParticle(particleOptions, this.getRandomX(1.0), this.getRandomY() + 0.5, this.getRandomZ(1.0), d, e, f);
		}
	}

	@Environment(EnvType.CLIENT)
	@Override
	public void handleEntityEvent(byte b) {
		if (b == 7) {
			this.spawnTamingParticles(true);
		} else if (b == 6) {
			this.spawnTamingParticles(false);
		} else {
			super.handleEntityEvent(b);
		}
	}

	@Override
	public void positionRider(Entity entity) {
		super.positionRider(entity);
		if (entity instanceof Mob) {
			Mob mob = (Mob)entity;
			this.yBodyRot = mob.yBodyRot;
		}

		if (this.standAnimO > 0.0F) {
			float f = Mth.sin(this.yBodyRot * (float) (Math.PI / 180.0));
			float g = Mth.cos(this.yBodyRot * (float) (Math.PI / 180.0));
			float h = 0.7F * this.standAnimO;
			float i = 0.15F * this.standAnimO;
			entity.setPos(this.getX() + (double)(h * f), this.getY() + this.getRideHeight() + entity.getRidingHeight() + (double)i, this.getZ() - (double)(h * g));
			if (entity instanceof LivingEntity) {
				((LivingEntity)entity).yBodyRot = this.yBodyRot;
			}
		}
	}

	protected float generateRandomMaxHealth() {
		return 15.0F + (float)this.random.nextInt(8) + (float)this.random.nextInt(9);
	}

	protected double generateRandomJumpStrength() {
		return 0.4F + this.random.nextDouble() * 0.2 + this.random.nextDouble() * 0.2 + this.random.nextDouble() * 0.2;
	}

	protected double generateRandomSpeed() {
		return (0.45F + this.random.nextDouble() * 0.3 + this.random.nextDouble() * 0.3 + this.random.nextDouble() * 0.3) * 0.25;
	}

	@Override
	public boolean onLadder() {
		return false;
	}

	@Override
	protected float getStandingEyeHeight(Pose pose, EntityDimensions entityDimensions) {
		return entityDimensions.height * 0.95F;
	}

	public boolean wearsArmor() {
		return false;
	}

	public boolean isArmor(ItemStack itemStack) {
		return false;
	}

	@Override
	public boolean setSlot(int i, ItemStack itemStack) {
		int j = i - 400;
		if (j >= 0 && j < 2 && j < this.inventory.getContainerSize()) {
			if (j == 0 && itemStack.getItem() != Items.SADDLE) {
				return false;
			} else if (j != 1 || this.wearsArmor() && this.isArmor(itemStack)) {
				this.inventory.setItem(j, itemStack);
				this.updateEquipment();
				return true;
			} else {
				return false;
			}
		} else {
			int k = i - 500 + 2;
			if (k >= 2 && k < this.inventory.getContainerSize()) {
				this.inventory.setItem(k, itemStack);
				return true;
			} else {
				return false;
			}
		}
	}

	@Nullable
	@Override
	public Entity getControllingPassenger() {
		return this.getPassengers().isEmpty() ? null : (Entity)this.getPassengers().get(0);
	}

	@Nullable
	@Override
	public SpawnGroupData finalizeSpawn(
		LevelAccessor levelAccessor,
		DifficultyInstance difficultyInstance,
		MobSpawnType mobSpawnType,
		@Nullable SpawnGroupData spawnGroupData,
		@Nullable CompoundTag compoundTag
	) {
		if (spawnGroupData == null) {
			spawnGroupData = new AgableMob.AgableMobGroupData();
			((AgableMob.AgableMobGroupData)spawnGroupData).setBabySpawnChance(0.2F);
		}

		return super.finalizeSpawn(levelAccessor, difficultyInstance, mobSpawnType, spawnGroupData, compoundTag);
	}
}
