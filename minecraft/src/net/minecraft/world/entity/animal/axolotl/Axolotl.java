package net.minecraft.world.entity.animal.axolotl;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;
import com.mojang.math.Vector3f;
import com.mojang.serialization.Dynamic;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.DebugPackets;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LerpingModel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.MobType;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.SmoothSwimmingLookControl;
import net.minecraft.world.entity.ai.control.SmoothSwimmingMoveControl;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.ai.navigation.WaterBoundPathNavigation;
import net.minecraft.world.entity.ai.sensing.Sensor;
import net.minecraft.world.entity.ai.sensing.SensorType;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.Bucketable;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.pathfinder.AmphibiousNodeEvaluator;
import net.minecraft.world.level.pathfinder.BlockPathTypes;
import net.minecraft.world.level.pathfinder.PathFinder;
import net.minecraft.world.phys.Vec3;

public class Axolotl extends Animal implements LerpingModel, Bucketable {
	public static final int TOTAL_PLAYDEAD_TIME = 200;
	protected static final ImmutableList<? extends SensorType<? extends Sensor<? super Axolotl>>> SENSOR_TYPES = ImmutableList.of(
		SensorType.NEAREST_LIVING_ENTITIES, SensorType.NEAREST_ADULT, SensorType.HURT_BY, SensorType.AXOLOTL_ATTACKABLES, SensorType.AXOLOTL_TEMPTATIONS
	);
	protected static final ImmutableList<? extends MemoryModuleType<?>> MEMORY_TYPES = ImmutableList.of(
		MemoryModuleType.BREED_TARGET,
		MemoryModuleType.NEAREST_LIVING_ENTITIES,
		MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES,
		MemoryModuleType.NEAREST_VISIBLE_PLAYER,
		MemoryModuleType.NEAREST_VISIBLE_ATTACKABLE_PLAYER,
		MemoryModuleType.LOOK_TARGET,
		MemoryModuleType.WALK_TARGET,
		MemoryModuleType.CANT_REACH_WALK_TARGET_SINCE,
		MemoryModuleType.PATH,
		MemoryModuleType.ATTACK_TARGET,
		MemoryModuleType.ATTACK_COOLING_DOWN,
		MemoryModuleType.NEAREST_VISIBLE_ADULT,
		MemoryModuleType.HURT_BY_ENTITY,
		MemoryModuleType.PLAY_DEAD_TICKS,
		MemoryModuleType.NEAREST_ATTACKABLE,
		MemoryModuleType.TEMPTING_PLAYER,
		MemoryModuleType.TEMPTATION_COOLDOWN_TICKS,
		MemoryModuleType.IS_TEMPTED,
		MemoryModuleType.HAS_HUNTING_COOLDOWN
	);
	private static final EntityDataAccessor<Integer> DATA_VARIANT = SynchedEntityData.defineId(Axolotl.class, EntityDataSerializers.INT);
	private static final EntityDataAccessor<Boolean> DATA_PLAYING_DEAD = SynchedEntityData.defineId(Axolotl.class, EntityDataSerializers.BOOLEAN);
	private static final EntityDataAccessor<Boolean> FROM_BUCKET = SynchedEntityData.defineId(Axolotl.class, EntityDataSerializers.BOOLEAN);
	public static final double PLAYER_REGEN_DETECTION_RANGE = 20.0;
	public static final int RARE_VARIANT_CHANCE = 1200;
	private static final int AXOLOTL_TOTAL_AIR_SUPPLY = 6000;
	public static final String VARIANT_TAG = "Variant";
	private static final int REHYDRATE_AIR_SUPPLY = 1800;
	private final Map<String, Vector3f> modelRotationValues = Maps.<String, Vector3f>newHashMap();
	private static final int REGEN_BUFF_BASE_DURATION = 100;

	public Axolotl(EntityType<? extends Axolotl> entityType, Level level) {
		super(entityType, level);
		this.setPathfindingMalus(BlockPathTypes.WATER, 0.0F);
		this.moveControl = new Axolotl.AxolotlMoveControl(this);
		this.lookControl = new Axolotl.AxolotlLookControl(this, 20);
		this.maxUpStep = 1.0F;
	}

