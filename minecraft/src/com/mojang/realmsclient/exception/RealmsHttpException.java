package com.mojang.realmsclient.exception;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public class RealmsHttpException extends RuntimeException {
	public RealmsHttpException(String string, Exception exception) {
		super(string, exception);
	}
}
