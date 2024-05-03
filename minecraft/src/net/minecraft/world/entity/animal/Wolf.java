package net.minecraft.world.entity.animal;

import java.util.Optional;
import java.util.UUID;
import java.util.function.Predicate;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.util.TimeUtil;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.Crackiness;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.NeutralMob;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.VariantHolder;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.AvoidEntityGoal;
import net.minecraft.world.entity.ai.goal.BegGoal;
import net.minecraft.world.entity.ai.goal.BreedGoal;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.FollowOwnerGoal;
import net.minecraft.world.entity.ai.goal.LeapAtTargetGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.PanicGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.SitWhenOrderedToGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NonTameRandomTargetGoal;
import net.minecraft.world.entity.ai.goal.target.OwnerHurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.OwnerHurtTargetGoal;
import net.minecraft.world.entity.ai.goal.target.ResetUniversalAngerTargetGoal;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.minecraft.world.entity.animal.horse.Llama;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.monster.AbstractSkeleton;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.monster.Ghast;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.ArmorMaterials;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.DyeItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.enchantment.EnchantmentEffectComponents;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.pathfinder.PathType;
import net.minecraft.world.phys.Vec3;

public class Wolf extends TamableAnimal implements NeutralMob, VariantHolder<Holder<WolfVariant>> {
	private static final EntityDataAccessor<Boolean> DATA_INTERESTED_ID = SynchedEntityData.defineId(Wolf.class, EntityDataSerializers.BOOLEAN);
	private static final EntityDataAccessor<Integer> DATA_COLLAR_COLOR = SynchedEntityData.defineId(Wolf.class, EntityDataSerializers.INT);
	private static final EntityDataAccessor<Integer> DATA_REMAINING_ANGER_TIME = SynchedEntityData.defineId(Wolf.class, EntityDataSerializers.INT);
	private static final EntityDataAccessor<Holder<WolfVariant>> DATA_VARIANT_ID = SynchedEntityData.defineId(Wolf.class, EntityDataSerializers.WOLF_VARIANT);
	public static final Predicate<LivingEntity> PREY_SELECTOR = livingEntity -> {
		EntityType<?> entityType = livingEntity.getType();
		return entityType == EntityType.SHEEP || entityType == EntityType.RABBIT || entityType == EntityType.FOX;
	};
	private static final float START_HEALTH = 8.0F;
	private static final float TAME_HEALTH = 40.0F;
	private static final float ARMOR_REPAIR_UNIT = 0.125F;
	private float interestedAngle;
	private float interestedAngleO;
	private boolean isWet;
	private boolean isShaking;
	private float shakeAnim;
	private float shakeAnimO;
	private static final UniformInt PERSISTENT_ANGER_TIME = TimeUtil.rangeOfSeconds(20, 39);
	@Nullable
	private UUID persistentAngerTarget;

	public Wolf(EntityType<? extends Wolf> entityType, Level level) {
		super(entityType, level);
		this.setTame(false, false);
		this.setPathfindingMalus(PathType.POWDER_SNOW, -1.0F);
		this.setPathfindingMalus(PathType.DANGER_POWDER_SNOW, -1.0F);
	}

