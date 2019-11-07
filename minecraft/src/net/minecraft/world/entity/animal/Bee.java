package net.minecraft.world.entity.animal;

import java.util.EnumSet;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Predicate;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.protocol.game.DebugPackets;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.Tag;
import net.minecraft.util.Mth;
import net.minecraft.world.Difficulty;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.AgableMob;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.ai.control.FlyingMoveControl;
import net.minecraft.world.entity.ai.control.LookControl;
import net.minecraft.world.entity.ai.goal.BreedGoal;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.FollowParentGoal;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.TemptGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.navigation.FlyingPathNavigation;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.ai.util.RandomPos;
import net.minecraft.world.entity.ai.village.poi.PoiManager;
import net.minecraft.world.entity.ai.village.poi.PoiRecord;
import net.minecraft.world.entity.ai.village.poi.PoiType;
import net.minecraft.world.entity.monster.SharedMonsterAttributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.DoublePlantBlock;
import net.minecraft.world.level.block.StemBlock;
import net.minecraft.world.level.block.SweetBerryBushBlock;
import net.minecraft.world.level.block.entity.BeehiveBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.pathfinder.BlockPathTypes;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.phys.Vec3;

public class Bee extends Animal implements FlyingAnimal {
	private static final EntityDataAccessor<Byte> DATA_FLAGS_ID = SynchedEntityData.defineId(Bee.class, EntityDataSerializers.BYTE);
	private static final EntityDataAccessor<Integer> ANGER_TIME = SynchedEntityData.defineId(Bee.class, EntityDataSerializers.INT);
	private UUID lastHurtByUUID;
	private float rollAmount;
	private float rollAmountO;
	private int timeSinceSting;
	private int ticksSincePollination;
	private int cannotEnterHiveTicks;
	private int numCropsGrownSincePollination;
	private BlockPos savedFlowerPos = BlockPos.ZERO;
	private BlockPos hivePos = BlockPos.ZERO;
	private Bee.BeePollinateGoal beePollinateGoal;
	private int underWaterTicks;

	public Bee(EntityType<? extends Bee> entityType, Level level) {
		super(entityType, level);
		this.moveControl = new FlyingMoveControl(this, 20, true);
		this.lookControl = new Bee.BeeLookControl(this);
		this.setPathfindingMalus(BlockPathTypes.WATER, -1.0F);
		this.setPathfindingMalus(BlockPathTypes.COCOA, -1.0F);
		this.setPathfindingMalus(BlockPathTypes.FENCE, -1.0F);
	}

	@Override
	protected void defineSynchedData() {
		super.defineSynchedData();
		this.entityData.define(DATA_FLAGS_ID, (byte)0);
		this.entityData.define(ANGER_TIME, 0);
	}

	@Override
	protected void registerGoals() {
		this.goalSelector.addGoal(0, new Bee.BeeLocateHiveGoal());
		this.goalSelector.addGoal(0, new Bee.BeeAttackGoal(this, 1.4F, true));
		this.goalSelector.addGoal(1, new Bee.BeeEnterHiveGoal());
		this.goalSelector.addGoal(2, new BreedGoal(this, 1.0));
		this.goalSelector.addGoal(3, new TemptGoal(this, 1.25, Ingredient.of(ItemTags.FLOWERS), false));
		this.beePollinateGoal = new Bee.BeePollinateGoal();
		this.goalSelector.addGoal(4, this.beePollinateGoal);
		this.goalSelector.addGoal(5, new FollowParentGoal(this, 1.25));
		this.goalSelector.addGoal(5, new Bee.BeeGoToHiveGoal());
		this.goalSelector.addGoal(6, new Bee.BeeGoToKnownFlowerGoal());
		this.goalSelector.addGoal(7, new Bee.BeeGrowCropGoal());
		this.goalSelector.addGoal(8, new Bee.BeeWanderGoal());
		this.goalSelector.addGoal(9, new FloatGoal(this));
		this.targetSelector.addGoal(1, new Bee.BeeHurtByOtherGoal(this).setAlertOthers(new Class[0]));
		this.targetSelector.addGoal(2, new Bee.BeeBecomeAngryTargetGoal(this));
	}