	@Override
	public Map<String, Vector3f> getModelRotationValues() {
		return this.modelRotationValues;
	}

	@Override
	public float getWalkTargetValue(BlockPos blockPos, LevelReader levelReader) {
		return 0.0F;
	}

	@Override
	protected void defineSynchedData() {
		super.defineSynchedData();
		this.entityData.define(DATA_VARIANT, 0);
		this.entityData.define(DATA_PLAYING_DEAD, false);
		this.entityData.define(FROM_BUCKET, false);
	}

	@Override
	public void addAdditionalSaveData(CompoundTag compoundTag) {
		super.addAdditionalSaveData(compoundTag);
		compoundTag.putInt("Variant", this.getVariant().getId());
		compoundTag.putBoolean("FromBucket", this.fromBucket());
	}

	@Override
	public void readAdditionalSaveData(CompoundTag compoundTag) {
		super.readAdditionalSaveData(compoundTag);
		this.setVariant(Axolotl.Variant.BY_ID[compoundTag.getInt("Variant")]);
		this.setFromBucket(compoundTag.getBoolean("FromBucket"));
	}

	@Override
	public void playAmbientSound() {
		if (!this.isPlayingDead()) {
			super.playAmbientSound();
		}
	}

	@Override
	public SpawnGroupData finalizeSpawn(
		ServerLevelAccessor serverLevelAccessor,
		DifficultyInstance difficultyInstance,
		MobSpawnType mobSpawnType,
		@Nullable SpawnGroupData spawnGroupData,
		@Nullable CompoundTag compoundTag
	) {
		boolean bl = false;
		if (mobSpawnType == MobSpawnType.BUCKET) {
			return spawnGroupData;
		} else {
			if (spawnGroupData instanceof Axolotl.AxolotlGroupData) {
				if (((Axolotl.AxolotlGroupData)spawnGroupData).getGroupSize() >= 2) {
					bl = true;
				}
			} else {
				spawnGroupData = new Axolotl.AxolotlGroupData(
					Axolotl.Variant.getCommonSpawnVariant(this.level.random), Axolotl.Variant.getCommonSpawnVariant(this.level.random)
				);
			}

			this.setVariant(((Axolotl.AxolotlGroupData)spawnGroupData).getVariant(this.level.random));
			if (bl) {
				this.setAge(-24000);
			}

			return super.finalizeSpawn(serverLevelAccessor, difficultyInstance, mobSpawnType, spawnGroupData, compoundTag);
		}
	}

	@Override
	public void baseTick() {
		int i = this.getAirSupply();
		super.baseTick();
		if (!this.isNoAi()) {
			this.handleAirSupply(i);
		}
	}

	protected void handleAirSupply(int i) {
		if (this.isAlive() && !this.isInWaterRainOrBubble()) {
			this.setAirSupply(i - 1);
			if (this.getAirSupply() == -20) {
				this.setAirSupply(0);
				this.hurt(DamageSource.DRY_OUT, 2.0F);
			}
		} else {
			this.setAirSupply(this.getMaxAirSupply());
		}
	}

	public void rehydrate() {
		int i = this.getAirSupply() + 1800;
		this.setAirSupply(Math.min(i, this.getMaxAirSupply()));
	}

	public boolean isDryingOut() {
		return this.getAirSupply() < this.getMaxAirSupply();
	}

	@Override
	public int getMaxAirSupply() {
		return 6000;
	}

	public Axolotl.Variant getVariant() {
		return Axolotl.Variant.BY_ID[this.entityData.get(DATA_VARIANT)];
	}

	private void setVariant(Axolotl.Variant variant) {
		this.entityData.set(DATA_VARIANT, variant.getId());
	}

	static boolean useRareVariant(Random random) {
		return random.nextInt(1200) == 0;
	}

	@Override
	public boolean checkSpawnObstruction(LevelReader levelReader) {
		return levelReader.isUnobstructed(this);
	}

	@Override
	public boolean canBreatheUnderwater() {
		return true;
	}

	@Override
	public boolean isPushedByFluid() {
		return false;
	}

	@Override
	public MobType getMobType() {
		return MobType.WATER;
	}

	public void setPlayingDead(boolean bl) {
		this.entityData.set(DATA_PLAYING_DEAD, bl);
	}

