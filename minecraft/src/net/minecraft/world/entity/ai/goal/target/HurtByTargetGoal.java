package net.minecraft.world.entity.ai.goal.target;

import java.util.EnumSet;
import java.util.Iterator;
import java.util.List;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.phys.AABB;

public class HurtByTargetGoal extends TargetGoal {
	private static final TargetingConditions HURT_BY_TARGETING = new TargetingConditions().allowUnseeable().ignoreInvisibilityTesting();
	private boolean alertSameType;
	private int timestamp;
	private final Class<?>[] toIgnoreDamage;
	private Class<?>[] toIgnoreAlert;

	public HurtByTargetGoal(PathfinderMob pathfinderMob, Class<?>... classs) {
		super(pathfinderMob, true);
		this.toIgnoreDamage = classs;
		this.setFlags(EnumSet.of(Goal.Flag.TARGET));
	}

	@Override
	public boolean canUse() {
		int i = this.mob.getLastHurtByMobTimestamp();
		LivingEntity livingEntity = this.mob.getLastHurtByMob();
		if (i != this.timestamp && livingEntity != null) {
			if (livingEntity.getType() == EntityType.PLAYER && this.mob.level.getGameRules().getBoolean(GameRules.RULE_UNIVERSAL_ANGER)) {
				return false;
			} else {
				for (Class<?> class_ : this.toIgnoreDamage) {
					if (class_.isAssignableFrom(livingEntity.getClass())) {
						return false;
					}
				}

				return this.canAttack(livingEntity, HURT_BY_TARGETING);
			}
		} else {
			return false;
		}
	}

	public HurtByTargetGoal setAlertOthers(Class<?>... classs) {
		this.alertSameType = true;
		this.toIgnoreAlert = classs;
		return this;
	}

	@Override
	public void start() {
		this.mob.setTarget(this.mob.getLastHurtByMob());
		this.targetMob = this.mob.getTarget();
		this.timestamp = this.mob.getLastHurtByMobTimestamp();
		this.unseenMemoryTicks = 300;
		if (this.alertSameType) {
			this.alertOthers();
		}

		super.start();
	}

	protected void alertOthers() {
		double d = this.getFollowDistance();
		AABB aABB = AABB.unitCubeFromLowerCorner(this.mob.position()).inflate(d, 10.0, d);
		List<Mob> list = this.mob.level.getLoadedEntitiesOfClass(this.mob.getClass(), aABB);
		Iterator var5 = list.iterator();

		while (true) {
			Mob mob;
			while (true) {
				if (!var5.hasNext()) {
					return;
				}

				mob = (Mob)var5.next();
				if (this.mob != mob
					&& mob.getTarget() == null
					&& (!(this.mob instanceof TamableAnimal) || ((TamableAnimal)this.mob).getOwner() == ((TamableAnimal)mob).getOwner())
					&& !mob.isAlliedTo(this.mob.getLastHurtByMob())) {
					if (this.toIgnoreAlert == null) {
						break;
					}

					boolean bl = false;

					for (Class<?> class_ : this.toIgnoreAlert) {
						if (mob.getClass() == class_) {
							bl = true;
							break;
						}
					}

					if (!bl) {
						break;
					}
				}
			}

			this.alertOther(mob, this.mob.getLastHurtByMob());
		}
	}

	protected void alertOther(Mob mob, LivingEntity livingEntity) {
		mob.setTarget(livingEntity);
	}
}