	@Override
	public void addAdditionalSaveData(CompoundTag compoundTag) {
		super.addAdditionalSaveData(compoundTag);
		compoundTag.put("HivePos", NbtUtils.writeBlockPos(this.hivePos));
		compoundTag.put("FlowerPos", NbtUtils.writeBlockPos(this.savedFlowerPos));
		compoundTag.putBoolean("HasNectar", this.hasNectar());
		compoundTag.putBoolean("HasStung", this.hasStung());
		compoundTag.putInt("TicksSincePollination", this.ticksSincePollination);
		compoundTag.putInt("CannotEnterHiveTicks", this.cannotEnterHiveTicks);
		compoundTag.putInt("CropsGrownSincePollination", this.numCropsGrownSincePollination);
		compoundTag.putInt("Anger", this.getAngerTime());
		if (this.lastHurtByUUID != null) {
			compoundTag.putString("HurtBy", this.lastHurtByUUID.toString());
		} else {
			compoundTag.putString("HurtBy", "");
		}
	}

	@Override
	public void readAdditionalSaveData(CompoundTag compoundTag) {
		this.hivePos = NbtUtils.readBlockPos(compoundTag.getCompound("HivePos"));
		this.savedFlowerPos = NbtUtils.readBlockPos(compoundTag.getCompound("FlowerPos"));
		super.readAdditionalSaveData(compoundTag);
		this.setHasNectar(compoundTag.getBoolean("HasNectar"));
		this.setHasStung(compoundTag.getBoolean("HasStung"));
		this.setAngerTime(compoundTag.getInt("Anger"));
		this.ticksSincePollination = compoundTag.getInt("TicksSincePollination");
		this.cannotEnterHiveTicks = compoundTag.getInt("CannotEnterHiveTicks");
		this.numCropsGrownSincePollination = compoundTag.getInt("NumCropsGrownSincePollination");
		String string = compoundTag.getString("HurtBy");
		if (!string.isEmpty()) {
			this.lastHurtByUUID = UUID.fromString(string);
			Player player = this.level.getPlayerByUUID(this.lastHurtByUUID);
			this.setLastHurtByMob(player);
			if (player != null) {
				this.lastHurtByPlayer = player;
				this.lastHurtByPlayerTime = this.getLastHurtByMobTimestamp();
			}
		}
	}

	@Override
	public boolean doHurtTarget(Entity entity) {
		boolean bl = entity.hurt(DamageSource.sting(this), (float)((int)this.getAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).getValue()));
		if (bl) {
			this.doEnchantDamageEffects(this, entity);
			if (entity instanceof LivingEntity) {
				((LivingEntity)entity).setStingerCount(((LivingEntity)entity).getStingerCount() + 1);
				int i = 0;
				if (this.level.getDifficulty() == Difficulty.NORMAL) {
					i = 10;
				} else if (this.level.getDifficulty() == Difficulty.HARD) {
					i = 18;
				}

				if (i > 0) {
					((LivingEntity)entity).addEffect(new MobEffectInstance(MobEffects.POISON, i * 20, 0));
				}
			}

			this.setHasStung(true);
			this.setTarget(null);
			this.playSound(SoundEvents.BEE_STING, 1.0F, 1.0F);
		}

