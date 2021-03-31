package net.minecraft.world.entity.ai.goal.target;

import javax.annotation.Nullable;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.level.pathfinder.Node;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.scores.Team;

public abstract class TargetGoal extends Goal {
	private static final int EMPTY_REACH_CACHE = 0;
	private static final int CAN_REACH_CACHE = 1;
	private static final int CANT_REACH_CACHE = 2;
	protected final Mob mob;
	protected final boolean mustSee;
	private final boolean mustReach;
	private int reachCache;
	private int reachCacheTime;
	private int unseenTicks;
	protected LivingEntity targetMob;
	protected int unseenMemoryTicks = 60;

	public TargetGoal(Mob mob, boolean bl) {
		this(mob, bl, false);
	}

	public TargetGoal(Mob mob, boolean bl, boolean bl2) {
		this.mob = mob;
		this.mustSee = bl;
		this.mustReach = bl2;
	}

	@Override
	public boolean canContinueToUse() {
		LivingEntity livingEntity = this.mob.getTarget();
		if (livingEntity == null) {
			livingEntity = this.targetMob;
		}

		if (livingEntity == null) {
			return false;
		} else if (!livingEntity.isAlive()) {
			return false;
		} else {
			Team team = this.mob.getTeam();
			Team team2 = livingEntity.getTeam();
			if (team != null && team2 == team) {
				return false;
			} else {
				double d = this.getFollowDistance();
				if (this.mob.distanceToSqr(livingEntity) > d * d) {
					return false;
				} else {
					if (this.mustSee) {
						if (this.mob.getSensing().canSee(livingEntity)) {
							this.unseenTicks = 0;
						} else if (++this.unseenTicks > this.unseenMemoryTicks) {
							return false;
						}
					}

					if (!livingEntity.canBeTargeted()) {
						return false;
					} else {
						this.mob.setTarget(livingEntity);
						return true;
					}
				}
			}
		}
	}

	protected double getFollowDistance() {
		return this.mob.getAttributeValue(Attributes.FOLLOW_RANGE);
	}

	@Override
	public void start() {
		this.reachCache = 0;
		this.reachCacheTime = 0;
		this.unseenTicks = 0;
	}

	@Override
	public void stop() {
		this.mob.setTarget(null);
		this.targetMob = null;
	}

	protected boolean canAttack(@Nullable LivingEntity livingEntity, TargetingConditions targetingConditions) {
		if (livingEntity == null) {
			return false;
		} else if (!targetingConditions.test(this.mob, livingEntity)) {
			return false;
		} else if (!this.mob.isWithinRestriction(livingEntity.blockPosition())) {
			return false;
		} else {
			if (this.mustReach) {
				if (--this.reachCacheTime <= 0) {
					this.reachCache = 0;
				}

				if (this.reachCache == 0) {
					this.reachCache = this.canReach(livingEntity) ? 1 : 2;
				}

				if (this.reachCache == 2) {
					return false;
				}
			}

			return true;
		}
	}

	private boolean canReach(LivingEntity livingEntity) {
		this.reachCacheTime = 10 + this.mob.getRandom().nextInt(5);
		Path path = this.mob.getNavigation().createPath(livingEntity, 0);
		if (path == null) {
			return false;
		} else {
			Node node = path.getEndNode();
			if (node == null) {
				return false;
			} else {
				int i = node.x - livingEntity.getBlockX();
				int j = node.z - livingEntity.getBlockZ();
				return (double)(i * i + j * j) <= 2.25;
			}
		}
	}

	public TargetGoal setUnseenMemoryTicks(int i) {
		this.unseenMemoryTicks = i;
		return this;
	}
}
