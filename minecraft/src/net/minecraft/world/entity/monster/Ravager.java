package net.minecraft.world.entity.monster;

import java.util.function.Predicate;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ColorParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.AvoidEntityGoal;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.animal.IronGolem;
import net.minecraft.world.entity.monster.creaking.Creaking;
import net.minecraft.world.entity.npc.AbstractVillager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.raid.Raider;
import net.minecraft.world.entity.vehicle.AbstractBoat;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.pathfinder.PathType;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class Ravager extends Raider {
	private static final Predicate<Entity> ROAR_TARGET_WITH_GRIEFING = entity -> !(entity instanceof Ravager) && entity.isAlive();
	private static final Predicate<Entity> ROAR_TARGET_WITHOUT_GRIEFING = entity -> ROAR_TARGET_WITH_GRIEFING.test(entity)
			&& !entity.getType().equals(EntityType.ARMOR_STAND);
	private static final Predicate<LivingEntity> ROAR_TARGET_ON_CLIENT = livingEntity -> !(livingEntity instanceof Ravager)
			&& livingEntity.isAlive()
			&& livingEntity.isControlledByLocalInstance();
	private static final double BASE_MOVEMENT_SPEED = 0.3;
	private static final double ATTACK_MOVEMENT_SPEED = 0.35;
	private static final int STUNNED_COLOR = 8356754;
	private static final float STUNNED_COLOR_BLUE = 0.57254905F;
	private static final float STUNNED_COLOR_GREEN = 0.5137255F;
	private static final float STUNNED_COLOR_RED = 0.49803922F;
	public static final int ATTACK_DURATION = 10;
	public static final int STUN_DURATION = 40;
	private int attackTick;
	private int stunnedTick;
	private int roarTick;

	public Ravager(EntityType<? extends Ravager> entityType, Level level) {
		super(entityType, level);
		this.xpReward = 20;
		this.setPathfindingMalus(PathType.LEAVES, 0.0F);
	}

	@Override
	protected void registerGoals() {
		super.registerGoals();
		this.goalSelector.addGoal(0, new FloatGoal(this));
		this.goalSelector.addGoal(3, new AvoidEntityGoal(this, Creaking.class, 8.0F, 1.0, 1.2));
		this.goalSelector.addGoal(4, new MeleeAttackGoal(this, 1.0, true));
		this.goalSelector.addGoal(5, new WaterAvoidingRandomStrollGoal(this, 0.4));
		this.goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 6.0F));
		this.goalSelector.addGoal(10, new LookAtPlayerGoal(this, Mob.class, 8.0F));
		this.targetSelector.addGoal(2, new HurtByTargetGoal(this, Raider.class).setAlertOthers());
		this.targetSelector.addGoal(3, new NearestAttackableTargetGoal(this, Player.class, true));
		this.targetSelector.addGoal(4, new NearestAttackableTargetGoal(this, AbstractVillager.class, true, (livingEntity, serverLevel) -> !livingEntity.isBaby()));
		this.targetSelector.addGoal(4, new NearestAttackableTargetGoal(this, IronGolem.class, true));
	}

	@Override
	protected void updateControlFlags() {
		boolean bl = !(this.getControllingPassenger() instanceof Mob) || this.getControllingPassenger().getType().is(EntityTypeTags.RAIDERS);
		boolean bl2 = !(this.getVehicle() instanceof AbstractBoat);
		this.goalSelector.setControlFlag(Goal.Flag.MOVE, bl);
		this.goalSelector.setControlFlag(Goal.Flag.JUMP, bl && bl2);
		this.goalSelector.setControlFlag(Goal.Flag.LOOK, bl);
		this.goalSelector.setControlFlag(Goal.Flag.TARGET, bl);
	}

	public static AttributeSupplier.Builder createAttributes() {
		return Monster.createMonsterAttributes()
			.add(Attributes.MAX_HEALTH, 100.0)
			.add(Attributes.MOVEMENT_SPEED, 0.3)
			.add(Attributes.KNOCKBACK_RESISTANCE, 0.75)
			.add(Attributes.ATTACK_DAMAGE, 12.0)
			.add(Attributes.ATTACK_KNOCKBACK, 1.5)
			.add(Attributes.FOLLOW_RANGE, 32.0)
			.add(Attributes.STEP_HEIGHT, 1.0);
	}

	@Override
	public void addAdditionalSaveData(CompoundTag compoundTag) {
		super.addAdditionalSaveData(compoundTag);
		compoundTag.putInt("AttackTick", this.attackTick);
		compoundTag.putInt("StunTick", this.stunnedTick);
		compoundTag.putInt("RoarTick", this.roarTick);
	}

	@Override
	public void readAdditionalSaveData(CompoundTag compoundTag) {
		super.readAdditionalSaveData(compoundTag);
		this.attackTick = compoundTag.getInt("AttackTick");
		this.stunnedTick = compoundTag.getInt("StunTick");
		this.roarTick = compoundTag.getInt("RoarTick");
	}

	@Override
	public SoundEvent getCelebrateSound() {
		return SoundEvents.RAVAGER_CELEBRATE;
	}

	@Override
	public int getMaxHeadYRot() {
		return 45;
	}

	@Override
	public void aiStep() {
		super.aiStep();
		if (this.isAlive()) {
			if (this.isImmobile()) {
				this.getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(0.0);
			} else {
				double d = this.getTarget() != null ? 0.35 : 0.3;
				double e = this.getAttribute(Attributes.MOVEMENT_SPEED).getBaseValue();
				this.getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(Mth.lerp(0.1, e, d));
			}

			if (this.level() instanceof ServerLevel serverLevel && this.horizontalCollision && serverLevel.getGameRules().getBoolean(GameRules.RULE_MOBGRIEFING)) {
				boolean bl = false;
				AABB aABB = this.getBoundingBox().inflate(0.2);

				for (BlockPos blockPos : BlockPos.betweenClosed(
					Mth.floor(aABB.minX), Mth.floor(aABB.minY), Mth.floor(aABB.minZ), Mth.floor(aABB.maxX), Mth.floor(aABB.maxY), Mth.floor(aABB.maxZ)
				)) {
					BlockState blockState = serverLevel.getBlockState(blockPos);
					Block block = blockState.getBlock();
					if (block instanceof LeavesBlock) {
						bl = serverLevel.destroyBlock(blockPos, true, this) || bl;
					}
				}

				if (!bl && this.onGround()) {
					this.jumpFromGround();
				}
			}

			if (this.roarTick > 0) {
				this.roarTick--;
				if (this.roarTick == 10) {
					this.roar();
				}
			}

			if (this.attackTick > 0) {
				this.attackTick--;
			}

			if (this.stunnedTick > 0) {
				this.stunnedTick--;
				this.stunEffect();
				if (this.stunnedTick == 0) {
					this.playSound(SoundEvents.RAVAGER_ROAR, 1.0F, 1.0F);
					this.roarTick = 20;
				}
			}
		}
	}

	private void stunEffect() {
		if (this.random.nextInt(6) == 0) {
			double d = this.getX() - (double)this.getBbWidth() * Math.sin((double)(this.yBodyRot * (float) (Math.PI / 180.0))) + (this.random.nextDouble() * 0.6 - 0.3);
			double e = this.getY() + (double)this.getBbHeight() - 0.3;
			double f = this.getZ() + (double)this.getBbWidth() * Math.cos((double)(this.yBodyRot * (float) (Math.PI / 180.0))) + (this.random.nextDouble() * 0.6 - 0.3);
			this.level().addParticle(ColorParticleOption.create(ParticleTypes.ENTITY_EFFECT, 0.49803922F, 0.5137255F, 0.57254905F), d, e, f, 0.0, 0.0, 0.0);
		}
	}

	@Override
	protected boolean isImmobile() {
		return super.isImmobile() || this.attackTick > 0 || this.stunnedTick > 0 || this.roarTick > 0;
	}

	@Override
	public boolean hasLineOfSight(Entity entity) {
		return this.stunnedTick <= 0 && this.roarTick <= 0 ? super.hasLineOfSight(entity) : false;
	}

	@Override
	protected void blockedByShield(LivingEntity livingEntity) {
		if (this.roarTick == 0) {
			if (this.random.nextDouble() < 0.5) {
				this.stunnedTick = 40;
				this.playSound(SoundEvents.RAVAGER_STUNNED, 1.0F, 1.0F);
				this.level().broadcastEntityEvent(this, (byte)39);
				livingEntity.push(this);
			} else {
				this.strongKnockback(livingEntity);
			}

			livingEntity.hurtMarked = true;
		}
	}

	private void roar() {
		if (this.isAlive()) {
			if (this.level() instanceof ServerLevel serverLevel) {
				Predicate<Entity> predicate = serverLevel.getGameRules().getBoolean(GameRules.RULE_MOBGRIEFING) ? ROAR_TARGET_WITH_GRIEFING : ROAR_TARGET_WITHOUT_GRIEFING;

				for (LivingEntity livingEntity : this.level().getEntitiesOfClass(LivingEntity.class, this.getBoundingBox().inflate(4.0), predicate)) {
					if (!(livingEntity instanceof AbstractIllager)) {
						livingEntity.hurtServer(serverLevel, this.damageSources().mobAttack(this), 6.0F);
					}

					if (!(livingEntity instanceof Player)) {
						this.strongKnockback(livingEntity);
					}
				}

				this.gameEvent(GameEvent.ENTITY_ACTION);
			} else {
				for (LivingEntity livingEntity2 : this.level().getEntitiesOfClass(LivingEntity.class, this.getBoundingBox().inflate(4.0), ROAR_TARGET_ON_CLIENT)) {
					this.strongKnockback(livingEntity2);
				}

				Vec3 vec3 = this.getBoundingBox().getCenter();

				for (int i = 0; i < 40; i++) {
					double d = this.random.nextGaussian() * 0.2;
					double e = this.random.nextGaussian() * 0.2;
					double f = this.random.nextGaussian() * 0.2;
					this.level().addParticle(ParticleTypes.POOF, vec3.x, vec3.y, vec3.z, d, e, f);
				}
			}
		}
	}

	private void strongKnockback(Entity entity) {
		double d = entity.getX() - this.getX();
		double e = entity.getZ() - this.getZ();
		double f = Math.max(d * d + e * e, 0.001);
		entity.push(d / f * 4.0, 0.2, e / f * 4.0);
	}

	@Override
	public void handleEntityEvent(byte b) {
		if (b == 4) {
			this.attackTick = 10;
			this.playSound(SoundEvents.RAVAGER_ATTACK, 1.0F, 1.0F);
		} else if (b == 39) {
			this.stunnedTick = 40;
		}

		super.handleEntityEvent(b);
	}

	public int getAttackTick() {
		return this.attackTick;
	}

	public int getStunnedTick() {
		return this.stunnedTick;
	}

	public int getRoarTick() {
		return this.roarTick;
	}

	@Override
	public boolean doHurtTarget(ServerLevel serverLevel, Entity entity) {
		this.attackTick = 10;
		serverLevel.broadcastEntityEvent(this, (byte)4);
		this.playSound(SoundEvents.RAVAGER_ATTACK, 1.0F, 1.0F);
		return super.doHurtTarget(serverLevel, entity);
	}

	@Nullable
	@Override
	protected SoundEvent getAmbientSound() {
		return SoundEvents.RAVAGER_AMBIENT;
	}

	@Override
	protected SoundEvent getHurtSound(DamageSource damageSource) {
		return SoundEvents.RAVAGER_HURT;
	}

	@Override
	protected SoundEvent getDeathSound() {
		return SoundEvents.RAVAGER_DEATH;
	}

	@Override
	protected void playStepSound(BlockPos blockPos, BlockState blockState) {
		this.playSound(SoundEvents.RAVAGER_STEP, 0.15F, 1.0F);
	}

	@Override
	public boolean checkSpawnObstruction(LevelReader levelReader) {
		return !levelReader.containsAnyLiquid(this.getBoundingBox());
	}

	@Override
	public void applyRaidBuffs(ServerLevel serverLevel, int i, boolean bl) {
	}

	@Override
	public boolean canBeLeader() {
		return false;
	}

	@Override
	protected AABB getAttackBoundingBox() {
		AABB aABB = super.getAttackBoundingBox();
		return aABB.deflate(0.05, 0.0, 0.05);
	}
}
