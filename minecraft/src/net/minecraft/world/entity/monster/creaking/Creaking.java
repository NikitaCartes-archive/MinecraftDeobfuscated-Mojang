package net.minecraft.world.entity.monster.creaking;

import com.mojang.serialization.Dynamic;
import java.util.List;
import java.util.function.DoubleSupplier;
import java.util.function.Predicate;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.game.DebugPackets;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.profiling.Profiler;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AnimationState;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.BodyRotationControl;
import net.minecraft.world.entity.ai.control.JumpControl;
import net.minecraft.world.entity.ai.control.LookControl;
import net.minecraft.world.entity.ai.control.MoveControl;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.navigation.GroundPathNavigation;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;

public class Creaking extends Monster {
	private static final EntityDataAccessor<Boolean> CAN_MOVE = SynchedEntityData.defineId(Creaking.class, EntityDataSerializers.BOOLEAN);
	private static final EntityDataAccessor<Boolean> IS_ACTIVE = SynchedEntityData.defineId(Creaking.class, EntityDataSerializers.BOOLEAN);
	private static final int ATTACK_ANIMATION_DURATION = 20;
	private static final int MAX_HEALTH = 1;
	private static final float ATTACK_DAMAGE = 2.0F;
	private static final float FOLLOW_RANGE = 32.0F;
	private static final float ACTIVATION_RANGE_SQ = 144.0F;
	public static final int ATTACK_INTERVAL = 40;
	private static final float MOVEMENT_SPEED_WHEN_FIGHTING = 0.3F;
	public static final float SPEED_MULTIPLIER_WHEN_IDLING = 0.2F;
	public static final int CREAKING_ORANGE = 16545810;
	public static final int CREAKING_GRAY = 6250335;
	private int attackAnimationRemainingTicks;
	public final AnimationState attackAnimationState = new AnimationState();
	public final AnimationState invulnerabilityAnimationState = new AnimationState();

	public Creaking(EntityType<? extends Creaking> entityType, Level level) {
		super(entityType, level);
		this.lookControl = new Creaking.CreakingLookControl(this);
		this.moveControl = new Creaking.CreakingMoveControl(this);
		this.jumpControl = new Creaking.CreakingJumpControl(this);
		GroundPathNavigation groundPathNavigation = (GroundPathNavigation)this.getNavigation();
		groundPathNavigation.setCanFloat(true);
		this.xpReward = 0;
	}

	@Override
	protected BodyRotationControl createBodyControl() {
		return new Creaking.CreakingBodyRotationControl(this);
	}

	@Override
	protected Brain.Provider<Creaking> brainProvider() {
		return CreakingAi.brainProvider();
	}

	@Override
	protected Brain<?> makeBrain(Dynamic<?> dynamic) {
		return CreakingAi.makeBrain(this.brainProvider().makeBrain(dynamic));
	}

	@Override
	protected void defineSynchedData(SynchedEntityData.Builder builder) {
		super.defineSynchedData(builder);
		builder.define(CAN_MOVE, true);
		builder.define(IS_ACTIVE, false);
	}

	public static AttributeSupplier.Builder createAttributes() {
		return Monster.createMonsterAttributes()
			.add(Attributes.MAX_HEALTH, 1.0)
			.add(Attributes.MOVEMENT_SPEED, 0.3F)
			.add(Attributes.ATTACK_DAMAGE, 2.0)
			.add(Attributes.FOLLOW_RANGE, 32.0)
			.add(Attributes.STEP_HEIGHT, 1.0);
	}

	public boolean canMove() {
		return this.entityData.get(CAN_MOVE);
	}

	@Override
	public boolean doHurtTarget(ServerLevel serverLevel, Entity entity) {
		if (!(entity instanceof LivingEntity)) {
			return false;
		} else {
			this.attackAnimationRemainingTicks = 20;
			this.level().broadcastEntityEvent(this, (byte)4);
			return super.doHurtTarget(serverLevel, entity);
		}
	}

	@Override
	public boolean isPushable() {
		return super.isPushable() && this.canMove();
	}

	@Override
	public Brain<Creaking> getBrain() {
		return (Brain<Creaking>)super.getBrain();
	}

	@Override
	protected void customServerAiStep(ServerLevel serverLevel) {
		ProfilerFiller profilerFiller = Profiler.get();
		profilerFiller.push("creakingBrain");
		this.getBrain().tick((ServerLevel)this.level(), this);
		profilerFiller.pop();
		CreakingAi.updateActivity(this);
	}

