package net.minecraft.world.entity.ai.behavior;

import com.google.common.collect.ImmutableMap;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;

public class LongJumpMidJump extends Behavior<Mob> {
	public static final int TIME_OUT_DURATION = 100;
	private final UniformInt timeBetweenLongJumps;
	private final SoundEvent landingSound;

	public LongJumpMidJump(UniformInt uniformInt, SoundEvent soundEvent) {
		super(ImmutableMap.of(MemoryModuleType.LOOK_TARGET, MemoryStatus.REGISTERED, MemoryModuleType.LONG_JUMP_MID_JUMP, MemoryStatus.VALUE_PRESENT), 100);
		this.timeBetweenLongJumps = uniformInt;
		this.landingSound = soundEvent;
	}

	protected boolean canStillUse(ServerLevel serverLevel, Mob mob, long l) {
		return !mob.onGround();
	}

	protected void start(ServerLevel serverLevel, Mob mob, long l) {
		mob.setDiscardFriction(true);
		mob.setPose(Pose.LONG_JUMPING);
	}

	protected void stop(ServerLevel serverLevel, Mob mob, long l) {
		if (mob.onGround()) {
			mob.setDeltaMovement(mob.getDeltaMovement().multiply(0.1F, 1.0, 0.1F));
			serverLevel.playSound(null, mob, this.landingSound, SoundSource.NEUTRAL, 2.0F, 1.0F);
		}

		mob.setDiscardFriction(false);
		mob.setPose(Pose.STANDING);
		mob.getBrain().eraseMemory(MemoryModuleType.LONG_JUMP_MID_JUMP);
		mob.getBrain().setMemory(MemoryModuleType.LONG_JUMP_COOLDOWN_TICKS, this.timeBetweenLongJumps.sample(serverLevel.random));
	}
}
