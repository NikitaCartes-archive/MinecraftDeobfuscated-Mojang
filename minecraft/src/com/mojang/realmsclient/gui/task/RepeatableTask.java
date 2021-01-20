package com.mojang.realmsclient.gui.task;

import java.time.Duration;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.BooleanSupplier;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public class RepeatableTask implements Runnable {
	private final BooleanSupplier isActive;
	private final RestartDelayCalculator restartDelayCalculator;
	private final Duration interval;
	private final Runnable runnable;

	private RepeatableTask(Runnable runnable, Duration duration, BooleanSupplier booleanSupplier, RestartDelayCalculator restartDelayCalculator) {
		this.runnable = runnable;
		this.interval = duration;
		this.isActive = booleanSupplier;
		this.restartDelayCalculator = restartDelayCalculator;
	}

	public void run() {
		if (this.isActive.getAsBoolean()) {
			this.restartDelayCalculator.markExecutionStart();
			this.runnable.run();
		}
	}

	public ScheduledFuture<?> schedule(ScheduledExecutorService scheduledExecutorService) {
		return scheduledExecutorService.scheduleAtFixedRate(this, this.restartDelayCalculator.getNextDelayMs(), this.interval.toMillis(), TimeUnit.MILLISECONDS);
	}

	public static RepeatableTask withRestartDelayAccountingForInterval(Runnable runnable, Duration duration, BooleanSupplier booleanSupplier) {
		return new RepeatableTask(runnable, duration, booleanSupplier, new IntervalBasedStartupDelay(duration));
	}

	public static RepeatableTask withImmediateRestart(Runnable runnable, Duration duration, BooleanSupplier booleanSupplier) {
		return new RepeatableTask(runnable, duration, booleanSupplier, new NoStartupDelay());
	}
}