	@Override
	public void aiStep() {
		if (this.attackAnimationRemainingTicks > 0) {
			this.attackAnimationRemainingTicks--;
		}

		if (!this.level().isClientSide) {
			boolean bl = this.entityData.get(CAN_MOVE);
			boolean bl2 = this.checkCanMove();
			if (bl2 != bl) {
				this.gameEvent(GameEvent.ENTITY_ACTION);
				if (bl2) {
					this.makeSound(SoundEvents.CREAKING_UNFREEZE);
				} else {
					this.stopInPlace();
					this.makeSound(SoundEvents.CREAKING_FREEZE);
				}
			}

			this.entityData.set(CAN_MOVE, bl2);
		}

		super.aiStep();
	}

	@Override
	public void tick() {
		super.tick();
		if (this.level().isClientSide) {
			this.setupAnimationStates();
		}
	}

	private void setupAnimationStates() {
		this.attackAnimationState.animateWhen(this.attackAnimationRemainingTicks > 0, this.tickCount);
	}

	@Override
	public void handleEntityEvent(byte b) {
		if (b == 4) {
			this.attackAnimationRemainingTicks = 20;
			this.playAttackSound();
		} else {
			super.handleEntityEvent(b);
		}
	}

	@Override
	public void playAttackSound() {
		this.makeSound(SoundEvents.CREAKING_ATTACK);
	}

	@Override
	protected SoundEvent getAmbientSound() {
		return this.isActive() ? null : SoundEvents.CREAKING_AMBIENT;
	}

	@Override
	protected SoundEvent getHurtSound(DamageSource damageSource) {
		return SoundEvents.CREAKING_SWAY;
	}

	@Override
	protected SoundEvent getDeathSound() {
		return SoundEvents.CREAKING_DEATH;
	}

	@Override
	protected void playStepSound(BlockPos blockPos, BlockState blockState) {
		this.playSound(SoundEvents.CREAKING_STEP, 0.15F, 1.0F);
	}

	@Nullable
	@Override
	public LivingEntity getTarget() {
		return this.getTargetFromBrain();
	}

	@Override
	protected void sendDebugPackets() {
		super.sendDebugPackets();
		DebugPackets.sendEntityBrain(this);
	}

	@Override
	public void knockback(double d, double e, double f) {
		if (this.canMove()) {
			super.knockback(d, e, f);
		}
	}

	public boolean checkCanMove() {
		List<Player> list = (List<Player>)this.brain.getMemory(MemoryModuleType.NEAREST_PLAYERS).orElse(List.of());
		if (list.isEmpty()) {
			if (this.isActive()) {
				this.gameEvent(GameEvent.ENTITY_ACTION);
				this.makeSound(SoundEvents.CREAKING_DEACTIVATE);
				this.setIsActive(false);
			}

			return true;
		} else {
			Predicate<LivingEntity> predicate = this.isActive() ? LivingEntity.PLAYER_NOT_WEARING_DISGUISE_ITEM : livingEntity -> true;

			for (Player player : list) {
				if (this.isLookingAtMe(player, 0.5, false, true, predicate, new DoubleSupplier[]{this::getEyeY, this::getY, () -> (this.getEyeY() + this.getY()) / 2.0})) {
					if (this.isActive()) {
						return false;
					}

					if (player.distanceToSqr(this) < 144.0) {
						this.gameEvent(GameEvent.ENTITY_ACTION);
						this.makeSound(SoundEvents.CREAKING_ACTIVATE);
						this.setIsActive(true);
						return false;
					}
				}
			}

			return true;
		}
	}

	public void setIsActive(boolean bl) {
		this.entityData.set(IS_ACTIVE, bl);
	}

	public boolean isActive() {
		return this.entityData.get(IS_ACTIVE);
	}

	@Override
	public float getWalkTargetValue(BlockPos blockPos, LevelReader levelReader) {
		return 0.0F;
	}

	class CreakingBodyRotationControl extends BodyRotationControl {
		public CreakingBodyRotationControl(final Creaking creaking2) {
			super(creaking2);
		}

		@Override
		public void clientTick() {
			if (Creaking.this.canMove()) {
				super.clientTick();
			}
		}
	}

	class CreakingJumpControl extends JumpControl {
		public CreakingJumpControl(final Creaking creaking2) {
			super(creaking2);
		}

		@Override
		public void tick() {
			if (Creaking.this.canMove()) {
				super.tick();
			} else {
				Creaking.this.setJumping(false);
			}
		}
	}

	class CreakingLookControl extends LookControl {
		public CreakingLookControl(final Creaking creaking2) {
			super(creaking2);
		}

		@Override
		public void tick() {
			if (Creaking.this.canMove()) {
				super.tick();
			}
		}
	}

	class CreakingMoveControl extends MoveControl {
		public CreakingMoveControl(final Creaking creaking2) {
			super(creaking2);
		}

		@Override
		public void tick() {
			if (Creaking.this.canMove()) {
				super.tick();
			}
		}
	}
}
