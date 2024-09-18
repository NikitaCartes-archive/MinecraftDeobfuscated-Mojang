package com.mojang.realmsclient.client.worldupload;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.network.chat.Component;

@Environment(EnvType.CLIENT)
public class RealmsUploadWorldNotClosedException extends RealmsUploadException {
	@Override
	public Component getStatusMessage() {
		return Component.translatable("mco.upload.close.failure");
	}
}
