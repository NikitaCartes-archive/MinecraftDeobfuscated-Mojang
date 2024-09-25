package net.minecraft.world.entity.ai.goal.target;

import javax.annotation.Nullable;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.raid.Raider;

public class NearestHealableRaiderTargetGoal<T extends LivingEntity> extends NearestAttackableTargetGoal<T> {
	private static final int DEFAULT_COOLDOWN = 200;
	private int cooldown = 0;

	public NearestHealableRaiderTargetGoal(Raider raider, Class<T> class_, boolean bl, @Nullable TargetingConditions.Selector selector) {
		super(raider, class_, 500, bl, false, selector);
	}

	public int getCooldown() {
		return this.cooldown;
	}

	public void decrementCooldown() {
		this.cooldown--;
	}

	@Override
	public boolean canUse() {
		if (this.cooldown > 0 || !this.mob.getRandom().nextBoolean()) {
			return false;
		} else if (!((Raider)this.mob).hasActiveRaid()) {
			return false;
		} else {
			this.findTarget();
			return this.target != null;
		}
	}

	@Override
	public void start() {
		this.cooldown = reducedTickDelay(200);
		super.start();
	}
}
