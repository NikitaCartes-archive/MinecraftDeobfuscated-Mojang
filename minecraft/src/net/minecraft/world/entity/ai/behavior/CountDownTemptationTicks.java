package net.minecraft.world.entity.ai.behavior;

import com.google.common.collect.ImmutableMap;
import java.util.Optional;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;

public class CountDownTemptationTicks extends Behavior<LivingEntity> {
	public CountDownTemptationTicks() {
		super(ImmutableMap.of(MemoryModuleType.TEMPTATION_COOLDOWN_TICKS, MemoryStatus.VALUE_PRESENT));
	}

	private Optional<Integer> getCalmDownTickMemory(LivingEntity livingEntity) {
		return livingEntity.getBrain().getMemory(MemoryModuleType.TEMPTATION_COOLDOWN_TICKS);
	}

	@Override
	protected boolean canStillUse(ServerLevel serverLevel, LivingEntity livingEntity, long l) {
		Optional<Integer> optional = this.getCalmDownTickMemory(livingEntity);
		return optional.isPresent() && (Integer)optional.get() > 0;
	}

	@Override
	protected void tick(ServerLevel serverLevel, LivingEntity livingEntity, long l) {
		Optional<Integer> optional = this.getCalmDownTickMemory(livingEntity);
		livingEntity.getBrain().setMemory(MemoryModuleType.TEMPTATION_COOLDOWN_TICKS, (Integer)optional.get() - 1);
	}

	@Override
	protected void stop(ServerLevel serverLevel, LivingEntity livingEntity, long l) {
		livingEntity.getBrain().eraseMemory(MemoryModuleType.TEMPTATION_COOLDOWN_TICKS);
	}
}
