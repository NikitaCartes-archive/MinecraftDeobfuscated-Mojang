package net.minecraft.world.entity.monster.warden;

import com.google.common.collect.ImmutableMap;
import java.util.Optional;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;

public class TryToSniff extends Behavior<Warden> {
	public TryToSniff() {
		super(
			ImmutableMap.of(
				MemoryModuleType.LAST_DISTURBANCE,
				MemoryStatus.REGISTERED,
				MemoryModuleType.LAST_SNIFF,
				MemoryStatus.REGISTERED,
				MemoryModuleType.NEAREST_ATTACKABLE,
				MemoryStatus.VALUE_PRESENT,
				MemoryModuleType.DISTURBANCE_LOCATION,
				MemoryStatus.VALUE_ABSENT
			)
		);
	}

	protected boolean checkExtraStartConditions(ServerLevel serverLevel, Warden warden) {
		Brain<Warden> brain = warden.getBrain();
		Optional<Long> optional = brain.getMemory(MemoryModuleType.LAST_DISTURBANCE);
		boolean bl = (Boolean)optional.map(long_ -> serverLevel.getGameTime() - long_ >= 100L).orElse(true);
		if (bl) {
			if (brain.hasMemoryValue(MemoryModuleType.LAST_SNIFF)) {
				long l = serverLevel.getGameTime() - (Long)brain.getMemory(MemoryModuleType.LAST_SNIFF).get();
				return l >= 120L;
			} else {
				return true;
			}
		} else {
			return false;
		}
	}

	protected void start(ServerLevel serverLevel, Warden warden, long l) {
		Brain<Warden> brain = warden.getBrain();
		brain.eraseMemory(MemoryModuleType.WALK_TARGET);
		warden.setPose(Pose.SNIFFING);
		brain.setMemory(MemoryModuleType.IS_SNIFFING, true);
		brain.setMemory(MemoryModuleType.LAST_SNIFF, l);
	}
}
