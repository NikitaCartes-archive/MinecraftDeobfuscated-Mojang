package net.minecraft.world.entity.ai.goal.target;

import javax.annotation.Nullable;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;

public class NonTameRandomTargetGoal<T extends LivingEntity> extends NearestAttackableTargetGoal<T> {
	private final TamableAnimal tamableMob;

	public NonTameRandomTargetGoal(TamableAnimal tamableAnimal, Class<T> class_, boolean bl, @Nullable TargetingConditions.Selector selector) {
		super(tamableAnimal, class_, 10, bl, false, selector);
		this.tamableMob = tamableAnimal;
	}

	@Override
	public boolean canUse() {
		return !this.tamableMob.isTame() && super.canUse();
	}

	@Override
	public boolean canContinueToUse() {
		return this.targetConditions != null ? this.targetConditions.test(getServerLevel(this.mob), this.mob, this.target) : super.canContinueToUse();
	}
}
