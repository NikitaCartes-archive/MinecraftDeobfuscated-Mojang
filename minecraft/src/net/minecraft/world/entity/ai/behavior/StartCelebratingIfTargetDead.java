package net.minecraft.world.entity.ai.behavior;

import com.google.common.collect.ImmutableMap;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;

public class StartCelebratingIfTargetDead extends Behavior<LivingEntity> {
	private final int celebrateDuration;

	public StartCelebratingIfTargetDead(int i) {
		super(
			ImmutableMap.of(
				MemoryModuleType.ATTACK_TARGET,
				MemoryStatus.VALUE_PRESENT,
				MemoryModuleType.ANGRY_AT,
				MemoryStatus.REGISTERED,
				MemoryModuleType.CELEBRATE_LOCATION,
				MemoryStatus.VALUE_ABSENT
			)
		);
		this.celebrateDuration = i;
	}

	@Override
	protected boolean checkExtraStartConditions(ServerLevel serverLevel, LivingEntity livingEntity) {
		return this.getAttackTarget(livingEntity).getHealth() <= 0.0F;
	}

	@Override
	protected void start(ServerLevel serverLevel, LivingEntity livingEntity, long l) {
		BlockPos blockPos = this.getAttackTarget(livingEntity).getBlockPos();
		livingEntity.getBrain().eraseMemory(MemoryModuleType.ATTACK_TARGET);
		livingEntity.getBrain().eraseMemory(MemoryModuleType.ANGRY_AT);
		livingEntity.getBrain().setMemoryWithExpiry(MemoryModuleType.CELEBRATE_LOCATION, blockPos, l, (long)this.celebrateDuration);
	}

	private LivingEntity getAttackTarget(LivingEntity livingEntity) {
		return (LivingEntity)livingEntity.getBrain().getMemory(MemoryModuleType.ATTACK_TARGET).get();
	}
}
