package com.mojang.realmsclient.gui.task;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public interface RestartDelayCalculator {
	void markExecutionStart();

	long getNextDelayMs();
}
