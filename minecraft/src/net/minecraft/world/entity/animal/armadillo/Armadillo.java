package net.minecraft.world.entity.animal.armadillo;

import com.mojang.serialization.Dynamic;
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
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.util.StringRepresentable;
import net.minecraft.util.TimeUtil;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.AnimationState;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.BodyRotationControl;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.AABB;

public class Armadillo extends Animal {
	public static final float BABY_SCALE = 0.6F;
	private static final int SCARE_SHELL_EXPOSURE = 5;
	private static final int SCARE_ANIMATION_DURATION = 8;
	public static final int SCARE_CHECK_INTERVAL = 60;
	private static final double SCARE_DISTANCE = 7.0;
	private static final EntityDataAccessor<Armadillo.ArmadilloState> ARMADILLO_STATE = SynchedEntityData.defineId(
		Armadillo.class, EntityDataSerializers.ARMADILLO_STATE
	);
	private long inStateTicks = 0L;
	public final AnimationState ballAnimationState = new AnimationState();
	public final AnimationState ballJumpAnimationState = new AnimationState();
	private int scuteTime;

	public Armadillo(EntityType<? extends Animal> entityType, Level level) {
		super(entityType, level);
		this.getNavigation().setCanFloat(true);
		this.scuteTime = this.pickNextScuteDropTime();
	}

	@Nullable
	@Override
	public AgeableMob getBreedOffspring(ServerLevel serverLevel, AgeableMob ageableMob) {
		return EntityType.ARMADILLO.create(serverLevel);
	}

	public static AttributeSupplier.Builder createAttributes() {
		return Mob.createMobAttributes().add(Attributes.MAX_HEALTH, 12.0).add(Attributes.MOVEMENT_SPEED, 0.14);
	}

	@Override
	protected void defineSynchedData() {
		super.defineSynchedData();
		this.entityData.define(ARMADILLO_STATE, Armadillo.ArmadilloState.IDLE);
	}

	public boolean isScared() {
		return this.entityData.get(ARMADILLO_STATE) != Armadillo.ArmadilloState.IDLE;
	}

	public boolean shouldHideInShell() {
		Armadillo.ArmadilloState armadilloState = this.getState();
		return armadilloState == Armadillo.ArmadilloState.SCARED || armadilloState == Armadillo.ArmadilloState.ROLLING && this.inStateTicks > 5L;
	}

	public boolean shouldSwitchToScaredState() {
		return this.getState() == Armadillo.ArmadilloState.ROLLING && this.inStateTicks > 8L;
	}

	private Armadillo.ArmadilloState getState() {
		return this.entityData.get(ARMADILLO_STATE);
	}

	@Override
	protected void sendDebugPackets() {
		super.sendDebugPackets();
		DebugPackets.sendEntityBrain(this);
	}

	public void switchToState(Armadillo.ArmadilloState armadilloState) {
		this.entityData.set(ARMADILLO_STATE, armadilloState);
	}

	private void setScared(boolean bl) {
		this.switchToState(bl ? Armadillo.ArmadilloState.ROLLING : Armadillo.ArmadilloState.IDLE);
	}

	@Override
	public void onSyncedDataUpdated(EntityDataAccessor<?> entityDataAccessor) {
		if (ARMADILLO_STATE.equals(entityDataAccessor)) {
			this.inStateTicks = 0L;
		}

		super.onSyncedDataUpdated(entityDataAccessor);
	}

	@Override
	protected Brain.Provider<Armadillo> brainProvider() {
		return ArmadilloAi.brainProvider();
	}

	@Override
	protected Brain<?> makeBrain(Dynamic<?> dynamic) {
		return ArmadilloAi.makeBrain(this.brainProvider().makeBrain(dynamic));
	}

	@Override
	protected void customServerAiStep() {
		this.level().getProfiler().push("armadilloBrain");
		((Brain<Armadillo>)this.brain).tick((ServerLevel)this.level(), this);
		this.level().getProfiler().pop();
		this.level().getProfiler().push("armadilloActivityUpdate");
		ArmadilloAi.updateActivity(this);
		this.level().getProfiler().pop();
		if (this.isAlive() && !this.isBaby() && --this.scuteTime <= 0) {
			this.playSound(SoundEvents.ARMADILLO_SCUTE_DROP, 1.0F, (this.random.nextFloat() - this.random.nextFloat()) * 0.2F + 1.0F);
			this.spawnAtLocation(Items.ARMADILLO_SCUTE);
			this.gameEvent(GameEvent.ENTITY_PLACE);
			this.scuteTime = this.pickNextScuteDropTime();
		}

		super.customServerAiStep();
	}

	private int pickNextScuteDropTime() {
		return this.random.nextInt(20 * TimeUtil.SECONDS_PER_MINUTE * 5) + 20 * TimeUtil.SECONDS_PER_MINUTE * 5;
	}

	@Override
	public void tick() {
		super.tick();
		if (this.level().isClientSide()) {
			this.setupAnimationStates();
		}

		this.inStateTicks++;
	}

	@Override
	public float getAgeScale() {
		return this.isBaby() ? 0.6F : 1.0F;
	}

	private void setupAnimationStates() {
		switch (this.getState()) {
			case IDLE:
				this.ballAnimationState.stop();
				this.ballJumpAnimationState.stop();
				break;
			case SCARED:
				this.ballAnimationState.startIfStopped(this.tickCount);
				this.ballJumpAnimationState.stop();
				break;
			case ROLLING:
				this.ballAnimationState.stop();
				this.ballJumpAnimationState.startIfStopped(this.tickCount);
		}
	}

