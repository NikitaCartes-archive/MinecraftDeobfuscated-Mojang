package com.mojang.realmsclient.client.worldupload;

import com.mojang.realmsclient.Unit;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.network.chat.Component;

@Environment(EnvType.CLIENT)
public class RealmsUploadTooLargeException extends RealmsUploadException {
	final long sizeLimit;

	public RealmsUploadTooLargeException(long l) {
		this.sizeLimit = l;
	}

	@Override
	public Component[] getErrorMessages() {
		return new Component[]{
			Component.translatable("mco.upload.failed.too_big.title"),
			Component.translatable("mco.upload.failed.too_big.description", Unit.humanReadable(this.sizeLimit, Unit.getLargest(this.sizeLimit)))
		};
	}
}
