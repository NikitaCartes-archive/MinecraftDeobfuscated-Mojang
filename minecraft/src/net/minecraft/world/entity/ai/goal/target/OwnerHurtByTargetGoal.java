package net.minecraft.world.entity.ai.goal.target;

import java.util.EnumSet;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;

public class OwnerHurtByTargetGoal extends TargetGoal {
	private final TamableAnimal tameAnimal;
	private LivingEntity ownerLastHurtBy;
	private int timestamp;

	public OwnerHurtByTargetGoal(TamableAnimal tamableAnimal) {
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
				this.ownerLastHurtBy = livingEntity.getLastHurtByMob();
				int i = livingEntity.getLastHurtByMobTimestamp();
				return i != this.timestamp
					&& this.canAttack(this.ownerLastHurtBy, TargetingConditions.DEFAULT)
					&& this.tameAnimal.wantsToAttack(this.ownerLastHurtBy, livingEntity);
			}
		} else {
			return false;
		}
	}

	@Override
	public void start() {
		this.mob.setTarget(this.ownerLastHurtBy);
		LivingEntity livingEntity = this.tameAnimal.getOwner();
		if (livingEntity != null) {
			this.timestamp = livingEntity.getLastHurtByMobTimestamp();
		}

		super.start();
	}
}