	@Override
	protected void registerGoals() {
		this.goalSelector.addGoal(1, new FloatGoal(this));
		this.goalSelector.addGoal(1, new Wolf.WolfPanicGoal(1.5));
		this.goalSelector.addGoal(2, new SitWhenOrderedToGoal(this));
		this.goalSelector.addGoal(3, new Wolf.WolfAvoidEntityGoal(this, Llama.class, 24.0F, 1.5, 1.5));
		this.goalSelector.addGoal(4, new LeapAtTargetGoal(this, 0.4F));
		this.goalSelector.addGoal(5, new MeleeAttackGoal(this, 1.0, true));
		this.goalSelector.addGoal(6, new FollowOwnerGoal(this, 1.0, 10.0F, 2.0F, false));
		this.goalSelector.addGoal(7, new BreedGoal(this, 1.0));
		this.goalSelector.addGoal(8, new WaterAvoidingRandomStrollGoal(this, 1.0));
		this.goalSelector.addGoal(9, new BegGoal(this, 8.0F));
		this.goalSelector.addGoal(10, new LookAtPlayerGoal(this, Player.class, 8.0F));
		this.goalSelector.addGoal(10, new RandomLookAroundGoal(this));
		this.targetSelector.addGoal(1, new OwnerHurtByTargetGoal(this));
		this.targetSelector.addGoal(2, new OwnerHurtTargetGoal(this));
		this.targetSelector.addGoal(3, new HurtByTargetGoal(this).setAlertOthers());
		this.targetSelector.addGoal(4, new NearestAttackableTargetGoal(this, Player.class, 10, true, false, this::isAngryAt));
		this.targetSelector.addGoal(5, new NonTameRandomTargetGoal(this, Animal.class, false, PREY_SELECTOR));
		this.targetSelector.addGoal(6, new NonTameRandomTargetGoal(this, Turtle.class, false, Turtle.BABY_ON_LAND_SELECTOR));
		this.targetSelector.addGoal(7, new NearestAttackableTargetGoal(this, AbstractSkeleton.class, false));
		this.targetSelector.addGoal(8, new ResetUniversalAngerTargetGoal<>(this, true));
	}

	public ResourceLocation getTexture() {
		WolfVariant wolfVariant = this.getVariant().value();
		if (this.isTame()) {
			return wolfVariant.tameTexture();
		} else {
			return this.isAngry() ? wolfVariant.angryTexture() : wolfVariant.wildTexture();
		}
	}

	public Holder<WolfVariant> getVariant() {
		return this.entityData.get(DATA_VARIANT_ID);
	}

	public void setVariant(Holder<WolfVariant> holder) {
		this.entityData.set(DATA_VARIANT_ID, holder);
	}

	public static AttributeSupplier.Builder createAttributes() {
		return Mob.createMobAttributes().add(Attributes.MOVEMENT_SPEED, 0.3F).add(Attributes.MAX_HEALTH, 8.0).add(Attributes.ATTACK_DAMAGE, 4.0);
	}

	@Override
	protected void defineSynchedData(SynchedEntityData.Builder builder) {
		super.defineSynchedData(builder);
		RegistryAccess registryAccess = this.registryAccess();
		Registry<WolfVariant> registry = registryAccess.registryOrThrow(Registries.WOLF_VARIANT);
		builder.define(DATA_VARIANT_ID, (Holder<WolfVariant>)registry.getHolder(WolfVariants.DEFAULT).or(registry::getAny).orElseThrow());
		builder.define(DATA_INTERESTED_ID, false);
		builder.define(DATA_COLLAR_COLOR, DyeColor.RED.getId());
		builder.define(DATA_REMAINING_ANGER_TIME, 0);
	}

	@Override
	protected void playStepSound(BlockPos blockPos, BlockState blockState) {
		this.playSound(SoundEvents.WOLF_STEP, 0.15F, 1.0F);
	}

	@Override
	public void addAdditionalSaveData(CompoundTag compoundTag) {
		super.addAdditionalSaveData(compoundTag);
		compoundTag.putByte("CollarColor", (byte)this.getCollarColor().getId());
		this.getVariant().unwrapKey().ifPresent(resourceKey -> compoundTag.putString("variant", resourceKey.location().toString()));
		this.addPersistentAngerSaveData(compoundTag);
	}

	@Override
	public void readAdditionalSaveData(CompoundTag compoundTag) {
		super.readAdditionalSaveData(compoundTag);
		Optional.ofNullable(ResourceLocation.tryParse(compoundTag.getString("variant")))
			.map(resourceLocation -> ResourceKey.create(Registries.WOLF_VARIANT, resourceLocation))
			.flatMap(resourceKey -> this.registryAccess().registryOrThrow(Registries.WOLF_VARIANT).getHolder(resourceKey))
			.ifPresent(this::setVariant);
		if (compoundTag.contains("CollarColor", 99)) {
			this.setCollarColor(DyeColor.byId(compoundTag.getInt("CollarColor")));
		}

		this.readPersistentAngerSaveData(this.level(), compoundTag);
	}

