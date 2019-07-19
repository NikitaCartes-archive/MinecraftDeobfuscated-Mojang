package net.minecraft.world.entity.monster;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobType;
import net.minecraft.world.entity.ai.goal.OpenDoorGoal;
import net.minecraft.world.entity.raid.Raider;
import net.minecraft.world.level.Level;

public abstract class AbstractIllager extends Raider {
	protected AbstractIllager(EntityType<? extends AbstractIllager> entityType, Level level) {
		super(entityType, level);
	}

	@Override
	protected void registerGoals() {
		super.registerGoals();
	}

	@Override
	public MobType getMobType() {
		return MobType.ILLAGER;
	}

	@Environment(EnvType.CLIENT)
	public AbstractIllager.IllagerArmPose getArmPose() {
		return AbstractIllager.IllagerArmPose.CROSSED;
	}

	@Environment(EnvType.CLIENT)
	public static enum IllagerArmPose {
		CROSSED,
		ATTACKING,
		SPELLCASTING,
		BOW_AND_ARROW,
		CROSSBOW_HOLD,
		CROSSBOW_CHARGE,
		CELEBRATING;
	}

	public class RaiderOpenDoorGoal extends OpenDoorGoal {
		public RaiderOpenDoorGoal(Raider raider) {
			super(raider, false);
		}

		@Override
		public boolean canUse() {
			return super.canUse() && AbstractIllager.this.hasActiveRaid();
		}
	}
}
