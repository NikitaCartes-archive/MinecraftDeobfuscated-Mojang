package net.minecraft.world.entity.monster;

import java.util.EnumSet;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.util.RandomSource;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.raid.Raid;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.Vec3;

public abstract class PatrollingMonster extends Monster {
	@Nullable
	private BlockPos patrolTarget;
	private boolean patrolLeader;
	private boolean patrolling;

	protected PatrollingMonster(EntityType<? extends PatrollingMonster> entityType, Level level) {
		super(entityType, level);
	}

	@Override
	protected void registerGoals() {
		super.registerGoals();
		this.goalSelector.addGoal(4, new PatrollingMonster.LongDistancePatrolGoal<>(this, 0.7, 0.595));
	}

	@Override
	public void addAdditionalSaveData(CompoundTag compoundTag) {
		super.addAdditionalSaveData(compoundTag);
		if (this.patrolTarget != null) {
			compoundTag.put("patrol_target", NbtUtils.writeBlockPos(this.patrolTarget));
		}

		compoundTag.putBoolean("PatrolLeader", this.patrolLeader);
		compoundTag.putBoolean("Patrolling", this.patrolling);
	}

	@Override
	public void readAdditionalSaveData(CompoundTag compoundTag) {
		super.readAdditionalSaveData(compoundTag);
		NbtUtils.readBlockPos(compoundTag, "patrol_target").ifPresent(blockPos -> this.patrolTarget = blockPos);
		this.patrolLeader = compoundTag.getBoolean("PatrolLeader");
		this.patrolling = compoundTag.getBoolean("Patrolling");
	}

	public boolean canBeLeader() {
		return true;
	}

	@Nullable
	@Override
	public SpawnGroupData finalizeSpawn(
		ServerLevelAccessor serverLevelAccessor, DifficultyInstance difficultyInstance, MobSpawnType mobSpawnType, @Nullable SpawnGroupData spawnGroupData
	) {
		if (mobSpawnType != MobSpawnType.PATROL
			&& mobSpawnType != MobSpawnType.EVENT
			&& mobSpawnType != MobSpawnType.STRUCTURE
			&& serverLevelAccessor.getRandom().nextFloat() < 0.06F
			&& this.canBeLeader()) {
			this.patrolLeader = true;
		}

		if (this.isPatrolLeader()) {
			this.setItemSlot(EquipmentSlot.HEAD, Raid.getLeaderBannerInstance(this.registryAccess().lookupOrThrow(Registries.BANNER_PATTERN)));
			this.setDropChance(EquipmentSlot.HEAD, 2.0F);
		}

		if (mobSpawnType == MobSpawnType.PATROL) {
			this.patrolling = true;
		}

		return super.finalizeSpawn(serverLevelAccessor, difficultyInstance, mobSpawnType, spawnGroupData);
	}

	public static boolean checkPatrollingMonsterSpawnRules(
		EntityType<? extends PatrollingMonster> entityType, LevelAccessor levelAccessor, MobSpawnType mobSpawnType, BlockPos blockPos, RandomSource randomSource
	) {
		return levelAccessor.getBrightness(LightLayer.BLOCK, blockPos) > 8
			? false
			: checkAnyLightMonsterSpawnRules(entityType, levelAccessor, mobSpawnType, blockPos, randomSource);
	}

	@Override
	public boolean removeWhenFarAway(double d) {
		return !this.patrolling || d > 16384.0;
	}

	public void setPatrolTarget(BlockPos blockPos) {
		this.patrolTarget = blockPos;
		this.patrolling = true;
	}

	public BlockPos getPatrolTarget() {
		return this.patrolTarget;
	}

	public boolean hasPatrolTarget() {
		return this.patrolTarget != null;
	}

	public void setPatrolLeader(boolean bl) {
		this.patrolLeader = bl;
		this.patrolling = true;
	}

	public boolean isPatrolLeader() {
		return this.patrolLeader;
	}

	public boolean canJoinPatrol() {
		return true;
	}

	public void findPatrolTarget() {
		this.patrolTarget = this.blockPosition().offset(-500 + this.random.nextInt(1000), 0, -500 + this.random.nextInt(1000));
		this.patrolling = true;
	}

	protected boolean isPatrolling() {
		return this.patrolling;
	}

	protected void setPatrolling(boolean bl) {
		this.patrolling = bl;
	}

	public static class LongDistancePatrolGoal<T extends PatrollingMonster> extends Goal {
		private static final int NAVIGATION_FAILED_COOLDOWN = 200;
		private final T mob;
		private final double speedModifier;
		private final double leaderSpeedModifier;
		private long cooldownUntil;

		public LongDistancePatrolGoal(T patrollingMonster, double d, double e) {
			this.mob = patrollingMonster;
			this.speedModifier = d;
			this.leaderSpeedModifier = e;
			this.cooldownUntil = -1L;
			this.setFlags(EnumSet.of(Goal.Flag.MOVE));
		}

		@Override
		public boolean canUse() {
			boolean bl = this.mob.level().getGameTime() < this.cooldownUntil;
			return this.mob.isPatrolling() && this.mob.getTarget() == null && !this.mob.hasControllingPassenger() && this.mob.hasPatrolTarget() && !bl;
		}

		@Override
		public void start() {
		}

		@Override
		public void stop() {
		}

		@Override
		public void tick() {
			boolean bl = this.mob.isPatrolLeader();
			PathNavigation pathNavigation = this.mob.getNavigation();
			if (pathNavigation.isDone()) {
				List<PatrollingMonster> list = this.findPatrolCompanions();
				if (this.mob.isPatrolling() && list.isEmpty()) {
					this.mob.setPatrolling(false);
				} else if (bl && this.mob.getPatrolTarget().closerToCenterThan(this.mob.position(), 10.0)) {
					this.mob.findPatrolTarget();
				} else {
					Vec3 vec3 = Vec3.atBottomCenterOf(this.mob.getPatrolTarget());
					Vec3 vec32 = this.mob.position();
					Vec3 vec33 = vec32.subtract(vec3);
					vec3 = vec33.yRot(90.0F).scale(0.4).add(vec3);
					Vec3 vec34 = vec3.subtract(vec32).normalize().scale(10.0).add(vec32);
					BlockPos blockPos = BlockPos.containing(vec34);
					blockPos = this.mob.level().getHeightmapPos(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, blockPos);
					if (!pathNavigation.moveTo((double)blockPos.getX(), (double)blockPos.getY(), (double)blockPos.getZ(), bl ? this.leaderSpeedModifier : this.speedModifier)) {
						this.moveRandomly();
						this.cooldownUntil = this.mob.level().getGameTime() + 200L;
					} else if (bl) {
						for (PatrollingMonster patrollingMonster : list) {
							patrollingMonster.setPatrolTarget(blockPos);
						}
					}
				}
			}
		}

		private List<PatrollingMonster> findPatrolCompanions() {
			return this.mob
				.level()
				.getEntitiesOfClass(
					PatrollingMonster.class,
					this.mob.getBoundingBox().inflate(16.0),
					patrollingMonster -> patrollingMonster.canJoinPatrol() && !patrollingMonster.is(this.mob)
				);
		}

		private boolean moveRandomly() {
			RandomSource randomSource = this.mob.getRandom();
			BlockPos blockPos = this.mob
				.level()
				.getHeightmapPos(
					Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, this.mob.blockPosition().offset(-8 + randomSource.nextInt(16), 0, -8 + randomSource.nextInt(16))
				);
			return this.mob.getNavigation().moveTo((double)blockPos.getX(), (double)blockPos.getY(), (double)blockPos.getZ(), this.speedModifier);
		}
	}
}
