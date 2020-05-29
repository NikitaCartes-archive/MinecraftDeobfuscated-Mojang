package net.minecraft.world.entity.monster;

import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.MobType;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockState;

public class Endermite extends Monster {
	private int life;
	private boolean playerSpawned;

	public Endermite(EntityType<? extends Endermite> entityType, Level level) {
		super(entityType, level);
		this.xpReward = 3;
	}

	@Override
	protected void registerGoals() {
		this.goalSelector.addGoal(1, new FloatGoal(this));
		this.goalSelector.addGoal(2, new MeleeAttackGoal(this, 1.0, false));
		this.goalSelector.addGoal(3, new WaterAvoidingRandomStrollGoal(this, 1.0));
		this.goalSelector.addGoal(7, new LookAtPlayerGoal(this, Player.class, 8.0F));
		this.goalSelector.addGoal(8, new RandomLookAroundGoal(this));
		this.targetSelector.addGoal(1, new HurtByTargetGoal(this).setAlertOthers());
		this.targetSelector.addGoal(2, new NearestAttackableTargetGoal(this, Player.class, true));
	}

	@Override
	protected float getStandingEyeHeight(Pose pose, EntityDimensions entityDimensions) {
		return 0.13F;
	}

	public static AttributeSupplier.Builder createAttributes() {
		return Monster.createMonsterAttributes().add(Attributes.MAX_HEALTH, 8.0).add(Attributes.MOVEMENT_SPEED, 0.25).add(Attributes.ATTACK_DAMAGE, 2.0);
	}

	@Override
	protected boolean isMovementNoisy() {
		return false;
	}

	@Override
	protected SoundEvent getAmbientSound() {
		return SoundEvents.ENDERMITE_AMBIENT;
	}

	@Override
	protected SoundEvent getHurtSound(DamageSource damageSource) {
		return SoundEvents.ENDERMITE_HURT;
	}

	@Override
	protected SoundEvent getDeathSound() {
		return SoundEvents.ENDERMITE_DEATH;
	}

	@Override
	protected void playStepSound(BlockPos blockPos, BlockState blockState) {
		this.playSound(SoundEvents.ENDERMITE_STEP, 0.15F, 1.0F);
	}

	@Override
	public void readAdditionalSaveData(CompoundTag compoundTag) {
		super.readAdditionalSaveData(compoundTag);
		this.life = compoundTag.getInt("Lifetime");
		this.playerSpawned = compoundTag.getBoolean("PlayerSpawned");
	}

	@Override
	public void addAdditionalSaveData(CompoundTag compoundTag) {
		super.addAdditionalSaveData(compoundTag);
		compoundTag.putInt("Lifetime", this.life);
		compoundTag.putBoolean("PlayerSpawned", this.playerSpawned);
	}

	@Override
	public void tick() {
		this.yBodyRot = this.yRot;
		super.tick();
	}

	@Override
	public void setYBodyRot(float f) {
		this.yRot = f;
		super.setYBodyRot(f);
	}

	@Override
	public double getMyRidingOffset() {
		return 0.1;
	}

	public boolean isPlayerSpawned() {
		return this.playerSpawned;
	}

	public void setPlayerSpawned(boolean bl) {
		this.playerSpawned = bl;
	}

	@Override
	public void aiStep() {
		super.aiStep();
		if (this.level.isClientSide) {
			for (int i = 0; i < 2; i++) {
				this.level
					.addParticle(
						ParticleTypes.PORTAL,
						this.getRandomX(0.5),
						this.getRandomY(),
						this.getRandomZ(0.5),
						(this.random.nextDouble() - 0.5) * 2.0,
						-this.random.nextDouble(),
						(this.random.nextDouble() - 0.5) * 2.0
					);
			}
		} else {
			if (!this.isPersistenceRequired()) {
				this.life++;
			}

			if (this.life >= 2400) {
				this.remove();
			}
		}
	}

	public static boolean checkEndermiteSpawnRules(
		EntityType<Endermite> entityType, LevelAccessor levelAccessor, MobSpawnType mobSpawnType, BlockPos blockPos, Random random
	) {
		if (checkAnyLightMonsterSpawnRules(entityType, levelAccessor, mobSpawnType, blockPos, random)) {
			Player player = levelAccessor.getNearestPlayer((double)blockPos.getX() + 0.5, (double)blockPos.getY() + 0.5, (double)blockPos.getZ() + 0.5, 5.0, true);
			return player == null;
		} else {
			return false;
		}
	}

	@Override
	public MobType getMobType() {
		return MobType.ARTHROPOD;
	}
}
