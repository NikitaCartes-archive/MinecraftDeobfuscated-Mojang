package com.mojang.realmsclient.exception;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public class RetryCallException extends RealmsServiceException {
	public final int delaySeconds;

	public RetryCallException(int i, int j) {
		super(j, "Retry operation", -1, "");
		if (i >= 0 && i <= 120) {
			this.delaySeconds = i;
		} else {
			this.delaySeconds = 5;
		}
	}
}
