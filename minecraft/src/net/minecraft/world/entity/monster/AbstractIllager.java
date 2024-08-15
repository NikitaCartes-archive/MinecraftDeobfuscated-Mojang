package net.minecraft.world.entity.monster;

import net.minecraft.tags.EntityTypeTags;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.OpenDoorGoal;
import net.minecraft.world.entity.npc.AbstractVillager;
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

	public AbstractIllager.IllagerArmPose getArmPose() {
		return AbstractIllager.IllagerArmPose.CROSSED;
	}

	@Override
	public boolean canAttack(LivingEntity livingEntity) {
		return livingEntity instanceof AbstractVillager && livingEntity.isBaby() ? false : super.canAttack(livingEntity);
	}

	@Override
	protected boolean considersEntityAsAlly(Entity entity) {
		if (super.considersEntityAsAlly(entity)) {
			return true;
		} else {
			return !entity.getType().is(EntityTypeTags.ILLAGER_FRIENDS) ? false : this.getTeam() == null && entity.getTeam() == null;
		}
	}

	public static enum IllagerArmPose {
		CROSSED,
		ATTACKING,
		SPELLCASTING,
		BOW_AND_ARROW,
		CROSSBOW_HOLD,
		CROSSBOW_CHARGE,
		CELEBRATING,
		NEUTRAL;
	}

	protected class RaiderOpenDoorGoal extends OpenDoorGoal {
		public RaiderOpenDoorGoal(final Raider raider) {
			super(raider, false);
		}

		@Override
		public boolean canUse() {
			return super.canUse() && AbstractIllager.this.hasActiveRaid();
		}
	}
}
