package net.minecraft.world.entity.monster;

import java.util.EnumSet;
import java.util.Optional;
import java.util.Random;
import java.util.UUID;
import java.util.function.Predicate;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.IndirectEntityDamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.BlockPathTypes;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

public class EnderMan extends Monster {
	private static final UUID SPEED_MODIFIER_ATTACKING_UUID = UUID.fromString("020E0DFB-87AE-4653-9556-831010E291A0");
	private static final AttributeModifier SPEED_MODIFIER_ATTACKING = new AttributeModifier(
			SPEED_MODIFIER_ATTACKING_UUID, "Attacking speed boost", 0.15F, AttributeModifier.Operation.ADDITION
		)
		.setSerialize(false);
	private static final EntityDataAccessor<Optional<BlockState>> DATA_CARRY_STATE = SynchedEntityData.defineId(EnderMan.class, EntityDataSerializers.BLOCK_STATE);
	private static final EntityDataAccessor<Boolean> DATA_CREEPY = SynchedEntityData.defineId(EnderMan.class, EntityDataSerializers.BOOLEAN);
	private static final EntityDataAccessor<Boolean> DATA_STARED_AT = SynchedEntityData.defineId(EnderMan.class, EntityDataSerializers.BOOLEAN);
	private static final Predicate<LivingEntity> ENDERMITE_SELECTOR = livingEntity -> livingEntity instanceof Endermite
			&& ((Endermite)livingEntity).isPlayerSpawned();
	private int lastStareSound = Integer.MIN_VALUE;
	private int targetChangeTime;

	public EnderMan(EntityType<? extends EnderMan> entityType, Level level) {
		super(entityType, level);
		this.maxUpStep = 1.0F;
		this.setPathfindingMalus(BlockPathTypes.WATER, -1.0F);
	}

	@Override
	protected void registerGoals() {
		this.goalSelector.addGoal(0, new FloatGoal(this));
		this.goalSelector.addGoal(1, new EnderMan.EndermanFreezeWhenLookedAt(this));
		this.goalSelector.addGoal(2, new MeleeAttackGoal(this, 1.0, false));
		this.goalSelector.addGoal(7, new WaterAvoidingRandomStrollGoal(this, 1.0, 0.0F));
		this.goalSelector.addGoal(8, new LookAtPlayerGoal(this, Player.class, 8.0F));
		this.goalSelector.addGoal(8, new RandomLookAroundGoal(this));
		this.goalSelector.addGoal(10, new EnderMan.EndermanLeaveBlockGoal(this));
		this.goalSelector.addGoal(11, new EnderMan.EndermanTakeBlockGoal(this));
		this.targetSelector.addGoal(1, new EnderMan.EndermanLookForPlayerGoal(this));
		this.targetSelector.addGoal(2, new HurtByTargetGoal(this));
		this.targetSelector.addGoal(3, new NearestAttackableTargetGoal(this, Endermite.class, 10, true, false, ENDERMITE_SELECTOR));
	}

