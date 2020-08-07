package net.minecraft.world.entity.ai.behavior;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.IntRange;
import net.minecraft.world.entity.LivingEntity;

public class RunSometimes<E extends LivingEntity> extends Behavior<E> {
	private boolean resetTicks;
	private boolean wasRunning;
	private final IntRange interval;
	private final Behavior<? super E> wrappedBehavior;
	private int ticksUntilNextStart;

	public RunSometimes(Behavior<? super E> behavior, IntRange intRange) {
		this(behavior, false, intRange);
	}

	public RunSometimes(Behavior<? super E> behavior, boolean bl, IntRange intRange) {
		super(behavior.entryCondition);
		this.wrappedBehavior = behavior;
		this.resetTicks = !bl;
		this.interval = intRange;
	}

	@Override
	protected boolean checkExtraStartConditions(ServerLevel serverLevel, E livingEntity) {
		if (!this.wrappedBehavior.checkExtraStartConditions(serverLevel, livingEntity)) {
			return false;
		} else {
			if (this.resetTicks) {
				this.resetTicksUntilNextStart(serverLevel);
				this.resetTicks = false;
			}

			if (this.ticksUntilNextStart > 0) {
				this.ticksUntilNextStart--;
			}

			return !this.wasRunning && this.ticksUntilNextStart == 0;
		}
	}

	@Override
	protected void start(ServerLevel serverLevel, E livingEntity, long l) {
		this.wrappedBehavior.start(serverLevel, livingEntity, l);
	}

	@Override
	protected boolean canStillUse(ServerLevel serverLevel, E livingEntity, long l) {
		return this.wrappedBehavior.canStillUse(serverLevel, livingEntity, l);
	}

	@Override
	protected void tick(ServerLevel serverLevel, E livingEntity, long l) {
		this.wrappedBehavior.tick(serverLevel, livingEntity, l);
		this.wasRunning = this.wrappedBehavior.getStatus() == Behavior.Status.RUNNING;
	}

	@Override
	protected void stop(ServerLevel serverLevel, E livingEntity, long l) {
		this.resetTicksUntilNextStart(serverLevel);
		this.wrappedBehavior.stop(serverLevel, livingEntity, l);
	}

	private void resetTicksUntilNextStart(ServerLevel serverLevel) {
		this.ticksUntilNextStart = this.interval.randomValue(serverLevel.random);
	}

	@Override
	protected boolean timedOut(long l) {
		return false;
	}

	@Override
	public String toString() {
		return "RunSometimes: " + this.wrappedBehavior;
	}
}
