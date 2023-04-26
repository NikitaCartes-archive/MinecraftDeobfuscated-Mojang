package net.minecraft.world.entity.raid;

import com.google.common.collect.Lists;
import java.util.EnumSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.Mth;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.PathfindToRaidGoal;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.ai.util.DefaultRandomPos;
import net.minecraft.world.entity.ai.village.poi.PoiManager;
import net.minecraft.world.entity.ai.village.poi.PoiTypes;
import net.minecraft.world.entity.animal.Wolf;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.AbstractIllager;
import net.minecraft.world.entity.monster.PatrollingMonster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.phys.Vec3;

public abstract class Raider extends PatrollingMonster {
	protected static final EntityDataAccessor<Boolean> IS_CELEBRATING = SynchedEntityData.defineId(Raider.class, EntityDataSerializers.BOOLEAN);
	static final Predicate<ItemEntity> ALLOWED_ITEMS = itemEntity -> !itemEntity.hasPickUpDelay()
			&& itemEntity.isAlive()
			&& ItemStack.matches(itemEntity.getItem(), Raid.getLeaderBannerInstance());
	@Nullable
	protected Raid raid;
	private int wave;
	private boolean canJoinRaid;
	private int ticksOutsideRaid;

	protected Raider(EntityType<? extends Raider> entityType, Level level) {
		super(entityType, level);
	}

	@Override
	protected void registerGoals() {
		super.registerGoals();
		this.goalSelector.addGoal(1, new Raider.ObtainRaidLeaderBannerGoal<>(this));
		this.goalSelector.addGoal(3, new PathfindToRaidGoal<>(this));
		this.goalSelector.addGoal(4, new Raider.RaiderMoveThroughVillageGoal(this, 1.05F, 1));
		this.goalSelector.addGoal(5, new Raider.RaiderCelebration(this));
	}

	@Override
	protected void defineSynchedData() {
		super.defineSynchedData();
		this.entityData.define(IS_CELEBRATING, false);
	}

	public abstract void applyRaidBuffs(int i, boolean bl);

	public boolean canJoinRaid() {
		return this.canJoinRaid;
	}

	public void setCanJoinRaid(boolean bl) {
		this.canJoinRaid = bl;
	}

	@Override
	public void aiStep() {
		if (this.level() instanceof ServerLevel && this.isAlive()) {
			Raid raid = this.getCurrentRaid();
			if (this.canJoinRaid()) {
				if (raid == null) {
					if (this.level().getGameTime() % 20L == 0L) {
						Raid raid2 = ((ServerLevel)this.level()).getRaidAt(this.blockPosition());
						if (raid2 != null && Raids.canJoinRaid(this, raid2)) {
							raid2.joinRaid(raid2.getGroupsSpawned(), this, null, true);
						}
					}
				} else {
					LivingEntity livingEntity = this.getTarget();
					if (livingEntity != null && (livingEntity.getType() == EntityType.PLAYER || livingEntity.getType() == EntityType.IRON_GOLEM)) {
						this.noActionTime = 0;
					}
				}
			}
		}

		super.aiStep();
	}

	@Override
	protected void updateNoActionTime() {
		this.noActionTime += 2;
	}

	@Override
	public void die(DamageSource damageSource) {
		if (this.level() instanceof ServerLevel) {
			Entity entity = damageSource.getEntity();
			Raid raid = this.getCurrentRaid();
			if (raid != null) {
				if (this.isPatrolLeader()) {
					raid.removeLeader(this.getWave());
				}

				if (entity != null && entity.getType() == EntityType.PLAYER) {
					raid.addHeroOfTheVillage(entity);
				}

				raid.removeFromRaid(this, false);
			}

			if (this.isPatrolLeader() && raid == null && ((ServerLevel)this.level()).getRaidAt(this.blockPosition()) == null) {
				ItemStack itemStack = this.getItemBySlot(EquipmentSlot.HEAD);
				Player player = null;
				if (entity instanceof Player) {
					player = (Player)entity;
				} else if (entity instanceof Wolf wolf) {
					LivingEntity livingEntity = wolf.getOwner();
					if (wolf.isTame() && livingEntity instanceof Player) {
						player = (Player)livingEntity;
					}
				}

				if (!itemStack.isEmpty() && ItemStack.matches(itemStack, Raid.getLeaderBannerInstance()) && player != null) {
					MobEffectInstance mobEffectInstance = player.getEffect(MobEffects.BAD_OMEN);
					int i = 1;
					if (mobEffectInstance != null) {
						i += mobEffectInstance.getAmplifier();
						player.removeEffectNoUpdate(MobEffects.BAD_OMEN);
					} else {
						i--;
					}

					i = Mth.clamp(i, 0, 4);
					MobEffectInstance mobEffectInstance2 = new MobEffectInstance(MobEffects.BAD_OMEN, 120000, i, false, false, true);
					if (!this.level().getGameRules().getBoolean(GameRules.RULE_DISABLE_RAIDS)) {
						player.addEffect(mobEffectInstance2);
					}
				}
			}
		}

		super.die(damageSource);
	}

