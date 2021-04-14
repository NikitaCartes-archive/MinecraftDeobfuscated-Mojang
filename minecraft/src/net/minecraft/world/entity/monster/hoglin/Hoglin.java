package net.minecraft.world.entity.monster.hoglin;

import com.google.common.collect.ImmutableList;
import com.mojang.serialization.Dynamic;
import java.util.Random;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.DebugPackets;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.sensing.Sensor;
import net.minecraft.world.entity.ai.sensing.SensorType;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.Zoglin;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

public class Hoglin extends Animal implements Enemy, HoglinBase {
	private static final EntityDataAccessor<Boolean> DATA_IMMUNE_TO_ZOMBIFICATION = SynchedEntityData.defineId(Hoglin.class, EntityDataSerializers.BOOLEAN);
	private static final float PROBABILITY_OF_SPAWNING_AS_BABY = 0.2F;
	private static final int MAX_HEALTH = 40;
	private static final float MOVEMENT_SPEED_WHEN_FIGHTING = 0.3F;
	private static final int ATTACK_KNOCKBACK = 1;
	private static final float KNOCKBACK_RESISTANCE = 0.6F;
	private static final int ATTACK_DAMAGE = 6;
	private static final float BABY_ATTACK_DAMAGE = 0.5F;
	private static final int CONVERSION_TIME = 300;
	private int attackAnimationRemainingTicks;
	private int timeInOverworld;
	private boolean cannotBeHunted;
	protected static final ImmutableList<? extends SensorType<? extends Sensor<? super Hoglin>>> SENSOR_TYPES = ImmutableList.of(
		SensorType.NEAREST_LIVING_ENTITIES, SensorType.NEAREST_PLAYERS, SensorType.NEAREST_ADULT, SensorType.HOGLIN_SPECIFIC_SENSOR
	);
	protected static final ImmutableList<? extends MemoryModuleType<?>> MEMORY_TYPES = ImmutableList.of(
		MemoryModuleType.BREED_TARGET,
		MemoryModuleType.NEAREST_LIVING_ENTITIES,
		MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES,
		MemoryModuleType.NEAREST_VISIBLE_PLAYER,
		MemoryModuleType.NEAREST_VISIBLE_TARGETABLE_PLAYER,
		MemoryModuleType.LOOK_TARGET,
		MemoryModuleType.WALK_TARGET,
		MemoryModuleType.CANT_REACH_WALK_TARGET_SINCE,
		MemoryModuleType.PATH,
		MemoryModuleType.ATTACK_TARGET,
		MemoryModuleType.ATTACK_COOLING_DOWN,
		MemoryModuleType.NEAREST_VISIBLE_ADULT_PIGLIN,
		MemoryModuleType.AVOID_TARGET,
		MemoryModuleType.VISIBLE_ADULT_PIGLIN_COUNT,
		MemoryModuleType.VISIBLE_ADULT_HOGLIN_COUNT,
		MemoryModuleType.NEAREST_VISIBLE_ADULT_HOGLINS,
		MemoryModuleType.NEAREST_VISIBLE_ADULT,
		MemoryModuleType.NEAREST_REPELLENT,
		MemoryModuleType.PACIFIED
	);

	public Hoglin(EntityType<? extends Hoglin> entityType, Level level) {
		super(entityType, level);
		this.xpReward = 5;
	}

	@Override
	public boolean canBeLeashed(Player player) {
		return !this.isLeashed();
	}

	public static AttributeSupplier.Builder createAttributes() {
		return Monster.createMonsterAttributes()
			.add(Attributes.MAX_HEALTH, 40.0)
			.add(Attributes.MOVEMENT_SPEED, 0.3F)
			.add(Attributes.KNOCKBACK_RESISTANCE, 0.6F)
			.add(Attributes.ATTACK_KNOCKBACK, 1.0)
			.add(Attributes.ATTACK_DAMAGE, 6.0);
	}

	@Override
	public boolean doHurtTarget(Entity entity) {
		if (!(entity instanceof LivingEntity)) {
			return false;
		} else {
			this.attackAnimationRemainingTicks = 10;
			this.level.broadcastEntityEvent(this, (byte)4);
			this.playSound(SoundEvents.HOGLIN_ATTACK, 1.0F, this.getVoicePitch());
			HoglinAi.onHitTarget(this, (LivingEntity)entity);
			return HoglinBase.hurtAndThrowTarget(this, (LivingEntity)entity);
		}
	}

