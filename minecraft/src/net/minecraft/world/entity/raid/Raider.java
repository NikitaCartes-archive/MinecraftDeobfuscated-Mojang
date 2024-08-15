package net.minecraft.world.entity.raid;

import com.google.common.collect.Lists;
import it.unimi.dsi.fastutil.ints.Int2LongOpenHashMap;
import java.util.EnumSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.PathfindToRaidGoal;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.ai.util.DefaultRandomPos;
import net.minecraft.world.entity.ai.village.poi.PoiManager;
import net.minecraft.world.entity.ai.village.poi.PoiTypes;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.AbstractIllager;
import net.minecraft.world.entity.monster.PatrollingMonster;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.phys.Vec3;

public abstract class Raider extends PatrollingMonster {
	protected static final EntityDataAccessor<Boolean> IS_CELEBRATING = SynchedEntityData.defineId(Raider.class, EntityDataSerializers.BOOLEAN);
	static final Predicate<ItemEntity> ALLOWED_ITEMS = itemEntity -> !itemEntity.hasPickUpDelay()
			&& itemEntity.isAlive()
			&& ItemStack.matches(itemEntity.getItem(), Raid.getOminousBannerInstance(itemEntity.registryAccess().lookupOrThrow(Registries.BANNER_PATTERN)));
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
	protected void defineSynchedData(SynchedEntityData.Builder builder) {
		super.defineSynchedData(builder);
		builder.define(IS_CELEBRATING, false);
	}

	public abstract void applyRaidBuffs(ServerLevel serverLevel, int i, boolean bl);

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

	public boolean isCaptain() {
		ItemStack itemStack = this.getItemBySlot(EquipmentSlot.HEAD);
		boolean bl = !itemStack.isEmpty()
			&& ItemStack.matches(itemStack, Raid.getOminousBannerInstance(this.registryAccess().lookupOrThrow(Registries.BANNER_PATTERN)));
		boolean bl2 = this.isPatrolLeader();
		return bl && bl2;
	}

	public boolean hasRaid() {
		return !(this.level() instanceof ServerLevel serverLevel) ? false : this.getCurrentRaid() != null || serverLevel.getRaidAt(this.blockPosition()) != null;
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
		if (this.hasActiveRaid()
			&& !bl
			&& ItemStack.matches(itemStack, Raid.getOminousBannerInstance(this.registryAccess().lookupOrThrow(Registries.BANNER_PATTERN)))) {
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
		ServerLevelAccessor serverLevelAccessor, DifficultyInstance difficultyInstance, EntitySpawnReason entitySpawnReason, @Nullable SpawnGroupData spawnGroupData
	) {
		this.setCanJoinRaid(this.getType() != EntityType.WITCH || entitySpawnReason != EntitySpawnReason.NATURAL);
		return super.finalizeSpawn(serverLevelAccessor, difficultyInstance, entitySpawnReason, spawnGroupData);
	}

	public abstract SoundEvent getCelebrateSound();

	protected class HoldGroundAttackGoal extends Goal {
		private final Raider mob;
		private final float hostileRadiusSqr;
		public final TargetingConditions shoutTargeting = TargetingConditions.forNonCombat().range(8.0).ignoreLineOfSight().ignoreInvisibilityTesting();

		public HoldGroundAttackGoal(final AbstractIllager abstractIllager, final float f) {
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
		private Int2LongOpenHashMap unreachableBannerCache = new Int2LongOpenHashMap();
		@Nullable
		private Path pathToBanner;
		@Nullable
		private ItemEntity pursuedBannerItemEntity;

		public ObtainRaidLeaderBannerGoal(final T raider2) {
			this.mob = raider2;
			this.setFlags(EnumSet.of(Goal.Flag.MOVE));
		}

		@Override
		public boolean canUse() {
			if (this.cannotPickUpBanner()) {
				return false;
			} else {
				Int2LongOpenHashMap int2LongOpenHashMap = new Int2LongOpenHashMap();
				double d = Raider.this.getAttributeValue(Attributes.FOLLOW_RANGE);

				for (ItemEntity itemEntity : this.mob
					.level()
					.getEntitiesOfClass((Class<T>)ItemEntity.class, this.mob.getBoundingBox().inflate(d, 8.0, d), Raider.ALLOWED_ITEMS)) {
					long l = this.unreachableBannerCache.getOrDefault(itemEntity.getId(), Long.MIN_VALUE);
					if (Raider.this.level().getGameTime() < l) {
						int2LongOpenHashMap.put(itemEntity.getId(), l);
					} else {
						Path path = this.mob.getNavigation().createPath(itemEntity, 1);
						if (path != null && path.canReach()) {
							this.pathToBanner = path;
							this.pursuedBannerItemEntity = itemEntity;
							return true;
						}

						int2LongOpenHashMap.put(itemEntity.getId(), Raider.this.level().getGameTime() + 600L);
					}
				}

				this.unreachableBannerCache = int2LongOpenHashMap;
				return false;
			}
		}

		@Override
		public boolean canContinueToUse() {
			if (this.pursuedBannerItemEntity == null || this.pathToBanner == null) {
				return false;
			} else if (this.pursuedBannerItemEntity.isRemoved()) {
				return false;
			} else {
				return this.pathToBanner.isDone() ? false : !this.cannotPickUpBanner();
			}
		}

		private boolean cannotPickUpBanner() {
			if (!this.mob.hasActiveRaid()) {
				return true;
			} else if (this.mob.getCurrentRaid().isOver()) {
				return true;
			} else if (!this.mob.canBeLeader()) {
				return true;
			} else if (ItemStack.matches(
				this.mob.getItemBySlot(EquipmentSlot.HEAD), Raid.getOminousBannerInstance(this.mob.registryAccess().lookupOrThrow(Registries.BANNER_PATTERN))
			)) {
				return true;
			} else {
				Raider raider = Raider.this.raid.getLeader(this.mob.getWave());
				return raider != null && raider.isAlive();
			}
		}

		@Override
		public void start() {
			this.mob.getNavigation().moveTo(this.pathToBanner, 1.15F);
		}

		@Override
		public void stop() {
			this.pathToBanner = null;
			this.pursuedBannerItemEntity = null;
		}

		@Override
		public void tick() {
			if (this.pursuedBannerItemEntity != null && this.pursuedBannerItemEntity.closerThan(this.mob, 1.414)) {
				this.mob.pickUpItem(this.pursuedBannerItemEntity);
			}
		}
	}

	public class RaiderCelebration extends Goal {
		private final Raider mob;

		RaiderCelebration(final Raider raider2) {
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
				Raider.this.makeSound(Raider.this.getCelebrateSound());
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
			if (optional.isEmpty()) {
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
