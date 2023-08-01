package com.mojang.realmsclient.exception;

import com.mojang.realmsclient.client.RealmsError;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public class RetryCallException extends RealmsServiceException {
	public static final int DEFAULT_DELAY = 5;
	public final int delaySeconds;

	public RetryCallException(int i, int j) {
		super(RealmsError.CustomError.retry(j));
		if (i >= 0 && i <= 120) {
			this.delaySeconds = i;
		} else {
			this.delaySeconds = 5;
		}
	}
}
