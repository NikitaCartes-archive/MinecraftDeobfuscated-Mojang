package com.mojang.realmsclient.gui;

import com.mojang.realmsclient.exception.RealmsServiceException;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.network.chat.Component;

@Environment(EnvType.CLIENT)
public interface ErrorCallback {
	void error(Component component);

	default void error(Exception exception) {
		if (exception instanceof RealmsServiceException realmsServiceException) {
			this.error(realmsServiceException.realmsError.errorMessage());
		} else {
			this.error(Component.literal(exception.getMessage()));
		}
	}

	default void error(RealmsServiceException realmsServiceException) {
		this.error(realmsServiceException.realmsError.errorMessage());
	}
}