	@Override
	public boolean canJoinPatrol() {
		return !this.hasActiveRaid();
	}

	public void setCurrentRaid(@Nullable Raid raid) {
		this.raid = raid;
	}

	@Nullable
	public Raid getCurrentRaid() {
		return this.raid;
	}

	public boolean hasActiveRaid() {
		return this.getCurrentRaid() != null && this.getCurrentRaid().isActive();
	}

	public void setWave(int i) {
		this.wave = i;
	}

	public int getWave() {
		return this.wave;
	}

	public boolean isCelebrating() {
		return this.entityData.get(IS_CELEBRATING);
	}

	public void setCelebrating(boolean bl) {
		this.entityData.set(IS_CELEBRATING, bl);
	}

	@Override
	public void addAdditionalSaveData(CompoundTag compoundTag) {
		super.addAdditionalSaveData(compoundTag);
		compoundTag.putInt("Wave", this.wave);
		compoundTag.putBoolean("CanJoinRaid", this.canJoinRaid);
		if (this.raid != null) {
			compoundTag.putInt("RaidId", this.raid.getId());
		}
	}

	@Override
	public void readAdditionalSaveData(CompoundTag compoundTag) {
		super.readAdditionalSaveData(compoundTag);
		this.wave = compoundTag.getInt("Wave");
		this.canJoinRaid = compoundTag.getBoolean("CanJoinRaid");
		if (compoundTag.contains("RaidId", 3)) {
			if (this.level() instanceof ServerLevel) {
				this.raid = ((ServerLevel)this.level()).getRaids().get(compoundTag.getInt("RaidId"));
			}

			if (this.raid != null) {
				this.raid.addWaveMob(this.wave, this, false);
				if (this.isPatrolLeader()) {
					this.raid.setLeader(this.wave, this);
				}
			}
		}
	}

	@Override
	protected void pickUpItem(ItemEntity itemEntity) {
		ItemStack itemStack = itemEntity.getItem();
		boolean bl = this.hasActiveRaid() && this.getCurrentRaid().getLeader(this.getWave()) != null;
		if (this.hasActiveRaid() && !bl && ItemStack.matches(itemStack, Raid.getLeaderBannerInstance())) {
			EquipmentSlot equipmentSlot = EquipmentSlot.HEAD;
			ItemStack itemStack2 = this.getItemBySlot(equipmentSlot);
			double d = (double)this.getEquipmentDropChance(equipmentSlot);
			if (!itemStack2.isEmpty() && (double)Math.max(this.random.nextFloat() - 0.1F, 0.0F) < d) {
				this.spawnAtLocation(itemStack2);
			}

			this.onItemPickup(itemEntity);
			this.setItemSlot(equipmentSlot, itemStack);
			this.take(itemEntity, itemStack.getCount());
			itemEntity.discard();
			this.getCurrentRaid().setLeader(this.getWave(), this);
			this.setPatrolLeader(true);
		} else {
			super.pickUpItem(itemEntity);
		}
	}

	@Override
	public boolean removeWhenFarAway(double d) {
		return this.getCurrentRaid() == null ? super.removeWhenFarAway(d) : false;
	}

	@Override
	public boolean requiresCustomPersistence() {
		return super.requiresCustomPersistence() || this.getCurrentRaid() != null;
	}

	public int getTicksOutsideRaid() {
		return this.ticksOutsideRaid;
	}

	public void setTicksOutsideRaid(int i) {
		this.ticksOutsideRaid = i;
	}

