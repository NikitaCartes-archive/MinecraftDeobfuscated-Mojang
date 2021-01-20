package com.mojang.realmsclient.gui.task;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public class NoStartupDelay implements RestartDelayCalculator {
	@Override
	public void markExecutionStart() {
	}

	@Override
	public long getNextDelayMs() {
		return 0L;
	}
}
