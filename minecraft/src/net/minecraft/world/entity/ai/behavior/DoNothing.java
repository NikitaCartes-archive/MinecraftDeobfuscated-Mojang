package net.minecraft.world.entity.ai.behavior;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;

public class DoNothing implements BehaviorControl<LivingEntity> {
	private final int minDuration;
	private final int maxDuration;
	private Behavior.Status status = Behavior.Status.STOPPED;
	private long endTimestamp;

	public DoNothing(int i, int j) {
		this.minDuration = i;
		this.maxDuration = j;
	}

	@Override
	public Behavior.Status getStatus() {
		return this.status;
	}

	@Override
	public final boolean tryStart(ServerLevel serverLevel, LivingEntity livingEntity, long l) {
		this.status = Behavior.Status.RUNNING;
		int i = this.minDuration + serverLevel.getRandom().nextInt(this.maxDuration + 1 - this.minDuration);
		this.endTimestamp = l + (long)i;
		return true;
	}

	@Override
	public final void tickOrStop(ServerLevel serverLevel, LivingEntity livingEntity, long l) {
		if (l > this.endTimestamp) {
			this.doStop(serverLevel, livingEntity, l);
		}
	}

	@Override
	public final void doStop(ServerLevel serverLevel, LivingEntity livingEntity, long l) {
		this.status = Behavior.Status.STOPPED;
	}

	@Override
	public String debugString() {
		return this.getClass().getSimpleName();
	}
}
