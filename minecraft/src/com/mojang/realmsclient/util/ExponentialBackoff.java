package com.mojang.realmsclient.util;

import com.mojang.logging.LogUtils;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import org.slf4j.Logger;

@Environment(EnvType.CLIENT)
public class ExponentialBackoff implements Runnable {
	private static final Logger LOGGER = LogUtils.getLogger();
	private final ExponentialBackoff.Task task;
	private final int maxSkips;
	private int toSkip;
	private int currentSkips;

	public ExponentialBackoff(ExponentialBackoff.Task task, int i) {
		this.task = task;
		this.maxSkips = i;
	}

	public void run() {
		if (this.toSkip > this.currentSkips) {
			this.currentSkips++;
		} else {
			this.currentSkips = 0;

			try {
				this.task.run();
				this.toSkip = 0;
			} catch (Exception var2) {
				if (this.toSkip == 0) {
					this.toSkip = 1;
				} else {
					this.toSkip = Math.min(2 * this.toSkip, this.maxSkips);
				}

				LOGGER.info("Skipping next {}", this.toSkip);
			}
		}
	}

	@FunctionalInterface
	@Environment(EnvType.CLIENT)
	public interface Task {
		void run() throws Exception;
	}
}
