package net.minecraft.world.entity.ai.goal.target;

import java.util.EnumSet;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;

public class OwnerHurtTargetGoal extends TargetGoal {
	private final TamableAnimal tameAnimal;
	private LivingEntity ownerLastHurt;
	private int timestamp;

	public OwnerHurtTargetGoal(TamableAnimal tamableAnimal) {
		super(tamableAnimal, false);
		this.tameAnimal = tamableAnimal;
		this.setFlags(EnumSet.of(Goal.Flag.TARGET));
	}

	@Override
	public boolean canUse() {
		if (this.tameAnimal.isTame() && !this.tameAnimal.isOrderedToSit()) {
			LivingEntity livingEntity = this.tameAnimal.getOwner();
			if (livingEntity == null) {
				return false;
			} else {
				this.ownerLastHurt = livingEntity.getLastHurtMob();
				int i = livingEntity.getLastHurtMobTimestamp();
				return i != this.timestamp
					&& this.canAttack(this.ownerLastHurt, TargetingConditions.DEFAULT)
					&& this.tameAnimal.wantsToAttack(this.ownerLastHurt, livingEntity);
			}
		} else {
			return false;
		}
	}

	@Override
	public void start() {
		this.mob.setTarget(this.ownerLastHurt);
		LivingEntity livingEntity = this.tameAnimal.getOwner();
		if (livingEntity != null) {
			this.timestamp = livingEntity.getLastHurtMobTimestamp();
		}

		super.start();
	}
}
