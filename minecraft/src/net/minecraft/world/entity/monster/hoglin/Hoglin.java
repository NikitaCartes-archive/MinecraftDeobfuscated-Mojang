package net.minecraft.world.entity.monster.hoglin;

import com.google.common.collect.ImmutableList;
import com.mojang.datafixers.Dynamic;
import java.util.Random;
import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.DebugPackets;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AgableMob;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.sensing.Sensor;
import net.minecraft.world.entity.ai.sensing.SensorType;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.monster.SharedMonsterAttributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Hoglin extends Animal implements Enemy {
	private static final Logger LOGGER = LogManager.getLogger();
	private int attackAnimationRemainingTicks;
	private static int createCounter = 0;
	private static int dieCounter = 0;
	private static int killedByPiglinCounter = 0;
	private static int removeCounter = 0;
	protected static final ImmutableList<? extends SensorType<? extends Sensor<? super Hoglin>>> SENSOR_TYPES = ImmutableList.of(
		SensorType.NEAREST_LIVING_ENTITIES, SensorType.NEAREST_PLAYERS, SensorType.HOGLIN_SPECIFIC_SENSOR
	);
	protected static final ImmutableList<? extends MemoryModuleType<?>> MEMORY_TYPES = ImmutableList.of(
		MemoryModuleType.BREED_TARGET,
		MemoryModuleType.LIVING_ENTITIES,
		MemoryModuleType.VISIBLE_LIVING_ENTITIES,
		MemoryModuleType.NEAREST_VISIBLE_PLAYER,
		MemoryModuleType.NEAREST_VISIBLE_TARGETABLE_PLAYER,
		MemoryModuleType.LOOK_TARGET,
		MemoryModuleType.WALK_TARGET,
		MemoryModuleType.CANT_REACH_WALK_TARGET_SINCE,
		MemoryModuleType.PATH,
		MemoryModuleType.ATTACK_TARGET,
		MemoryModuleType.NEAREST_VISIBLE_ADULT_PIGLIN,
		MemoryModuleType.AVOID_TARGET,
		MemoryModuleType.VISIBLE_ADULT_PIGLIN_COUNT,
		MemoryModuleType.VISIBLE_ADULT_HOGLIN_COUNT,
		MemoryModuleType.NEAREST_VISIBLE_ADULT_HOGLINS,
		MemoryModuleType.NEAREST_WARPED_FUNGUS,
		MemoryModuleType.PACIFIED
	);

	public Hoglin(EntityType<? extends Hoglin> entityType, Level level) {
		super(entityType, level);
		this.xpReward = 5;
	}

	@Override
	public void die(DamageSource damageSource) {
		super.die(damageSource);
	}

	@Override
	public void remove() {
		super.remove();
	}

	@Override
	protected void registerAttributes() {
		super.registerAttributes();
		this.getAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(40.0);
		this.getAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(0.4F);
		this.getAttribute(SharedMonsterAttributes.KNOCKBACK_RESISTANCE).setBaseValue(0.5);
		this.getAttributes().registerAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).setBaseValue(6.0);
	}

	private float getAttackDamage() {
		return (float)this.getAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).getValue();
	}

	@Override
	public boolean doHurtTarget(Entity entity) {
		this.attackAnimationRemainingTicks = 10;
		this.level.broadcastEntityEvent(this, (byte)4);
		float f = this.isBaby() ? 0.5F : this.getAttackDamage() / 2.0F + (float)this.random.nextInt((int)this.getAttackDamage());
		boolean bl = entity.hurt(DamageSource.mobAttack(this), f);
		if (bl) {
			this.doEnchantDamageEffects(this, entity);
			if (this.isAdult()) {
				this.throwTarget(entity);
			}
		}

		this.playSound(SoundEvents.HOGLIN_ATTACK, 1.0F, this.getVoicePitch());
		if (entity instanceof LivingEntity) {
			HoglinAi.onHitTarget(this, (LivingEntity)entity);
		}

		return bl;
	}

	private void throwTarget(Entity entity) {
		entity.setDeltaMovement(
			entity.getDeltaMovement()
				.add((double)((this.random.nextFloat() - 0.5F) * 0.5F), (double)(this.random.nextFloat() * 0.5F), (double)(this.random.nextFloat() * -0.5F))
		);
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
	protected Brain<?> makeBrain(Dynamic<?> dynamic) {
		return HoglinAi.makeBrain(this, dynamic);
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
		HoglinAi.maybePlayActivitySound(this);
	}

	@Override
	public void aiStep() {
		if (this.attackAnimationRemainingTicks > 0) {
			this.attackAnimationRemainingTicks--;
		}

		super.aiStep();
	}

	public static boolean checkHoglinSpawnRules(
		EntityType<Hoglin> entityType, LevelAccessor levelAccessor, MobSpawnType mobSpawnType, BlockPos blockPos, Random random
	) {
		return levelAccessor.getBlockState(blockPos.below()).getBlock() != Blocks.NETHER_WART_BLOCK;
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
		if (levelAccessor.getRandom().nextFloat() < 0.2F) {
			this.setBaby(true);
		}

		return super.finalizeSpawn(levelAccessor, difficultyInstance, mobSpawnType, spawnGroupData, compoundTag);
	}

	@Override
	public boolean removeWhenFarAway(double d) {
		return !this.isPersistenceRequired();
	}

	@Override
	public float getWalkTargetValue(BlockPos blockPos, LevelReader levelReader) {
		if (HoglinAi.isPosNearNearestWarpedFungus(this, blockPos)) {
			return -1.0F;
		} else {
			return levelReader.getBlockState(blockPos.below()).getBlock() == Blocks.CRIMSON_NYLIUM ? 10.0F : 0.0F;
		}
	}

	@Override
	public boolean mobInteract(Player player, InteractionHand interactionHand) {
		boolean bl = super.mobInteract(player, interactionHand);
		if (bl) {
			this.setPersistenceRequired();
		}

		return bl;
	}

	@Environment(EnvType.CLIENT)
	@Override
	public void handleEntityEvent(byte b) {
		if (b == 4) {
			this.attackAnimationRemainingTicks = 10;
			this.playSound(SoundEvents.HOGLIN_ATTACK, 1.0F, this.getVoicePitch());
		} else {
			super.handleEntityEvent(b);
		}
	}

	@Environment(EnvType.CLIENT)
	public int getAttackAnimationRemainingTicks() {
		return this.attackAnimationRemainingTicks;
	}

	@Override
	protected int getExperienceReward(Player player) {
		return this.xpReward;
	}

	@Override
	public boolean isFood(ItemStack itemStack) {
		return itemStack.getItem() == Items.CRIMSON_FUNGUS;
	}

	public boolean isAdult() {
		return !this.isBaby();
	}

	@Nullable
	@Override
	public AgableMob getBreedOffspring(AgableMob agableMob) {
		Hoglin hoglin = EntityType.HOGLIN.create(this.level);
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
		return SoundEvents.HOGLIN_AMBIENT;
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
	public void playAmbientSound() {
		if (HoglinAi.isIdle(this)) {
			super.playAmbientSound();
		}
	}

	@Override
	protected void playStepSound(BlockPos blockPos, BlockState blockState) {
		this.playSound(SoundEvents.HOGLIN_STEP, 0.15F, 1.0F);
	}

	protected void playAngrySound() {
		this.playSound(SoundEvents.HOGLIN_ANGRY, 1.0F, this.getVoicePitch());
	}

	protected void playRetreatSound() {
		this.playSound(SoundEvents.HOGLIN_RETREAT, 1.0F, this.getVoicePitch());
	}

	@Override
	protected void sendDebugPackets() {
		super.sendDebugPackets();
		DebugPackets.sendEntityBrain(this);
	}
}
