package com.mojang.realmsclient.client.worldupload;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.network.chat.Component;

@Environment(EnvType.CLIENT)
public class RealmsUploadFailedException extends RealmsUploadException {
	private final Component errorMessage;

	public RealmsUploadFailedException(Component component) {
		this.errorMessage = component;
	}

	public RealmsUploadFailedException(String string) {
		this(Component.literal(string));
	}

	@Override
	public Component getStatusMessage() {
		return Component.translatable("mco.upload.failed", this.errorMessage);
	}
}