		return bl;
	}

	@Override
	public void tick() {
		super.tick();
		if (this.hasNectar() && this.getCropsGrownSincePollination() < 10 && this.random.nextFloat() < 0.05F) {
			for (int i = 0; i < this.random.nextInt(2) + 1; i++) {
				this.spawnFluidParticle(
					this.level, this.getX() - 0.3F, this.getX() + 0.3F, this.getZ() - 0.3F, this.getZ() + 0.3F, this.getY(0.5), ParticleTypes.FALLING_NECTAR
				);
			}
		}

		this.updateRollAmount();
	}

	private void spawnFluidParticle(Level level, double d, double e, double f, double g, double h, ParticleOptions particleOptions) {
		level.addParticle(particleOptions, Mth.lerp(level.random.nextDouble(), d, e), h, Mth.lerp(level.random.nextDouble(), f, g), 0.0, 0.0, 0.0);
	}

	public BlockPos getSavedFlowerPos() {
		return this.savedFlowerPos;
	}

	public boolean hasSavedFlowerPos() {
		return this.savedFlowerPos != BlockPos.ZERO;
	}

	public void setSavedFlowerPos(BlockPos blockPos) {
		this.savedFlowerPos = blockPos;
	}

	private boolean canEnterHive() {
		if (this.cannotEnterHiveTicks > 0) {
			return false;
		} else if (!this.hasHive()) {
			return false;
		} else if (this.hasStung()) {
			return false;
		} else {
			boolean bl = this.ticksSincePollination > 3600;
			if (!bl && !this.hasNectar() && this.level.isDay() && !this.level.isRaining()) {
				return false;
			} else {
				BlockEntity blockEntity = this.level.getBlockEntity(this.hivePos);
				return blockEntity instanceof BeehiveBlockEntity && !((BeehiveBlockEntity)blockEntity).isFireNearby();
			}
		}
	}

	public void setCannotEnterHiveTicks(int i) {
		this.cannotEnterHiveTicks = i;
	}

	@Environment(EnvType.CLIENT)
	public float getRollAmount(float f) {
		return Mth.lerp(f, this.rollAmountO, this.rollAmount);
	}

	private void updateRollAmount() {
		this.rollAmountO = this.rollAmount;
		if (this.isRolling()) {
			this.rollAmount = Math.min(1.0F, this.rollAmount + 0.2F);
		} else {
			this.rollAmount = Math.max(0.0F, this.rollAmount - 0.24F);
		}
	}

	@Override
	public void setLastHurtByMob(@Nullable LivingEntity livingEntity) {
		super.setLastHurtByMob(livingEntity);
		if (livingEntity != null) {
			this.lastHurtByUUID = livingEntity.getUUID();
		}
	}

	@Override
	protected void customServerAiStep() {
		boolean bl = this.hasStung();
		if (this.isInWaterOrBubble()) {
			this.underWaterTicks++;
		} else {
			this.underWaterTicks = 0;
		}

		if (this.underWaterTicks > 20) {
			this.hurt(DamageSource.DROWN, 1.0F);
		}

		if (bl) {
			this.timeSinceSting++;
			if (this.timeSinceSting % 5 == 0 && this.random.nextInt(Mth.clamp(1200 - this.timeSinceSting, 1, 1200)) == 0) {
				this.hurt(DamageSource.GENERIC, this.getHealth());
			}
		}

		if (this.isAngry()) {
			int i = this.getAngerTime();
			this.setAngerTime(i - 1);
			LivingEntity livingEntity = this.getTarget();
			if (i == 0 && livingEntity != null) {
				this.makeAngry(livingEntity);
			}
		}

		if (!this.hasNectar()) {
			this.ticksSincePollination++;
		}
	}

	public void resetTicksSincePollination() {
		this.ticksSincePollination = 0;
	}

	public boolean isAngry() {
		return this.getAngerTime() > 0;
	}

	public int getAngerTime() {
		return this.entityData.get(ANGER_TIME);
	}

	public void setAngerTime(int i) {
		this.entityData.set(ANGER_TIME, i);
	}

	public boolean hasHive() {
		return this.hivePos != BlockPos.ZERO;
	}

	@Override
	protected void sendDebugPackets() {
		super.sendDebugPackets();
		DebugPackets.sendBeeInfo(this);
	}

	private int getCropsGrownSincePollination() {
		return this.numCropsGrownSincePollination;
	}

	public void resetNumCropsGrownSincePollination() {
		this.numCropsGrownSincePollination = 0;
	}

	private void incrementNumCropsGrownSincePollination() {
		this.numCropsGrownSincePollination++;
	}

	@Override
	public void aiStep() {
		super.aiStep();
		if (!this.level.isClientSide) {
			if (this.cannotEnterHiveTicks > 0) {
				this.cannotEnterHiveTicks--;
			}

			boolean bl = this.isAngry() && !this.hasStung() && this.getTarget() != null && this.getTarget().distanceToSqr(this) < 4.0;
			this.setRolling(bl);
			if (this.tickCount % 20 == 0 && !this.isHiveValid()) {
				this.hivePos = BlockPos.ZERO;
			}
		}
	}

	private boolean isHiveValid() {
		if (!this.hasHive()) {
			return false;
		} else {
			BlockEntity blockEntity = this.level.getBlockEntity(this.hivePos);
			return blockEntity != null && blockEntity.getType() == BlockEntityType.BEEHIVE;
		}
	}

	public boolean hasNectar() {
		return this.getFlag(8);
	}

	public void setHasNectar(boolean bl) {
		this.setFlag(8, bl);
	}

	public boolean hasStung() {
		return this.getFlag(4);
	}

	public void setHasStung(boolean bl) {
		this.setFlag(4, bl);
	}

	public boolean isRolling() {
		return this.getFlag(2);
	}

	public void setRolling(boolean bl) {
		this.setFlag(2, bl);
	}

	private void setFlag(int i, boolean bl) {
		if (bl) {
			this.entityData.set(DATA_FLAGS_ID, (byte)(this.entityData.get(DATA_FLAGS_ID) | i));
		} else {
			this.entityData.set(DATA_FLAGS_ID, (byte)(this.entityData.get(DATA_FLAGS_ID) & ~i));
		}
	}

	private boolean getFlag(int i) {
		return (this.entityData.get(DATA_FLAGS_ID) & i) != 0;
	}

	@Override
	protected void registerAttributes() {
		super.registerAttributes();
		this.getAttributes().registerAttribute(SharedMonsterAttributes.FLYING_SPEED);
		this.getAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(10.0);
		this.getAttribute(SharedMonsterAttributes.FLYING_SPEED).setBaseValue(0.6F);
		this.getAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(0.3F);
		this.getAttributes().registerAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).setBaseValue(2.0);
	}

	@Override
	protected PathNavigation createNavigation(Level level) {
		FlyingPathNavigation flyingPathNavigation = new FlyingPathNavigation(this, level) {
			@Override
			public boolean isStableDestination(BlockPos blockPos) {
				return !this.level.getBlockState(blockPos.below()).isAir();
			}

			@Override
			public void tick() {
				if (!Bee.this.beePollinateGoal.isPollinating()) {
					super.tick();
				}
			}
		};
		flyingPathNavigation.setCanOpenDoors(false);
		flyingPathNavigation.setCanFloat(false);
		flyingPathNavigation.setCanPassDoors(true);
		return flyingPathNavigation;
	}

	@Override
	public boolean isFood(ItemStack itemStack) {
		return itemStack.getItem().is(ItemTags.FLOWERS);
	}

	@Override
	protected void playStepSound(BlockPos blockPos, BlockState blockState) {
	}

	@Override
	protected SoundEvent getAmbientSound() {
		return null;
	}

	@Override
	protected SoundEvent getHurtSound(DamageSource damageSource) {
		return SoundEvents.BEE_HURT;
	}

	@Override
	protected SoundEvent getDeathSound() {
		return SoundEvents.BEE_DEATH;
	}

	@Override
	protected float getSoundVolume() {
		return 0.4F;
	}

	public Bee getBreedOffspring(AgableMob agableMob) {
		return EntityType.BEE.create(this.level);
	}

	@Override
	protected float getStandingEyeHeight(Pose pose, EntityDimensions entityDimensions) {
		return this.isBaby() ? entityDimensions.height * 0.5F : entityDimensions.height * 0.5F;
	}

	@Override
	public boolean causeFallDamage(float f, float g) {
		return false;
	}

	@Override
	protected void checkFallDamage(double d, boolean bl, BlockState blockState, BlockPos blockPos) {
	}

	@Override
	protected boolean makeFlySound() {
		return true;
	}

	public void dropOffNectar() {
		this.setHasNectar(false);
		this.resetNumCropsGrownSincePollination();
	}

	public boolean makeAngry(Entity entity) {
		this.setAngerTime(400 + this.random.nextInt(400));
		if (entity instanceof LivingEntity) {
			this.setLastHurtByMob((LivingEntity)entity);
		}

		return true;
	}

	@Override
	public boolean hurt(DamageSource damageSource, float f) {
		if (this.isInvulnerableTo(damageSource)) {
			return false;
		} else {
			Entity entity = damageSource.getEntity();
			if (!this.level.isClientSide && entity instanceof Player && !((Player)entity).isCreative() && this.canSee(entity) && !this.isNoAi()) {
				this.beePollinateGoal.stopPollinating();
				this.makeAngry(entity);
			}

			return super.hurt(damageSource, f);
		}
	}

	@Override
	public MobType getMobType() {
		return MobType.ARTHROPOD;
	}

	@Override
	protected void jumpInLiquid(Tag<Fluid> tag) {
		this.setDeltaMovement(this.getDeltaMovement().add(0.0, 0.01, 0.0));
	}

	abstract class BaseBeeGoal extends Goal {
		private BaseBeeGoal() {
		}

		public abstract boolean canBeeUse();

		public abstract boolean canBeeContinueToUse();

		@Override
		public boolean canUse() {
			return this.canBeeUse() && !Bee.this.isAngry();
		}

		@Override
		public boolean canContinueToUse() {
			return this.canBeeContinueToUse() && !Bee.this.isAngry();
		}
	}

	class BeeAttackGoal extends MeleeAttackGoal {
		public BeeAttackGoal(PathfinderMob pathfinderMob, double d, boolean bl) {
			super(pathfinderMob, d, bl);
		}

		@Override
		public boolean canUse() {
			return super.canUse() && Bee.this.isAngry() && !Bee.this.hasStung();
		}

		@Override
		public boolean canContinueToUse() {
			return super.canContinueToUse() && Bee.this.isAngry() && !Bee.this.hasStung();
		}
	}

	static class BeeBecomeAngryTargetGoal extends NearestAttackableTargetGoal<Player> {
		public BeeBecomeAngryTargetGoal(Bee bee) {
			super(bee, Player.class, true);
		}

		@Override
		public boolean canUse() {
			return this.beeCanTarget() && super.canUse();
		}

		@Override
		public boolean canContinueToUse() {
			boolean bl = this.beeCanTarget();
			if (bl && this.mob.getTarget() != null) {
				return super.canContinueToUse();
			} else {
				this.targetMob = null;
				return false;
			}
		}

		private boolean beeCanTarget() {
			Bee bee = (Bee)this.mob;
			return bee.isAngry() && !bee.hasStung();
		}
	}

	class BeeEnterHiveGoal extends Bee.BaseBeeGoal {
		private BeeEnterHiveGoal() {
		}

		@Override
		public boolean canBeeUse() {
			if (!Bee.this.canEnterHive()) {
				return false;
			} else {
				if (Bee.this.hivePos.distSqr(new BlockPos(Bee.this)) < 4.0) {
					BlockEntity blockEntity = Bee.this.level.getBlockEntity(Bee.this.hivePos);
					if (blockEntity instanceof BeehiveBlockEntity) {
						BeehiveBlockEntity beehiveBlockEntity = (BeehiveBlockEntity)blockEntity;
						if (!beehiveBlockEntity.isFull()) {
							return true;
						}

						Bee.this.hivePos = BlockPos.ZERO;
					}
				}

				return false;
			}
		}

		@Override
		public boolean canBeeContinueToUse() {
			return false;
		}

		@Override
		public void start() {
			BlockEntity blockEntity = Bee.this.level.getBlockEntity(Bee.this.hivePos);
			if (blockEntity instanceof BeehiveBlockEntity) {
				BeehiveBlockEntity beehiveBlockEntity = (BeehiveBlockEntity)blockEntity;
				beehiveBlockEntity.addOccupant(Bee.this, Bee.this.hasNectar());
			}
		}
	}

	abstract class BeeGoToBlockGoal extends Bee.BaseBeeGoal {
		protected boolean stuck = false;
		private int minThreshold;
		private int maxThreshold;

		public BeeGoToBlockGoal(int i, int j) {
			this.minThreshold = i;
			this.maxThreshold = j;
			this.setFlags(EnumSet.of(Goal.Flag.MOVE));
		}

		protected abstract BlockPos getTargetPos();

		@Override
		public boolean canBeeContinueToUse() {
			return this.isBeeWithinTargetPosRange();
		}

		boolean isBeeWithinTargetPosRange() {
			BlockPos blockPos = this.getTargetPos();
			if (blockPos == BlockPos.ZERO) {
				return false;
			} else {
				double d = blockPos.distSqr(new BlockPos(Bee.this));
				boolean bl = d > (double)(this.minThreshold * this.minThreshold);
				boolean bl2 = d < (double)(this.maxThreshold * this.maxThreshold);
				return bl && bl2;
			}
		}

		@Override
		public void tick() {
			BlockPos blockPos = this.getTargetPos();
			if (Bee.this.getNavigation().isDone()) {
				Vec3 vec3 = new Vec3(blockPos);
				Vec3 vec32 = RandomPos.getPosTowards(Bee.this, 8, 6, vec3, (float) (Math.PI / 10), false);
				if (vec32 == null) {
					vec32 = RandomPos.getPosTowards(Bee.this, 3, 3, vec3, false);
				}

				if (vec32 == null) {
					this.stuck = true;
					return;
				}

				Bee.this.getNavigation().moveTo(vec32.x, vec32.y, vec32.z, 1.0);
			}
		}
	}

	class BeeGoToHiveGoal extends Bee.BeeGoToBlockGoal {
		public BeeGoToHiveGoal() {
			super(2, 48);
		}

		@Override
		protected BlockPos getTargetPos() {
			return Bee.this.hivePos;
		}

		@Override
		public boolean canBeeUse() {
			if (this.isBeeWithinTargetPosRange()) {
				return Bee.this.canEnterHive();
			} else {
				Bee.this.hivePos = BlockPos.ZERO;
				return false;
			}
		}

		@Override
		public boolean canBeeContinueToUse() {
			return this.canBeeUse();
		}
	}

	public class BeeGoToKnownFlowerGoal extends Bee.BeeGoToBlockGoal {
		public BeeGoToKnownFlowerGoal() {
			super(1, 48);
		}

		@Override
		public boolean canBeeUse() {
			return this.isTargetPosValid() && Bee.this.ticksSincePollination > 3600;
		}

		@Override
		public boolean canBeeContinueToUse() {
			return this.isTargetPosValid();
		}

		@Override
		public void stop() {
			if (!this.isTargetPosValid()) {
				Bee.this.savedFlowerPos = BlockPos.ZERO;
			}
		}

		@Override
		protected BlockPos getTargetPos() {
			return Bee.this.savedFlowerPos;
		}

		private boolean isTargetPosValid() {
			if (this.isBeeWithinTargetPosRange()) {
				return Bee.this.level.getBlockState(this.getTargetPos()).is(BlockTags.FLOWERS);
			} else {
				Bee.this.savedFlowerPos = BlockPos.ZERO;
				return false;
			}
		}
	}

	class BeeGrowCropGoal extends Bee.BaseBeeGoal {
		private BeeGrowCropGoal() {
		}

		@Override
		public boolean canBeeUse() {
			if (Bee.this.getCropsGrownSincePollination() >= 10) {
				return false;
			} else {
				return Bee.this.random.nextFloat() < 0.3F ? false : Bee.this.hasNectar() && Bee.this.isHiveValid();
			}
		}

		@Override
		public boolean canBeeContinueToUse() {
			return this.canBeeUse();
		}

		@Override
		public void tick() {
			if (Bee.this.random.nextInt(30) == 0) {
				for (int i = 1; i <= 2; i++) {
					BlockPos blockPos = new BlockPos(Bee.this).below(i);
					BlockState blockState = Bee.this.level.getBlockState(blockPos);
					Block block = blockState.getBlock();
					boolean bl = false;
					IntegerProperty integerProperty = null;
					if (block.is(BlockTags.BEE_GROWABLES)) {
						if (block instanceof CropBlock) {
							CropBlock cropBlock = (CropBlock)block;
							if (!cropBlock.isMaxAge(blockState)) {
								bl = true;
								integerProperty = cropBlock.getAgeProperty();
							}
						} else if (block instanceof StemBlock) {
							int j = (Integer)blockState.getValue(StemBlock.AGE);
							if (j < 7) {
								bl = true;
								integerProperty = StemBlock.AGE;
							}
						} else if (block == Blocks.SWEET_BERRY_BUSH) {
							int j = (Integer)blockState.getValue(SweetBerryBushBlock.AGE);
							if (j < 3) {
								bl = true;
								integerProperty = SweetBerryBushBlock.AGE;
							}
						}

						if (bl) {
							Bee.this.level.levelEvent(2005, blockPos, 0);
							Bee.this.level.setBlockAndUpdate(blockPos, blockState.setValue(integerProperty, Integer.valueOf((Integer)blockState.getValue(integerProperty) + 1)));
							Bee.this.incrementNumCropsGrownSincePollination();
						}
					}
				}
			}
		}
	}

	class BeeHurtByOtherGoal extends HurtByTargetGoal {
		public BeeHurtByOtherGoal(Bee bee2) {
			super(bee2);
		}

		@Override
		protected void alertOther(Mob mob, LivingEntity livingEntity) {
			if (mob instanceof Bee && this.mob.canSee(livingEntity) && ((Bee)mob).makeAngry(livingEntity)) {
				mob.setTarget(livingEntity);
			}
		}
	}

	class BeeLocateHiveGoal extends Bee.BaseBeeGoal {
		private BeeLocateHiveGoal() {
		}

		@Override
		public boolean canBeeUse() {
			return Bee.this.tickCount % 10 == 0 && !Bee.this.hasHive();
		}

		@Override
		public boolean canBeeContinueToUse() {
			return false;
		}

		@Override
		public void start() {
			Stream<BlockPos> stream = this.findNearestHives(20);
			Optional<BlockPos> optional = stream.filter(blockPos -> {
				BlockEntity blockEntity = Bee.this.level.getBlockEntity(blockPos);
				if (blockEntity instanceof BeehiveBlockEntity && !((BeehiveBlockEntity)blockEntity).isFull()) {
					Path path = Bee.this.getNavigation().createPath(blockPos, 20);
					return path != null;
				} else {
					return false;
				}
			}).findFirst();
			optional.ifPresent(blockPos -> Bee.this.hivePos = blockPos);
		}

		private Stream<BlockPos> findNearestHives(int i) {
			BlockPos blockPos = new BlockPos(Bee.this);
			if (Bee.this.level instanceof ServerLevel) {
				Stream<PoiRecord> stream = ((ServerLevel)Bee.this.level)
					.getPoiManager()
					.getInRange(poiType -> poiType == PoiType.BEEHIVE || poiType == PoiType.BEE_NEST, blockPos, i, PoiManager.Occupancy.ANY);
				return stream.map(PoiRecord::getPos);
			} else {
				return Stream.empty();
			}
		}
	}

	class BeeLookControl extends LookControl {
		public BeeLookControl(Mob mob) {
			super(mob);
		}

		@Override
		public void tick() {
			if (!Bee.this.isAngry()) {
				super.tick();
			}
		}

		@Override
		protected boolean resetXRotOnTick() {
			return !Bee.this.beePollinateGoal.isPollinating();
		}
	}

	class BeePollinateGoal extends Bee.BaseBeeGoal {
		private final Predicate<BlockState> VALID_POLLINATION_BLOCKS = blockState -> {
			if (blockState.is(BlockTags.TALL_FLOWERS)) {
				return blockState.getBlock() == Blocks.SUNFLOWER ? blockState.getValue(DoublePlantBlock.HALF) == DoubleBlockHalf.UPPER : true;
			} else {
				return blockState.is(BlockTags.SMALL_FLOWERS);
			}
		};
		private int pollinateTicks = 0;
		private int lastSoundPlayedTick = 0;
		private boolean pollinating;
		private Vec3 hoverPos;

		public BeePollinateGoal() {
			this.setFlags(EnumSet.of(Goal.Flag.MOVE));
		}

		@Override
		public boolean canBeeUse() {
			if (Bee.this.hasNectar()) {
				return false;
			} else if (Bee.this.level.isRaining()) {
				return false;
			} else if (Bee.this.random.nextFloat() < 0.7F) {
				return false;
			} else {
				Optional<BlockPos> optional = this.findNearbyFlower();
				if (optional.isPresent()) {
					Bee.this.savedFlowerPos = (BlockPos)optional.get();
					Bee.this.getNavigation()
						.moveTo((double)Bee.this.savedFlowerPos.getX() + 0.5, (double)Bee.this.savedFlowerPos.getY() + 0.5, (double)Bee.this.savedFlowerPos.getZ() + 0.5, 1.2F);
					return true;
				} else {
					return false;
				}
			}
		}

		@Override
		public boolean canBeeContinueToUse() {
			if (!this.pollinating) {
				return false;
			} else if (Bee.this.level.isRaining()) {
				return false;
			} else if (this.hasPollinatedLongEnough()) {
				return Bee.this.random.nextFloat() < 0.2F;
			} else {
				return Bee.this.tickCount % 20 != 0
					? true
					: Bee.this.level.isLoaded(Bee.this.savedFlowerPos)
						&& Bee.this.level.getBlockState(Bee.this.savedFlowerPos).getBlock().is(BlockTags.FLOWERS)
						&& new BlockPos(Bee.this).closerThan(Bee.this.savedFlowerPos, 2.0);
			}
		}

		private boolean hasPollinatedLongEnough() {
			return this.pollinateTicks > 400;
		}

		private boolean isPollinating() {
			return this.pollinating;
		}

		private void stopPollinating() {
			this.pollinating = false;
		}

		@Override
		public void start() {
			this.pollinateTicks = 0;
			this.lastSoundPlayedTick = 0;
			this.pollinating = true;
		}

		@Override
		public void stop() {
			if (this.hasPollinatedLongEnough()) {
				Bee.this.setHasNectar(true);
			}

			this.pollinating = false;
		}

		@Override
		public void tick() {
			Vec3 vec3 = new Vec3(Bee.this.savedFlowerPos).add(0.5, 0.6F, 0.5);
			if (vec3.distanceTo(Bee.this.position()) > 1.0) {
				this.hoverPos = vec3;
				this.setWantedPos();
			} else {
				if (this.hoverPos == null) {
					this.hoverPos = vec3;
				}

				boolean bl = Bee.this.position().distanceTo(this.hoverPos) <= 0.1;
				boolean bl2 = true;
				if (bl) {
					boolean bl3 = Bee.this.random.nextInt(100) == 0;
					if (bl3) {
						this.hoverPos = new Vec3(vec3.x() + (double)this.getOffset(), vec3.y(), vec3.z() + (double)this.getOffset());
						Bee.this.getNavigation().stop();
					} else {
						bl2 = false;
					}

					Bee.this.getLookControl().setLookAt(vec3.x(), vec3.y(), vec3.z());
				}

				if (bl2) {
					this.setWantedPos();
				}

				this.pollinateTicks++;
				if (Bee.this.random.nextFloat() < 0.05F && this.pollinateTicks > this.lastSoundPlayedTick + 60) {
					this.lastSoundPlayedTick = this.pollinateTicks;
					Bee.this.playSound(SoundEvents.BEE_POLLINATE, 1.0F, 1.0F);
				}
			}
		}

		private void setWantedPos() {
			Bee.this.getMoveControl().setWantedPosition(this.hoverPos.x(), this.hoverPos.y(), this.hoverPos.z(), 0.35F);
		}

		private float getOffset() {
			return (Bee.this.random.nextFloat() * 2.0F - 1.0F) * 0.33333334F;
		}

		private Optional<BlockPos> findNearbyFlower() {
			return this.findNearestBlock(this.VALID_POLLINATION_BLOCKS, 5.0);
		}

		private Optional<BlockPos> findNearestBlock(Predicate<BlockState> predicate, double d) {
			BlockPos blockPos = new BlockPos(Bee.this);
			BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();

			for (int i = 0; (double)i <= d; i = i > 0 ? -i : 1 - i) {
				for (int j = 0; (double)j < d; j++) {
					for (int k = 0; k <= j; k = k > 0 ? -k : 1 - k) {
						for (int l = k < j && k > -j ? j : 0; l <= j; l = l > 0 ? -l : 1 - l) {
							mutableBlockPos.set(blockPos).move(k, i - 1, l);
							if (blockPos.distSqr(mutableBlockPos) < d * d && predicate.test(Bee.this.level.getBlockState(mutableBlockPos))) {
								return Optional.of(mutableBlockPos);
							}
						}
					}
				}
			}

			return Optional.empty();
		}
	}

	class BeeWanderGoal extends Goal {
		public BeeWanderGoal() {
			this.setFlags(EnumSet.of(Goal.Flag.MOVE));
		}

		@Override
		public boolean canUse() {
			return Bee.this.getNavigation().isDone() && Bee.this.random.nextInt(10) == 0;
		}

		@Override
		public boolean canContinueToUse() {
			return Bee.this.getNavigation().getPath() != null && !Bee.this.getNavigation().isDone();
		}

		@Override
		public void start() {
			Vec3 vec3 = this.findPos();
			if (vec3 != null) {
				PathNavigation pathNavigation = Bee.this.getNavigation();
				pathNavigation.moveTo(pathNavigation.createPath(new BlockPos(vec3), 1), 1.0);
			}
		}

		@Nullable
		private Vec3 findPos() {
			Vec3 vec32;
			if (!Bee.this.hivePos.closerThan(Bee.this.position(), 40.0) && Bee.this.isHiveValid()) {
				Vec3 vec3 = new Vec3(Bee.this.hivePos);
				vec32 = vec3.subtract(Bee.this.position()).normalize();
			} else {
				vec32 = Bee.this.getViewVector(0.0F);
			}

			int i = 8;
			Vec3 vec33 = RandomPos.getPosAboveSolid(Bee.this, 8, 7, vec32, (float) (Math.PI / 2), 2, 1);
			return vec33 != null ? vec33 : RandomPos.getAirPos(Bee.this, 8, 4, -2, vec32, (float) (Math.PI / 2));
		}
	}
}
