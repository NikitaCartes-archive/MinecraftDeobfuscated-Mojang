package net.minecraft.world.entity.ai.behavior;

import com.google.common.collect.ImmutableMap;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.item.ProjectileWeaponItem;

public class MeleeAttack extends Behavior<Mob> {
	private final double attackRange;
	private final int cooldownBetweenAttacks;

	public MeleeAttack(double d, int i) {
		super(
			ImmutableMap.of(
				MemoryModuleType.LOOK_TARGET,
				MemoryStatus.REGISTERED,
				MemoryModuleType.ATTACK_TARGET,
				MemoryStatus.VALUE_PRESENT,
				MemoryModuleType.ATTACK_COOLING_DOWN,
				MemoryStatus.VALUE_ABSENT
			)
		);
		this.attackRange = d;
		this.cooldownBetweenAttacks = i;
	}

	protected boolean checkExtraStartConditions(ServerLevel serverLevel, Mob mob) {
		return !mob.isHolding(item -> item instanceof ProjectileWeaponItem) && BehaviorUtils.isAttackTargetVisibleAndInRange(mob, this.attackRange);
	}

	protected void start(ServerLevel serverLevel, Mob mob, long l) {
		LivingEntity livingEntity = (LivingEntity)mob.getBrain().getMemory(MemoryModuleType.ATTACK_TARGET).get();
		BehaviorUtils.lookAtEntity(mob, livingEntity);
		mob.swing(InteractionHand.MAIN_HAND);
		mob.doHurtTarget(livingEntity);
		mob.getBrain().setMemoryWithExpiry(MemoryModuleType.ATTACK_COOLING_DOWN, true, (long)this.cooldownBetweenAttacks);
	}
}
