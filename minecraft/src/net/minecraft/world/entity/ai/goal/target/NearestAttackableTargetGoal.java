package net.minecraft.world.entity.ai.goal.target;

import java.util.EnumSet;
import java.util.Objects;
import java.util.function.Predicate;
import javax.annotation.Nullable;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.phys.AABB;

public class NearestAttackableTargetGoal<T extends LivingEntity> extends TargetGoal {
	private static final int DEFAULT_RANDOM_INTERVAL = 10;
	private final Predicate<Entity> targetFilter;
	protected final int randomInterval;
	@Nullable
	protected LivingEntity target;
	protected TargetingConditions targetConditions;

	public NearestAttackableTargetGoal(Mob mob, Class<T> class_, boolean bl) {
		this(mob, class_, 10, bl, false, null);
	}

	public NearestAttackableTargetGoal(Mob mob, Class<T> class_, boolean bl, Predicate<LivingEntity> predicate) {
		this(mob, class_, 10, bl, false, predicate);
	}

	public NearestAttackableTargetGoal(Mob mob, Class<T> class_, boolean bl, boolean bl2) {
		this(mob, class_, 10, bl, bl2, null);
	}

	public NearestAttackableTargetGoal(Mob mob, Class<T> class_, int i, boolean bl, boolean bl2, @Nullable Predicate<LivingEntity> predicate) {
		this(mob, (Predicate<Entity>)(entity -> class_.isAssignableFrom(entity.getClass())), i, bl, bl2, predicate);
	}

	public NearestAttackableTargetGoal(Mob mob, Predicate<Entity> predicate, int i, boolean bl, boolean bl2, @Nullable Predicate<LivingEntity> predicate2) {
		super(mob, bl, bl2);
		this.targetFilter = predicate;
		this.randomInterval = reducedTickDelay(i);
		this.setFlags(EnumSet.of(Goal.Flag.TARGET));
		this.targetConditions = TargetingConditions.forCombat().range(this.getFollowDistance()).selector(predicate2);
	}

	@Override
	public boolean canUse() {
		if (this.randomInterval > 0 && this.mob.getRandom().nextInt(this.randomInterval) != 0) {
			return false;
		} else {
			this.findTarget();
			return this.target != null;
		}
	}

	protected AABB getTargetSearchArea(double d) {
		return this.mob.getBoundingBox().inflate(d, 4.0, d);
	}

	protected void findTarget() {
		this.target = this.mob
			.level
			.getNearestEntity(this.mob.level.getEntitiesOfClass(LivingEntity.class, this.getTargetSearchArea(this.getFollowDistance()), livingEntity -> {
				Entity entity = (Entity)Objects.requireNonNullElse(livingEntity.getTransform().entity(), livingEntity);
				return this.targetFilter.test(entity);
			}), this.targetConditions, this.mob, this.mob.getX(), this.mob.getEyeY(), this.mob.getZ());
	}

	@Override
	public void start() {
		this.mob.setTarget(this.target);
		super.start();
	}

	public void setTarget(@Nullable LivingEntity livingEntity) {
		this.target = livingEntity;
	}
}