	@Override
	protected void registerAttributes() {
		super.registerAttributes();
		this.getAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(40.0);
		this.getAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(0.3F);
		this.getAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).setBaseValue(7.0);
		this.getAttribute(SharedMonsterAttributes.FOLLOW_RANGE).setBaseValue(64.0);
	}

	@Override
	public void setTarget(@Nullable LivingEntity livingEntity) {
		super.setTarget(livingEntity);
		AttributeInstance attributeInstance = this.getAttribute(SharedMonsterAttributes.MOVEMENT_SPEED);
		if (livingEntity == null) {
			this.targetChangeTime = 0;
			this.entityData.set(DATA_CREEPY, false);
			this.entityData.set(DATA_STARED_AT, false);
			attributeInstance.removeModifier(SPEED_MODIFIER_ATTACKING);
		} else {
			this.targetChangeTime = this.tickCount;
			this.entityData.set(DATA_CREEPY, true);
			if (!attributeInstance.hasModifier(SPEED_MODIFIER_ATTACKING)) {
				attributeInstance.addModifier(SPEED_MODIFIER_ATTACKING);
			}
		}
	}

	@Override
	protected void defineSynchedData() {
		super.defineSynchedData();
		this.entityData.define(DATA_CARRY_STATE, Optional.empty());
		this.entityData.define(DATA_CREEPY, false);
		this.entityData.define(DATA_STARED_AT, false);
	}

	public void playStareSound() {
		if (this.tickCount >= this.lastStareSound + 400) {
			this.lastStareSound = this.tickCount;
			if (!this.isSilent()) {
				this.level.playLocalSound(this.getX(), this.getEyeY(), this.getZ(), SoundEvents.ENDERMAN_STARE, this.getSoundSource(), 2.5F, 1.0F, false);
			}
		}
	}

	@Override
	public void onSyncedDataUpdated(EntityDataAccessor<?> entityDataAccessor) {
		if (DATA_CREEPY.equals(entityDataAccessor) && this.hasBeenStaredAt() && this.level.isClientSide) {
			this.playStareSound();
		}

		super.onSyncedDataUpdated(entityDataAccessor);
	}

	@Override
	public void addAdditionalSaveData(CompoundTag compoundTag) {
		super.addAdditionalSaveData(compoundTag);
		BlockState blockState = this.getCarriedBlock();
		if (blockState != null) {
			compoundTag.put("carriedBlockState", NbtUtils.writeBlockState(blockState));
		}
	}

	@Override
	public void readAdditionalSaveData(CompoundTag compoundTag) {
		super.readAdditionalSaveData(compoundTag);
		BlockState blockState = null;
		if (compoundTag.contains("carriedBlockState", 10)) {
			blockState = NbtUtils.readBlockState(compoundTag.getCompound("carriedBlockState"));
			if (blockState.isAir()) {
				blockState = null;
			}
		}

		this.setCarriedBlock(blockState);
	}

	private boolean isLookingAtMe(Player player) {
		ItemStack itemStack = player.inventory.armor.get(3);
		if (itemStack.getItem() == Blocks.CARVED_PUMPKIN.asItem()) {
			return false;
		} else {
			Vec3 vec3 = player.getViewVector(1.0F).normalize();
			Vec3 vec32 = new Vec3(this.getX() - player.getX(), this.getEyeY() - player.getEyeY(), this.getZ() - player.getZ());
			double d = vec32.length();
			vec32 = vec32.normalize();
			double e = vec3.dot(vec32);
			return e > 1.0 - 0.025 / d ? player.canSee(this) : false;
		}
	}

	@Override
	protected float getStandingEyeHeight(Pose pose, EntityDimensions entityDimensions) {
		return 2.55F;
	}

	@Override
	public void aiStep() {
		if (this.level.isClientSide) {
			for (int i = 0; i < 2; i++) {
				this.level
					.addParticle(
						ParticleTypes.PORTAL,
						this.getRandomX(0.5),
						this.getRandomY() - 0.25,
						this.getRandomZ(0.5),
						(this.random.nextDouble() - 0.5) * 2.0,
						-this.random.nextDouble(),
						(this.random.nextDouble() - 0.5) * 2.0
					);
			}
		}

		this.jumping = false;
		super.aiStep();
	}

	@Override
	protected void customServerAiStep() {
		if (this.isInWaterRainOrBubble()) {
			this.hurt(DamageSource.DROWN, 1.0F);
		}

		if (this.level.isDay() && this.tickCount >= this.targetChangeTime + 600) {
			float f = this.getBrightness();
			if (f > 0.5F && this.level.canSeeSky(new BlockPos(this)) && this.random.nextFloat() * 30.0F < (f - 0.4F) * 2.0F) {
				this.setTarget(null);
				this.teleport();
			}
		}

		super.customServerAiStep();
	}

	protected boolean teleport() {
		if (!this.level.isClientSide() && this.isAlive()) {
			double d = this.getX() + (this.random.nextDouble() - 0.5) * 64.0;
			double e = this.getY() + (double)(this.random.nextInt(64) - 32);
			double f = this.getZ() + (this.random.nextDouble() - 0.5) * 64.0;
			return this.teleport(d, e, f);
		} else {
			return false;
		}
	}

	private boolean teleportTowards(Entity entity) {
		Vec3 vec3 = new Vec3(this.getX() - entity.getX(), this.getY(0.5) - entity.getEyeY(), this.getZ() - entity.getZ());
		vec3 = vec3.normalize();
		double d = 16.0;
		double e = this.getX() + (this.random.nextDouble() - 0.5) * 8.0 - vec3.x * 16.0;
		double f = this.getY() + (double)(this.random.nextInt(16) - 8) - vec3.y * 16.0;
		double g = this.getZ() + (this.random.nextDouble() - 0.5) * 8.0 - vec3.z * 16.0;
		return this.teleport(e, f, g);
	}

	private boolean teleport(double d, double e, double f) {
		BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos(d, e, f);

		while (mutableBlockPos.getY() > 0 && !this.level.getBlockState(mutableBlockPos).getMaterial().blocksMotion()) {
			mutableBlockPos.move(Direction.DOWN);
		}

		BlockState blockState = this.level.getBlockState(mutableBlockPos);
		boolean bl = blockState.getMaterial().blocksMotion();
		boolean bl2 = blockState.getFluidState().is(FluidTags.WATER);
		if (bl && !bl2) {
			boolean bl3 = this.randomTeleport(d, e, f, true);
			if (bl3) {
				this.level.playSound(null, this.xo, this.yo, this.zo, SoundEvents.ENDERMAN_TELEPORT, this.getSoundSource(), 1.0F, 1.0F);
				this.playSound(SoundEvents.ENDERMAN_TELEPORT, 1.0F, 1.0F);
			}

			return bl3;
		} else {
			return false;
		}
	}

	@Override
	protected SoundEvent getAmbientSound() {
		return this.isCreepy() ? SoundEvents.ENDERMAN_SCREAM : SoundEvents.ENDERMAN_AMBIENT;
	}

	@Override
	protected SoundEvent getHurtSound(DamageSource damageSource) {
		return SoundEvents.ENDERMAN_HURT;
	}

	@Override
	protected SoundEvent getDeathSound() {
		return SoundEvents.ENDERMAN_DEATH;
	}

	@Override
	protected void dropCustomDeathLoot(DamageSource damageSource, int i, boolean bl) {
		super.dropCustomDeathLoot(damageSource, i, bl);
		BlockState blockState = this.getCarriedBlock();
		if (blockState != null) {
			this.spawnAtLocation(blockState.getBlock());
		}
	}

	public void setCarriedBlock(@Nullable BlockState blockState) {
		this.entityData.set(DATA_CARRY_STATE, Optional.ofNullable(blockState));
	}

	@Nullable
	public BlockState getCarriedBlock() {
		return (BlockState)this.entityData.get(DATA_CARRY_STATE).orElse(null);
	}

	@Override
	public boolean hurt(DamageSource damageSource, float f) {
		if (this.isInvulnerableTo(damageSource)) {
			return false;
		} else if (!(damageSource instanceof IndirectEntityDamageSource) && damageSource != DamageSource.FIREWORKS) {
			boolean bl = super.hurt(damageSource, f);
			if (!this.level.isClientSide() && damageSource.isBypassArmor() && this.random.nextInt(10) != 0) {
				this.teleport();
			}

			return bl;
		} else {
			for (int i = 0; i < 64; i++) {
				if (this.teleport()) {
					return true;
				}
			}

			return false;
		}
	}

	public boolean isCreepy() {
		return this.entityData.get(DATA_CREEPY);
	}

	public boolean hasBeenStaredAt() {
		return this.entityData.get(DATA_STARED_AT);
	}

	public void setBeingStaredAt() {
		this.entityData.set(DATA_STARED_AT, true);
	}

	static class EndermanFreezeWhenLookedAt extends Goal {
		private final EnderMan enderman;
		private LivingEntity target;

		public EndermanFreezeWhenLookedAt(EnderMan enderMan) {
			this.enderman = enderMan;
			this.setFlags(EnumSet.of(Goal.Flag.JUMP, Goal.Flag.MOVE));
		}

		@Override
		public boolean canUse() {
			this.target = this.enderman.getTarget();
			if (!(this.target instanceof Player)) {
				return false;
			} else {
				double d = this.target.distanceToSqr(this.enderman);
				return d > 256.0 ? false : this.enderman.isLookingAtMe((Player)this.target);
			}
		}

		@Override
		public void start() {
			this.enderman.getNavigation().stop();
		}

		@Override
		public void tick() {
			this.enderman.getLookControl().setLookAt(this.target.getX(), this.target.getEyeY(), this.target.getZ());
		}
	}

	static class EndermanLeaveBlockGoal extends Goal {
		private final EnderMan enderman;

		public EndermanLeaveBlockGoal(EnderMan enderMan) {
			this.enderman = enderMan;
		}

		@Override
		public boolean canUse() {
			if (this.enderman.getCarriedBlock() == null) {
				return false;
			} else {
				return !this.enderman.level.getGameRules().getBoolean(GameRules.RULE_MOBGRIEFING) ? false : this.enderman.getRandom().nextInt(2000) == 0;
			}
		}

		@Override
		public void tick() {
			Random random = this.enderman.getRandom();
			LevelAccessor levelAccessor = this.enderman.level;
			int i = Mth.floor(this.enderman.getX() - 1.0 + random.nextDouble() * 2.0);
			int j = Mth.floor(this.enderman.getY() + random.nextDouble() * 2.0);
			int k = Mth.floor(this.enderman.getZ() - 1.0 + random.nextDouble() * 2.0);
			BlockPos blockPos = new BlockPos(i, j, k);
			BlockState blockState = levelAccessor.getBlockState(blockPos);
			BlockPos blockPos2 = blockPos.below();
			BlockState blockState2 = levelAccessor.getBlockState(blockPos2);
			BlockState blockState3 = this.enderman.getCarriedBlock();
			if (blockState3 != null && this.canPlaceBlock(levelAccessor, blockPos, blockState3, blockState, blockState2, blockPos2)) {
				levelAccessor.setBlock(blockPos, blockState3, 3);
				this.enderman.setCarriedBlock(null);
			}
		}

		private boolean canPlaceBlock(
			LevelReader levelReader, BlockPos blockPos, BlockState blockState, BlockState blockState2, BlockState blockState3, BlockPos blockPos2
		) {
			return blockState2.isAir()
				&& !blockState3.isAir()
				&& blockState3.isCollisionShapeFullBlock(levelReader, blockPos2)
				&& blockState.canSurvive(levelReader, blockPos);
		}
	}

	static class EndermanLookForPlayerGoal extends NearestAttackableTargetGoal<Player> {
		private final EnderMan enderman;
		private Player pendingTarget;
		private int aggroTime;
		private int teleportTime;
		private final TargetingConditions startAggroTargetConditions;
		private final TargetingConditions continueAggroTargetConditions = new TargetingConditions().allowUnseeable();

		public EndermanLookForPlayerGoal(EnderMan enderMan) {
			super(enderMan, Player.class, false);
			this.enderman = enderMan;
			this.startAggroTargetConditions = new TargetingConditions()
				.range(this.getFollowDistance())
				.selector(livingEntity -> enderMan.isLookingAtMe((Player)livingEntity));
		}

		@Override
		public boolean canUse() {
			this.pendingTarget = this.enderman.level.getNearestPlayer(this.startAggroTargetConditions, this.enderman);
			return this.pendingTarget != null;
		}

		@Override
		public void start() {
			this.aggroTime = 5;
			this.teleportTime = 0;
			this.enderman.setBeingStaredAt();
		}

		@Override
		public void stop() {
			this.pendingTarget = null;
			super.stop();
		}

		@Override
		public boolean canContinueToUse() {
			if (this.pendingTarget != null) {
				if (!this.enderman.isLookingAtMe(this.pendingTarget)) {
					return false;
				} else {
					this.enderman.lookAt(this.pendingTarget, 10.0F, 10.0F);
					return true;
				}
			} else {
				return this.target != null && this.continueAggroTargetConditions.test(this.enderman, this.target) ? true : super.canContinueToUse();
			}
		}

		@Override
		public void tick() {
			if (this.pendingTarget != null) {
				if (--this.aggroTime <= 0) {
					this.target = this.pendingTarget;
					this.pendingTarget = null;
					super.start();
				}
			} else {
				if (this.target != null && !this.enderman.isPassenger()) {
					if (this.enderman.isLookingAtMe((Player)this.target)) {
						if (this.target.distanceToSqr(this.enderman) < 16.0) {
							this.enderman.teleport();
						}

						this.teleportTime = 0;
					} else if (this.target.distanceToSqr(this.enderman) > 256.0 && this.teleportTime++ >= 30 && this.enderman.teleportTowards(this.target)) {
						this.teleportTime = 0;
					}
				}

				super.tick();
			}
		}
	}

	static class EndermanTakeBlockGoal extends Goal {
		private final EnderMan enderman;

		public EndermanTakeBlockGoal(EnderMan enderMan) {
			this.enderman = enderMan;
		}

		@Override
		public boolean canUse() {
			if (this.enderman.getCarriedBlock() != null) {
				return false;
			} else {
				return !this.enderman.level.getGameRules().getBoolean(GameRules.RULE_MOBGRIEFING) ? false : this.enderman.getRandom().nextInt(20) == 0;
			}
		}

		@Override
		public void tick() {
			Random random = this.enderman.getRandom();
			Level level = this.enderman.level;
			int i = Mth.floor(this.enderman.getX() - 2.0 + random.nextDouble() * 4.0);
			int j = Mth.floor(this.enderman.getY() + random.nextDouble() * 3.0);
			int k = Mth.floor(this.enderman.getZ() - 2.0 + random.nextDouble() * 4.0);
			BlockPos blockPos = new BlockPos(i, j, k);
			BlockState blockState = level.getBlockState(blockPos);
			Block block = blockState.getBlock();
			Vec3 vec3 = new Vec3((double)Mth.floor(this.enderman.getX()) + 0.5, (double)j + 0.5, (double)Mth.floor(this.enderman.getZ()) + 0.5);
			Vec3 vec32 = new Vec3((double)i + 0.5, (double)j + 0.5, (double)k + 0.5);
			BlockHitResult blockHitResult = level.clip(new ClipContext(vec3, vec32, ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, this.enderman));
			boolean bl = blockHitResult.getBlockPos().equals(blockPos);
			if (block.is(BlockTags.ENDERMAN_HOLDABLE) && bl) {
				this.enderman.setCarriedBlock(blockState);
				level.removeBlock(blockPos, false);
			}
		}
	}
}