	public boolean isPlayingDead() {
		return this.entityData.get(DATA_PLAYING_DEAD);
	}

	@Override
	public boolean fromBucket() {
		return this.entityData.get(FROM_BUCKET);
	}

	@Override
	public void setFromBucket(boolean bl) {
		this.entityData.set(FROM_BUCKET, bl);
	}

	@Nullable
	@Override
	public AgeableMob getBreedOffspring(ServerLevel serverLevel, AgeableMob ageableMob) {
		Axolotl axolotl = EntityType.AXOLOTL.create(serverLevel);
		if (axolotl != null) {
			Axolotl.Variant variant;
			if (useRareVariant(this.random)) {
				variant = Axolotl.Variant.getRareSpawnVariant(this.random);
			} else {
				variant = this.random.nextBoolean() ? this.getVariant() : ((Axolotl)ageableMob).getVariant();
			}

			axolotl.setVariant(variant);
		}

		return axolotl;
	}

	@Override
	public double getMeleeAttackRangeSqr(LivingEntity livingEntity) {
		return 1.5 + (double)livingEntity.getBbWidth() * 2.0;
	}

	@Override
	public boolean isFood(ItemStack itemStack) {
		return ItemTags.AXOLOTL_TEMPT_ITEMS.contains(itemStack.getItem());
	}

	@Override
	public boolean canBeLeashed(Player player) {
		return true;
	}

	@Override
	protected void customServerAiStep() {
		this.level.getProfiler().push("axolotlBrain");
		this.getBrain().tick((ServerLevel)this.level, this);
		this.level.getProfiler().pop();
		this.level.getProfiler().push("axolotlActivityUpdate");
		AxolotlAi.updateActivity(this);
		this.level.getProfiler().pop();
		if (!this.isNoAi()) {
			Optional<Integer> optional = this.getBrain().getMemory(MemoryModuleType.PLAY_DEAD_TICKS);
			this.setPlayingDead(optional.isPresent() && (Integer)optional.get() > 0);
		}
	}

	public static AttributeSupplier.Builder createAttributes() {
		return Mob.createMobAttributes().add(Attributes.MAX_HEALTH, 14.0).add(Attributes.MOVEMENT_SPEED, 1.0).add(Attributes.ATTACK_DAMAGE, 2.0);
	}

	@Override
	protected PathNavigation createNavigation(Level level) {
		return new Axolotl.AxolotlPathNavigation(this, level);
	}

	@Override
	public boolean doHurtTarget(Entity entity) {
		boolean bl = entity.hurt(DamageSource.mobAttack(this), (float)((int)this.getAttributeValue(Attributes.ATTACK_DAMAGE)));
		if (bl) {
			this.doEnchantDamageEffects(this, entity);
			this.playSound(SoundEvents.AXOLOTL_ATTACK, 1.0F, 1.0F);
		}

		return bl;
	}

	@Override
	public boolean hurt(DamageSource damageSource, float f) {
		float g = this.getHealth();
		if (!this.level.isClientSide
			&& !this.isNoAi()
			&& this.level.random.nextInt(3) == 0
			&& ((float)this.level.random.nextInt(3) < f || g / this.getMaxHealth() < 0.5F)
			&& f < g
			&& this.isInWater()
			&& (damageSource.getEntity() != null || damageSource.getDirectEntity() != null)
			&& !this.isPlayingDead()) {
			this.brain.setMemory(MemoryModuleType.PLAY_DEAD_TICKS, 200);
		}

		return super.hurt(damageSource, f);
	}

	@Override
	protected float getStandingEyeHeight(Pose pose, EntityDimensions entityDimensions) {
		return entityDimensions.height * 0.655F;
	}

	@Override
	public int getMaxHeadXRot() {
		return 1;
	}

	@Override
	public int getMaxHeadYRot() {
		return 1;
	}

	@Override
	public InteractionResult mobInteract(Player player, InteractionHand interactionHand) {
		return (InteractionResult)Bucketable.bucketMobPickup(player, interactionHand, this).orElse(super.mobInteract(player, interactionHand));
	}

