package net.minecraft.world.entity.ai.goal;

import java.util.EnumSet;
import javax.annotation.Nullable;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.item.Items;

public class SwellGoal extends Goal {
	private final Creeper creeper;
	@Nullable
	private LivingEntity target;

	public SwellGoal(Creeper creeper) {
		this.creeper = creeper;
		this.setFlags(EnumSet.of(Goal.Flag.MOVE));
	}

	@Override
	public boolean canUse() {
		LivingEntity livingEntity = this.creeper.getTarget();
		return livingEntity != null && livingEntity.isCrouching() && livingEntity.getItemBySlot(EquipmentSlot.HEAD).is(Items.BARREL)
			? false
			: this.creeper.getSwellDir() > 0 || livingEntity != null && this.creeper.distanceToSqr(livingEntity) < 9.0;
	}

	@Override
	public void start() {
		this.creeper.getNavigation().stop();
		this.target = this.creeper.getTarget();
	}

	@Override
	public void stop() {
		this.target = null;
	}

	@Override
	public boolean requiresUpdateEveryTick() {
		return true;
	}

	@Override
	public void tick() {
		if (!this.creeper.wasPickedUpByPlayer) {
			if (this.target == null) {
				this.creeper.setSwellDir(-1);
				return;
			}

			if (this.target.isCrouching() && this.target.getItemBySlot(EquipmentSlot.HEAD).is(Items.BARREL)) {
				this.creeper.setSwellDir(-1);
				return;
			}

			if (this.creeper.distanceToSqr(this.target) > 49.0) {
				this.creeper.setSwellDir(-1);
				return;
			}

			if (!this.creeper.getSensing().hasLineOfSight(this.target)) {
				this.creeper.setSwellDir(-1);
				return;
			}
		}

		this.creeper.setSwellDir(1);
	}
}
