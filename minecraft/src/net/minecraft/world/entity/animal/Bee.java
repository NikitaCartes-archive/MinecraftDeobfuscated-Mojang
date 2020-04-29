package net.minecraft.world.entity.animal;

import com.google.common.collect.Lists;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Predicate;
import java.util.stream.Collectors;
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
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
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
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
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
	private int ticksWithoutNectarSinceExitingHive;
	private int stayOutOfHiveCountdown;
	private int numCropsGrownSincePollination;
	private int remainingCooldownBeforeLocatingNewHive = 0;
	private int remainingCooldownBeforeLocatingNewFlower = 0;
	@Nullable
	private BlockPos savedFlowerPos = null;
	@Nullable
	private BlockPos hivePos = null;
	private Bee.BeePollinateGoal beePollinateGoal;
	private Bee.BeeGoToHiveGoal goToHiveGoal;
	private Bee.BeeGoToKnownFlowerGoal goToKnownFlowerGoal;
	private int underWaterTicks;

	public Bee(EntityType<? extends Bee> entityType, Level level) {
		super(entityType, level);
		this.moveControl = new FlyingMoveControl(this, 20, true);
		this.lookControl = new Bee.BeeLookControl(this);
		this.setPathfindingMalus(BlockPathTypes.DANGER_FIRE, -1.0F);
		this.setPathfindingMalus(BlockPathTypes.DAMAGE_FIRE, -1.0F);
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
	public float getWalkTargetValue(BlockPos blockPos, LevelReader levelReader) {
		return levelReader.getBlockState(blockPos).isAir() ? 10.0F : 0.0F;
	}

	@Override
	protected void registerGoals() {
		this.goalSelector.addGoal(0, new Bee.BeeAttackGoal(this, 1.4F, true));
		this.goalSelector.addGoal(1, new Bee.BeeEnterHiveGoal());
		this.goalSelector.addGoal(2, new BreedGoal(this, 1.0));
		this.goalSelector.addGoal(3, new TemptGoal(this, 1.25, Ingredient.of(ItemTags.FLOWERS), false));
		this.beePollinateGoal = new Bee.BeePollinateGoal();
		this.goalSelector.addGoal(4, this.beePollinateGoal);
		this.goalSelector.addGoal(5, new FollowParentGoal(this, 1.25));
		this.goalSelector.addGoal(5, new Bee.BeeLocateHiveGoal());
		this.goToHiveGoal = new Bee.BeeGoToHiveGoal();
		this.goalSelector.addGoal(5, this.goToHiveGoal);
		this.goToKnownFlowerGoal = new Bee.BeeGoToKnownFlowerGoal();
		this.goalSelector.addGoal(6, this.goToKnownFlowerGoal);
		this.goalSelector.addGoal(7, new Bee.BeeGrowCropGoal());
		this.goalSelector.addGoal(8, new Bee.BeeWanderGoal());
		this.goalSelector.addGoal(9, new FloatGoal(this));
		this.targetSelector.addGoal(1, new Bee.BeeHurtByOtherGoal(this).setAlertOthers(new Class[0]));
		this.targetSelector.addGoal(2, new Bee.BeeBecomeAngryTargetGoal(this));
	}

	@Override
	public void addAdditionalSaveData(CompoundTag compoundTag) {
		super.addAdditionalSaveData(compoundTag);
		if (this.hasHive()) {
			compoundTag.put("HivePos", NbtUtils.writeBlockPos(this.getHivePos()));
		}

		if (this.hasSavedFlowerPos()) {
			compoundTag.put("FlowerPos", NbtUtils.writeBlockPos(this.getSavedFlowerPos()));
		}

		compoundTag.putBoolean("HasNectar", this.hasNectar());
		compoundTag.putBoolean("HasStung", this.hasStung());
		compoundTag.putInt("TicksSincePollination", this.ticksWithoutNectarSinceExitingHive);
		compoundTag.putInt("CannotEnterHiveTicks", this.stayOutOfHiveCountdown);
		compoundTag.putInt("CropsGrownSincePollination", this.numCropsGrownSincePollination);
		compoundTag.putInt("Anger", this.getAngerTime());
		if (this.lastHurtByUUID != null) {
			compoundTag.putUUID("HurtBy", this.lastHurtByUUID);
		}
	}

	@Override
	public void readAdditionalSaveData(CompoundTag compoundTag) {
		this.hivePos = null;
		if (compoundTag.contains("HivePos")) {
			this.hivePos = NbtUtils.readBlockPos(compoundTag.getCompound("HivePos"));
		}

		this.savedFlowerPos = null;
		if (compoundTag.contains("FlowerPos")) {
			this.savedFlowerPos = NbtUtils.readBlockPos(compoundTag.getCompound("FlowerPos"));
		}

		super.readAdditionalSaveData(compoundTag);
		this.setHasNectar(compoundTag.getBoolean("HasNectar"));
		this.setHasStung(compoundTag.getBoolean("HasStung"));
		this.setAngerTime(compoundTag.getInt("Anger"));
		this.ticksWithoutNectarSinceExitingHive = compoundTag.getInt("TicksSincePollination");
		this.stayOutOfHiveCountdown = compoundTag.getInt("CannotEnterHiveTicks");
		this.numCropsGrownSincePollination = compoundTag.getInt("CropsGrownSincePollination");
		if (compoundTag.hasUUID("HurtBy")) {
			this.lastHurtByUUID = compoundTag.getUUID("HurtBy");
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
		boolean bl = entity.hurt(DamageSource.sting(this), (float)((int)this.getAttributeValue(Attributes.ATTACK_DAMAGE)));
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

	private void pathfindRandomlyTowards(BlockPos blockPos) {
		Vec3 vec3 = Vec3.atBottomCenterOf(blockPos);
		int i = 0;
		BlockPos blockPos2 = this.blockPosition();
		int j = (int)vec3.y - blockPos2.getY();
		if (j > 2) {
			i = 4;
		} else if (j < -2) {
			i = -4;
		}

		int k = 6;
		int l = 8;
		int m = blockPos2.distManhattan(blockPos);
		if (m < 15) {
			k = m / 2;
			l = m / 2;
		}

		Vec3 vec32 = RandomPos.getAirPosTowards(this, k, l, i, vec3, (float) (Math.PI / 10));
		if (vec32 != null) {
			this.navigation.setMaxVisitedNodesMultiplier(0.5F);
			this.navigation.moveTo(vec32.x, vec32.y, vec32.z, 1.0);
		}
	}

	@Nullable
	public BlockPos getSavedFlowerPos() {
		return this.savedFlowerPos;
	}

	public boolean hasSavedFlowerPos() {
		return this.savedFlowerPos != null;
	}

	public void setSavedFlowerPos(BlockPos blockPos) {
		this.savedFlowerPos = blockPos;
	}

	private boolean isTiredOfLookingForNectar() {
		return this.ticksWithoutNectarSinceExitingHive > 3600;
	}

	private boolean wantsToEnterHive() {
		if (this.stayOutOfHiveCountdown <= 0 && !this.beePollinateGoal.isPollinating() && !this.hasStung()) {
			boolean bl = this.isTiredOfLookingForNectar() || this.level.isRaining() || this.level.isNight() || this.hasNectar();
			return bl && !this.isHiveNearFire();
		} else {
			return false;
		}
	}

	public void setStayOutOfHiveCountdown(int i) {
		this.stayOutOfHiveCountdown = i;
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
			this.ticksWithoutNectarSinceExitingHive++;
		}
	}

	public void resetTicksWithoutNectarSinceExitingHive() {
		this.ticksWithoutNectarSinceExitingHive = 0;
	}

	private boolean isHiveNearFire() {
		if (this.hivePos == null) {
			return false;
		} else {
			BlockEntity blockEntity = this.level.getBlockEntity(this.hivePos);
			return blockEntity instanceof BeehiveBlockEntity && ((BeehiveBlockEntity)blockEntity).isFireNearby();
		}
	}

	public boolean isAngry() {
		return this.getAngerTime() > 0;
	}

	private int getAngerTime() {
		return this.entityData.get(ANGER_TIME);
	}

	private void setAngerTime(int i) {
		this.entityData.set(ANGER_TIME, i);
	}

	private boolean doesHiveHaveSpace(BlockPos blockPos) {
		BlockEntity blockEntity = this.level.getBlockEntity(blockPos);
		return blockEntity instanceof BeehiveBlockEntity ? !((BeehiveBlockEntity)blockEntity).isFull() : false;
	}

	public boolean hasHive() {
		return this.hivePos != null;
	}

	@Nullable
	public BlockPos getHivePos() {
		return this.hivePos;
	}

	@Override
	protected void sendDebugPackets() {
		super.sendDebugPackets();
		DebugPackets.sendBeeInfo(this);
	}

	private int getCropsGrownSincePollination() {
		return this.numCropsGrownSincePollination;
	}

	private void resetNumCropsGrownSincePollination() {
		this.numCropsGrownSincePollination = 0;
	}

	private void incrementNumCropsGrownSincePollination() {
		this.numCropsGrownSincePollination++;
	}

	@Override
	public void aiStep() {
		super.aiStep();
		if (!this.level.isClientSide) {
			if (this.stayOutOfHiveCountdown > 0) {
				this.stayOutOfHiveCountdown--;
			}

			if (this.remainingCooldownBeforeLocatingNewHive > 0) {
				this.remainingCooldownBeforeLocatingNewHive--;
			}

			if (this.remainingCooldownBeforeLocatingNewFlower > 0) {
				this.remainingCooldownBeforeLocatingNewFlower--;
			}

			boolean bl = this.isAngry() && !this.hasStung() && this.getTarget() != null && this.getTarget().distanceToSqr(this) < 4.0;
			this.setRolling(bl);
			if (this.tickCount % 20 == 0 && !this.isHiveValid()) {
				this.hivePos = null;
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

	private void setHasNectar(boolean bl) {
		if (bl) {
			this.resetTicksWithoutNectarSinceExitingHive();
		}

		this.setFlag(8, bl);
	}

	public boolean hasStung() {
		return this.getFlag(4);
	}

	private void setHasStung(boolean bl) {
		this.setFlag(4, bl);
	}

	private boolean isRolling() {
		return this.getFlag(2);
	}

	private void setRolling(boolean bl) {
		this.setFlag(2, bl);
	}

	private boolean isTooFarAway(BlockPos blockPos) {
		return !this.closerThan(blockPos, 32);
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

	public static AttributeSupplier.Builder createAttributes() {
		return Mob.createMobAttributes()
			.add(Attributes.MAX_HEALTH, 10.0)
			.add(Attributes.FLYING_SPEED, 0.6F)
			.add(Attributes.MOVEMENT_SPEED, 0.3F)
			.add(Attributes.ATTACK_DAMAGE, 2.0)
			.add(Attributes.FOLLOW_RANGE, 48.0);
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

	private boolean isFlowerValid(BlockPos blockPos) {
		return this.level.isLoaded(blockPos) && this.level.getBlockState(blockPos).getBlock().is(BlockTags.FLOWERS);
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

	private boolean closerThan(BlockPos blockPos, int i) {
		return blockPos.closerThan(this.blockPosition(), (double)i);
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
		BeeAttackGoal(PathfinderMob pathfinderMob, double d, boolean bl) {
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
		BeeBecomeAngryTargetGoal(Bee bee) {
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
			if (Bee.this.hasHive() && Bee.this.wantsToEnterHive() && Bee.this.hivePos.closerThan(Bee.this.position(), 2.0)) {
				BlockEntity blockEntity = Bee.this.level.getBlockEntity(Bee.this.hivePos);
				if (blockEntity instanceof BeehiveBlockEntity) {
					BeehiveBlockEntity beehiveBlockEntity = (BeehiveBlockEntity)blockEntity;
					if (!beehiveBlockEntity.isFull()) {
						return true;
					}

					Bee.this.hivePos = null;
				}
			}

			return false;
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

	public class BeeGoToHiveGoal extends Bee.BaseBeeGoal {
		private int travellingTicks = Bee.this.level.random.nextInt(10);
		private List<BlockPos> blacklistedTargets = Lists.<BlockPos>newArrayList();
		@Nullable
		private Path lastPath = null;
		private int ticksStuck;

		BeeGoToHiveGoal() {
			this.setFlags(EnumSet.of(Goal.Flag.MOVE));
		}

		@Override
		public boolean canBeeUse() {
			return Bee.this.hivePos != null
				&& !Bee.this.hasRestriction()
				&& Bee.this.wantsToEnterHive()
				&& !this.hasReachedTarget(Bee.this.hivePos)
				&& Bee.this.level.getBlockState(Bee.this.hivePos).is(BlockTags.BEEHIVES);
		}

		@Override
		public boolean canBeeContinueToUse() {
			return this.canBeeUse();
		}

		@Override
		public void start() {
			this.travellingTicks = 0;
			this.ticksStuck = 0;
			super.start();
		}

		@Override
		public void stop() {
			this.travellingTicks = 0;
			this.ticksStuck = 0;
			Bee.this.navigation.stop();
			Bee.this.navigation.resetMaxVisitedNodesMultiplier();
		}

		@Override
		public void tick() {
			if (Bee.this.hivePos != null) {
				this.travellingTicks++;
				if (this.travellingTicks > 600) {
					this.dropAndBlacklistHive();
				} else if (!Bee.this.navigation.isInProgress()) {
					if (!Bee.this.closerThan(Bee.this.hivePos, 16)) {
						if (Bee.this.isTooFarAway(Bee.this.hivePos)) {
							this.dropHive();
						} else {
							Bee.this.pathfindRandomlyTowards(Bee.this.hivePos);
						}
					} else {
						boolean bl = this.pathfindDirectlyTowards(Bee.this.hivePos);
						if (!bl) {
							this.dropAndBlacklistHive();
						} else if (this.lastPath != null && Bee.this.navigation.getPath().sameAs(this.lastPath)) {
							this.ticksStuck++;
							if (this.ticksStuck > 60) {
								this.dropHive();
								this.ticksStuck = 0;
							}
						} else {
							this.lastPath = Bee.this.navigation.getPath();
						}
					}
				}
			}
		}

		private boolean pathfindDirectlyTowards(BlockPos blockPos) {
			Bee.this.navigation.setMaxVisitedNodesMultiplier(10.0F);
			Bee.this.navigation.moveTo((double)blockPos.getX(), (double)blockPos.getY(), (double)blockPos.getZ(), 1.0);
			return Bee.this.navigation.getPath() != null && Bee.this.navigation.getPath().canReach();
		}

		private boolean isTargetBlacklisted(BlockPos blockPos) {
			return this.blacklistedTargets.contains(blockPos);
		}

		private void blacklistTarget(BlockPos blockPos) {
			this.blacklistedTargets.add(blockPos);

			while (this.blacklistedTargets.size() > 3) {
				this.blacklistedTargets.remove(0);
			}
		}

		private void clearBlacklist() {
			this.blacklistedTargets.clear();
		}

		private void dropAndBlacklistHive() {
			if (Bee.this.hivePos != null) {
				this.blacklistTarget(Bee.this.hivePos);
			}

			this.dropHive();
		}

		private void dropHive() {
			Bee.this.hivePos = null;
			Bee.this.remainingCooldownBeforeLocatingNewHive = 200;
		}

		private boolean hasReachedTarget(BlockPos blockPos) {
			if (Bee.this.closerThan(blockPos, 2)) {
				return true;
			} else {
				Path path = Bee.this.navigation.getPath();
				return path != null && path.getTarget().equals(blockPos) && path.canReach() && path.isDone();
			}
		}
	}

	public class BeeGoToKnownFlowerGoal extends Bee.BaseBeeGoal {
		private int travellingTicks = Bee.this.level.random.nextInt(10);

		BeeGoToKnownFlowerGoal() {
			this.setFlags(EnumSet.of(Goal.Flag.MOVE));
		}

		@Override
		public boolean canBeeUse() {
			return Bee.this.savedFlowerPos != null
				&& !Bee.this.hasRestriction()
				&& this.wantsToGoToKnownFlower()
				&& Bee.this.isFlowerValid(Bee.this.savedFlowerPos)
				&& !Bee.this.closerThan(Bee.this.savedFlowerPos, 2);
		}

		@Override
		public boolean canBeeContinueToUse() {
			return this.canBeeUse();
		}

		@Override
		public void start() {
			this.travellingTicks = 0;
			super.start();
		}

		@Override
		public void stop() {
			this.travellingTicks = 0;
			Bee.this.navigation.stop();
			Bee.this.navigation.resetMaxVisitedNodesMultiplier();
		}

		@Override
		public void tick() {
			if (Bee.this.savedFlowerPos != null) {
				this.travellingTicks++;
				if (this.travellingTicks > 600) {
					Bee.this.savedFlowerPos = null;
				} else if (!Bee.this.navigation.isInProgress()) {
					if (Bee.this.isTooFarAway(Bee.this.savedFlowerPos)) {
						Bee.this.savedFlowerPos = null;
					} else {
						Bee.this.pathfindRandomlyTowards(Bee.this.savedFlowerPos);
					}
				}
			}
		}

		private boolean wantsToGoToKnownFlower() {
			return Bee.this.ticksWithoutNectarSinceExitingHive > 2400;
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
					BlockPos blockPos = Bee.this.blockPosition().below(i);
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
		BeeHurtByOtherGoal(Bee bee2) {
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
			return Bee.this.remainingCooldownBeforeLocatingNewHive == 0 && !Bee.this.hasHive() && Bee.this.wantsToEnterHive();
		}

		@Override
		public boolean canBeeContinueToUse() {
			return false;
		}

		@Override
		public void start() {
			Bee.this.remainingCooldownBeforeLocatingNewHive = 200;
			List<BlockPos> list = this.findNearbyHivesWithSpace();
			if (!list.isEmpty()) {
				for (BlockPos blockPos : list) {
					if (!Bee.this.goToHiveGoal.isTargetBlacklisted(blockPos)) {
						Bee.this.hivePos = blockPos;
						return;
					}
				}

				Bee.this.goToHiveGoal.clearBlacklist();
				Bee.this.hivePos = (BlockPos)list.get(0);
			}
		}

		private List<BlockPos> findNearbyHivesWithSpace() {
			BlockPos blockPos = Bee.this.blockPosition();
			PoiManager poiManager = ((ServerLevel)Bee.this.level).getPoiManager();
			Stream<PoiRecord> stream = poiManager.getInRange(
				poiType -> poiType == PoiType.BEEHIVE || poiType == PoiType.BEE_NEST, blockPos, 20, PoiManager.Occupancy.ANY
			);
			return (List<BlockPos>)stream.map(PoiRecord::getPos)
				.filter(blockPosx -> Bee.this.doesHiveHaveSpace(blockPosx))
				.sorted(Comparator.comparingDouble(blockPos2 -> blockPos2.distSqr(blockPos)))
				.collect(Collectors.toList());
		}
	}

	class BeeLookControl extends LookControl {
		BeeLookControl(Mob mob) {
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
				return blockState.is(Blocks.SUNFLOWER) ? blockState.getValue(DoublePlantBlock.HALF) == DoubleBlockHalf.UPPER : true;
			} else {
				return blockState.is(BlockTags.SMALL_FLOWERS);
			}
		};
		private int successfulPollinatingTicks = 0;
		private int lastSoundPlayedTick = 0;
		private boolean pollinating;
		private Vec3 hoverPos;
		private int pollinatingTicks = 0;

		BeePollinateGoal() {
			this.setFlags(EnumSet.of(Goal.Flag.MOVE));
		}

		@Override
		public boolean canBeeUse() {
			if (Bee.this.remainingCooldownBeforeLocatingNewFlower > 0) {
				return false;
			} else if (Bee.this.hasNectar()) {
				return false;
			} else if (Bee.this.level.isRaining()) {
				return false;
			} else if (Bee.this.random.nextFloat() < 0.7F) {
				return false;
			} else {
				Optional<BlockPos> optional = this.findNearbyFlower();
				if (optional.isPresent()) {
					Bee.this.savedFlowerPos = (BlockPos)optional.get();
					Bee.this.navigation
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
			} else if (!Bee.this.hasSavedFlowerPos()) {
				return false;
			} else if (Bee.this.level.isRaining()) {
				return false;
			} else if (this.hasPollinatedLongEnough()) {
				return Bee.this.random.nextFloat() < 0.2F;
			} else if (Bee.this.tickCount % 20 == 0 && !Bee.this.isFlowerValid(Bee.this.savedFlowerPos)) {
				Bee.this.savedFlowerPos = null;
				return false;
			} else {
				return true;
			}
		}

		private boolean hasPollinatedLongEnough() {
			return this.successfulPollinatingTicks > 400;
		}

		private boolean isPollinating() {
			return this.pollinating;
		}

		private void stopPollinating() {
			this.pollinating = false;
		}

		@Override
		public void start() {
			this.successfulPollinatingTicks = 0;
			this.pollinatingTicks = 0;
			this.lastSoundPlayedTick = 0;
			this.pollinating = true;
			Bee.this.resetTicksWithoutNectarSinceExitingHive();
		}

		@Override
		public void stop() {
			if (this.hasPollinatedLongEnough()) {
				Bee.this.setHasNectar(true);
			}

			this.pollinating = false;
			Bee.this.navigation.stop();
			Bee.this.remainingCooldownBeforeLocatingNewFlower = 200;
		}

		@Override
		public void tick() {
			this.pollinatingTicks++;
			if (this.pollinatingTicks > 600) {
				Bee.this.savedFlowerPos = null;
			} else {
				Vec3 vec3 = Vec3.atBottomCenterOf(Bee.this.savedFlowerPos).add(0.0, 0.6F, 0.0);
				if (vec3.distanceTo(Bee.this.position()) > 1.0) {
					this.hoverPos = vec3;
					this.setWantedPos();
				} else {
					if (this.hoverPos == null) {
						this.hoverPos = vec3;
					}

					boolean bl = Bee.this.position().distanceTo(this.hoverPos) <= 0.1;
					boolean bl2 = true;
					if (!bl && this.pollinatingTicks > 600) {
						Bee.this.savedFlowerPos = null;
					} else {
						if (bl) {
							boolean bl3 = Bee.this.random.nextInt(25) == 0;
							if (bl3) {
								this.hoverPos = new Vec3(vec3.x() + (double)this.getOffset(), vec3.y(), vec3.z() + (double)this.getOffset());
								Bee.this.navigation.stop();
							} else {
								bl2 = false;
							}

							Bee.this.getLookControl().setLookAt(vec3.x(), vec3.y(), vec3.z());
						}

						if (bl2) {
							this.setWantedPos();
						}

						this.successfulPollinatingTicks++;
						if (Bee.this.random.nextFloat() < 0.05F && this.successfulPollinatingTicks > this.lastSoundPlayedTick + 60) {
							this.lastSoundPlayedTick = this.successfulPollinatingTicks;
							Bee.this.playSound(SoundEvents.BEE_POLLINATE, 1.0F, 1.0F);
						}
					}
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
			BlockPos blockPos = Bee.this.blockPosition();
			BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();

			for (int i = 0; (double)i <= d; i = i > 0 ? -i : 1 - i) {
				for (int j = 0; (double)j < d; j++) {
					for (int k = 0; k <= j; k = k > 0 ? -k : 1 - k) {
						for (int l = k < j && k > -j ? j : 0; l <= j; l = l > 0 ? -l : 1 - l) {
							mutableBlockPos.setWithOffset(blockPos, k, i - 1, l);
							if (blockPos.closerThan(mutableBlockPos, d) && predicate.test(Bee.this.level.getBlockState(mutableBlockPos))) {
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
		BeeWanderGoal() {
			this.setFlags(EnumSet.of(Goal.Flag.MOVE));
		}

		@Override
		public boolean canUse() {
			return Bee.this.navigation.isDone() && Bee.this.random.nextInt(10) == 0;
		}

		@Override
		public boolean canContinueToUse() {
			return Bee.this.navigation.isInProgress();
		}

		@Override
		public void start() {
			Vec3 vec3 = this.findPos();
			if (vec3 != null) {
				Bee.this.navigation.moveTo(Bee.this.navigation.createPath(new BlockPos(vec3), 1), 1.0);
			}
		}

		@Nullable
		private Vec3 findPos() {
			Vec3 vec32;
			if (Bee.this.isHiveValid() && !Bee.this.closerThan(Bee.this.hivePos, 22)) {
				Vec3 vec3 = Vec3.atCenterOf(Bee.this.hivePos);
				vec32 = vec3.subtract(Bee.this.position()).normalize();
			} else {
				vec32 = Bee.this.getViewVector(0.0F);
			}

			int i = 8;
			Vec3 vec33 = RandomPos.getAboveLandPos(Bee.this, 8, 7, vec32, (float) (Math.PI / 2), 2, 1);
			return vec33 != null ? vec33 : RandomPos.getAirPos(Bee.this, 8, 4, -2, vec32, (float) (Math.PI / 2));
		}
	}
}
