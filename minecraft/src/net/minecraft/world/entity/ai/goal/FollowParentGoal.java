package net.minecraft.world.entity.ai.goal;

import java.util.List;
import net.minecraft.world.entity.animal.Animal;

public class FollowParentGoal extends Goal {
	private final Animal animal;
	private Animal parent;
	private final double speedModifier;
	private int timeToRecalcPath;

	public FollowParentGoal(Animal animal, double d) {
		this.animal = animal;
		this.speedModifier = d;
	}

	@Override
	public boolean canUse() {
		if (this.animal.getAge() >= 0) {
			return false;
		} else {
			List<Animal> list = this.animal.level.getEntitiesOfClass(this.animal.getClass(), this.animal.getBoundingBox().inflate(8.0, 4.0, 8.0));
			Animal animal = null;
			double d = Double.MAX_VALUE;

			for (Animal animal2 : list) {
				if (animal2.getAge() >= 0) {
					double e = this.animal.distanceToSqr(animal2);
					if (!(e > d)) {
						d = e;
						animal = animal2;
					}
				}
			}

			if (animal == null) {
				return false;
			} else if (d < 9.0) {
				return false;
			} else {
				this.parent = animal;
				return true;
			}
		}
	}

	@Override
	public boolean canContinueToUse() {
		if (this.animal.getAge() >= 0) {
			return false;
		} else if (!this.parent.isAlive()) {
			return false;
		} else {
			double d = this.animal.distanceToSqr(this.parent);
			return !(d < 9.0) && !(d > 256.0);
		}
	}

	@Override
	public void start() {
		this.timeToRecalcPath = 0;
	}

	@Override
	public void stop() {
		this.parent = null;
	}

	@Override
	public void tick() {
		if (--this.timeToRecalcPath <= 0) {
			this.timeToRecalcPath = 10;
			this.animal.getNavigation().moveTo(this.parent, this.speedModifier);
		}
	}
}