	@Nullable
	@Override
	public SpawnGroupData finalizeSpawn(
		ServerLevelAccessor serverLevelAccessor, DifficultyInstance difficultyInstance, MobSpawnType mobSpawnType, @Nullable SpawnGroupData spawnGroupData
	) {
		Holder<Biome> holder = serverLevelAccessor.getBiome(this.blockPosition());
		Holder<WolfVariant> holder2;
		if (spawnGroupData instanceof Wolf.WolfPackData wolfPackData) {
			holder2 = wolfPackData.type;
		} else {
			holder2 = WolfVariants.getSpawnVariant(this.registryAccess(), holder);
			spawnGroupData = new Wolf.WolfPackData(holder2);
		}

		this.setVariant(holder2);
		return super.finalizeSpawn(serverLevelAccessor, difficultyInstance, mobSpawnType, spawnGroupData);
	}

	@Override
	protected SoundEvent getAmbientSound() {
		if (this.isAngry()) {
			return SoundEvents.WOLF_GROWL;
		} else if (this.random.nextInt(3) == 0) {
			return this.isTame() && this.getHealth() < 20.0F ? SoundEvents.WOLF_WHINE : SoundEvents.WOLF_PANT;
		} else {
			return SoundEvents.WOLF_AMBIENT;
		}
	}

