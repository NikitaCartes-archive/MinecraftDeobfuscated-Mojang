package net.minecraft.world.entity.ai.goal;

import java.util.EnumSet;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.player.Player;

public class LookAtPlayerGoal extends Goal {
	protected final Mob mob;
	protected Entity lookAt;
	protected final float lookDistance;
	private int lookTime;
	protected final float probability;
	protected final Class<? extends LivingEntity> lookAtType;
	protected final TargetingConditions lookAtContext;

	public LookAtPlayerGoal(Mob mob, Class<? extends LivingEntity> class_, float f) {
		this(mob, class_, f, 0.02F);
	}

	public LookAtPlayerGoal(Mob mob, Class<? extends LivingEntity> class_, float f, float g) {
		this.mob = mob;
		this.lookAtType = class_;
		this.lookDistance = f;
		this.probability = g;
		this.setFlags(EnumSet.of(Goal.Flag.LOOK));
		if (class_ == Player.class) {
			this.lookAtContext = new TargetingConditions()
				.range((double)f)
				.allowSameTeam()
				.allowInvulnerable()
				.allowNonAttackable()
				.selector(livingEntity -> EntitySelector.notRiding(mob).test(livingEntity));
		} else {
			this.lookAtContext = new TargetingConditions().range((double)f).allowSameTeam().allowInvulnerable().allowNonAttackable();
		}
	}

	@Override
	public boolean canUse() {
		if (this.mob.getRandom().nextFloat() >= this.probability) {
			return false;
		} else {
			if (this.mob.getTarget() != null) {
				this.lookAt = this.mob.getTarget();
			}

			if (this.lookAtType == Player.class) {
				this.lookAt = this.mob.level.getNearestPlayer(this.lookAtContext, this.mob, this.mob.getX(), this.mob.getEyeY(), this.mob.getZ());
			} else {
				this.lookAt = this.mob
					.level
					.getNearestLoadedEntity(
						this.lookAtType,
						this.lookAtContext,
						this.mob,
						this.mob.getX(),
						this.mob.getEyeY(),
						this.mob.getZ(),
						this.mob.getBoundingBox().inflate((double)this.lookDistance, 3.0, (double)this.lookDistance)
					);
			}

			return this.lookAt != null;
		}
	}

	@Override
	public boolean canContinueToUse() {
		if (!this.lookAt.isAlive()) {
			return false;
		} else {
			return this.mob.distanceToSqr(this.lookAt) > (double)(this.lookDistance * this.lookDistance) ? false : this.lookTime > 0;
		}
	}

	@Override
	public void start() {
		this.lookTime = 40 + this.mob.getRandom().nextInt(40);
	}

	@Override
	public void stop() {
		this.lookAt = null;
	}

	@Override
	public void tick() {
		this.mob.getLookControl().setLookAt(this.lookAt.getX(), this.lookAt.getEyeY(), this.lookAt.getZ());
		this.lookTime--;
	}
}