	@Override
	public void saveToBucketTag(ItemStack itemStack) {
		Bucketable.saveDefaultDataToBucketTag(this, itemStack);
		CompoundTag compoundTag = itemStack.getOrCreateTag();
		compoundTag.putInt("Variant", this.getVariant().getId());
		compoundTag.putInt("Age", this.getAge());
		Brain<?> brain = this.getBrain();
		if (brain.hasMemoryValue(MemoryModuleType.HAS_HUNTING_COOLDOWN)) {
			compoundTag.putLong("HuntingCooldown", brain.getTimeUntilExpiry(MemoryModuleType.HAS_HUNTING_COOLDOWN));
		}
	}

	@Override
	public void loadFromBucketTag(CompoundTag compoundTag) {
		Bucketable.loadDefaultDataFromBucketTag(this, compoundTag);
		this.setVariant(Axolotl.Variant.BY_ID[compoundTag.getInt("Variant")]);
		if (compoundTag.contains("Age")) {
			this.setAge(compoundTag.getInt("Age"));
		}

		if (compoundTag.contains("HuntingCooldown")) {
			this.getBrain().setMemoryWithExpiry(MemoryModuleType.HAS_HUNTING_COOLDOWN, true, compoundTag.getLong("HuntingCooldown"));
		}
	}

	@Override
	public ItemStack getBucketItemStack() {
		return new ItemStack(Items.AXOLOTL_BUCKET);
	}

	@Override
	public SoundEvent getPickupSound() {
		return SoundEvents.BUCKET_FILL_AXOLOTL;
	}

	@Override
	public boolean canBeSeenAsEnemy() {
		return !this.isPlayingDead() && super.canBeSeenAsEnemy();
	}

	public static void onStopAttacking(Axolotl axolotl) {
		Optional<LivingEntity> optional = axolotl.getBrain().getMemory(MemoryModuleType.ATTACK_TARGET);
		if (optional.isPresent()) {
			Level level = axolotl.level;
			LivingEntity livingEntity = (LivingEntity)optional.get();
			if (livingEntity.isDeadOrDying()) {
				DamageSource damageSource = livingEntity.getLastDamageSource();
				if (damageSource != null) {
					Entity entity = damageSource.getEntity();
					if (entity != null && entity.getType() == EntityType.PLAYER) {
						Player player = (Player)entity;
						List<Player> list = level.getEntitiesOfClass(Player.class, axolotl.getBoundingBox().inflate(20.0));
						if (list.contains(player)) {
							axolotl.applySupportingEffects(player);
						}
					}
				}
			}
		}
	}

