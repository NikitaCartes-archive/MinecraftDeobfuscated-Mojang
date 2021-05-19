package net.minecraft.world.entity.ai.targeting;

import java.util.function.Predicate;
import javax.annotation.Nullable;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;

public class TargetingConditions {
	public static final TargetingConditions DEFAULT = forCombat();
	private static final double MIN_VISIBILITY_DISTANCE_FOR_INVISIBLE_TARGET = 2.0;
	private final boolean isCombat;
	private double range = -1.0;
	private boolean checkLineOfSight = true;
	private boolean testInvisible = true;
	private Predicate<LivingEntity> selector;

	private TargetingConditions(boolean bl) {
		this.isCombat = bl;
	}

	public static TargetingConditions forCombat() {
		return new TargetingConditions(true);
	}

	public static TargetingConditions forNonCombat() {
		return new TargetingConditions(false);
	}

	public TargetingConditions copy() {
		TargetingConditions targetingConditions = this.isCombat ? forCombat() : forNonCombat();
		targetingConditions.range = this.range;
		targetingConditions.checkLineOfSight = this.checkLineOfSight;
		targetingConditions.testInvisible = this.testInvisible;
		targetingConditions.selector = this.selector;
		return targetingConditions;
	}

	public TargetingConditions range(double d) {
		this.range = d;
		return this;
	}

	public TargetingConditions ignoreLineOfSight() {
		this.checkLineOfSight = false;
		return this;
	}

	public TargetingConditions ignoreInvisibilityTesting() {
		this.testInvisible = false;
		return this;
	}

	public TargetingConditions selector(@Nullable Predicate<LivingEntity> predicate) {
		this.selector = predicate;
		return this;
	}

	public boolean test(@Nullable LivingEntity livingEntity, LivingEntity livingEntity2) {
		if (livingEntity == livingEntity2) {
			return false;
		} else if (!livingEntity2.canBeSeenByAnyone()) {
			return false;
		} else if (this.selector != null && !this.selector.test(livingEntity2)) {
			return false;
		} else {
			if (livingEntity == null) {
				if (this.isCombat && !livingEntity2.canBeSeenAsEnemy()) {
					return false;
				}
			} else {
				if (this.isCombat
					&& (!livingEntity.canAttack(livingEntity2) || !livingEntity.canAttackType(livingEntity2.getType()) || livingEntity.isAlliedTo(livingEntity2))) {
					return false;
				}

				if (this.range > 0.0) {
					double d = this.testInvisible ? livingEntity2.getVisibilityPercent(livingEntity) : 1.0;
					double e = Math.max(this.range * d, 2.0);
					double f = livingEntity.distanceToSqr(livingEntity2.getX(), livingEntity2.getY(), livingEntity2.getZ());
					if (f > e * e) {
						return false;
					}
				}

				if (this.checkLineOfSight && livingEntity instanceof Mob && !((Mob)livingEntity).getSensing().hasLineOfSight(livingEntity2)) {
					return false;
				}
			}

			return true;
		}
	}
}