	@Override
	protected void blockedByShield(LivingEntity livingEntity) {
		if (this.isAdult()) {
			HoglinBase.throwTarget(this, livingEntity);
		}
	}

	@Override
	public boolean hurt(DamageSource damageSource, float f) {
		boolean bl = super.hurt(damageSource, f);
		if (this.level.isClientSide) {
			return false;
		} else {
			if (bl && damageSource.getEntity() instanceof LivingEntity) {
				HoglinAi.wasHurtBy(this, (LivingEntity)damageSource.getEntity());
			}

			return bl;
		}
	}

	@Override
	protected Brain.Provider<Hoglin> brainProvider() {
		return Brain.provider(MEMORY_TYPES, SENSOR_TYPES);
	}

	@Override
	protected Brain<?> makeBrain(Dynamic<?> dynamic) {
		return HoglinAi.makeBrain(this.brainProvider().makeBrain(dynamic));
	}

	@Override
	public Brain<Hoglin> getBrain() {
		return (Brain<Hoglin>)super.getBrain();
	}

	@Override
	protected void customServerAiStep() {
		this.level.getProfiler().push("hoglinBrain");
		this.getBrain().tick((ServerLevel)this.level, this);
		this.level.getProfiler().pop();
		HoglinAi.updateActivity(this);
		if (this.isConverting()) {
			this.timeInOverworld++;
			if (this.timeInOverworld > 300) {
				this.playSound(SoundEvents.HOGLIN_CONVERTED_TO_ZOMBIFIED);
				this.finishConversion((ServerLevel)this.level);
			}
		} else {
			this.timeInOverworld = 0;
		}
	}

	@Override
	public void aiStep() {
		if (this.attackAnimationRemainingTicks > 0) {
			this.attackAnimationRemainingTicks--;
		}

		super.aiStep();
	}

	@Override
	protected void ageBoundaryReached() {
		if (this.isBaby()) {
			this.xpReward = 3;
			this.getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue(0.5);
		} else {
			this.xpReward = 5;
			this.getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue(6.0);
		}
	}

	public static boolean checkHoglinSpawnRules(
		EntityType<Hoglin> entityType, LevelAccessor levelAccessor, MobSpawnType mobSpawnType, BlockPos blockPos, Random random
	) {
		return !levelAccessor.getBlockState(blockPos.below()).is(Blocks.NETHER_WART_BLOCK);
	}

	@Nullable
	@Override
	public SpawnGroupData finalizeSpawn(
		ServerLevelAccessor serverLevelAccessor,
		DifficultyInstance difficultyInstance,
		MobSpawnType mobSpawnType,
		@Nullable SpawnGroupData spawnGroupData,
		@Nullable CompoundTag compoundTag
	) {
		if (serverLevelAccessor.getRandom().nextFloat() < 0.2F) {
			this.setBaby(true);
		}

		return super.finalizeSpawn(serverLevelAccessor, difficultyInstance, mobSpawnType, spawnGroupData, compoundTag);
	}

	@Override
	public boolean removeWhenFarAway(double d) {
		return !this.isPersistenceRequired();
	}

	@Override
	public float getWalkTargetValue(BlockPos blockPos, LevelReader levelReader) {
		if (HoglinAi.isPosNearNearestRepellent(this, blockPos)) {
			return -1.0F;
		} else {
			return levelReader.getBlockState(blockPos.below()).is(Blocks.CRIMSON_NYLIUM) ? 10.0F : 0.0F;
		}
	}

	@Override
	public double getPassengersRidingOffset() {
		return (double)this.getBbHeight() - (this.isBaby() ? 0.2 : 0.15);
	}

	@Override
	public InteractionResult mobInteract(Player player, InteractionHand interactionHand) {
		InteractionResult interactionResult = super.mobInteract(player, interactionHand);
		if (interactionResult.consumesAction()) {
			this.setPersistenceRequired();
		}

		return interactionResult;
	}

	@Override
	public void handleEntityEvent(byte b) {
		if (b == 4) {
			this.attackAnimationRemainingTicks = 10;
			this.playSound(SoundEvents.HOGLIN_ATTACK, 1.0F, this.getVoicePitch());
		} else {
			super.handleEntityEvent(b);
		}
	}

