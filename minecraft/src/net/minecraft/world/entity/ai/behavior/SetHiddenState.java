package net.minecraft.world.entity.ai.behavior;

import com.google.common.collect.ImmutableMap;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;

public class SetHiddenState extends Behavior<LivingEntity> {
	private static final int HIDE_TIMEOUT = 300;
	private final int closeEnoughDist;
	private final int stayHiddenTicks;
	private int ticksHidden;

	public SetHiddenState(int i, int j) {
		super(ImmutableMap.of(MemoryModuleType.HIDING_PLACE, MemoryStatus.VALUE_PRESENT, MemoryModuleType.HEARD_BELL_TIME, MemoryStatus.VALUE_PRESENT));
		this.stayHiddenTicks = i * 20;
		this.ticksHidden = 0;
		this.closeEnoughDist = j;
	}

	@Override
	protected void start(ServerLevel serverLevel, LivingEntity livingEntity, long l) {
		Brain<?> brain = livingEntity.getBrain();
		Optional<Long> optional = brain.getMemory(MemoryModuleType.HEARD_BELL_TIME);
		boolean bl = (Long)optional.get() + 300L <= l;
		if (this.ticksHidden <= this.stayHiddenTicks && !bl) {
			BlockPos blockPos = ((GlobalPos)brain.getMemory(MemoryModuleType.HIDING_PLACE).get()).pos();
			if (blockPos.closerThan(livingEntity.blockPosition(), (double)this.closeEnoughDist)) {
				this.ticksHidden++;
			}
		} else {
			brain.eraseMemory(MemoryModuleType.HEARD_BELL_TIME);
			brain.eraseMemory(MemoryModuleType.HIDING_PLACE);
			brain.updateActivityFromSchedule(serverLevel.getDayTime(), serverLevel.getGameTime());
			this.ticksHidden = 0;
		}
	}
}