	@Override
	public boolean hurt(DamageSource damageSource, float f) {
		if (this.hasActiveRaid()) {
			this.getCurrentRaid().updateBossbar();
		}

		return super.hurt(damageSource, f);
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
		this.setCanJoinRaid(this.getType() != EntityType.WITCH || mobSpawnType != MobSpawnType.NATURAL);
		return super.finalizeSpawn(serverLevelAccessor, difficultyInstance, mobSpawnType, spawnGroupData, compoundTag);
	}

	public abstract SoundEvent getCelebrateSound();

	protected class HoldGroundAttackGoal extends Goal {
		private final Raider mob;
		private final float hostileRadiusSqr;
		public final TargetingConditions shoutTargeting = TargetingConditions.forNonCombat().range(8.0).ignoreLineOfSight().ignoreInvisibilityTesting();

		public HoldGroundAttackGoal(AbstractIllager abstractIllager, float f) {
			this.mob = abstractIllager;
			this.hostileRadiusSqr = f * f;
			this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
		}

		@Override
		public boolean canUse() {
			LivingEntity livingEntity = this.mob.getLastHurtByMob();
			return this.mob.getCurrentRaid() == null
				&& this.mob.isPatrolling()
				&& this.mob.getTarget() != null
				&& !this.mob.isAggressive()
				&& (livingEntity == null || livingEntity.getType() != EntityType.PLAYER);
		}

		@Override
		public void start() {
			super.start();
			this.mob.getNavigation().stop();

			for (Raider raider : this.mob.level().getNearbyEntities(Raider.class, this.shoutTargeting, this.mob, this.mob.getBoundingBox().inflate(8.0, 8.0, 8.0))) {
				raider.setTarget(this.mob.getTarget());
			}
		}

		@Override
		public void stop() {
			super.stop();
			LivingEntity livingEntity = this.mob.getTarget();
			if (livingEntity != null) {
				for (Raider raider : this.mob.level().getNearbyEntities(Raider.class, this.shoutTargeting, this.mob, this.mob.getBoundingBox().inflate(8.0, 8.0, 8.0))) {
					raider.setTarget(livingEntity);
					raider.setAggressive(true);
				}

				this.mob.setAggressive(true);
			}
		}

		@Override
		public boolean requiresUpdateEveryTick() {
			return true;
		}

		@Override
		public void tick() {
			LivingEntity livingEntity = this.mob.getTarget();
			if (livingEntity != null) {
				if (this.mob.distanceToSqr(livingEntity) > (double)this.hostileRadiusSqr) {
					this.mob.getLookControl().setLookAt(livingEntity, 30.0F, 30.0F);
					if (this.mob.random.nextInt(50) == 0) {
						this.mob.playAmbientSound();
					}
				} else {
					this.mob.setAggressive(true);
				}

				super.tick();
			}
		}
	}

	public class ObtainRaidLeaderBannerGoal<T extends Raider> extends Goal {
		private final T mob;

		public ObtainRaidLeaderBannerGoal(T raider2) {
			this.mob = raider2;
			this.setFlags(EnumSet.of(Goal.Flag.MOVE));
		}

		@Override
		public boolean canUse() {
			Raid raid = this.mob.getCurrentRaid();
			if (this.mob.hasActiveRaid()
				&& !this.mob.getCurrentRaid().isOver()
				&& this.mob.canBeLeader()
				&& !ItemStack.matches(this.mob.getItemBySlot(EquipmentSlot.HEAD), Raid.getLeaderBannerInstance())) {
				Raider raider = raid.getLeader(this.mob.getWave());
				if (raider == null || !raider.isAlive()) {
					List<ItemEntity> list = this.mob.level().getEntitiesOfClass(ItemEntity.class, this.mob.getBoundingBox().inflate(16.0, 8.0, 16.0), Raider.ALLOWED_ITEMS);
					if (!list.isEmpty()) {
						return this.mob.getNavigation().moveTo((Entity)list.get(0), 1.15F);
					}
				}

				return false;
			} else {
				return false;
			}
		}

		@Override
		public void tick() {
			if (this.mob.getNavigation().getTargetPos().closerToCenterThan(this.mob.position(), 1.414)) {
				List<ItemEntity> list = this.mob.level().getEntitiesOfClass(ItemEntity.class, this.mob.getBoundingBox().inflate(4.0, 4.0, 4.0), Raider.ALLOWED_ITEMS);
				if (!list.isEmpty()) {
					this.mob.pickUpItem((ItemEntity)list.get(0));
				}
			}
		}
	}

	public class RaiderCelebration extends Goal {
		private final Raider mob;

