package net.minecraft.world.entity.monster;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobType;
import net.minecraft.world.entity.ai.goal.OpenDoorGoal;
import net.minecraft.world.entity.npc.AbstractVillager;
import net.minecraft.world.entity.raid.Raider;
import net.minecraft.world.level.Level;
import org.joml.Vector3f;

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

	public AbstractIllager.IllagerArmPose getArmPose() {
		return AbstractIllager.IllagerArmPose.CROSSED;
	}

	@Override
	public boolean canAttack(LivingEntity livingEntity) {
		return livingEntity instanceof AbstractVillager && livingEntity.isBaby() ? false : super.canAttack(livingEntity);
	}

	@Override
	protected float ridingOffset(Entity entity) {
		return -0.6F;
	}

	@Override
	protected Vector3f getPassengerAttachmentPoint(Entity entity, EntityDimensions entityDimensions, float f) {
		return new Vector3f(0.0F, entityDimensions.height + 0.05F * f, 0.0F);
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
		public RaiderOpenDoorGoal(Raider raider) {
			super(raider, false);
		}

		@Override
		public boolean canUse() {
			return super.canUse() && AbstractIllager.this.hasActiveRaid();
		}
	}
}