	@Override
	public boolean isFood(ItemStack itemStack) {
		return ArmadilloAi.TEMPTATION_ITEM.test(itemStack);
	}

	public boolean isScaredBy(LivingEntity livingEntity) {
		if (!new AABB(this.position(), this.position()).inflate(7.0).contains(livingEntity.position())) {
			return false;
		} else if (livingEntity.getType().is(EntityTypeTags.UNDEAD)) {
			return true;
		} else {
			if (livingEntity instanceof Player player && (player.isSprinting() || player.isPassenger())) {
				return true;
			}

			return false;
		}
	}

	@Override
	public void addAdditionalSaveData(CompoundTag compoundTag) {
		super.addAdditionalSaveData(compoundTag);
		compoundTag.putString("state", this.getState().getSerializedName());
	}

	@Override
	public void readAdditionalSaveData(CompoundTag compoundTag) {
		super.readAdditionalSaveData(compoundTag);
		this.switchToState(Armadillo.ArmadilloState.fromId(compoundTag.getString("state")));
	}

	public void rollUp() {
		if (!this.isScared()) {
			this.stopInPlace();
			this.resetLove();
			this.gameEvent(GameEvent.ENTITY_ACTION);
			this.level().playSound(null, this.blockPosition(), SoundEvents.ARMADILLO_ROLL, this.getSoundSource(), 1.0F, 1.0F);
			this.setScared(true);
		}
	}

	public void rollOut(boolean bl) {
		if (this.isScared()) {
			this.gameEvent(GameEvent.ENTITY_ACTION);
			if (!bl) {
				this.level().playSound(null, this.blockPosition(), SoundEvents.ARMADILLO_UNROLL, this.getSoundSource(), 1.0F, 1.0F);
			}

			this.setScared(false);
		}
	}

	@Override
	protected void actuallyHurt(DamageSource damageSource, float f) {
		this.rollOut(true);
		super.actuallyHurt(damageSource, f);
	}

	@Override
	public InteractionResult mobInteract(Player player, InteractionHand interactionHand) {
		ItemStack itemStack = player.getItemInHand(interactionHand);
		if (this.level().isClientSide) {
			boolean bl = itemStack.is(Items.BRUSH);
			return bl ? InteractionResult.CONSUME : InteractionResult.PASS;
		} else if (itemStack.is(Items.BRUSH)) {
			if (!player.getAbilities().instabuild) {
				itemStack.hurtAndBreak(16, player, playerx -> playerx.broadcastBreakEvent(interactionHand));
			}

			this.brushOffScute();
			return InteractionResult.SUCCESS;
		} else {
			return super.mobInteract(player, interactionHand);
		}
	}

	public void brushOffScute() {
		this.spawnAtLocation(new ItemStack(Items.ARMADILLO_SCUTE));
		this.gameEvent(GameEvent.ENTITY_INTERACT);
		this.level().playSound(null, this.blockPosition(), SoundEvents.ARMADILLO_BRUSH, this.getSoundSource(), 1.0F, 1.0F);
	}

	public boolean canStayRolledUp() {
		return !this.isPanicking() && !this.isInLiquid() && !this.isLeashed();
	}

	@Override
	public void setInLove(@Nullable Player player) {
		super.setInLove(player);
		this.level().playSound(null, this.blockPosition(), SoundEvents.ARMADILLO_EAT, this.getSoundSource(), 1.0F, 1.0F);
	}

	@Override
	public boolean canFallInLove() {
		return super.canFallInLove() && !this.isScared();
	}

	@Override
	public SoundEvent getEatingSound(ItemStack itemStack) {
		return SoundEvents.ARMADILLO_EAT;
	}

	@Override
	protected SoundEvent getAmbientSound() {
		return this.isScared() ? null : SoundEvents.ARMADILLO_AMBIENT;
	}

	@Override
	protected SoundEvent getDeathSound() {
		return SoundEvents.ARMADILLO_DEATH;
	}

	@Override
	protected SoundEvent getHurtSound(DamageSource damageSource) {
		return SoundEvents.ARMADILLO_HURT;
	}

	@Override
	protected void playStepSound(BlockPos blockPos, BlockState blockState) {
		this.playSound(SoundEvents.ARMADILLO_STEP, 0.15F, 1.0F);
	}

	@Override
	protected BodyRotationControl createBodyControl() {
		return new BodyRotationControl(this) {
			@Override
			public void clientTick() {
				if (!Armadillo.this.isScared()) {
					super.clientTick();
				}
			}
		};
	}

	public static enum ArmadilloState implements StringRepresentable {
		IDLE("idle"),
		ROLLING("rolling"),
		SCARED("scared");

		private static StringRepresentable.EnumCodec<Armadillo.ArmadilloState> CODEC = StringRepresentable.fromEnum(Armadillo.ArmadilloState::values);
		final String id;

		private ArmadilloState(String string2) {
			this.id = string2;
		}

		public static Armadillo.ArmadilloState fromId(String string) {
			return (Armadillo.ArmadilloState)CODEC.byName(string, IDLE);
		}

		@Override
		public String getSerializedName() {
			return this.id;
		}
	}
}
