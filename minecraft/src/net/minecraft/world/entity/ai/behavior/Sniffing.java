package net.minecraft.world.entity.ai.behavior;

import com.google.common.collect.ImmutableMap;
import java.util.Optional;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.monster.warden.Warden;
import net.minecraft.world.entity.monster.warden.WardenAi;

public class Sniffing<E extends Warden> extends Behavior<E> {
	public Sniffing(int i) {
		super(
			ImmutableMap.of(
				MemoryModuleType.IS_SNIFFING,
				MemoryStatus.VALUE_PRESENT,
				MemoryModuleType.ATTACK_TARGET,
				MemoryStatus.VALUE_ABSENT,
				MemoryModuleType.WALK_TARGET,
				MemoryStatus.VALUE_ABSENT,
				MemoryModuleType.LOOK_TARGET,
				MemoryStatus.REGISTERED
			),
			i
		);
	}

	protected boolean canStillUse(ServerLevel serverLevel, E warden, long l) {
		return (Boolean)warden.getBrain().getMemory(MemoryModuleType.IS_SNIFFING).orElse(false);
	}

	protected void start(ServerLevel serverLevel, E warden, long l) {
		warden.playSound(SoundEvents.WARDEN_SNIFF, 5.0F, 1.0F);
	}

	protected void stop(ServerLevel serverLevel, E warden, long l) {
		warden.setPose(Pose.STANDING);
		warden.getBrain().eraseMemory(MemoryModuleType.IS_SNIFFING);
		warden.getBrain().getMemory(MemoryModuleType.NEAREST_ATTACKABLE).ifPresent(livingEntity -> {
			Optional<Long> optional = warden.getBrain().getMemory(MemoryModuleType.LAST_DISTURBANCE);
			boolean bl = (Boolean)optional.map(long_ -> serverLevel.getGameTime() - long_ >= 100L).orElse(true);
			boolean bl2 = warden.distanceTo(livingEntity) <= 6.0F;
			if (bl2) {
				warden.increaseAngerAt(livingEntity);
				WardenAi.markDisturbed(warden, l);
			}

			if (bl) {
				WardenAi.noticeSuspiciousLocation(warden, livingEntity.blockPosition());
			}
		});
	}
}