	public void applySupportingEffects(Player player) {
		MobEffectInstance mobEffectInstance = player.getEffect(MobEffects.REGENERATION);
		int i = 100 + (mobEffectInstance != null ? mobEffectInstance.getDuration() : 0);
		player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, i, 0), this);
		player.removeEffect(MobEffects.DIG_SLOWDOWN);
	}

	@Override
	public boolean requiresCustomPersistence() {
		return super.requiresCustomPersistence() || this.fromBucket();
	}

	@Override
	protected SoundEvent getHurtSound(DamageSource damageSource) {
		return SoundEvents.AXOLOTL_HURT;
	}

	@Nullable
	@Override
	protected SoundEvent getDeathSound() {
		return SoundEvents.AXOLOTL_DEATH;
	}

	@Nullable
	@Override
	protected SoundEvent getAmbientSound() {
		return this.isInWater() ? SoundEvents.AXOLOTL_IDLE_WATER : SoundEvents.AXOLOTL_IDLE_AIR;
	}

	@Override
	protected SoundEvent getSwimSplashSound() {
		return SoundEvents.AXOLOTL_SPLASH;
	}

	@Override
	protected SoundEvent getSwimSound() {
		return SoundEvents.AXOLOTL_SWIM;
	}

	@Override
	protected Brain.Provider<Axolotl> brainProvider() {
		return Brain.provider(MEMORY_TYPES, SENSOR_TYPES);
	}

	@Override
	protected Brain<?> makeBrain(Dynamic<?> dynamic) {
		return AxolotlAi.makeBrain(this.brainProvider().makeBrain(dynamic));
	}

	@Override
	public Brain<Axolotl> getBrain() {
		return (Brain<Axolotl>)super.getBrain();
	}

	@Override
	protected void sendDebugPackets() {
		super.sendDebugPackets();
		DebugPackets.sendEntityBrain(this);
	}

	@Override
	public void travel(Vec3 vec3) {
		if (this.isEffectiveAi() && this.isInWater()) {
			this.moveRelative(this.getSpeed(), vec3);
			this.move(MoverType.SELF, this.getDeltaMovement());
			this.setDeltaMovement(this.getDeltaMovement().scale(0.9));
		} else {
			super.travel(vec3);
		}
	}

	@Override
	protected void usePlayerItem(Player player, InteractionHand interactionHand, ItemStack itemStack) {
		if (itemStack.is(Items.TROPICAL_FISH_BUCKET)) {
			player.setItemInHand(interactionHand, new ItemStack(Items.WATER_BUCKET));
		} else {
			super.usePlayerItem(player, interactionHand, itemStack);
		}
	}

	@Override
	public boolean removeWhenFarAway(double d) {
		return !this.fromBucket() && !this.hasCustomName();
	}

	public static class AxolotlGroupData extends AgeableMob.AgeableMobGroupData {
		public final Axolotl.Variant[] types;

		public AxolotlGroupData(Axolotl.Variant... variants) {
			super(false);
			this.types = variants;
		}

		public Axolotl.Variant getVariant(Random random) {
			return Axolotl.useRareVariant(random) ? Axolotl.Variant.getRareSpawnVariant(random) : this.types[random.nextInt(this.types.length)];
		}
	}

	class AxolotlLookControl extends SmoothSwimmingLookControl {
		public AxolotlLookControl(Axolotl axolotl2, int i) {
			super(axolotl2, i);
		}

		@Override
		public void tick() {
			if (!Axolotl.this.isPlayingDead()) {
				super.tick();
			}
		}
	}

	static class AxolotlMoveControl extends SmoothSwimmingMoveControl {
		private final Axolotl axolotl;

		public AxolotlMoveControl(Axolotl axolotl) {
			super(axolotl, 85, 10, 0.1F, 0.5F, false);
			this.axolotl = axolotl;
		}

		@Override
		public void tick() {
			if (!this.axolotl.isPlayingDead()) {
				super.tick();
			}
		}
	}

	static class AxolotlPathNavigation extends WaterBoundPathNavigation {
		AxolotlPathNavigation(Axolotl axolotl, Level level) {
			super(axolotl, level);
		}

		@Override
		protected boolean canUpdatePath() {
			return true;
		}

		@Override
		protected PathFinder createPathFinder(int i) {
			this.nodeEvaluator = new AmphibiousNodeEvaluator(false);
			return new PathFinder(this.nodeEvaluator, i);
		}

		@Override
		public boolean isStableDestination(BlockPos blockPos) {
			return !this.level.getBlockState(blockPos.below()).isAir();
		}
	}

	public static enum Variant {
		LUCY(0, "lucy", true),
		WILD(1, "wild", true),
		GOLD(2, "gold", true),
		CYAN(3, "cyan", true),
		BLUE(4, "blue", false);

		public static final Axolotl.Variant[] BY_ID = (Axolotl.Variant[])Arrays.stream(values())
			.sorted(Comparator.comparingInt(Axolotl.Variant::getId))
			.toArray(Axolotl.Variant[]::new);
		private final int id;
		private final String name;
		private final boolean common;

		private Variant(int j, String string2, boolean bl) {
			this.id = j;
			this.name = string2;
			this.common = bl;
		}

		public int getId() {
			return this.id;
		}

		public String getName() {
			return this.name;
		}

		public static Axolotl.Variant getCommonSpawnVariant(Random random) {
			return getSpawnVariant(random, true);
		}

		public static Axolotl.Variant getRareSpawnVariant(Random random) {
			return getSpawnVariant(random, false);
		}

		private static Axolotl.Variant getSpawnVariant(Random random, boolean bl) {
			Axolotl.Variant[] variants = (Axolotl.Variant[])Arrays.stream(BY_ID).filter(variant -> variant.common == bl).toArray(Axolotl.Variant[]::new);
			return Util.getRandom(variants, random);
		}
	}
}