	@Override
	public int getAttackAnimationRemainingTicks() {
		return this.attackAnimationRemainingTicks;
	}

	@Override
	protected boolean shouldDropExperience() {
		return true;
	}

	@Override
	protected int getExperienceReward(Player player) {
		return this.xpReward;
	}

	private void finishConversion(ServerLevel serverLevel) {
		Zoglin zoglin = this.convertTo(EntityType.ZOGLIN, true);
		if (zoglin != null) {
			zoglin.addEffect(new MobEffectInstance(MobEffects.CONFUSION, 200, 0));
		}
	}

	@Override
	public boolean isFood(ItemStack itemStack) {
		return itemStack.is(Items.CRIMSON_FUNGUS);
	}

	public boolean isAdult() {
		return !this.isBaby();
	}

	@Override
	protected void defineSynchedData() {
		super.defineSynchedData();
		this.entityData.define(DATA_IMMUNE_TO_ZOMBIFICATION, false);
	}

	@Override
	public void addAdditionalSaveData(CompoundTag compoundTag) {
		super.addAdditionalSaveData(compoundTag);
		if (this.isImmuneToZombification()) {
			compoundTag.putBoolean("IsImmuneToZombification", true);
		}

		compoundTag.putInt("TimeInOverworld", this.timeInOverworld);
		if (this.cannotBeHunted) {
			compoundTag.putBoolean("CannotBeHunted", true);
		}
	}

	@Override
	public void readAdditionalSaveData(CompoundTag compoundTag) {
		super.readAdditionalSaveData(compoundTag);
		this.setImmuneToZombification(compoundTag.getBoolean("IsImmuneToZombification"));
		this.timeInOverworld = compoundTag.getInt("TimeInOverworld");
		this.setCannotBeHunted(compoundTag.getBoolean("CannotBeHunted"));
	}

	public void setImmuneToZombification(boolean bl) {
		this.getEntityData().set(DATA_IMMUNE_TO_ZOMBIFICATION, bl);
	}

	private boolean isImmuneToZombification() {
		return this.getEntityData().get(DATA_IMMUNE_TO_ZOMBIFICATION);
	}

	public boolean isConverting() {
		return !this.level.dimensionType().piglinSafe() && !this.isImmuneToZombification() && !this.isNoAi();
	}

	private void setCannotBeHunted(boolean bl) {
		this.cannotBeHunted = bl;
	}

	public boolean canBeHunted() {
		return this.isAdult() && !this.cannotBeHunted;
	}

	@Nullable
	@Override
	public AgeableMob getBreedOffspring(ServerLevel serverLevel, AgeableMob ageableMob) {
		Hoglin hoglin = EntityType.HOGLIN.create(serverLevel);
		if (hoglin != null) {
			hoglin.setPersistenceRequired();
		}

		return hoglin;
	}

	@Override
	public boolean canFallInLove() {
		return !HoglinAi.isPacified(this) && super.canFallInLove();
	}

	@Override
	public SoundSource getSoundSource() {
		return SoundSource.HOSTILE;
	}

	@Override
	protected SoundEvent getAmbientSound() {
		return this.level.isClientSide ? null : (SoundEvent)HoglinAi.getSoundForCurrentActivity(this).orElse(null);
	}

	@Override
	protected SoundEvent getHurtSound(DamageSource damageSource) {
		return SoundEvents.HOGLIN_HURT;
	}

	@Override
	protected SoundEvent getDeathSound() {
		return SoundEvents.HOGLIN_DEATH;
	}

	@Override
	protected SoundEvent getSwimSound() {
		return SoundEvents.HOSTILE_SWIM;
	}

	@Override
	protected SoundEvent getSwimSplashSound() {
		return SoundEvents.HOSTILE_SPLASH;
	}

	@Override
	protected void playStepSound(BlockPos blockPos, BlockState blockState) {
		this.playSound(SoundEvents.HOGLIN_STEP, 0.15F, 1.0F);
	}

	protected void playSound(SoundEvent soundEvent) {
		this.playSound(soundEvent, this.getSoundVolume(), this.getVoicePitch());
	}

	@Override
	protected void sendDebugPackets() {
		super.sendDebugPackets();
		DebugPackets.sendEntityBrain(this);
	}
}
