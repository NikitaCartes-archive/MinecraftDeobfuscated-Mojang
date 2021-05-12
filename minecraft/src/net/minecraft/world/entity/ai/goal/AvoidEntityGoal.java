package net.minecraft.world.entity.ai.goal;

import java.util.EnumSet;
import java.util.function.Predicate;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.ai.util.DefaultRandomPos;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.phys.Vec3;

public class AvoidEntityGoal<T extends LivingEntity> extends Goal {
	protected final PathfinderMob mob;
	private final double walkSpeedModifier;
	private final double sprintSpeedModifier;
	protected T toAvoid;
	protected final float maxDist;
	protected Path path;
	protected final PathNavigation pathNav;
	protected final Class<T> avoidClass;
	protected final Predicate<LivingEntity> avoidPredicate;
	protected final Predicate<LivingEntity> predicateOnAvoidEntity;
	private final TargetingConditions avoidEntityTargeting;

	public AvoidEntityGoal(PathfinderMob pathfinderMob, Class<T> class_, float f, double d, double e) {
		this(pathfinderMob, class_, livingEntity -> true, f, d, e, EntitySelector.NO_CREATIVE_OR_SPECTATOR::test);
	}

	public AvoidEntityGoal(
		PathfinderMob pathfinderMob, Class<T> class_, Predicate<LivingEntity> predicate, float f, double d, double e, Predicate<LivingEntity> predicate2
	) {
		this.mob = pathfinderMob;
		this.avoidClass = class_;
		this.avoidPredicate = predicate;
		this.maxDist = f;
		this.walkSpeedModifier = d;
		this.sprintSpeedModifier = e;
		this.predicateOnAvoidEntity = predicate2;
		this.pathNav = pathfinderMob.getNavigation();
		this.setFlags(EnumSet.of(Goal.Flag.MOVE));
		this.avoidEntityTargeting = TargetingConditions.forCombat().range((double)f).selector(predicate2.and(predicate));
	}

	public AvoidEntityGoal(PathfinderMob pathfinderMob, Class<T> class_, float f, double d, double e, Predicate<LivingEntity> predicate) {
		this(pathfinderMob, class_, livingEntity -> true, f, d, e, predicate);
	}

	@Override
	public boolean canUse() {
		this.toAvoid = this.mob
			.level
			.getNearestEntity(
				this.mob
					.level
					.getEntitiesOfClass(this.avoidClass, this.mob.getBoundingBox().inflate((double)this.maxDist, 3.0, (double)this.maxDist), livingEntity -> true),
				this.avoidEntityTargeting,
				this.mob,
				this.mob.getX(),
				this.mob.getY(),
				this.mob.getZ()
			);
		if (this.toAvoid == null) {
			return false;
		} else {
			Vec3 vec3 = DefaultRandomPos.getPosAway(this.mob, 16, 7, this.toAvoid.position());
			if (vec3 == null) {
				return false;
			} else if (this.toAvoid.distanceToSqr(vec3.x, vec3.y, vec3.z) < this.toAvoid.distanceToSqr(this.mob)) {
				return false;
			} else {
				this.path = this.pathNav.createPath(vec3.x, vec3.y, vec3.z, 0);
				return this.path != null;
			}
		}
	}

	@Override
	public boolean canContinueToUse() {
		return !this.pathNav.isDone();
	}

	@Override
	public void start() {
		this.pathNav.moveTo(this.path, this.walkSpeedModifier);
	}

	@Override
	public void stop() {
		this.toAvoid = null;
	}

	@Override
	public void tick() {
		if (this.mob.distanceToSqr(this.toAvoid) < 49.0) {
			this.mob.getNavigation().setSpeedModifier(this.sprintSpeedModifier);
		} else {
			this.mob.getNavigation().setSpeedModifier(this.walkSpeedModifier);
		}
	}
}
