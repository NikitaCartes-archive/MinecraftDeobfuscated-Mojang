package net.minecraft.world.entity.monster;

import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Difficulty;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.MobType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LeapAtTargetGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.ai.navigation.WallClimberNavigation;
import net.minecraft.world.entity.animal.IronGolem;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public class Spider extends Monster {
	private static final EntityDataAccessor<Byte> DATA_FLAGS_ID = SynchedEntityData.defineId(Spider.class, EntityDataSerializers.BYTE);
	private static final float SPIDER_SPECIAL_EFFECT_CHANCE = 0.1F;

	public Spider(EntityType<? extends Spider> entityType, Level level) {
		super(entityType, level);
	}

	@Override
	protected void registerGoals() {
		this.goalSelector.addGoal(1, new FloatGoal(this));
		this.goalSelector.addGoal(3, new LeapAtTargetGoal(this, 0.4F));
		this.goalSelector.addGoal(4, new Spider.SpiderAttackGoal(this));
		this.goalSelector.addGoal(5, new WaterAvoidingRandomStrollGoal(this, 0.8));
		this.goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 8.0F));
		this.goalSelector.addGoal(6, new RandomLookAroundGoal(this));
		this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
		this.targetSelector.addGoal(2, new Spider.SpiderTargetGoal(this, Player.class));
		this.targetSelector.addGoal(3, new Spider.SpiderTargetGoal(this, IronGolem.class));
	}

	@Override
	protected PathNavigation createNavigation(Level level) {
		return new WallClimberNavigation(this, level);
	}

	@Override
	protected void defineSynchedData() {
		super.defineSynchedData();
		this.entityData.define(DATA_FLAGS_ID, (byte)0);
	}

	@Override
	public void tick() {
		super.tick();
		if (!this.level().isClientSide) {
			this.setClimbing(this.horizontalCollision);
		}
	}

	public static AttributeSupplier.Builder createAttributes() {
		return Monster.createMonsterAttributes().add(Attributes.MAX_HEALTH, 16.0).add(Attributes.MOVEMENT_SPEED, 0.3F);
	}

	@Override
	protected SoundEvent getAmbientSound() {
		return SoundEvents.SPIDER_AMBIENT;
	}

	@Override
	protected SoundEvent getHurtSound(DamageSource damageSource) {
		return SoundEvents.SPIDER_HURT;
	}

	@Override
	protected SoundEvent getDeathSound() {
		return SoundEvents.SPIDER_DEATH;
	}

	@Override
	protected void playStepSound(BlockPos blockPos, BlockState blockState) {
		this.playSound(SoundEvents.SPIDER_STEP, 0.15F, 1.0F);
	}

	@Override
	public boolean onClimbable() {
		return this.isClimbing();
	}

	@Override
	public void makeStuckInBlock(BlockState blockState, Vec3 vec3) {
		if (!blockState.is(Blocks.COBWEB)) {
			super.makeStuckInBlock(blockState, vec3);
		}
	}

	@Override
	public MobType getMobType() {
		return MobType.ARTHROPOD;
	}

	@Override
	public boolean canBeAffected(MobEffectInstance mobEffectInstance) {
		return mobEffectInstance.is(MobEffects.POISON) ? false : super.canBeAffected(mobEffectInstance);
	}

	public boolean isClimbing() {
		return (this.entityData.get(DATA_FLAGS_ID) & 1) != 0;
	}

	public void setClimbing(boolean bl) {
		byte b = this.entityData.get(DATA_FLAGS_ID);
		if (bl) {
			b = (byte)(b | 1);
		} else {
			b = (byte)(b & -2);
		}

		this.entityData.set(DATA_FLAGS_ID, b);
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
		spawnGroupData = super.finalizeSpawn(serverLevelAccessor, difficultyInstance, mobSpawnType, spawnGroupData, compoundTag);
		RandomSource randomSource = serverLevelAccessor.getRandom();
		if (randomSource.nextInt(100) == 0) {
			Skeleton skeleton = EntityType.SKELETON.create(this.level());
			if (skeleton != null) {
				skeleton.moveTo(this.getX(), this.getY(), this.getZ(), this.getYRot(), 0.0F);
				skeleton.finalizeSpawn(serverLevelAccessor, difficultyInstance, mobSpawnType, null, null);
				skeleton.startRiding(this);
			}
		}

		if (spawnGroupData == null) {
			spawnGroupData = new Spider.SpiderEffectsGroupData();
			if (serverLevelAccessor.getDifficulty() == Difficulty.HARD && randomSource.nextFloat() < 0.1F * difficultyInstance.getSpecialMultiplier()) {
				((Spider.SpiderEffectsGroupData)spawnGroupData).setRandomEffect(randomSource);
			}
		}

		if (spawnGroupData instanceof Spider.SpiderEffectsGroupData spiderEffectsGroupData) {
			Holder<MobEffect> holder = spiderEffectsGroupData.effect;
			if (holder != null) {
				this.addEffect(new MobEffectInstance(holder, -1));
			}
		}

		return spawnGroupData;
	}

	@Override
	public Vec3 getVehicleAttachmentPoint(Entity entity) {
		return entity.getBbWidth() <= this.getBbWidth() ? new Vec3(0.0, 0.3125 * (double)this.getScale(), 0.0) : super.getVehicleAttachmentPoint(entity);
	}

	static class SpiderAttackGoal extends MeleeAttackGoal {
		public SpiderAttackGoal(Spider spider) {
			super(spider, 1.0, true);
		}

		@Override
		public boolean canUse() {
			return super.canUse() && !this.mob.isVehicle();
		}

		@Override
		public boolean canContinueToUse() {
			float f = this.mob.getLightLevelDependentMagicValue();
			if (f >= 0.5F && this.mob.getRandom().nextInt(100) == 0) {
				this.mob.setTarget(null);
				return false;
			} else {
				return super.canContinueToUse();
			}
		}
	}

	public static class SpiderEffectsGroupData implements SpawnGroupData {
		@Nullable
		public Holder<MobEffect> effect;

		public void setRandomEffect(RandomSource randomSource) {
			int i = randomSource.nextInt(5);
			if (i <= 1) {
				this.effect = MobEffects.MOVEMENT_SPEED;
			} else if (i <= 2) {
				this.effect = MobEffects.DAMAGE_BOOST;
			} else if (i <= 3) {
				this.effect = MobEffects.REGENERATION;
			} else if (i <= 4) {
				this.effect = MobEffects.INVISIBILITY;
			}
		}
	}

	static class SpiderTargetGoal<T extends LivingEntity> extends NearestAttackableTargetGoal<T> {
		public SpiderTargetGoal(Spider spider, Class<T> class_) {
			super(spider, class_, true);
		}

		@Override
		public boolean canUse() {
			float f = this.mob.getLightLevelDependentMagicValue();
			return f >= 0.5F ? false : super.canUse();
		}
	}
}