		RaiderCelebration(Raider raider2) {
			this.mob = raider2;
			this.setFlags(EnumSet.of(Goal.Flag.MOVE));
		}

		@Override
		public boolean canUse() {
			Raid raid = this.mob.getCurrentRaid();
			return this.mob.isAlive() && this.mob.getTarget() == null && raid != null && raid.isLoss();
		}

		@Override
		public void start() {
			this.mob.setCelebrating(true);
			super.start();
		}

		@Override
		public void stop() {
			this.mob.setCelebrating(false);
			super.stop();
		}

		@Override
		public void tick() {
			if (!this.mob.isSilent() && this.mob.random.nextInt(this.adjustedTickDelay(100)) == 0) {
				Raider.this.playSound(Raider.this.getCelebrateSound(), Raider.this.getSoundVolume(), Raider.this.getVoicePitch());
			}

			if (!this.mob.isPassenger() && this.mob.random.nextInt(this.adjustedTickDelay(50)) == 0) {
				this.mob.getJumpControl().jump();
			}

			super.tick();
		}
	}

	static class RaiderMoveThroughVillageGoal extends Goal {
		private final Raider raider;
		private final double speedModifier;
		private BlockPos poiPos;
		private final List<BlockPos> visited = Lists.<BlockPos>newArrayList();
		private final int distanceToPoi;
		private boolean stuck;

		public RaiderMoveThroughVillageGoal(Raider raider, double d, int i) {
			this.raider = raider;
			this.speedModifier = d;
			this.distanceToPoi = i;
			this.setFlags(EnumSet.of(Goal.Flag.MOVE));
		}

		@Override
		public boolean canUse() {
			this.updateVisited();
			return this.isValidRaid() && this.hasSuitablePoi() && this.raider.getTarget() == null;
		}

		private boolean isValidRaid() {
			return this.raider.hasActiveRaid() && !this.raider.getCurrentRaid().isOver();
		}

		private boolean hasSuitablePoi() {
			ServerLevel serverLevel = (ServerLevel)this.raider.level();
			BlockPos blockPos = this.raider.blockPosition();
			Optional<BlockPos> optional = serverLevel.getPoiManager()
				.getRandom(holder -> holder.is(PoiTypes.HOME), this::hasNotVisited, PoiManager.Occupancy.ANY, blockPos, 48, this.raider.random);
			if (!optional.isPresent()) {
				return false;
			} else {
				this.poiPos = ((BlockPos)optional.get()).immutable();
				return true;
			}
		}

		@Override
		public boolean canContinueToUse() {
			return this.raider.getNavigation().isDone()
				? false
				: this.raider.getTarget() == null
					&& !this.poiPos.closerToCenterThan(this.raider.position(), (double)(this.raider.getBbWidth() + (float)this.distanceToPoi))
					&& !this.stuck;
		}

		@Override
		public void stop() {
			if (this.poiPos.closerToCenterThan(this.raider.position(), (double)this.distanceToPoi)) {
				this.visited.add(this.poiPos);
			}
		}

		@Override
		public void start() {
			super.start();
			this.raider.setNoActionTime(0);
			this.raider.getNavigation().moveTo((double)this.poiPos.getX(), (double)this.poiPos.getY(), (double)this.poiPos.getZ(), this.speedModifier);
			this.stuck = false;
		}

		@Override
		public void tick() {
			if (this.raider.getNavigation().isDone()) {
				Vec3 vec3 = Vec3.atBottomCenterOf(this.poiPos);
				Vec3 vec32 = DefaultRandomPos.getPosTowards(this.raider, 16, 7, vec3, (float) (Math.PI / 10));
				if (vec32 == null) {
					vec32 = DefaultRandomPos.getPosTowards(this.raider, 8, 7, vec3, (float) (Math.PI / 2));
				}

				if (vec32 == null) {
					this.stuck = true;
					return;
				}

				this.raider.getNavigation().moveTo(vec32.x, vec32.y, vec32.z, this.speedModifier);
			}
		}

		private boolean hasNotVisited(BlockPos blockPos) {
			for (BlockPos blockPos2 : this.visited) {
				if (Objects.equals(blockPos, blockPos2)) {
					return false;
				}
			}

			return true;
		}

		private void updateVisited() {
			if (this.visited.size() > 2) {
				this.visited.remove(0);
			}
		}
	}
}