	@Override
	protected SoundEvent getHurtSound(DamageSource damageSource) {
		return this.canArmorAbsorb(damageSource) ? SoundEvents.WOLF_ARMOR_DAMAGE : SoundEvents.WOLF_HURT;
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
		if (!this.level().isClientSide && this.isWet && !this.isShaking && !this.isPathFinding() && this.onGround()) {
			this.isShaking = true;
			this.shakeAnim = 0.0F;
			this.shakeAnimO = 0.0F;
			this.level().broadcastEntityEvent(this, (byte)8);
		}

		if (!this.level().isClientSide) {
			this.updatePersistentAnger((ServerLevel)this.level(), true);
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
				if (this.isShaking && !this.level().isClientSide) {
					this.level().broadcastEntityEvent(this, (byte)56);
					this.cancelShake();
				}
			} else if ((this.isWet || this.isShaking) && this.isShaking) {
				if (this.shakeAnim == 0.0F) {
					this.playSound(SoundEvents.WOLF_SHAKE, this.getSoundVolume(), (this.random.nextFloat() - this.random.nextFloat()) * 0.2F + 1.0F);
					this.gameEvent(GameEvent.ENTITY_ACTION);
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
					float f = (float)this.getY();
					int i = (int)(Mth.sin((this.shakeAnim - 0.4F) * (float) Math.PI) * 7.0F);
					Vec3 vec3 = this.getDeltaMovement();

					for (int j = 0; j < i; j++) {
						float g = (this.random.nextFloat() * 2.0F - 1.0F) * this.getBbWidth() * 0.5F;
						float h = (this.random.nextFloat() * 2.0F - 1.0F) * this.getBbWidth() * 0.5F;
						this.level().addParticle(ParticleTypes.SPLASH, this.getX() + (double)g, (double)(f + 0.8F), this.getZ() + (double)h, vec3.x, vec3.y, vec3.z);
					}
				}
			}
		}
	}

	private void cancelShake() {
		this.isShaking = false;
		this.shakeAnim = 0.0F;
		this.shakeAnimO = 0.0F;
	}

	@Override
	public void die(DamageSource damageSource) {
		this.isWet = false;
		this.isShaking = false;
		this.shakeAnimO = 0.0F;
		this.shakeAnim = 0.0F;
		super.die(damageSource);
	}

	public boolean isWet() {
		return this.isWet;
	}

	public float getWetShade(float f) {
		return Math.min(0.75F + Mth.lerp(f, this.shakeAnimO, this.shakeAnim) / 2.0F * 0.25F, 1.0F);
	}

	public float getBodyRollAngle(float f, float g) {
		float h = (Mth.lerp(f, this.shakeAnimO, this.shakeAnim) + g) / 1.8F;
		if (h < 0.0F) {
			h = 0.0F;
		} else if (h > 1.0F) {
			h = 1.0F;
		}

		return Mth.sin(h * (float) Math.PI) * Mth.sin(h * (float) Math.PI * 11.0F) * 0.15F * (float) Math.PI;
	}

	public float getHeadRollAngle(float f) {
		return Mth.lerp(f, this.interestedAngleO, this.interestedAngle) * 0.15F * (float) Math.PI;
	}

	@Override
	public int getMaxHeadXRot() {
		return this.isInSittingPose() ? 20 : super.getMaxHeadXRot();
	}

	@Override
	public boolean hurt(DamageSource damageSource, float f) {
		if (this.isInvulnerableTo(damageSource)) {
			return false;
		} else {
			if (!this.level().isClientSide) {
				this.setOrderedToSit(false);
			}

			return super.hurt(damageSource, f);
		}
	}

	@Override
	protected void actuallyHurt(DamageSource damageSource, float f) {
		if (!this.canArmorAbsorb(damageSource)) {
			super.actuallyHurt(damageSource, f);
		} else {
			ItemStack itemStack = this.getBodyArmorItem();
			int i = itemStack.getDamageValue();
			int j = itemStack.getMaxDamage();
			itemStack.hurtAndBreak(Mth.ceil(f), this, EquipmentSlot.BODY);
			if (Crackiness.WOLF_ARMOR.byDamage(i, j) != Crackiness.WOLF_ARMOR.byDamage(this.getBodyArmorItem())) {
				this.playSound(SoundEvents.WOLF_ARMOR_CRACK);
				if (this.level() instanceof ServerLevel serverLevel) {
					serverLevel.sendParticles(
						new ItemParticleOption(ParticleTypes.ITEM, Items.ARMADILLO_SCUTE.getDefaultInstance()),
						this.getX(),
						this.getY() + 1.0,
						this.getZ(),
						20,
						0.2,
						0.1,
						0.2,
						0.1
					);
				}
			}
		}
	}

	private boolean canArmorAbsorb(DamageSource damageSource) {
		return this.hasArmor() && !damageSource.is(DamageTypeTags.BYPASSES_WOLF_ARMOR);
	}

	@Override
	protected void applyTamingSideEffects() {
		if (this.isTame()) {
			this.getAttribute(Attributes.MAX_HEALTH).setBaseValue(40.0);
			this.setHealth(40.0F);
		} else {
			this.getAttribute(Attributes.MAX_HEALTH).setBaseValue(8.0);
		}
	}

	@Override
	protected void hurtArmor(DamageSource damageSource, float f) {
		this.doHurtEquipment(damageSource, f, new EquipmentSlot[]{EquipmentSlot.BODY});
	}

	@Override
	public InteractionResult mobInteract(Player player, InteractionHand interactionHand) {
		ItemStack itemStack = player.getItemInHand(interactionHand);
		Item item = itemStack.getItem();
		if (!this.level().isClientSide || this.isBaby() && this.isFood(itemStack)) {
			if (this.isTame()) {
				if (this.isFood(itemStack) && this.getHealth() < this.getMaxHealth()) {
					itemStack.consume(1, player);
					FoodProperties foodProperties = itemStack.get(DataComponents.FOOD);
					float f = foodProperties != null ? (float)foodProperties.nutrition() : 1.0F;
					this.heal(2.0F * f);
					return InteractionResult.sidedSuccess(this.level().isClientSide());
				} else {
					if (item instanceof DyeItem dyeItem && this.isOwnedBy(player)) {
						DyeColor dyeColor = dyeItem.getDyeColor();
						if (dyeColor != this.getCollarColor()) {
							this.setCollarColor(dyeColor);
							itemStack.consume(1, player);
							return InteractionResult.SUCCESS;
						}

						return super.mobInteract(player, interactionHand);
					}

					if (itemStack.is(Items.WOLF_ARMOR) && this.isOwnedBy(player) && !this.hasArmor() && !this.isBaby()) {
						this.setBodyArmorItem(itemStack.copyWithCount(1));
						itemStack.consume(1, player);
						return InteractionResult.SUCCESS;
					} else if (itemStack.is(Items.SHEARS)
						&& this.isOwnedBy(player)
						&& this.hasArmor()
						&& (!EnchantmentHelper.has(this.getBodyArmorItem(), EnchantmentEffectComponents.PREVENT_ARMOR_CHANGE) || player.isCreative())) {
						itemStack.hurtAndBreak(1, player, getSlotForHand(interactionHand));
						this.playSound(SoundEvents.ARMOR_UNEQUIP_WOLF);
						ItemStack itemStack2 = this.getBodyArmorItem();
						this.setBodyArmorItem(ItemStack.EMPTY);
						this.spawnAtLocation(itemStack2);
						return InteractionResult.SUCCESS;
					} else if (((Ingredient)ArmorMaterials.ARMADILLO.value().repairIngredient().get()).test(itemStack)
						&& this.isInSittingPose()
						&& this.hasArmor()
						&& this.isOwnedBy(player)
						&& this.getBodyArmorItem().isDamaged()) {
						itemStack.shrink(1);
						this.playSound(SoundEvents.WOLF_ARMOR_REPAIR);
						ItemStack itemStack2 = this.getBodyArmorItem();
						int i = (int)((float)itemStack2.getMaxDamage() * 0.125F);
						itemStack2.setDamageValue(Math.max(0, itemStack2.getDamageValue() - i));
						return InteractionResult.SUCCESS;
					} else {
						InteractionResult interactionResult = super.mobInteract(player, interactionHand);
						if (!interactionResult.consumesAction() && this.isOwnedBy(player)) {
							this.setOrderedToSit(!this.isOrderedToSit());
							this.jumping = false;
							this.navigation.stop();
							this.setTarget(null);
							return InteractionResult.SUCCESS_NO_ITEM_USED;
						} else {
							return interactionResult;
						}
					}
				}
			} else if (itemStack.is(Items.BONE) && !this.isAngry()) {
				itemStack.consume(1, player);
				this.tryToTame(player);
				return InteractionResult.SUCCESS;
			} else {
				return super.mobInteract(player, interactionHand);
			}
		} else {
			boolean bl = this.isOwnedBy(player) || this.isTame() || itemStack.is(Items.BONE) && !this.isTame() && !this.isAngry();
			return bl ? InteractionResult.CONSUME : InteractionResult.PASS;
		}
	}

	private void tryToTame(Player player) {
		if (this.random.nextInt(3) == 0) {
			this.tame(player);
			this.navigation.stop();
			this.setTarget(null);
			this.setOrderedToSit(true);
			this.level().broadcastEntityEvent(this, (byte)7);
		} else {
			this.level().broadcastEntityEvent(this, (byte)6);
		}
	}

	@Override
	public void handleEntityEvent(byte b) {
		if (b == 8) {
			this.isShaking = true;
			this.shakeAnim = 0.0F;
			this.shakeAnimO = 0.0F;
		} else if (b == 56) {
			this.cancelShake();
		} else {
			super.handleEntityEvent(b);
		}
	}

	public float getTailAngle() {
		if (this.isAngry()) {
			return 1.5393804F;
		} else if (this.isTame()) {
			float f = this.getMaxHealth();
			float g = (f - this.getHealth()) / f;
			return (0.55F - g * 0.4F) * (float) Math.PI;
		} else {
			return (float) (Math.PI / 5);
		}
	}

	@Override
	public boolean isFood(ItemStack itemStack) {
		return itemStack.is(ItemTags.WOLF_FOOD);
	}

	@Override
	public int getMaxSpawnClusterSize() {
		return 8;
	}

	@Override
	public int getRemainingPersistentAngerTime() {
		return this.entityData.get(DATA_REMAINING_ANGER_TIME);
	}

	@Override
	public void setRemainingPersistentAngerTime(int i) {
		this.entityData.set(DATA_REMAINING_ANGER_TIME, i);
	}

	@Override
	public void startPersistentAngerTimer() {
		this.setRemainingPersistentAngerTime(PERSISTENT_ANGER_TIME.sample(this.random));
	}

	@Nullable
	@Override
	public UUID getPersistentAngerTarget() {
		return this.persistentAngerTarget;
	}

	@Override
	public void setPersistentAngerTarget(@Nullable UUID uUID) {
		this.persistentAngerTarget = uUID;
	}

	public DyeColor getCollarColor() {
		return DyeColor.byId(this.entityData.get(DATA_COLLAR_COLOR));
	}

	public boolean hasArmor() {
		return !this.getBodyArmorItem().isEmpty();
	}

	private void setCollarColor(DyeColor dyeColor) {
		this.entityData.set(DATA_COLLAR_COLOR, dyeColor.getId());
	}

	@Nullable
	public Wolf getBreedOffspring(ServerLevel serverLevel, AgeableMob ageableMob) {
		Wolf wolf = EntityType.WOLF.create(serverLevel);
		if (wolf != null && ageableMob instanceof Wolf wolf2) {
			if (this.random.nextBoolean()) {
				wolf.setVariant(this.getVariant());
			} else {
				wolf.setVariant(wolf2.getVariant());
			}

			if (this.isTame()) {
				wolf.setOwnerUUID(this.getOwnerUUID());
				wolf.setTame(true, true);
				if (this.random.nextBoolean()) {
					wolf.setCollarColor(this.getCollarColor());
				} else {
					wolf.setCollarColor(wolf2.getCollarColor());
				}
			}
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
		} else if (!(animal instanceof Wolf wolf)) {
			return false;
		} else if (!wolf.isTame()) {
			return false;
		} else {
			return wolf.isInSittingPose() ? false : this.isInLove() && wolf.isInLove();
		}
	}

	public boolean isInterested() {
		return this.entityData.get(DATA_INTERESTED_ID);
	}

	@Override
	public boolean wantsToAttack(LivingEntity livingEntity, LivingEntity livingEntity2) {
		if (livingEntity instanceof Creeper || livingEntity instanceof Ghast || livingEntity instanceof ArmorStand) {
			return false;
		} else if (livingEntity instanceof Wolf wolf) {
			return !wolf.isTame() || wolf.getOwner() != livingEntity2;
		} else {
			if (livingEntity instanceof Player player && livingEntity2 instanceof Player player2 && !player2.canHarmPlayer(player)) {
				return false;
			}

			if (livingEntity instanceof AbstractHorse abstractHorse && abstractHorse.isTamed()) {
				return false;
			}

			if (livingEntity instanceof TamableAnimal tamableAnimal && tamableAnimal.isTame()) {
				return false;
			}

			return true;
		}
	}

	@Override
	public boolean canBeLeashed(Player player) {
		return !this.isAngry() && super.canBeLeashed(player);
	}

	@Override
	public Vec3 getLeashOffset() {
		return new Vec3(0.0, (double)(0.6F * this.getEyeHeight()), (double)(this.getBbWidth() * 0.4F));
	}

	public static boolean checkWolfSpawnRules(
		EntityType<Wolf> entityType, LevelAccessor levelAccessor, MobSpawnType mobSpawnType, BlockPos blockPos, RandomSource randomSource
	) {
		return levelAccessor.getBlockState(blockPos.below()).is(BlockTags.WOLVES_SPAWNABLE_ON) && isBrightEnoughToSpawn(levelAccessor, blockPos);
	}

	class WolfAvoidEntityGoal<T extends LivingEntity> extends AvoidEntityGoal<T> {
		private final Wolf wolf;

		public WolfAvoidEntityGoal(final Wolf wolf2, final Class<T> class_, final float f, final double d, final double e) {
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

	public static class WolfPackData extends AgeableMob.AgeableMobGroupData {
		public final Holder<WolfVariant> type;

		public WolfPackData(Holder<WolfVariant> holder) {
			super(false);
			this.type = holder;
		}
	}

	class WolfPanicGoal extends PanicGoal {
		public WolfPanicGoal(final double d) {
			super(Wolf.this, d);
		}

		@Override
		protected boolean shouldPanic() {
			return this.mob.isFreezing() || this.mob.isOnFire();
		}
	}
}
