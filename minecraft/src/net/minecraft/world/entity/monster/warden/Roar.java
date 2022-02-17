package net.minecraft.world.entity.monster.warden;

import com.google.common.collect.ImmutableMap;
import java.util.Optional;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;

public class Roar extends Behavior<Warden> {
	private static final int TICKS_BEFORE_PLAYING_ROAR_SOUND = 25;

	public Roar() {
		super(
			ImmutableMap.of(
				MemoryModuleType.ROAR_TARGET,
				MemoryStatus.VALUE_PRESENT,
				MemoryModuleType.ATTACK_TARGET,
				MemoryStatus.VALUE_ABSENT,
				MemoryModuleType.LAST_AUDIBLE_ROAR,
				MemoryStatus.REGISTERED,
				MemoryModuleType.LAST_ROAR_STARTED,
				MemoryStatus.REGISTERED
			),
			WardenAi.ROAR_DURATION
		);
	}

	protected void start(ServerLevel serverLevel, Warden warden, long l) {
		Brain<Warden> brain = warden.getBrain();
		brain.setMemory(MemoryModuleType.LAST_ROAR_STARTED, l);
		brain.eraseMemory(MemoryModuleType.WALK_TARGET);
		warden.setPose(Pose.ROARING);
	}

	protected boolean canStillUse(ServerLevel serverLevel, Warden warden, long l) {
		return warden.getBrain().hasMemoryValue(MemoryModuleType.ROAR_TARGET);
	}

	protected void tick(ServerLevel serverLevel, Warden warden, long l) {
		Optional<Long> optional = warden.getBrain().getMemory(MemoryModuleType.LAST_ROAR_STARTED);
		optional.ifPresent(long_ -> {
			Optional<Long> optionalx = warden.getBrain().getMemory(MemoryModuleType.LAST_AUDIBLE_ROAR);
			boolean bl = optionalx.isEmpty() || l > (Long)optionalx.get() + (long)WardenAi.ROAR_DURATION;
			if (bl && l >= 25L + long_) {
				warden.getBrain().setMemory(MemoryModuleType.LAST_AUDIBLE_ROAR, l);
				warden.playSound(SoundEvents.WARDEN_ROAR, 3.0F, 1.0F);
			}
		});
	}

	protected void stop(ServerLevel serverLevel, Warden warden, long l) {
		Brain<Warden> brain = warden.getBrain();
		brain.eraseMemory(MemoryModuleType.LAST_AUDIBLE_ROAR);
		brain.eraseMemory(MemoryModuleType.LAST_ROAR_STARTED);
		warden.setPose(Pose.STANDING);
	}
}
