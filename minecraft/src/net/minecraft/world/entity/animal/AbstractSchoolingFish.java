package net.minecraft.world.entity.animal;

import java.util.List;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.goal.FollowFlockLeaderGoal;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;

public abstract class AbstractSchoolingFish extends AbstractFish {
	@Nullable
	private AbstractSchoolingFish leader;
	private int schoolSize = 1;

	public AbstractSchoolingFish(EntityType<? extends AbstractSchoolingFish> entityType, Level level) {
		super(entityType, level);
	}

	@Override
	protected void registerGoals() {
		super.registerGoals();
		this.goalSelector.addGoal(5, new FollowFlockLeaderGoal(this));
	}

	@Override
	public int getMaxSpawnClusterSize() {
		return this.getMaxSchoolSize();
	}

	public int getMaxSchoolSize() {
		return super.getMaxSpawnClusterSize();
	}

	@Override
	protected boolean canRandomSwim() {
		return !this.isFollower();
	}

	public boolean isFollower() {
		return this.leader != null && this.leader.isAlive();
	}

	public AbstractSchoolingFish startFollowing(AbstractSchoolingFish abstractSchoolingFish) {
		this.leader = abstractSchoolingFish;
		abstractSchoolingFish.addFollower();
		return abstractSchoolingFish;
	}

	public void stopFollowing() {
		this.leader.removeFollower();
		this.leader = null;
	}

	private void addFollower() {
		this.schoolSize++;
	}

	private void removeFollower() {
		this.schoolSize--;
	}

	public boolean canBeFollowed() {
		return this.hasFollowers() && this.schoolSize < this.getMaxSchoolSize();
	}

	@Override
	public void tick() {
		super.tick();
		if (this.hasFollowers() && this.level().random.nextInt(200) == 1) {
			List<? extends AbstractFish> list = this.level().getEntitiesOfClass(this.getClass(), this.getBoundingBox().inflate(8.0, 8.0, 8.0));
			if (list.size() <= 1) {
				this.schoolSize = 1;
			}
		}
	}

	public boolean hasFollowers() {
		return this.schoolSize > 1;
	}

	public boolean inRangeOfLeader() {
		return this.distanceToSqr(this.leader) <= 121.0;
	}

	public void pathToLeader() {
		if (this.isFollower()) {
			this.getNavigation().moveTo(this.leader, 1.0);
		}
	}

	public void addFollowers(Stream<? extends AbstractSchoolingFish> stream) {
		stream.limit((long)(this.getMaxSchoolSize() - this.schoolSize))
			.filter(abstractSchoolingFish -> abstractSchoolingFish != this)
			.forEach(abstractSchoolingFish -> abstractSchoolingFish.startFollowing(this));
	}

	@Nullable
	@Override
	public SpawnGroupData finalizeSpawn(
		ServerLevelAccessor serverLevelAccessor, DifficultyInstance difficultyInstance, MobSpawnType mobSpawnType, @Nullable SpawnGroupData spawnGroupData
	) {
		super.finalizeSpawn(serverLevelAccessor, difficultyInstance, mobSpawnType, spawnGroupData);
		if (spawnGroupData == null) {
			spawnGroupData = new AbstractSchoolingFish.SchoolSpawnGroupData(this);
		} else {
			this.startFollowing(((AbstractSchoolingFish.SchoolSpawnGroupData)spawnGroupData).leader);
		}

		return spawnGroupData;
	}

	public static class SchoolSpawnGroupData implements SpawnGroupData {
		public final AbstractSchoolingFish leader;

		public SchoolSpawnGroupData(AbstractSchoolingFish abstractSchoolingFish) {
			this.leader = abstractSchoolingFish;
		}
	}
}
